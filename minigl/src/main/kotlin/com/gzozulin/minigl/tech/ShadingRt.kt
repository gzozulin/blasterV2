package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.Sphere

private val window = GlWindow()

private val matrix = constm4(mat4().orthoBox())
private val color = fragmentColorRt(namedTexCoordsV2())
private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val hitables = listOf(Sphere(vec3(0f, 0f, -1f), 0.5f), Sphere(vec3(0f, -100.5f, -1f), 100f))

fun main() {
    window.create {
        glViewportBindPrev {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    glShadingFlatDraw(shadingFlat) {
                        glProgramSubmitHitables(shadingFlat.program, hitables)
                        var buffers = 0
                        window.show {
                            if (buffers < 2) {
                                glShadingFlatInstance(shadingFlat, rect)
                                println("Buffer: $buffers")
                                buffers++
                            }
                        }
                    }
                }
            }
        }
    }
}