package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.assets.libWavefrontGroupUse
import com.gzozulin.minigl.scene.*

/*package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.*
import org.joml.Matrix4f

private const val MAX_LIGHTS = 128

@Deprecated("Use assembly instead!")
class StaticPbrTechnique: GlResource() {
    private val program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")

    init {
        addChildren(program)
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, lights: () -> Unit, meshes: () -> Unit) {
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M.label,    camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M.label,    camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE.label,       camera.position)
            lights.invoke()
            program.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT.label, pointLightCnt)
            //program.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT.label, dirLightCnt)
            meshes.invoke()
        }
        pointLightCnt = 0
        dirLightCnt = 0
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        if (light is PointLight) {
            modelM.getColumn(3, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR.label, pointLightCnt, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY.label, pointLightCnt, light.color)
            pointLightCnt++
        } else {
            TODO()
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {
        glBind(mesh, material.albedo, material.normal, material.metallic, material.roughness, material.ao) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M.label,           modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ALBEDO.label,    material.albedo)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL.label,    material.normal)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_METALLIC.label,  material.metallic)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ROUGHNESS.label, material.roughness)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_AO.label,        material.ao)
            program.draw(mesh)
        }
    }
}

private val window = GlWindow()

private val camera = Camera()
private val controller = ControllerFirstPerson(position = vec3(0f, 2.5f, 4f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/miramar")
private val pbrTechnique = StaticPbrTechnique()

private val obj = modelLib.load("models/mandalorian/mandalorian").first()
    .copy(material = texturesLib.loadPbr("models/mandalorian"))

private val light = PointLight(vec3(3f), vec3(25f), 100f)

private val objMatrix = mat4().identity().scale(obj.aabb.scaleTo(5f))
private val lightMatrix = mat4().identity().translate(vec3(3f))

private var mouseLook = false

fun main() {
    window.create(resizables = listOf(camera), isFullscreen = false, isHoldingCursor = false, isMultisampling = true) {
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
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        glUse(skyboxTechnique, pbrTechnique, obj) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glCulling {
                    glDepthTest {
                        pbrTechnique.draw(camera, lights = {
                            pbrTechnique.light(light, lightMatrix)
                        }, meshes = {
                            pbrTechnique.instance(obj.mesh, objMatrix, obj.pbr())
                        })
                    }
                }
            }
        }
    }
}*/

private val vertexSrc = """
    $VERT_SHADER_HEADER

    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;

    uniform mat4 uModelM;
    uniform mat4 uProjM;
    uniform mat4 uViewM;

    out vec3 vWorldPos;
    out vec2 vTexCoord;
    out vec3 vNormal;

    void main() {
        vTexCoord = aTexCoord;
        vWorldPos = vec3(uModelM * vec4(aPosition, 1.0));
        vNormal = mat3(uModelM) * aNormal;
        gl_Position =  uProjectionM * uViewM * vec4(vWorldPos, 1.0);
    }
""".trimIndent()

private val fragmentSrc = """
    #version 300 es

    precision highp float;

    const float PI = 3.14159265359;

    const float lightConstantAtt    = 0.9;
    const float lightLinearAtt      = 0.7;
    const float lightQuadraticAtt   = 0.3;

    in vec3 vWorldPos;
    in vec2 vTexCoord;
    in vec3 vNormal;

    uniform vec3 uEye;

    struct Light {
        vec3 vector;
        vec3 intensity;
    };

    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[128];

    uniform sampler2D uTexAlbedo;
    uniform sampler2D uTexNormal;
    uniform sampler2D uTexMetallic;
    uniform sampler2D uTexRoughness;
    uniform sampler2D uTexAo;

    layout (location = 0) out vec4 oFragColor;

    float attenuation(float distance) {
        return 1.0 / (lightConstantAtt + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
    }

    vec3 getNormalFromMap() {
        vec3 tangentNormal = texture(uTexNormal, vTexCoord).xyz * 2.0 - 1.0;

        vec3 Q1  = dFdx(vWorldPos);
        vec3 Q2  = dFdy(vWorldPos);
        vec2 st1 = dFdx(vTexCoord);
        vec2 st2 = dFdy(vTexCoord);

        vec3 N   = normalize(vNormal);
        vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
        vec3 B  = -normalize(cross(N, T));
        mat3 TBN = mat3(T, B, N);

        return normalize(TBN * tangentNormal);
    }

    float distributionGGX(vec3 N, vec3 H, float roughness) {
        float a = roughness*roughness;
        float a2 = a*a;
        float NdotH = max(dot(N, H), 0.0);
        float NdotH2 = NdotH*NdotH;

        float nom   = a2;
        float denom = (NdotH2 * (a2 - 1.0) + 1.0);
        denom = PI * denom * denom;

        return nom / denom;
    }

    float geometrySchlickGGX(float NdotV, float roughness) {
        float r = (roughness + 1.0);
        float k = (r*r) / 8.0;

        float nom   = NdotV;
        float denom = NdotV * (1.0 - k) + k;

        return nom / denom;
    }

    float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
        float NdotV = max(dot(N, V), 0.0);
        float NdotL = max(dot(N, L), 0.0);
        float ggx2 = geometrySchlickGGX(NdotV, roughness);
        float ggx1 = geometrySchlickGGX(NdotL, roughness);

        return ggx1 * ggx2;
    }

    vec3 fresnelSchlick(float cosTheta, vec3 F0) {
        return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
    }

    void main() {
        vec3 albedo     = pow(texture(uTexAlbedo, vTexCoord).rgb, vec3(2.2));
        float metallic  = texture(uTexMetallic, vTexCoord).r;
        float roughness = texture(uTexRoughness, vTexCoord).r;
        float ao        = texture(uTexAo, vTexCoord).r;

        vec3 N = getNormalFromMap();
        vec3 V = normalize(uEye - vWorldPos);

        // calculate reflectance at normal incidence; if dia-electric (like plastic) use F0
        // of 0.04 and if it's a metal, use the albedo color as F0 (metallic workflow)
        vec3 F0 = vec3(0.04);
        F0 = mix(F0, albedo, metallic);

        // reflectance equation
        vec3 Lo = vec3(0.0);
        for(int i = 0; i < uLightsPointCnt; ++i)
        {
            // calculate per-light radiance
            vec3 L = normalize(uLights[i].vector - vWorldPos);
            vec3 H = normalize(V + L);
            float distance = length(uLights[i].vector - vWorldPos);
            float attenuation = attenuation(distance);
            vec3 radiance = uLights[i].intensity * attenuation;

            // Cook-Torrance BRDF
            float NDF = distributionGGX(N, H, roughness);
            float G   = geometrySmith(N, V, L, roughness);
            vec3 F    = fresnelSchlick(max(dot(H, V), 0.0), F0);

            vec3 nominator    = NDF * G * F;
            float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001; // 0.001 to prevent divide by zero.
            vec3 specular = nominator / denominator;

            // kS is equal to Fresnel
            vec3 kS = F;
            // for energy conservation, the diffuse and specular light can't
            // be above 1.0 (unless the surface emits light); to preserve this
            // relationship the diffuse component (kD) should equal 1.0 - kS.
            vec3 kD = vec3(1.0) - kS;
            // multiply kD by the inverse metalness such that only non-metals
            // have diffuse lighting, or a linear blend if partly metal (pure metals
            // have no diffuse light).
            kD *= (1.0 - metallic);

            // scale light by NdotL
            float NdotL = max(dot(N, L), 0.0);

            // add to outgoing radiance Lo
            Lo += (kD * albedo / PI + specular) * radiance * NdotL;  // note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
        }

        // ambient lighting (note that the next IBL tutorial will replace
        // this ambient lighting with environment lighting).
        vec3 ambient = vec3(0.1) * albedo * ao;

        vec3 color = ambient + Lo;

        // HDR tonemapping
        color = color / (color + vec3(1.0));
        // gamma correct
        color = pow(color, vec3(1.0/2.2));

        oFragColor = vec4(color, 1.0);
    }
""".trimIndent()

