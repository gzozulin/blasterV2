package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow

private val window = GlWindow()
private val capturer = Capturer(window)

private val projectorModel = ProjectorModel()
private val projectorView = ProjectorView(projectorModel)
private val projectorController = ProjectorController(projectorModel, projectorView, capturer)

fun main() {
    window.create(
        isFullscreen = true, isHoldingCursor = false, isMultisampling = true) {
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
            window.copyWindowBuffer()
            capturer.frame()
        }
    }
}

private fun justFrame() {
    window.show {
        projectorController.onFrame()
    }
}