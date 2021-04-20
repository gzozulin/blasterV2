package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import com.gzozulin.minigl.api.glUse
import org.lwjgl.glfw.GLFW

// todo: off-screen MSAA for code, just antialiasing for text before
// todo: repository, assets, projects
// todo: fix texture for the bedroom scene
// todo: new thumbnail, new backgrounds
// todo: highlighting for fields and variables
// todo: highlight sections of code (same step, but multiple orders)
// todo: multipage/multiclass projects
// todo: switching to fullscreen
// todo: scene arrangement: me completely in the frame
// todo: link to the code in video description
// todo: support for C and GLSL
// todo: background music
// todo: crash main thread if parsing issue

private val window = GlWindow()
private val capturer = Capturer(window)

private val projectorModel = ProjectorModel()
private val projectorView = ProjectorView(projectorModel, window.width, window.height)
private val projectorController = ProjectorController(projectorModel, projectorView)

fun main() {
    window.create(resizables = listOf(projectorView), isFullscreen = true, isHoldingCursor = false) {
        window.keyCallback = { key, pressed ->
            if (key == GLFW.GLFW_KEY_R && pressed) {
                capturer.isCapturing = !capturer.isCapturing
            }
            projectorController.keyPressed(key, pressed)
        }
        glUse(projectorView) {
            //capturer.capture {
                window.show {
                    projectorController.onFrame()
                    //window.copyWindowBuffer()
                    //capturer.frame()
                }
            //}
        }
    }
}