package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.scene.*
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.joml.Matrix4f

const val MAX_LIGHTS = 128

private val deferredGeomVert = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_VERT

    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;

    uniform mat4 uModelM;
    uniform mat4 uViewM;
    uniform mat4 uProjectionM;

    uniform vec3 uMatAmbient;
    uniform vec3 uMatDiffuse;
    uniform vec3 uMatSpecular;
    uniform float uMatShine;
    uniform float uMatTransp;

    out vec4 vFragPosition;
    out vec2 vTexCoord;
    out vec3 vNormal;

    out vec3 vMatAmbient;
    out vec3 vMatDiffuse;
    out vec3 vMatSpecular;
    out float vMatShine;
    out float vMatTransp;

    void main()
    {
        vec4 worldPos = uModelM * vec4(aPosition, 1.0);
        vFragPosition = worldPos;

        vTexCoord = aTexCoord;

        mat3 normalMatrix = transpose(inverse(mat3(uModelM)));
        vNormal = normalMatrix * aNormal;

        gl_Position = uProjectionM * uViewM * worldPos;

        vMatAmbient = uMatAmbient;
        vMatDiffuse = uMatDiffuse;
        vMatSpecular = uMatSpecular;
        vMatShine = uMatShine;
        vMatTransp = uMatTransp;
    }
""".trimIndent()

private val deferredGeomFrag = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_FRAG

    in vec4 vFragPosition;
    in vec2 vTexCoord;
    in vec3 vNormal;

    in vec3 vMatAmbient;
    in vec3 vMatDiffuse;
    in vec3 vMatSpecular;
    in float vMatShine;
    in float vMatTransp;

    uniform sampler2D uTexDiffuse;

    layout (location = 0) out vec4 oPosition;
    layout (location = 1) out vec3 oNormal;
    layout (location = 2) out vec4 oDiffuse;

    layout (location = 3) out vec4 oMatAmbientShine;
    layout (location = 4) out vec4 oMatDiffTransp;
    layout (location = 5) out vec3 oMatSpecular;

    void main()
    {
        vec4 diffuse = texture(uTexDiffuse, vTexCoord);
        if (diffuse.a < 0.1) {
            discard;
        }
        oDiffuse = diffuse;
        oPosition = vFragPosition;
        oNormal = normalize(vNormal);
        oMatAmbientShine.a = vMatShine;
        oMatAmbientShine.rgb = vMatAmbient;
        oMatDiffTransp.a = vMatTransp;
        oMatDiffTransp.rgb = vMatDiffuse;
        oMatSpecular = vMatSpecular;
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

    in vec2 vTexCoord;

    uniform sampler2D uTexPosition;
    uniform sampler2D uTexNormal;
    uniform sampler2D uTexDiffuse;

    uniform sampler2D uTexMatAmbientShine;
    uniform sampler2D uTexMatDiffTransp;
    uniform sampler2D uTexMatSpecular;

    uniform vec3 uEye;

    struct Light {
        vec3 vector;
        vec3 intensity;
    };

    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];

    out vec4 oFragColor;

    void main()
    {
        vec4 positionLookup = texture(uTexPosition, vTexCoord);
        if (positionLookup.a != 1.0) {
            discard;
        }

        vec3 fragPosition = positionLookup.rgb;
        vec3 fragNormal = texture(uTexNormal, vTexCoord).rgb;
        vec3 fragDiffuse = texture(uTexDiffuse, vTexCoord).rgb;

        vec4 matAmbientShine = texture(uTexMatAmbientShine, vTexCoord);
        vec4 matDiffuseTransp = texture(uTexMatDiffTransp, vTexCoord);
        vec3 matSpecular = texture(uTexMatSpecular, vTexCoord).rgb;

        vec3 viewDir  = normalize(uEye - fragPosition);
        vec3 lighting  = matAmbientShine.rgb;

        for (int i = 0; i < uLightsPointCnt; ++i) {
            lighting += expr_pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i].vector, uLights[i].intensity,
            matDiffuseTransp.rgb, matSpecular, matAmbientShine.a);
        }

        for (int i = 0; i < uLightsDirCnt; ++i) {
            lighting += expr_dirLightContrib(viewDir, fragNormal, uLights[i].vector, uLights[i].intensity,
            matDiffuseTransp.rgb, matSpecular, matAmbientShine.a);
        }

        lighting *= fragDiffuse;
        // todo: oFragColor = vec4(lighting, matDiffuseTransp.a);
        oFragColor = vec4(lighting, 1.0);
    }
""".trimIndent()

class DeferredTechnique : GlResource() {
    private val programGeomPass = GlProgram(
        vertexShader = GlShader(GlShaderType.VERTEX_SHADER, deferredGeomVert),
        fragmentShader = GlShader(GlShaderType.FRAGMENT_SHADER, deferredGeomFrag))

    private val programLightPass = GlProgram(
        vertexShader = GlShader(GlShaderType.VERTEX_SHADER, deferredLightVert),
        fragmentShader = GlShader(GlShaderType.FRAGMENT_SHADER, deferredLightFrag))

    private val quadMesh = GlMesh.rect()

    private val framebuffer = GlFrameBuffer()

    private lateinit var positionStorage: GlTexture
    private lateinit var normalStorage: GlTexture
    private lateinit var diffuseStorage: GlTexture

