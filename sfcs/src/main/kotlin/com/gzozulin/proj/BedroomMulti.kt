package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.assets.Object
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.scene.*
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique

private val window = GlWindow()

private val camera = Camera()
private val controller = Controller(position = vec3().front(), velocity = 1f)
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")

private val modelM = constm4(mat4().identity())
private val viewM = unifm4 { camera.calculateViewM() }
private val eye = unifv3 { camera.position }
private val projM = unifm4 { camera.projectionM }

private val texCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
private val sampler = unifsampler()
private val diffuseMap = tex(texCoords, sampler)
private val diffuseColor = constv4(vec4(1f))

private val matAmbient = unifv3()
private val matDiffuse = unifv3()
private val matSpecular = unifv3()
private val matShine = uniff()
private val matTransparency = uniff()

private val deferredTextureTechnique = DeferredTechnique(
    modelM, viewM, projM, eye, diffuseMap, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)

private val model = modelLib.load("models/bedroom/bedroom", join = false)
private val empty = texturesLib.loadTexture("textures/snow.png")

private val light = PointLight(model.aabb.center(), vec3(1f), 1000f)
private val lights = listOf(light)

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
        window.resizeCallback = { width: Int, height: Int ->
            deferredTextureTechnique.resize(width, height)
        }
        glUse(skyboxTechnique, deferredTextureTechnique, model, empty) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                light.position.set(camera.position)
                glMultiSample {
                    glDepthTest {
                        glBind(empty) {
                            deferredTextureTechnique.draw(lights) {
                                model.objects.forEach { obj ->
                                    val material = obj.phong()
                                    sampler.value = material.mapDiffuse ?: empty
                                    matAmbient.value = vec3(0f)
                                    matDiffuse.value = material.diffuse
                                    matSpecular.value = material.specular
                                    matShine.value = material.shine
                                    matTransparency.value = material.transparency
                                    deferredTextureTechnique.instance(obj)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
