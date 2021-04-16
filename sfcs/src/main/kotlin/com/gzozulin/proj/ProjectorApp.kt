package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import com.gzozulin.minigl.api.glUse

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
// todo: capturing on/off

private const val FULL_WIDTH = 1920
private const val FULL_HEIGHT = 1080

private val window = GlWindow()
private val capturer = Capturer(window, FULL_WIDTH, FULL_HEIGHT)

private val projectorModel = ProjectorModel()
private val projectorView = ProjectorView(projectorModel, window.width, window.height)
private val projectorController = ProjectorController(projectorModel, projectorView)

fun main() {
    window.create(resizables = listOf(projectorView), isFullscreen = true, isHoldingCursor = true) {
        window.keyCallback = { key, pressed ->
            projectorController.keyPressed(key, pressed)
        }
        glUse(projectorView) {
            capturer.capture {
                window.show(
                    onBuffer = {
                        capturer.onBuffer()
                    },
                    onFrame = {
                        projectorController.onFrame()
                    })
            }
        }
    }
}