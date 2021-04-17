package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import com.gzozulin.minigl.api.glUse
import org.lwjgl.glfw.GLFW

// todo: capture: codecs pipeline
// todo: code page positioning on the screen
// todo: multipage/multiclass projects
// todo: better highlighting for code
// todo: splines for camera animation
// todo: better on-page scrolling: center on function (stop - start)/2
// todo: off-screen MSAA for code
// todo: introduce offset to do not count manually
// todo: repository, assets, projects
// todo: highlight sections of code
// todo: new thumbnail, new backgrounds
// todo: switching to fullscreen

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
            capturer.capture {
                window.show {
                    projectorController.onFrame()
                    window.copyWindowBuffer()
                    capturer.frame()
                }
            }
        }
    }
}