package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.Sphere

private val window = GlWindow()

private val matrix = constm4(mat4().orthoBox())
private val color = shadingRt(namedTexCoordsV2())
private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val hitables = listOf(Sphere(vec3().back(), 0.5f), Sphere(vec3(0f, -100.5f, -1f), 100.0f))

fun main() {
    window.create {
        glShadingFlatUse(shadingFlat) {
            glMeshUse(rect) {
                glShadingFlatDraw(shadingFlat) {
                    glProgramSubmitHitables(shadingFlat.program, hitables)
                    window.show {
                        glShadingFlatInstance(shadingFlat, rect)
                    }
                }
            }
        }
    }
}