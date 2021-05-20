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

private const val TEXTURE_SIDE = 4096

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3().front().mul(10f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val cameraLight = PointLight(vec3(), vec3(5f), 100f)

// -------------------------------- TVs --------------------------------

private val tvGroup = libWavefrontCreate("models/tv/tv")
private val tvObject = tvGroup.objects.first()
private val tvMaterial = libTextureCreatePbr("models/tv")

private val unifTvModel       = unifm4()
private val unifTvView        = unifm4 { camera.calculateViewM() }
private val unifTvEye         = unifv3 { camera.position }

private val unifTvAlbedo      = sampler(unift(tvMaterial.albedo))
private val unifTvNormal      = sampler(unift(tvMaterial.normal))
private val unifTvMetallic    = sampler(unift(tvMaterial.metallic))
private val unifTvRoughness   = sampler(unift(tvMaterial.roughness))
private val unifTvAO          = sampler(unift(tvMaterial.ao))

// -------------------------------- Teapots --------------------------------

private val teapotLight = PointLight(vec3(3f), vec3(1f), 100f)

private val teapotGroup = libWavefrontCreate("models/teapot/teapot")
private val teapotObject = teapotGroup.objects.first()

var rotation = 0f
private val unifTeaModel = unifm4 {
    rotation += 0.01f
    mat4().identity()
        .translate(-10f, 2f, -10f)
        .rotate(radf(-90f), vec3().front())
        .rotate(rotation, vec3().up())
}

private val constTeaView = constm4(mat4().identity())
private val constTeaProj = constm4(camera.projectionM)
private val constTeaEye = constv3(vec3().zero())
private val constTeaAlbedo = constv4(vec4(vec3().rose(), 1f))
private val constTeaMatAmbient = constv3(vec3(0.1f))
private val constTeaMatDiffuse = constv3(vec3(1f))
private val constTeaMatSpecular = constv3(vec3(0.5f))
private val constTeaMatShine = constf(100f)

private fun filter(screen: GlTexture): Expression<vec4> {
    val tvAlbedo = cachev4(unifTvAlbedo)
    val teapotColor = cachev4(sampler(unift { screen }))
    val mixedTeapot = add(mul(teapotColor, tov4(constf(0.8f))), mul(tvAlbedo, tov4(constf(0.2f))))
    return ifexp(more(geta(teapotColor), constf(0f)), mixedTeapot, tvAlbedo)
}

// -------------------------------- Techniques --------------------------------

private val shadingPhong = ShadingPhong(unifTeaModel, constTeaView, constTeaProj, constTeaEye,
    constTeaAlbedo, constTeaMatAmbient, constTeaMatDiffuse, constTeaMatSpecular, constTeaMatShine)

private val postProcessing = TechniquePostProcessing(TEXTURE_SIDE, TEXTURE_SIDE, ::filter)
private val techniqueRtt = TechniqueRtt(TEXTURE_SIDE, TEXTURE_SIDE)

private val unifTvAlbedoTeapot = sampler(unift(techniqueRtt.color))

private val shadingPbr = ShadingPbr(
    unifTvModel, unifTvView, constm4(camera.projectionM), unifTvEye,
    unifTvAlbedoTeapot, unifTvNormal, unifTvMetallic, unifTvRoughness, unifTvAO)

// -------------------------------- Business --------------------------------

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

private fun drawTeapots() {
    glTechRttDraw(techniqueRtt) {
        glTextureBind(tvMaterial.albedo) {
            glTechPostProcessingDraw(postProcessing) {
                glDepthTest {
                    glCulling {
                        glClear()
                        glShadingPhongDraw(shadingPhong, listOf(teapotLight)) {
                            glShadingPhongInstance(shadingPhong, teapotObject.mesh)
                        }
                    }
                }
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
                                cameraLight.position.set(camera.position).add(0f, 0f, -1f)
                                glShadingPbrDraw(shadingPbr, listOf(cameraLight)) {
                                    for (i in 0 until 20) {
                                        for (j in 0 until 20) {
                                            unifTvModel.value = mat4().identity().translate(i * 5f, j * 3.5f, 0f).scale(10f)
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