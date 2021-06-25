package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import com.gzozulin.minigl.capture.Capturer
import java.io.File

private val scenario = File("/home/greg/Dropbox/episodes/ep4-raytracer/scenario")

private val window = GlWindow(isFullscreen = true, isHoldingCursor = false, isMultisampling = true)
private val capturer = Capturer(window, "code")

private val projectorModel = ProjectorModel(scenario)
private val projectorView = ProjectorView(projectorModel)
private val projectorController = ProjectorController(projectorModel, projectorView, capturer)

fun main() {
    window.create {
        window.keyCallback = { key, pressed ->
            projectorController.keyPressed(key, pressed)
        }
        projectorView.use {
            frameWithCapture()
            //justFrame()
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