data class ShadingPbr(val modelM: Expression<mat4>, val viewM: Expression<mat4>, val projM: Expression<mat4>,
                      val albedo: Expression<vec4>, val normal: Expression<vec4>, val metallic: Expression<vec4>,
                      val roughness: Expression<vec4>, val ao: Expression<vec4>) {

    private val vertShader = GlShader(
        backend.GL_VERTEX_SHADER,
        glExprSubstitute(vertexSrc, mapOf(
            "MODEL"     to modelM,
            "VIEW"      to viewM,
            "PROJ"      to projM,
        ))
    )

    private val fragShader = GlShader(
        backend.GL_FRAGMENT_SHADER,
        glExprSubstitute(fragmentSrc, mapOf(
            "ALBEDO"    to albedo,
            "NORMAL"    to normal,
            "METALLIC"  to metallic,
            "ROUGHNESS" to roughness,
            "AO"        to ao,
        ))
    )

    internal val program = GlProgram(vertShader, fragShader)
}

fun glShadingPbrUse(shadingPbr: ShadingPbr, callback: Callback) =
    glProgramUse(shadingPbr.program, callback)

fun glShadingPbrDraw(shadingPbr: ShadingPbr, lights: List<Light>, callback: Callback) {
    glProgramBind(shadingPbr.program) {
        glProgramSubmitLights(shadingPbr.program, lights)
        shadingPbr.viewM.submit(shadingPbr.program)
        shadingPbr.projM.submit(shadingPbr.program)
        callback.invoke()
    }
}

fun glShadingPbrInstance(shadingPbr: ShadingPbr, mesh: GlMesh) {
    shadingPbr.modelM.submit(shadingPbr.program)
    shadingPbr.albedo.submit(shadingPbr.program)
    shadingPbr.normal.submit(shadingPbr.program)
    shadingPbr.metallic.submit(shadingPbr.program)
    shadingPbr.roughness.submit(shadingPbr.program)
    shadingPbr.ao.submit(shadingPbr.program)
    glMeshBind(mesh) {
        glDrawTriangles(shadingPbr.program, mesh)
    }
}

/*
private val window = GlWindow(isFullscreen = false, isHoldingCursor = false, isMultisampling = true)

private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3(0f, 2.5f, 4f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val group = libWavefrontCreate("models/mandalorian/mandalorian")

private val obj = modelLib.load("models/mandalorian/mandalorian").first()
    .copy(material = texturesLib.loadPbr("models/mandalorian"))

private val light = PointLight(vec3(3f), vec3(25f), 100f)

private val objMatrix = mat4().identity().scale(obj.aabb.scaleTo(5f))
private val lightMatrix = mat4().identity().translate(vec3(3f))

private var mouseLook = false

fun main() {
    window.create {
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
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        glUse(skyboxTechnique, pbrTechnique, obj) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                glCulling {
                    glDepthTest {
                        pbrTechnique.draw(camera, lights = {
                            pbrTechnique.light(light, lightMatrix)
                        }, meshes = {
                            pbrTechnique.instance(obj.mesh, objMatrix, obj.pbr())
                        })
                    }
                }
            }
        }
    }
}*/
