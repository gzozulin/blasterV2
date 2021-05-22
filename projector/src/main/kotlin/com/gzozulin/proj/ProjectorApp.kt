package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)
private val capturer = Capturer(window)

private val projectorModel = ProjectorModel()
private val projectorView = ProjectorView(projectorModel)
private val projectorController = ProjectorController(projectorModel, projectorView, capturer)

fun main() {
    window.create {
        window.keyCallback = { key, pressed ->
            projectorController.keyPressed(key, pressed)
        }
        projectorView.use {
            justFrame()
        }
    }
}

private fun frameWithCapture() {
    capturer.capture {
        window.show {
            projectorController.onFrame()
            capturer.addFrame()
        }
    }
}

private fun justFrame() {
    window.show {
        projectorController.onFrame()
    }
}