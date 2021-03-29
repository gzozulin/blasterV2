package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.Object
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.scene.*
import org.lwjgl.glfw.GLFW
import java.lang.Float.max
import java.lang.Float.min

// todo: create/release is probably working improperly

private const val MAX_LIGHTS = 128

private var deferredDebug = false
private var debugItem = 0

private val deferredGeomVert = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_VERT

    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;

    out vec4 vPosition;
    out vec2 vTexCoord;
    out vec3 vNormal;
    
    %DECL%

    void main()
    {
        %VRBL%
        vec4 worldPos = %MODEL% * vec4(aPosition, 1.0);
        vPosition = worldPos;
        vTexCoord = aTexCoord;
        mat3 normalM = transpose(inverse(mat3(%MODEL%)));
        vNormal = normalM * aNormal;
        gl_Position = %PROJ% * %VIEW% * worldPos;
    }
""".trimIndent()

private val deferredGeomFrag = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_FRAG

    in vec4 vPosition;
    in vec2 vTexCoord;
    in vec3 vNormal;

    layout (location = 0) out vec4 oPosition;
    layout (location = 1) out vec3 oNormal;
    layout (location = 2) out vec4 oDiffuse;
    layout (location = 3) out vec3 oMatAmbient;
    layout (location = 4) out vec3 oMatDiffuse;
    layout (location = 5) out vec3 oMatSpecular;
    layout (location = 6) out vec3 oMatShineTransp;
    
    %DECL%

    void main()
    {
        %VRBL%
        oDiffuse = %DIFFUSE%;
        oPosition = vPosition;
        oNormal = normalize(vNormal);
        oMatAmbient = %MAT_AMBIENT%;
        oMatDiffuse = %MAT_DIFFUSE%;
        oMatSpecular = %MAT_SPECULAR%;
        oMatShineTransp = vec3(%MAT_SHINE%, %MAT_TRANSPARENCY%, 1.0);
    }
""".trimIndent()

private val deferredLightVert = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_VERT
    
    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;

    out vec2 vTexCoord;

    void main()
    {
        vTexCoord = aTexCoord;
        gl_Position = vec4(aPosition, 1.0);
    }
""".trimIndent()

private val deferredLightFrag = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_FRAG
    
    %DECL%

    in vec2 vTexCoord;

    uniform sampler2D uTexPosition;
    uniform sampler2D uTexNormal;
    uniform sampler2D uTexDiffuse;
    uniform sampler2D uTexMatAmbient;
    uniform sampler2D uTexMatDiffuse;
    uniform sampler2D uTexMatSpecular;
    uniform sampler2D uTexMatShineTransp;

    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];

    out vec4 oFragColor;

    void main()
    {
        %VRBL%
        
        vec4 positionLookup = texture(uTexPosition, vTexCoord);
        if (positionLookup.a != 1.0) {
            discard;
        }

        vec3 fragPosition = positionLookup.rgb;
        vec3 fragNormal = texture(uTexNormal, vTexCoord).rgb;
        vec3 fragDiffuse = texture(uTexDiffuse, vTexCoord).rgb;
        vec3 matAmbient = texture(uTexMatAmbient, vTexCoord).rgb;
        vec3 matDiffuse = texture(uTexMatDiffuse, vTexCoord).rgb;
        vec3 matSpecular = texture(uTexMatSpecular, vTexCoord).rgb;
        vec3 matShineTransp = texture(uTexMatShineTransp, vTexCoord).rgb;
        float shine = matShineTransp[0];
        float transparency = matShineTransp[1];
        
        PhongMaterial material = {
            matAmbient,
            matDiffuse,
            matSpecular,
            shine,
            transparency
        };

        vec3 viewDir = normalize(%EYE% - fragPosition);
        vec3 lighting = matAmbient;

        for (int i = 0; i < uLightsPointCnt; ++i) {
            lighting += expr_pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i], material);
        }
        for (int i = uLightsPointCnt; i < uLightsPointCnt + uLightsDirCnt; ++i) {
            lighting += expr_dirLightContrib(viewDir, fragNormal, uLights[i], material);
        }
        
        // todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
        // https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/

        lighting *= fragDiffuse;
        oFragColor = vec4(lighting, transparency);
    }
""".trimIndent()