    private lateinit var matAmbShineStorage: GlTexture // ambient + shine
    private lateinit var matDiffTranspStorage: GlTexture // diffuse + transparency
    private lateinit var matSpecularStorage: GlTexture

    private lateinit var depthBuffer: GlRenderBuffer

    init {
        addChildren(programGeomPass, programLightPass, quadMesh, framebuffer)
    }

    override fun onRelease() {
        releaseFrame()
    }

    private fun releaseFrame() {
        positionStorage.release()
        normalStorage.release()
        diffuseStorage.release()
        matAmbShineStorage.release()
        matDiffTranspStorage.release()
        matSpecularStorage.release()
        depthBuffer.release()
    }

    fun resize(width: Int, height: Int) {
        checkReady()
        if (::positionStorage.isInitialized) {
            releaseFrame()
        }
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
        matAmbShineStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGBA16F,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        matAmbShineStorage.use()
        matDiffTranspStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGBA16F,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        matDiffTranspStorage.use()
        matSpecularStorage = GlTexture(
            width = width, height = height, internalFormat = backend.GL_RGB16F,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        matSpecularStorage.use()
        depthBuffer = GlRenderBuffer(width = width, height = height)
        depthBuffer.use()
        glBind(framebuffer, positionStorage, normalStorage, diffuseStorage,
            matAmbShineStorage, matDiffTranspStorage, matSpecularStorage, depthBuffer) {
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT3, matAmbShineStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT4, matDiffTranspStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT5, matSpecularStorage)
            framebuffer.setOutputs(intArrayOf(
                backend.GL_COLOR_ATTACHMENT0, backend.GL_COLOR_ATTACHMENT1, backend.GL_COLOR_ATTACHMENT2,
                backend.GL_COLOR_ATTACHMENT3, backend.GL_COLOR_ATTACHMENT4, backend.GL_COLOR_ATTACHMENT5
            ))
            framebuffer.setRenderBuffer(backend.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
            glBind(programLightPass) {
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_POSITION.label, positionStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL.label, normalStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE.label, diffuseStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_AMB_SHINE.label, matAmbShineStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_DIFF_TRANSP.label, matDiffTranspStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_SPECULAR.label, matSpecularStorage)
            }
        }
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, instances: () -> Unit, lights: () -> Unit) {
        checkReady()
        pointLightCnt = 0
        dirLightCnt = 0
        glBind(framebuffer) {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            glBind(programGeomPass) {
                programGeomPass.setUniform(GlUniform.UNIFORM_VIEW_M.label, camera.calculateViewM())
                programGeomPass.setUniform(GlUniform.UNIFORM_PROJ_M.label, camera.projectionM)
                instances.invoke()
            }
        }
        glBind(programLightPass, quadMesh, depthBuffer,
            positionStorage, normalStorage, diffuseStorage,
            matAmbShineStorage, matDiffTranspStorage, matSpecularStorage) {
            lights.invoke()
            programLightPass.setUniform(GlUniform.UNIFORM_EYE.label, camera.position)
            programLightPass.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT.label, pointLightCnt)
            programLightPass.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT.label, dirLightCnt)
            programLightPass.draw(indicesCount = quadMesh.indicesCount)
        }
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        checkReady()
        if (light.point) {
            modelM.getColumn(3, lightVectorBuf)
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR.label, pointLightCnt, lightVectorBuf)
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY.label, pointLightCnt, light.intensity)
            pointLightCnt++
        } else {
            modelM.getRow(2, lightVectorBuf) // transpose
            lightVectorBuf.negate() // -Z
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR.label, dirLightCnt, lightVectorBuf)
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY.label, dirLightCnt, light.intensity)
            dirLightCnt++
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, diffuse: GlTexture, material: PhongMaterial) {
        checkReady()
        glBind(mesh, diffuse) {
            programGeomPass.setUniform(GlUniform.UNIFORM_MODEL_M.label, modelM)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_AMBIENT.label, material.ambient)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_DIFFUSE.label, material.diffuse)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_SPECULAR.label, material.specular)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_SHINE.label, material.shine)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_TRANSP.label, material.transparency)
            programGeomPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE.label, diffuse)
            programGeomPass.draw(indicesCount = mesh.indicesCount)
        }
    }
}

private val camera = Camera()
private val controller = Controller(position = vec3(1f, 4f, 6f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val light = Light(vec3(1f), false)
private val light2 = Light(vec3(1f), false)

private val objMatrix = mat4().identity()
private val lightMatrix = mat4().identity().lookAlong(vec3(1f, -1f, -1f), vec3().up())
private val lightMatrix2 = mat4().identity().lookAlong(vec3(-1f, -1f, -1f), vec3().up())

private val deferredTechnique = DeferredTechnique()
private val skyboxTechnique = StaticSkyboxTechnique("textures/miramar")

private val obj = modelLib.load("models/house/low").first()

private var mouseLook = false

fun main() {
    val window = GlWindow()
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
            deferredTechnique.resize(width, height)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
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
        glUse(deferredTechnique, skyboxTechnique, obj) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glDepthTest {
                    glCulling {
                        deferredTechnique.draw(camera, instances = {
                            deferredTechnique.instance(obj.mesh, objMatrix, obj.phong().mapDiffuse!!, PhongMaterial.CONCRETE)
                        }, lights = {
                            deferredTechnique.light(light, lightMatrix)
                            deferredTechnique.light(light2, lightMatrix2)
                        })
                    }
                }
            }
        }
    }
}