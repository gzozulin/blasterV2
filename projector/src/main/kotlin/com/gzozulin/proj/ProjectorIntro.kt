package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.assets.libTextureCreatePbr
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.assets.libWavefrontGroupUse
import com.gzozulin.minigl.scene.*
import com.gzozulin.minigl.techniques.*

private const val TEXTURE_SIDE = 4096
private const val HORIZONTAL_CNT = 24
private const val VERTICAL_CNT = 24
private const val HORIZONTAL_STEP = 5f
private const val VERTICAL_STEP = 3.5f

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3().front().mul(10f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

// -------------------------------- Lights --------------------------------

private val lightsColors = listOf(
    vec3().red(),
    vec3().green(),
    vec3().blue(),
    vec3().yellow(),
    vec3().magenta(),
    vec3().cyan(),
    vec3().orange(),
    vec3().azure(),
    vec3().chartreuse()
)

private val lightCamera = PointLight(vec3(), vec3(5f), 100f)
private val lightsScene = mutableListOf<Light>().apply {
    for (i in 0 until HORIZONTAL_CNT/4) {
        for (j in 0 until VERTICAL_CNT/4) {
            add(PointLight(
                vec3(i * HORIZONTAL_STEP*4, j * VERTICAL_STEP*4, 5f),
                vec3().set(lightsColors.random()).mul(10f),
                10f))
        }
    }
}

private val lightsDirection = mutableListOf<Boolean>().apply {
    for (light in lightsScene) {
        add(randi() % 2 == 0)
    }
}

private fun lightsUpdate() {
    for (i in 0 until lightsScene.size) {
        val sceneLight = lightsScene[i] as PointLight
        val direction = lightsDirection[i]
        val delta = randf(0.1f, 10f)
        if (direction) {
            sceneLight.range += delta
            if (sceneLight.range >= 100f) {
                lightsDirection[i] = false
            }
        } else {
            sceneLight.range -= delta
            if (sceneLight.range <= 5f) {
                lightsDirection[i] = true
            }
        }
    }
}

private val lightsAll = lightsScene + lightCamera

// -------------------------------- Resources --------------------------------

private val tvGroup = libWavefrontCreate("models/tv/tv")
private val tvObject = tvGroup.objects.first()
private val tvMaterial = libTextureCreatePbr("models/tv")

private val teapotLight = PointLight(vec3(3f), vec3(1f), 100f)

private val teapotGroup = libWavefrontCreate("models/teapot/teapot")
private val teapotObject = teapotGroup.objects.first()

private val ditheringTexture = libTextureCreate("textures/points.jpg")

// -------------------------------- TVs --------------------------------

private val unifTvModel       = unifm4()
private val unifTvView        = unifm4 { camera.calculateViewM() }
private val unifTvEye         = unifv3 { camera.position }

private val unifTvAlbedo      = sampler(unifs(tvMaterial.albedo))
private val unifTvNormal      = sampler(unifs(tvMaterial.normal))
private val unifTvMetallic    = sampler(unifs(tvMaterial.metallic))
private val unifTvRoughness   = sampler(unifs(tvMaterial.roughness))
private val unifTvAO          = sampler(unifs(tvMaterial.ao))

private fun tvsUse(callback: Callback) {
    glShadingPbrUse(shadingPbr) {
        glMeshUse(tvObject.mesh) {
            glTextureUse(listOf(tvMaterial.albedo, tvMaterial.normal, tvMaterial.metallic,
                tvMaterial.roughness, tvMaterial.ao)) {
                callback.invoke()
            }
        }
    }
}

private fun tvsDraw() {
    glCulling {
        glDepthTest {
            glTextureBind(techniqueRtt.color) {
                glTextureBind(tvMaterial.normal) {
                    glTextureBind(tvMaterial.metallic) {
                        glTextureBind(tvMaterial.roughness) {
                            glTextureBind(tvMaterial.ao) {
                                lightCamera.position.set(camera.position).add(0f, 0f, -1f)
                                glShadingPbrDraw(shadingPbr, lightsAll) {
                                    for (i in 0 until HORIZONTAL_CNT) {
                                        for (j in 0 until VERTICAL_CNT) {
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

// -------------------------------- Teapots --------------------------------

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

private fun teapotsUse(callback: Callback) {
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

private fun teapotsDraw() {
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

// -------------------------------- Techniques --------------------------------

private val shadingPhong = ShadingPhong(unifTeaModel, constTeaView, constTeaProj, constTeaEye,
    constTeaAlbedo, constTeaMatAmbient, constTeaMatDiffuse, constTeaMatSpecular, constTeaMatShine)

private fun mapTeapot(screen: GlTexture): Expression<vec4> {
    val tvAlbedo = cachev4(unifTvAlbedo)
    val teapotColor = cachev4(sampler(unifs { screen }))
    val mixedTeapot = add(mul(teapotColor, tov4(constf(0.8f))), mul(tvAlbedo, tov4(constf(0.2f))))
    return ifexp(more(geta(teapotColor), constf(0f)), mixedTeapot, tvAlbedo)
}

private val postProcessing = TechniquePostProcessing(TEXTURE_SIDE, TEXTURE_SIDE, ::mapTeapot)
private val techniqueRtt = TechniqueRtt(TEXTURE_SIDE, TEXTURE_SIDE)

private val unifTvAlbedoTeapot = sampler(unifs(techniqueRtt.color))

private val shadingPbr = ShadingPbr(
    unifTvModel, unifTvView, constm4(camera.projectionM), unifTvEye,
    unifTvAlbedoTeapot, unifTvNormal, unifTvMetallic, unifTvRoughness, unifTvAO)

// -------------------------------- Dithering --------------------------------

private fun ditherScreen(screen: GlTexture) = mul(sampler(unifs(ditheringTexture)), sampler(unifs(screen)))
private val ditheringTechnique = TechniquePostProcessing(window, ::ditherScreen)

private fun ditheringUse(callback: Callback) {
    glTextureUse(ditheringTexture) {
        glTechPostProcessingUse(ditheringTechnique) {
            callback.invoke()
        }
    }
}

private fun ditheringDraw(callback: Callback) {
    glTextureBind(ditheringTexture) {
        glTechPostProcessingDraw(ditheringTechnique) {
            callback.invoke()
        }
    }
}

// -------------------------------- Main --------------------------------

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
        tvsUse {
            teapotsUse {
                ditheringUse {
                    window.show {
                        lightsUpdate()
                        glClear(col3().black())
                        controller.apply { position, direction ->
                            camera.setPosition(position)
                            camera.lookAlong(direction)
                        }
                        teapotsDraw()
                        //ditheringDraw {
                            tvsDraw()
                        //}
                    }
                }
            }
        }
    }
}