class DeferredTechnique(
    private val modelM: Expression<mat4>,
    private val viewM: Expression<mat4>,
    private val projM: Expression<mat4>,
    private val eye: Expression<vec3>,
    private val diffuse: Expression<vec4> = constv4(vec4(1f)),
    private val matAmbient: Expression<vec3> = constv3(vec3(1f)),
    private val matDiffuse: Expression<vec3> = constv3(vec3(1f)),
    private val matSpecular: Expression<vec3> = constv3(vec3(1f)),
    private val matShine: Expression<Float> = constf(10f),
    private val matTransparency: Expression<Float> = constf(1f)) : GlResource() {

    private val programGeomPass: GlProgram
    private val programLightPass: GlProgram

    init {
        val geomVertSrc = deferredGeomVert.substituteDeclVrbl(
            modelM, viewM, projM)
            .replace("%MODEL%", modelM.expr())
            .replace("%VIEW%", viewM.expr())
            .replace("%PROJ%", projM.expr())
        val geomFragSrc = deferredGeomFrag.substituteDeclVrbl(
            diffuse, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)
            .replace("%DIFFUSE%", diffuse.expr())
            .replace("%MAT_AMBIENT%", matAmbient.expr())
            .replace("%MAT_DIFFUSE%", matDiffuse.expr())
            .replace("%MAT_SPECULAR%", matSpecular.expr())
            .replace("%MAT_SHINE%", matShine.expr())
            .replace("%MAT_TRANSPARENCY%", matTransparency.expr())
        programGeomPass = GlProgram(
            vertexShader = GlShader(GlShaderType.VERTEX_SHADER, geomVertSrc),
            fragmentShader = GlShader(GlShaderType.FRAGMENT_SHADER, geomFragSrc))
        val lightVertSrc = deferredLightVert.substituteDeclVrbl()
        val lightFragSrc = deferredLightFrag.substituteDeclVrbl(eye)
            .replace("%EYE%", eye.expr())
        programLightPass = GlProgram(
            vertexShader = GlShader(GlShaderType.VERTEX_SHADER, lightVertSrc),
            fragmentShader = GlShader(GlShaderType.FRAGMENT_SHADER, lightFragSrc))
    }

    private val constDebugModelM = constm4(mat4().identity())
    private val constDebugViewM = constm4(mat4().identity())
    private val constDebugProjM = constm4(mat4().ortho(-1f, 1f, -1f, 1f, -1f, 1f))
    private val constDebugUnifSampler = unifsampler()
    private val texCoord = varying<vec2>(SimpleVarrying.vTexCoord.name)
    private val debugTechnique = FlatTechnique(constDebugModelM, constDebugViewM, constDebugProjM, tex(texCoord, constDebugUnifSampler))

    private val rect = GlMesh.rect()

    private val framebuffer = GlFrameBuffer()

    private lateinit var positionStorage: GlTexture
    private lateinit var normalStorage: GlTexture
    private lateinit var diffuseStorage: GlTexture
    private lateinit var matAmbientStorage: GlTexture
    private lateinit var matDiffuseStorage: GlTexture
    private lateinit var matSpecularStorage: GlTexture
    private lateinit var matShineTranspStorage: GlTexture

    private lateinit var depthBuffer: GlRenderBuffer

    init {
        addChildren(programGeomPass, programLightPass, debugTechnique, rect, framebuffer)
    }

    override fun onRelease() {
        releaseStorage()
    }

    fun resize(width: Int, height: Int) {
        checkReady()
        if (::positionStorage.isInitialized) {
            releaseStorage()
        }
        createStorage(width, height)
        bindFramebuffer()
    }

    fun draw(lights: List<Light>, instances: () -> Unit) {
        checkReady()
        geomPass(instances)
        if (deferredDebug) {
            debugPass()
        } else {
            lightPass(lights)
        }
    }

    fun instance(mesh: GlMesh, vararg bindables: GlBindable) {
        glBind(mesh) {
            glBind(bindables.toList()) {
                renderInstance(mesh)
            }
        }
    }

    fun instance(obj: Object) {
        glBind(obj) {
            renderInstance(obj.mesh)
        }
    }

    private fun geomPass(instances: () -> Unit) {
        glBind(framebuffer) {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            glBind(programGeomPass) {
                viewM.submit(programGeomPass)
                projM.submit(programGeomPass)
                instances.invoke()
            }
        }
    }

    private fun debugPass() {
        val debug = when (debugItem % 7) {
            0 -> positionStorage
            1 -> normalStorage
            2 -> diffuseStorage
            3 -> matAmbientStorage
            4 -> matDiffuseStorage
            5 -> matSpecularStorage
            6 -> matShineTranspStorage
            else -> error("wtf??")
        }
        debugTechnique.draw {
            glBind(debug) {
                constDebugUnifSampler.value = debug
                debugTechnique.instance(rect)
            }
        }
    }

    private fun lightPass(lights: List<Light>) {
        glBind(programLightPass, rect, depthBuffer,
            positionStorage, normalStorage, diffuseStorage,
            matAmbientStorage, matDiffuseStorage, matSpecularStorage, matShineTranspStorage) {
            eye.submit(programLightPass)
            submitLights(lights)
            programLightPass.draw(indicesCount = rect.indicesCount)
        }
    }

    private fun submitLights(lights: List<Light>) {
        check(lights.size <= MAX_LIGHTS) { "More lights than defined in shader!" }
        val sorted = lights.sortedBy { it is PointLight }
        var pointLightCnt = 0
        var dirLightCnt = 0
        sorted.forEachIndexed { index, light ->
            programLightPass.setArrayUniform("uLights[%d].vector",          index, light.vector)
            programLightPass.setArrayUniform("uLights[%d].color",           index, light.color)
            programLightPass.setArrayUniform("uLights[%d].attenConstant",   index, light.attenConstant)
            programLightPass.setArrayUniform("uLights[%d].attenLinear",     index, light.attenLinear)
            programLightPass.setArrayUniform("uLights[%d].attenQuadratic",  index, light.attenQuadratic)
            if (light is PointLight) {
                pointLightCnt++
            } else {
                dirLightCnt++
            }
        }
        programLightPass.setUniform("uLightsPointCnt", pointLightCnt)
        programLightPass.setUniform("uLightsDirCnt",   dirLightCnt)
    }

    private fun createStorage(width: Int, height: Int) {
        positionStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGBA16F,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        positionStorage.use()
        normalStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGB16F,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        normalStorage.use()
        diffuseStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGBA,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_UNSIGNED_BYTE)
        diffuseStorage.use()
        matAmbientStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGB,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_UNSIGNED_BYTE)
        matAmbientStorage.use()
        matDiffuseStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGB,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_UNSIGNED_BYTE)
        matDiffuseStorage.use()
        matSpecularStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGB,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_UNSIGNED_BYTE)
        matSpecularStorage.use()
        matShineTranspStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGB16F,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        matShineTranspStorage.use()
        depthBuffer = GlRenderBuffer(width = width, height = height)
        depthBuffer.use()
    }

    private fun releaseStorage() {
        positionStorage.release()
        normalStorage.release()
        diffuseStorage.release()
        matAmbientStorage.release()
        matDiffuseStorage.release()
        matSpecularStorage.release()
        matShineTranspStorage.release()
        depthBuffer.release()
    }

    private fun bindFramebuffer() {
        glBind(framebuffer, positionStorage, normalStorage, diffuseStorage,
            matAmbientStorage, matDiffuseStorage, matSpecularStorage, matShineTranspStorage, depthBuffer) {
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT3, matAmbientStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT4, matDiffuseStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT5, matSpecularStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT6, matShineTranspStorage)
            framebuffer.setOutputs(intArrayOf(
                backend.GL_COLOR_ATTACHMENT0, backend.GL_COLOR_ATTACHMENT1, backend.GL_COLOR_ATTACHMENT2,
                backend.GL_COLOR_ATTACHMENT3, backend.GL_COLOR_ATTACHMENT4, backend.GL_COLOR_ATTACHMENT5,
                backend.GL_COLOR_ATTACHMENT6
            ))
            framebuffer.setRenderBuffer(backend.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
            glBind(programLightPass) {
                programLightPass.setTexture("uTexPosition",         positionStorage)
                programLightPass.setTexture("uTexNormal",           normalStorage)
                programLightPass.setTexture("uTexDiffuse",          diffuseStorage)
                programLightPass.setTexture("uTexMatAmbient",       matAmbientStorage)
                programLightPass.setTexture("uTexMatDiffuse",       matDiffuseStorage)
                programLightPass.setTexture("uTexMatSpecular",      matSpecularStorage)
                programLightPass.setTexture("uTexMatShineTransp",   matShineTranspStorage)
            }
        }
    }

    private fun renderInstance(mesh: GlMesh) {
        checkReady()
        modelM.submit(programGeomPass)
        diffuse.submit(programGeomPass)
        matAmbient.submit(programGeomPass)
        matDiffuse.submit(programGeomPass)
        matSpecular.submit(programGeomPass)
        matShine.submit(programGeomPass)
        matTransparency.submit(programGeomPass)
        programGeomPass.draw(indicesCount = mesh.indicesCount)
    }
}

