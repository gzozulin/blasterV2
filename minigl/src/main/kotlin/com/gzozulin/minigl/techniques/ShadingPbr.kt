package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreatePbr
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.scene.*

private val vertexSrc = """
    $VERT_SHADER_HEADER

    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;

    out vec3 vWorldPos;
    out vec2 vTexCoord;
    out vec3 vNormal;

    void main() {
        vTexCoord = aTexCoord;
        vWorldPos = vec3(%MODEL% * vec4(aPosition, 1.0));
        vNormal = mat3(%MODEL%) * aNormal;
        gl_Position = %PROJ% * %VIEW% * vec4(vWorldPos, 1.0);
    }
""".trimIndent()

private val fragmentSrc = """
    $FRAG_SHADER_HEADER

    in vec3 vWorldPos;
    in vec2 vTexCoord;
    in vec3 vNormal;

    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[128];

    layout (location = 0) out vec4 oFragColor;

    vec3 getNormalFromMap(vec3 fromMap) {
        vec3 tangentNormal = fromMap * 2.0 - 1.0;

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
        vec3 albedo     = pow(%ALBEDO%.rgb, vec3(2.2));
        float metallic  = %METALLIC%.r;
        float roughness = %ROUGHNESS%.r;
        float ao        = %AO%.r;

        vec3 N = getNormalFromMap(%NORMAL%.xyz);
        vec3 V = normalize(%EYE% - vWorldPos);

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
            float luminosity = expr_luminosity(distance, uLights[i]);
            vec3 radiance = uLights[i].color * luminosity;

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
            Lo += (kD * albedo / PI + specular) * radiance * NdotL;  
            // note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
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

data class ShadingPbr(val modelM: Expression<mat4>, val viewM: Expression<mat4>, val projM: Expression<mat4>, val eye: Expression<vec3>,
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
            "EYE"       to eye,
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
        shadingPbr.eye.submit(shadingPbr.program)
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

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3(0f, 2.5f, 4f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val group = libWavefrontCreate("models/mandalorian/mandalorian")
private val obj = group.objects.first()
private val material = libTextureCreatePbr("models/mandalorian")

private val light = PointLight(vec3(3f), vec3(25f), 100f)

private var rotation        = 0.0f
private val unifModel       = unifm4 {
    rotation += 0.005f
    mat4().identity().scale(obj.aabb.scaleTo(5f)).rotate(rotation, vec3().up())
}
private val unifView        = unifm4 { camera.calculateViewM() }
private val unifEye         = unifv3 { camera.position }

private val texCoords       = namedTexCoordsV2()
private val unifAlbedo      = tex(texCoords, unifs { material.albedo })
private val unifNormal      = tex(texCoords, unifs { material.normal })
private val unifMetallic    = tex(texCoords, unifs { material.metallic })
private val unifRoughness   = tex(texCoords, unifs { material.roughness })
private val unifAO          = tex(texCoords, unifs { material.ao })

private val shadingPbr = ShadingPbr(
    unifModel, unifView, constm4(camera.projectionM), unifEye,
    unifAlbedo, unifNormal, unifMetallic, unifRoughness, unifAO)

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
        glShadingPbrUse(shadingPbr) {
            glMeshUse(obj.mesh) {
                glTextureUse(listOf(material.albedo, material.normal, material.metallic,
                    material.roughness, material.ao)) {
                    glCulling {
                        glDepthTest {
                            window.show {
                                glClear(col3().black())
                                controller.apply { position, direction ->
                                    camera.setPosition(position)
                                    camera.lookAlong(direction)
                                }
                                glTextureBind(material.albedo) {
                                    glTextureBind(material.normal) {
                                        glTextureBind(material.metallic) {
                                            glTextureBind(material.roughness) {
                                                glTextureBind(material.ao) {
                                                    glShadingPbrDraw(shadingPbr, listOf(light)) {
                                                        glShadingPbrInstance(shadingPbr, obj.mesh)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
