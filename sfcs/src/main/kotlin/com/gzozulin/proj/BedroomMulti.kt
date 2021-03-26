package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique

private val window = GlWindow()

private val camera = Camera()
private val controller = Controller(position = vec3().front(), velocity = 1f)
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")

private val texCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
private val unifSampler = unifsampler()
private val unifColor = unifv4()

private val identityM = constm4(mat4().identity())
private val unifViewM = unifm4(camera.calculateViewM())
private val projM = constm4(camera.projectionM)

private val flatTexTechnique = FlatTechnique(identityM, unifViewM, projM, tex(texCoords, unifSampler))
private val flatColorTechnique = FlatTechnique(identityM, unifViewM, projM, unifColor)

private val model = modelLib.load("models/bedroom/bedroom", join = false)
private val colorOnly = model.objects.filter { it.phong().mapDiffuse == null }
private val texOnly = model.objects.filter { it.phong().mapDiffuse != null }

private var mouseLook = false

fun main() {
    window.create(isFullscreen = true, isHoldingCursor = false, isMultisampling = true) {
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
        glUse(skyboxTechnique, model, flatTexTechnique, flatColorTechnique) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                unifViewM.value = camera.calculateViewM()
                glMultiSample {
                    glDepthTest {
                        flatColorTechnique.draw {
                            colorOnly.forEach {
                                unifColor.value = vec4(it.phong().diffuse, 1f)
                                flatColorTechnique.instance(it)
                            }
                        }
                        flatTexTechnique.draw {
                            texOnly.forEach {
                                unifSampler.value = it.phong().mapDiffuse!!
                                flatTexTechnique.instance(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
