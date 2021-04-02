package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.lwjgl.glfw.GLFW
import java.util.concurrent.TimeUnit

private const val MILLIS_PER_FRAME = 16

class CrossFadeTechnique(private val color: col3 = col3().back(),
                         private val timeout: Long = TimeUnit.SECONDS.toMillis(1)) : GlResource() {

    private val colorUnif = unifv4(vec4())
    private val constProjM = constm4(mat4().ortho(-1f, 1f, -1f, 1f, 1f, -1f))
    private val fadeTechnique = FlatTechnique(constm4(mat4().identity()), constm4(mat4().identity()), constProjM, colorUnif)

    private val rect = GlMesh.rect()

    private var current = 0L
    private var isFadeOut = false

    init {
        addChildren(fadeTechnique, rect)
    }

    fun fadeIn() {
        current = timeout
        isFadeOut = false
    }

    fun fadeOut() {
        current = timeout
        isFadeOut = true
    }

    fun switch() {
        if (isFadeOut) {
            fadeIn()
        } else {
            fadeOut()
        }
    }

    fun draw() {
        if (current > 0L) {
            current -= MILLIS_PER_FRAME
            val progress = powf(1f - current.toFloat() / timeout.toFloat(), 3f)
            println(progress)
            val alpha = if (isFadeOut) progress else 1f - progress
            val color = vec4(color, alpha)
            colorUnif.value = color
            glBlend {
                fadeTechnique.draw {
                    fadeTechnique.instance(rect)
                }
            }
        } else if (isFadeOut) {
            glClear(color)
        }
    }
}

private val window = GlWindow()
private var mouseLook = false

private val camera = Camera()
private val controller = ControllerFirstPerson(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val obj = modelLib.load("models/pcjr/pcjr").first()

private val unifViewM = unifm4()
private val unifSampler = unifsampler()
private val attribTexCoord = varying<vec2>(SimpleVarrying.vTexCoord.name)
private val simpleTechnique = FlatTechnique(
    constm4(mat4().identity()), unifViewM, constm4(camera.projectionM), tex(attribTexCoord, unifSampler))
private val skyboxTechnique = StaticSkyboxTechnique("textures/hills")
private val crossFadeTechnique = CrossFadeTechnique(color = col3().orange())

fun main() {
    window.create(isHoldingCursor = false) {
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
            if (key == GLFW.GLFW_KEY_SPACE && !pressed) {
                crossFadeTechnique.switch()
            }
            wasdInput.onKeyPressed(key, pressed)
        }
        glUse(simpleTechnique, skyboxTechnique, crossFadeTechnique, obj) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glDepthTest {
                    unifSampler.value = obj.phong().mapDiffuse!!
                    unifViewM.value = camera.calculateViewM()
                    simpleTechnique.draw {
                        simpleTechnique.instance(obj)
                    }
                }
                crossFadeTechnique.draw()
            }
        }
    }
}