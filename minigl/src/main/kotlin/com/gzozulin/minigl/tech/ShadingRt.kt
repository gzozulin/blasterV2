package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.Sphere
import java.lang.Thread.sleep
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val window = GlWindow()

private val matrix = constm4(mat4().orthoBox())
private val color = shadingRt(namedGlFragCoordV2(), namedTexCoordsV2())
private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val hitables = listOf(Sphere(vec3(0f, 0f, -1f), 0.5f), Sphere(vec3(0f, -100.5f, -1f), 100f))

fun main() {
    window.create {
        glViewportBindPrev {
            backend.glViewport(0, 0, 640, 480)
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    glShadingFlatDraw(shadingFlat) {
                        glProgramSubmitHitables(shadingFlat.program, hitables)
                        window.show {
                            val millis = measureTimeMillis {
                                glShadingFlatInstance(shadingFlat, rect)
                            }
                            println(String.format("Frame time: %f", millis / 1000f))
                            sleep(1000L)
                        }
                    }
                }
            }
        }
    }
}