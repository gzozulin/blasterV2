package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow(isFullscreen = true)
private val capturer = Capturer(window)

private val shadingFlat = ShadingFlat(
    color = raymarcher(namedTexCoordsV2(), timef(), aspectf(window)))

private val rect = glMeshCreateRect()

private fun rmUse(callback: Callback) {
    glShadingFlatUse(shadingFlat) {
        glMeshUse(rect) {
            callback.invoke()
        }
    }
}

fun main() = window.create {
    rmUse {
        //capturer.capture {
            window.show {
                glShadingFlatDraw(shadingFlat) {
                    glShadingFlatInstance(shadingFlat, rect)
                }
                //capturer.addFrame()
            }
        //}
    }
}