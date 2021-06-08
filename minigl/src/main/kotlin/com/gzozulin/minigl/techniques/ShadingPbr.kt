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

    layout (location = 0) out vec4 oFragColor;
    
    void main() {
        vec3 N = getNormalFromMap(%NORMAL%.xyz, vWorldPos, vTexCoord, vNormal);
        oFragColor = shadingPbr(%ALBEDO%.rgb, N, %METALLIC%.r, %ROUGHNESS%.r, %AO%.r, %EYE%, vWorldPos);
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

private val unifAlbedo      = sampler(unifs { material.albedo })
private val unifNormal      = sampler(unifs { material.normal })
private val unifMetallic    = sampler(unifs { material.metallic })
private val unifRoughness   = sampler(unifs { material.roughness })
private val unifAO          = sampler(unifs { material.ao })

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
