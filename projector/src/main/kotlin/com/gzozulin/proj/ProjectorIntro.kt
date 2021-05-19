package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreatePbr
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.assets.libWavefrontGroupUse
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.PointLight
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.*

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3().front().mul(10f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val light = PointLight(vec3(3f), vec3(25f), 100f)

// -------------------------------- TVs --------------------------------

private val tvGroup = libWavefrontCreate("models/tv/tv")
private val tvObject = tvGroup.objects.first()
private val tvMaterial = libTextureCreatePbr("models/tv")

private val unifTvModel       = unifm4()
private val unifTvView        = unifm4 { camera.calculateViewM() }
private val unifTvEye         = unifv3 { camera.position }

private val unifTvAlbedo      = sampler(unift { tvMaterial.albedo })
private val unifTvAlbedoTeapot = sampler(unift { techniqueRtt.color })
private val unifTvNormal      = sampler(unift { tvMaterial.normal })
private val unifTvMetallic    = sampler(unift { tvMaterial.metallic })
private val unifTvRoughness   = sampler(unift { tvMaterial.roughness })
private val unifTvAO          = sampler(unift { tvMaterial.ao })

// -------------------------------- Teapots --------------------------------

private val teapotGroup = libWavefrontCreate("models/teapot/teapot")
private val teapotObject = teapotGroup.objects.first()

private val unifTeaModel = constm4(mat4().identity()
    .translate(-10f, 2f, -10f).rotate(radf(-90f), vec3().front()))
private val unifTeaView = constm4(mat4().identity())
private val unifTeaProj = unifm4(camera.projectionM)
private val unifTeaEye = unifv3 { camera.position }

private val shadingPhong = ShadingPhong(unifTeaModel, unifTeaView, unifTeaProj, unifTeaEye)

private fun filter(screen: GlTexture): Expression<vec4> {
    val teapotColor = cachev4(sampler(unift { screen }))
    val mixedTeapot = add(mul(teapotColor, tov4(constf(0.1f))), mul(unifTvAlbedo, tov4(constf(0.9f))))
    return ifexp(
        more(geta(teapotColor), constf(0f)),
        mixedTeapot,
        unifTvAlbedo
    )
}

// -------------------------------- Techniques --------------------------------

private val postProcessing = TechniquePostProcessing(window, ::filter) // todo: texture size
private val techniqueRtt = TechniqueRtt(window) // todo: texture size
private val shadingPbr = ShadingPbr(
    unifTvModel, unifTvView, constm4(camera.projectionM), unifTvEye,
    unifTvAlbedoTeapot, unifTvNormal, unifTvMetallic, unifTvRoughness, unifTvAO)

private fun useTeapots(callback: Callback) {
    glTechRttUse(techniqueRtt) {
        glTechPostProcessingUse(postProcessing) {
            glShadingPhongUse(shadingPhong) {
                libWavefrontGroupUse(teapotGroup) {
                    callback.invoke()
                }
            }
        }
    }
}

private fun drawTeapots() {
    glTechRttDraw(techniqueRtt) {
        glTextureBind(tvMaterial.albedo) {
            glTechPostProcessingDraw(postProcessing) {
                glClear()
                glShadingPhongDraw(shadingPhong, listOf(light)) {
                    glShadingPhongInstance(shadingPhong, teapotObject.mesh)
                }
            }
        }
    }
}

private fun useTVs(callback: Callback) {
    glShadingPbrUse(shadingPbr) {
        glMeshUse(tvObject.mesh) {
            glTextureUse(listOf(tvMaterial.albedo, tvMaterial.normal, tvMaterial.metallic,
                tvMaterial.roughness, tvMaterial.ao)) {
                callback.invoke()
            }
        }
    }
}

private fun drawTVs() {
    glCulling {
        glDepthTest {
            glTextureBind(techniqueRtt.color) {
                glTextureBind(tvMaterial.normal) {
                    glTextureBind(tvMaterial.metallic) {
                        glTextureBind(tvMaterial.roughness) {
                            glTextureBind(tvMaterial.ao) {
                                glShadingPbrDraw(shadingPbr, listOf(light)) {
                                    for (i in 0 until 20) {
                                        for (j in 0 until 20) {
                                            unifTvModel.value =
                                                mat4().identity().translate(i * 5f, j * 5f, 0f).scale(10f)
                                            glShadingPbrInstance(shadingPbr, tvObject.mesh)
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
        useTVs {
            useTeapots {

                window.show {
                    glClear(col3().black())
                    controller.apply { position, direction ->
                        camera.setPosition(position)
                        camera.lookAlong(direction)
                    }
                    drawTeapots()
                    drawTVs()
                }
            }
        }
    }
}