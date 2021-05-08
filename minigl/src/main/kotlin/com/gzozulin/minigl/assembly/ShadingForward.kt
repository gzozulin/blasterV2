package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.Object
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.scene.*
import org.lwjgl.glfw.GLFW
import java.lang.Float.max

// todo: create/release is probably working improperly

private const val MAX_LIGHTS = 128

private const val forwardVert = """
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
"""

private const val forwardFrag = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_FRAG
    
    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];

    in vec4 vPosition;
    in vec2 vTexCoord;
    in vec3 vNormal;
    
    layout (location = 0) out vec4 oFragColor;
    
    %DECL%

    void main()
    {
        %VRBL%
        
        vec3 fragPosition = vPosition.xyz;
        vec3 fragNormal = normalize(vNormal);
        vec3 fragDiffuse = %DIFFUSE%.rgb;
        vec3 matAmbient = %MAT_AMBIENT%;
        vec3 matDiffuse = %MAT_DIFFUSE%;
        vec3 matSpecular = %MAT_SPECULAR%;
        float shine = %MAT_SHINE%;
        float transparency = %MAT_TRANSPARENCY%;
        
        PhongMaterial material = { matAmbient, matDiffuse, matSpecular, shine, transparency };

        vec3 viewDir = normalize(%EYE% - fragPosition);
        vec3 color = matAmbient;

        for (int i = 0; i < uLightsPointCnt; ++i) {
            color += expr_pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i], material);
        }
        for (int i = uLightsPointCnt; i < uLightsPointCnt + uLightsDirCnt; ++i) {
            color += expr_dirLightContrib(viewDir, fragNormal, uLights[i], material);
        }
        
        // todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
        // https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/

        color *= fragDiffuse;
        oFragColor = vec4(color, transparency);
       
    }
"""

class ForwardTechnique(
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

    private val forwardProgram: GlProgram

    init {
        val forwardVertSrc = forwardVert.substituteDeclVrbl(
            modelM, viewM, projM)
            .replace("%MODEL%", modelM.expr())
            .replace("%VIEW%", viewM.expr())
            .replace("%PROJ%", projM.expr())
        val forwardFragSrc = forwardFrag.substituteDeclVrbl(
            eye, diffuse, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)
            .replace("%EYE%", eye.expr())
            .replace("%DIFFUSE%", diffuse.expr())
            .replace("%MAT_AMBIENT%", matAmbient.expr())
            .replace("%MAT_DIFFUSE%", matDiffuse.expr())
            .replace("%MAT_SPECULAR%", matSpecular.expr())
            .replace("%MAT_SHINE%", matShine.expr())
            .replace("%MAT_TRANSPARENCY%", matTransparency.expr())
        forwardProgram = GlProgram(
            vertexShader = GlShader(GlShaderType.VERTEX_SHADER, forwardVertSrc),
            fragmentShader = GlShader(GlShaderType.FRAGMENT_SHADER, forwardFragSrc))
    }

    init {
        addChildren(forwardProgram)
    }

    fun draw(lights: List<Light>, instances: () -> Unit) {
        checkReady()
        glBind(forwardProgram) {
            viewM.submit(forwardProgram)
            projM.submit(forwardProgram)
            eye.submit(forwardProgram)
            submitLights(lights)
            instances.invoke()
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

    private fun submitLights(lights: List<Light>) {
        check(lights.size <= MAX_LIGHTS) { "More lights than defined in shader!" }
        val sorted = lights.sortedBy { it is PointLight }
        var pointLightCnt = 0
        var dirLightCnt = 0
        sorted.forEachIndexed { index, light ->
            forwardProgram.setArrayUniform("uLights[%d].vector",          index, light.vector)
            forwardProgram.setArrayUniform("uLights[%d].color",           index, light.color)
            forwardProgram.setArrayUniform("uLights[%d].attenConstant",   index, light.attenConstant)
            forwardProgram.setArrayUniform("uLights[%d].attenLinear",     index, light.attenLinear)
            forwardProgram.setArrayUniform("uLights[%d].attenQuadratic",  index, light.attenQuadratic)
            if (light is PointLight) {
                pointLightCnt++
            } else {
                dirLightCnt++
            }
        }
        forwardProgram.setUniform("uLightsPointCnt", pointLightCnt)
        forwardProgram.setUniform("uLightsDirCnt",   dirLightCnt)
    }

    private fun renderInstance(mesh: GlMesh) {
        checkReady()
        modelM.submit(forwardProgram)
        diffuse.submit(forwardProgram)
        matAmbient.submit(forwardProgram)
        matDiffuse.submit(forwardProgram)
        matSpecular.submit(forwardProgram)
        matShine.submit(forwardProgram)
        matTransparency.submit(forwardProgram)
        forwardProgram.draw(indicesCount = mesh.indicesCount)
    }
}

private val camera = Camera()
private val controller = ControllerFirstPerson(velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val obj = modelLib.load("models/sphere/sphere").first()
private val sphereMaterials = mutableListOf<PhongMaterial>()
private val sphereMatrices = mutableListOf<mat4>()
private val sphereFill = (1..64).count {
    sphereMaterials.add(PHONG_MATERIALS.random())
    sphereMatrices.add(mat4().identity().translate(vec3().rand(vec3(-30f), vec3(30f))))
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

private val forwardTechnique = ForwardTechnique(
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
    window.create(resizables = listOf(camera), isFullscreen = true, isHoldingCursor = false, isMultisampling = true) {
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
            if (pressed) {
                when (key) {
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
        glUse(forwardTechnique, obj) {
            window.show {
                glClear(vec3().white())
                if (lightsUp) {
                    light.range += 10f
                    println(light.range)
                } else if (lightsDown) {
                    light.range -= 10f
                    println(light.range)
                }
                light.range = max(0f, light.range)
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                glDepthTest {
                    glCulling {
                        forwardTechnique.draw(listOf(light)) {
                            sphereMatrices.forEachIndexed { index, mat ->
                                unifModelM.value = mat
                                val material = sphereMaterials[index]
                                unifMaterialAmbient.value = material.ambient
                                unifMaterialDiffuse.value = material.diffuse
                                unifMaterialSpecular.value = material.specular
                                unifMaterialShine.value = material.shine
                                unifMaterialTransp.value = material.transparency
                                forwardTechnique.instance(obj)
                            }
                        }
                    }
                }
            }
        }
    }
}