package com.gzozulin.rt
/*

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.Sphere
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow()

private val matrix = constm4(mat4().orthoBox())
private val color = shadingRt(namedTexCoordsV2())
private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val hitables = listOf(Sphere(vec3().front(), 0.5f), Sphere(vec3(0f, -100.5f, -1f), 100.0f))

fun main() {
    window.create {
        glShadingFlatUse(shadingFlat) {
            glMeshUse(rect) {
                glShadingFlatDraw(shadingFlat) {
                    //glProgramSubmitHitables(shadingFlat)
                    window.show {
                        glShadingFlatInstance(shadingFlat, rect)
                    }
                }
            }
        }
    }
}*/
