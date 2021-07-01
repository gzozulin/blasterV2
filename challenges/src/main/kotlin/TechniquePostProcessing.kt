package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.assets.libTextureCreatePbr
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.PointLight
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.tech.*

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

private val foggyTexture = libTextureCreate("textures/foggy.jpg")
private val foggyUniform = sampler(unifs { foggyTexture })

private fun filter(screen: GlTexture) = mulv4(sampler(unifs(screen)), foggyUniform)

private val techniquePostProcessing = TechniquePostProcessing(window, ::filter)

private fun useScene(callback: Callback) {
    glShadingPbrUse(shadingPbr) {
        glPostProcessingUse(techniquePostProcessing) {
            glMeshUse(obj.mesh) {
                glTextureUse(listOf(material.albedo, material.normal, material.metallic,
                    material.roughness, material.ao, foggyTexture)) {
                    callback.invoke()
                }
            }
        }
    }
}

private fun drawScene() {
    glCulling {
        glDepthTest {
            glClear(col3().red())
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
        useScene {
            window.show {
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                glTextureBind(foggyTexture) {
                    glPostProcessingDraw(techniquePostProcessing) {
                        drawScene()
                    }
                }
            }
        }
    }
}