package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow(isFullscreen = true)
private val capturer = Capturer(window)

private var mouseLook = false
private val controller = ControllerFirstPerson(
    position = vec3(0f, 1f, 0f),
    velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val unifPos = unifv3()
private val unifCenter = unifv3()

private val shadingFlat = ShadingFlat(
    color = raymarcher(unifPos, unifCenter, namedTexCoordsV2(), fovyf(), aspectf(window), whf(window)))

private val rect = glMeshCreateRect()

private fun rmUse(callback: Callback) {
    glShadingFlatUse(shadingFlat) {
        glMeshUse(rect) {
            callback.invoke()
        }
    }
}

fun main() = window.create {
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

    rmUse {
        //capturer.capture {
            window.show {
                controller.apply { position, direction ->
                    unifPos.value = position
                    unifCenter.value = vec3(position).add(direction)
                }
                glShadingFlatDraw(shadingFlat) {
                    glShadingFlatInstance(shadingFlat, rect)
                }
                //capturer.addFrame()
            }
        //}
    }
}