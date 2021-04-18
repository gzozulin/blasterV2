package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import com.gzozulin.minigl.api.glUse
import org.lwjgl.glfw.GLFW

// todo: better highlighting for code: assign color in visitor based on token, expression
// todo: do not add a newline if already exists
// todo: better on-page scrolling: center on function (stop - start)/2
// todo: highlight sections of code (same step, but multiple orders)
// todo: off-screen MSAA for code, just antialiasing for text before
// todo: code page positioning on the screen
// todo: multipage/multiclass projects
// todo: repository, assets, projects
// todo: fix texture for the bedroom scene
// todo: new thumbnail, new backgrounds
// todo: switching to fullscreen
// todo: scene arrangement: me completely in the frame
// todo: link to the code in video description

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