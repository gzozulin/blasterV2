package com.gzozulin.minigl.techniques2

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api2.*
import com.gzozulin.minigl.api2.GlMesh
import com.gzozulin.minigl.api2.GlProgram
import com.gzozulin.minigl.api2.GlShader
import com.gzozulin.minigl.assets2.libWavefrontCreate
import com.gzozulin.minigl.assets2.PhongMaterial
import com.gzozulin.minigl.assets2.PHONG_MATERIALS
import com.gzozulin.minigl.scene.*
import org.lwjgl.glfw.GLFW
import java.lang.Float.max

private const val MAX_LIGHTS = 128

private const val vertSrc = """
    $VERT_SHADER_HEADER

    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;
    
    out vec4 vPosition;
    out vec2 vTexCoord;
    out vec3 vNormal;

    void main()
    {
        vec4 worldPos = %MODEL% * vec4(aPosition, 1.0);
        vPosition = worldPos;
        vTexCoord = aTexCoord;
        mat3 normalM = transpose(inverse(mat3(%MODEL%)));
        vNormal = normalM * aNormal;
        gl_Position = %PROJ% * %VIEW% * worldPos;
    }
"""

private const val fragSrc = """
    $FRAG_SHADER_HEADER
    
    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];

    in vec4 vPosition;
    in vec2 vTexCoord;
    in vec3 vNormal;
    
    layout (location = 0) out vec4 oFragColor;

    void main()
    {
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

data class ShadingPhong(val modelM: Expression<mat4>,
                   val viewM: Expression<mat4>,
                   val projM: Expression<mat4>,
                   val eye: Expression<vec3>,
                   val diffuse: Expression<vec4> = constv4(vec4(1f)),
                   val matAmbient: Expression<vec3> = constv3(vec3(1f)),
                   val matDiffuse: Expression<vec3> = constv3(vec3(1f)),
                   val matSpecular: Expression<vec3> = constv3(vec3(1f)),
                   val matShine: Expression<Float> = constf(10f),
                   val matTransparency: Expression<Float> = constf(1f)
) {
    private val vertShader = GlShader(backend.GL_VERTEX_SHADER,
        glExprSubstitute(vertSrc, mapOf(
            "MODEL"     to modelM,
            "VIEW"      to viewM,
            "PROJ"      to projM,
        )))

    private val fragShader = GlShader(backend.GL_FRAGMENT_SHADER,
        glExprSubstitute(fragSrc, mapOf(
            "EYE"           to eye,
            "DIFFUSE"       to diffuse,
            "MAT_AMBIENT"   to matAmbient,
            "MAT_DIFFUSE"   to matDiffuse,
            "MAT_SPECULAR"  to matSpecular,
            "MAT_SHINE"     to matShine,
            "MAT_TRANSPARENCY" to matTransparency,
        )))

    internal val program = GlProgram(vertShader, fragShader)
}

private fun glShadingPhongSubmitLights(shadingPhong: ShadingPhong, lights: List<Light>) {
    check(lights.size <= MAX_LIGHTS) { "More lights than defined in shader!" }
    val sorted = lights.sortedBy { it is PointLight }
    var pointLightCnt = 0
    var dirLightCnt = 0
    sorted.forEachIndexed { index, light ->
        glProgramArrayUniform(shadingPhong.program, "uLights[%d].vector",          index, light.vector)
        glProgramArrayUniform(shadingPhong.program, "uLights[%d].color",           index, light.color)
        glProgramArrayUniform(shadingPhong.program, "uLights[%d].attenConstant",   index, light.attenConstant)
        glProgramArrayUniform(shadingPhong.program, "uLights[%d].attenLinear",     index, light.attenLinear)
        glProgramArrayUniform(shadingPhong.program, "uLights[%d].attenQuadratic",  index, light.attenQuadratic)
        if (light is PointLight) {
            pointLightCnt++
        } else {
            dirLightCnt++
        }
    }
    glProgramUniform(shadingPhong.program, "uLightsPointCnt", pointLightCnt)
    glProgramUniform(shadingPhong.program, "uLightsDirCnt",   dirLightCnt)
}

fun glShadingPhongUse(shadingPhong: ShadingPhong, callback: Callback) =
    glProgramUse(shadingPhong.program, callback)

fun glShadingPhongDraw(shadingPhong: ShadingPhong, lights: List<Light>, callback: Callback) {
    glProgramBind(shadingPhong.program) {
        glShadingPhongSubmitLights(shadingPhong, lights)
        shadingPhong.viewM.submit(shadingPhong.program)
        shadingPhong.eye.submit(shadingPhong.program)
        shadingPhong.projM.submit(shadingPhong.program)
        callback.invoke()
    }
}

fun glShadingPhongInstance(shadingPhong: ShadingPhong, mesh: GlMesh) {
    shadingPhong.modelM.submit(shadingPhong.program)
    shadingPhong.diffuse.submit(shadingPhong.program)
    shadingPhong.matAmbient.submit(shadingPhong.program)
    shadingPhong.matDiffuse.submit(shadingPhong.program)
    shadingPhong.matSpecular.submit(shadingPhong.program)
    shadingPhong.matTransparency.submit(shadingPhong.program)
    shadingPhong.matShine.submit(shadingPhong.program)
    glMeshBind(mesh) {
        glDrawTriangles(mesh)
    }
}

private val camera = Camera()
private val controller = ControllerFirstPerson(velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val group = libWavefrontCreate("models/sphere/sphere")
private val mesh = group.objects.first().mesh

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

private val shadingPhong = ShadingPhong(
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
        glShadingPhongUse(shadingPhong) {
            glMeshUse(mesh) {
                window.show {
                    glClear(vec3().ltGrey())
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
                            glShadingPhongDraw(shadingPhong, listOf(light)) {
                                sphereMatrices.forEachIndexed { index, mat ->
                                    unifModelM.value = mat
                                    val material = sphereMaterials[index]
                                    unifMaterialAmbient.value = material.ambient
                                    unifMaterialDiffuse.value = material.diffuse
                                    unifMaterialSpecular.value = material.specular
                                    unifMaterialShine.value = material.shine
                                    unifMaterialTransp.value = material.transparency
                                    glShadingPhongInstance(shadingPhong, mesh)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}