private val camera = Camera()
private val controller = Controller(velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val obj = modelLib.load("models/sphere/sphere").first()
private val sphereMaterials = mutableListOf<PhongMaterial>()
private val sphereMatrices = mutableListOf<mat4>().apply {
    (1..64).count {
        sphereMaterials.add(PHONG_MATERIALS.random())
        add(mat4().identity().translate(vec3().rand(vec3(-30f), vec3(30f))))
    }
}

private val unifEye = unifv3 { camera.position }
private val unifModelM = unifm4()
private val unifViewM = unifm4 { camera.calculateViewM() }
private val unifProjM = unifm4 { camera.projectionM }

private val unifMaterialAmbient = unifv3()
private val unifMaterialDiffuse = unifv3()
private val unifMaterialSpecular = unifv3()
private val unifMaterialShine = uniff()
private val unifMaterialTransp = uniff()

private val deferredTechnique = DeferredTechnique(
    unifModelM, unifViewM, unifProjM, unifEye,
    matAmbient = constv3(vec3(0.01f)),
    matDiffuse = unifMaterialDiffuse,
    matSpecular = unifMaterialSpecular,
    matShine = unifMaterialShine,
    matTransparency = unifMaterialTransp
)

private val light = PointLight(camera.position, col3().white(), 300f)

private var mouseLook = false
private var lightsUp = false
private var lightsDown = false

fun main() {
    val window = GlWindow()
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
            deferredTechnique.resize(width, height)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
            if (pressed) {
                when (key) {
                    GLFW.GLFW_KEY_DELETE    -> deferredDebug = !deferredDebug
                    GLFW.GLFW_KEY_SPACE     -> debugItem++
                    GLFW.GLFW_KEY_UP        -> lightsUp = true
                    GLFW.GLFW_KEY_DOWN      -> lightsDown = true
                }
            } else {
                when (key) {
                    GLFW.GLFW_KEY_UP        -> lightsUp = false
                    GLFW.GLFW_KEY_DOWN      -> lightsDown = false
                }
            }
        }
        window.buttonCallback = { button, pressed ->
            if (button == MouseButton.LEFT) {
                mouseLook = pressed
            }
        }
        window.deltaCallback = { delta ->
            if (mouseLook) {
                wasdInput.onCursorDelta(delta)
            }
        }
        glUse(deferredTechnique, obj) {
            window.show {
                if (lightsUp) {
                    light.range += 5f
                    println(light.range)
                } else if (lightsDown) {
                    light.range -= 5f
                    println(light.range)
                }
                light.range = max(0f, light.range)
                glClear(col3().black())
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                glDepthTest {
                    glCulling {
                        deferredTechnique.draw(listOf(light)) {
                            sphereMatrices.forEachIndexed { index, mat ->
                                unifModelM.value = mat
                                val material = sphereMaterials[index]
                                unifMaterialAmbient.value = material.ambient
                                unifMaterialDiffuse.value = material.diffuse
                                unifMaterialSpecular.value = material.specular
                                unifMaterialShine.value = material.shine
                                unifMaterialTransp.value = material.transparency
                                deferredTechnique.instance(obj)
                            }
                        }
                    }
                }
            }
        }
    }
}