package com.gzozulin.rt

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow()

private val matrix = constm4(mat4().orthoBox())
private val color = shadingRt(namedTexCoordsV2())
private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

fun main() {
    window.create {
        glShadingFlatUse(shadingFlat) {
            glMeshUse(rect) {
                window.show {
                    glShadingFlatDraw(shadingFlat) {
                        glShadingFlatInstance(shadingFlat, rect)
                    }
                }
            }
        }
    }
}