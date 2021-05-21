package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.assets.libTextureCreatePbr
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.assets.libWavefrontGroupUse
import com.gzozulin.minigl.scene.*
import com.gzozulin.minigl.techniques.*

private const val TEXTURE_SIDE = 4096
private const val HORIZONTAL_CNT = 32
private const val VERTICAL_CNT = 32
private const val HORIZONTAL_STEP = 5f
private const val VERTICAL_STEP = 3.5f
private const val DRAW_DISTANCE = 100f

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3().front().mul(10f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

// -------------------------------- Resources --------------------------------

private val tvGroup = libWavefrontCreate("models/tv/tv")
private val tvObject = tvGroup.objects.first()
private val tvMaterial = libTextureCreatePbr("models/tv")

private val itemLight = PointLight(vec3(3f), vec3(1f), 100f)

private val teapotGroup = libWavefrontCreate("models/teapot/teapot")
private val teapotObject = teapotGroup.objects.first()
private val teapotTexture = libTextureCreate("textures/marble.jpg")

private val computerGroup = libWavefrontCreate("models/pcjr/pcjr")
private val computerObject = computerGroup.objects.first()
private val computerTexture = computerObject.phong.mapDiffuse!!

private val mandalorianGroup = libWavefrontCreate("models/mandalorian/mandalorian")
private val mandalorianObject = mandalorianGroup.objects.first()
private val mandalorianTexture = libTextureCreate("models/mandalorian/albedo.png")

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

// -------------------------------- TVs --------------------------------

private val unifTvModel       = unifm4()
private val unifTvView        = unifm4 { camera.calculateViewM() }
private val unifTvEye         = unifv3 { camera.position }

private val unifTvAlbedo      = sampler(unifs(tvMaterial.albedo))
private val unifTvNormal      = sampler(unifs(tvMaterial.normal))
private val unifTvMetallic    = sampler(unifs(tvMaterial.metallic))
private val unifTvRoughness   = sampler(unifs(tvMaterial.roughness))
private val unifTvAO          = sampler(unifs(tvMaterial.ao))

private enum class TvType { TEAPOT, COMPUTER, MANDALORIAN }
private data class TvInstance(val type: TvType, val matrix: mat4)

private val tvs = mutableListOf<TvInstance>().apply {
    for (i in 0 until HORIZONTAL_CNT) {
        for (j in 0 until VERTICAL_CNT) {
            add(TvInstance(
                TvType.values().random(),
                mat4().identity().translate(i * 5f, j * 3.5f, 0f).scale(10f)))
        }
    }
}

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
            glTextureBind(computerRtt.color) {
                glTextureBind(teapotRtt.color) {
                    glTextureBind(mandalorianRtt.color) {
                        glTextureBind(tvMaterial.normal) {
                            glTextureBind(tvMaterial.metallic) {
                                glTextureBind(tvMaterial.roughness) {
                                    glTextureBind(tvMaterial.ao) {
                                        lightCamera.position.set(camera.position).add(0f, 0f, -1f)
                                        glShadingPbrDraw(shadingPbr, lightsAll) {
                                            for (tv in tvs) {
                                                val worldPosition = tv.matrix.transform(vec4())
                                                if (worldPosition.distance(vec4(camera.position, 1f)) > DRAW_DISTANCE) {
                                                    continue
                                                }
                                                unifTvModel.value = tv.matrix
                                                when (tv.type) {
                                                    TvType.TEAPOT -> unifTvItemAlbedo.value = teapotRtt.color
                                                    TvType.COMPUTER -> unifTvItemAlbedo.value = computerRtt.color
                                                    TvType.MANDALORIAN -> unifTvItemAlbedo.value = mandalorianRtt.color
                                                }
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
}

// -------------------------------- Items --------------------------------

private var teapotRotation = 0f
private val teapotMatrix = mat4()

private var computerRotation = 0f
private val computerMatrix = mat4()

private var mandalorianRotation = 0f
private val mandalorianMatrix = mat4()

private val unifItemModel = unifm4()
private val unifItemAlbedo = unifs()

private val unifItemAlbedoSampler = sampler(unifItemAlbedo)
private val constItemView = constm4(mat4().identity())
private val constItemProj = constm4(camera.projectionM)
private val constIteEye = constv3(vec3().zero())
private val constItemMatAmbient = constv3(vec3(0.1f))
private val constItemMatDiffuse = constv3(vec3(1f))
private val constItemMatSpecular = constv3(vec3(0.5f))
private val constItemMatShine = constf(100f)

private fun mapItem(screen: GlTexture): Expression<vec4> {
    val tvAlbedo = cachev4(unifTvAlbedo)
    val itemColor = cachev4(sampler(unifs { screen }))
    val itemMerged = add(mul(itemColor, tov4(constf(0.8f))), mul(tvAlbedo, tov4(constf(0.2f))))
    return ifexp(more(geta(itemColor), constf(0f)), itemMerged, tvAlbedo)
}

private val mergingItem = TechniquePostProcessing(TEXTURE_SIDE, TEXTURE_SIDE, ::mapItem)

private val teapotRtt = TechniqueRtt(TEXTURE_SIDE, TEXTURE_SIDE)
private val computerRtt = TechniqueRtt(TEXTURE_SIDE, TEXTURE_SIDE)
private val mandalorianRtt = TechniqueRtt(TEXTURE_SIDE, TEXTURE_SIDE)

private fun itemUse(callback: Callback) {
    glTechRttUse(teapotRtt) {
        glTechRttUse(computerRtt) {
            glTechRttUse(mandalorianRtt) {
                glTechPostProcessingUse(mergingItem) {
                    glShadingPhongUse(shadingPhong) {
                        glTextureUse(teapotTexture) {
                            libWavefrontGroupUse(mandalorianGroup) {
                                glTextureUse(mandalorianTexture) {
                                    libWavefrontGroupUse(teapotGroup) {
                                        libWavefrontGroupUse(computerGroup) {
                                            callback.invoke()
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

private fun teapotUpdate() {
    teapotRotation += 0.01f
    teapotMatrix.identity()
        .translate(-10f, 2f, -10f)
        .rotate(radf(-90f), vec3().front())
        .rotate(teapotRotation, vec3().up())
}

private fun computerUpdate() {
    computerRotation += 0.01f
    computerMatrix.identity()
        .translate(-10f, 2f, -10f)
        .rotate(radf(-90f), vec3().front())
        .rotate(computerRotation, vec3().up())
        .scale(6f)
}

private fun mandalorianUpdate() {
    mandalorianRotation += 0.01f
    mandalorianMatrix.identity()
        .translate(-10f, 1.8f, -9f)
        .rotate(radf(-90f), vec3().front())
        .rotate(mandalorianRotation, vec3().up())
        .scale(0.15f)
}

private fun itemDraw(where: TechniqueRtt, mesh: GlMesh, matrix: mat4, albedo: GlTexture) {
    unifItemModel.value = matrix
    unifItemAlbedo.value = albedo
    glTechRttDraw(where) {
        glTextureBind(tvMaterial.albedo) {
            glTextureBind(albedo) {
                glTechPostProcessingDraw(mergingItem) {
                    glDepthTest {
                        glCulling {
                            glClear()
                            glShadingPhongDraw(shadingPhong, listOf(itemLight)) {
                                glShadingPhongInstance(shadingPhong, mesh)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------- Techniques --------------------------------

private val shadingPhong = ShadingPhong(unifItemModel, constItemView, constItemProj, constIteEye,
    unifItemAlbedoSampler, constItemMatAmbient, constItemMatDiffuse, constItemMatSpecular, constItemMatShine)

private val unifTvItemAlbedo = unifs(computerRtt.color)
private val unifTvItemAlbedoSampler = sampler(unifTvItemAlbedo)

private val shadingPbr = ShadingPbr(
    unifTvModel, unifTvView, constm4(camera.projectionM), unifTvEye,
    unifTvItemAlbedoSampler, unifTvNormal, unifTvMetallic, unifTvRoughness, unifTvAO)

// -------------------------------- Logo --------------------------------

//private val logoTechnique = ShadingFlat()

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
            itemUse {
                window.show {
                    lightsUpdate()
                    teapotUpdate()
                    computerUpdate()
                    mandalorianUpdate()
                    glClear(col3().black())
                    controller.apply { position, direction ->
                        camera.setPosition(position)
                        camera.lookAlong(direction)
                    }
                    itemDraw(teapotRtt, teapotObject.mesh, teapotMatrix, teapotTexture)
                    itemDraw(computerRtt, computerObject.mesh, computerMatrix, computerTexture)
                    itemDraw(mandalorianRtt, mandalorianObject.mesh, mandalorianMatrix, mandalorianTexture)
                    tvsDraw()
                }
            }
        }
    }
}