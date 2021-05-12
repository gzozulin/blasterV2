package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.PointLight
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.*
import org.lwjgl.glfw.GLFW

private val window = GlWindow()

private val model = modelLib.load("models/bedroom/bedroom", join = false)
private val empty = texturesLib.loadTexture("textures/snow.png")

private val camera = Camera()
private val controller = ControllerFirstPerson(position = model.aabb.center(), velocity = 1f)
private val wasdInput = WasdInput(controller)

private val cameraLight = PointLight(model.aabb.center(), vec3(1f), 350f)
private val lights = listOf(
    cameraLight,
    PointLight(vec3(2.732e1f, 150.3f, 5.401e1f),    vec3().white(),  800f),
    PointLight(vec3(178.7f,   91.33f,  -107.6f),    vec3().yellow(), 500f),
    PointLight(vec3(-113.8f,  104.5f,  -91.5f),     vec3().azure(),  1000f),
    PointLight(vec3(28.2f,    112.2f,   193.7f),    vec3().cyan(),   300f),
)

private val modelM = constm4(mat4().identity())
private val viewM = unifm4 { camera.calculateViewM() }
private val eye = unifv3 { camera.position }
private val projM = unifm4 { camera.projectionM }

private val texCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
private val sampler = unifsampler()
private val diffuseMap = tex(texCoords, sampler)

private val matAmbient = unifv3()
private val matDiffuse = unifv3()
private val matSpecular = unifv3()
private val matShine = uniff()
private val matTransparency = uniff()

private val forwardTechnique = ForwardTechnique(
    modelM, viewM, projM, eye, diffuseMap, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)

private var mouseLook = false

fun main() {
    window.create(resizables = listOf(camera),
        isFullscreen = true, isHoldingCursor = false, isMultisampling = true) {
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
            if (pressed && key == GLFW.GLFW_KEY_SPACE) {
                println("Pos: ${controller.position} dir: ${controller.direction}")
            }
        }
        glUse(forwardTechnique, model, empty) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                cameraLight.position.set(camera.position)
                glDepthTest {
                    glBind(empty) {
                        forwardTechnique.draw(lights) {
                            model.objects.forEach { obj ->
                                val material = obj.phong()
                                sampler.value = material.mapDiffuse ?: empty
                                matAmbient.value = vec3(0f)
                                matDiffuse.value = material.diffuse
                                matSpecular.value = material.specular
                                matShine.value = 1f
                                matTransparency.value = material.transparency
                                forwardTechnique.instance(obj)
                            }
                        }
                    }
                }
            }
        }
    }
}
