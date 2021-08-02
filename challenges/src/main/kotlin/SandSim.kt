package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.*

private val window = GlWindow()

private var currentBuffer = 0
private val buffer0 = TechniqueRtt(window, internalFormat = backend.GL_RGBA32F)
private val buffer1 = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F)

private val physicsIn = unifs()
private val sandPhysics = ShadingFlat(constm4(mat4().orthoBox()), sampler(physicsIn))

private val solveOrigin = unifs()
private val solveDeltas = unifs()
private val sandSolve = ShadingFlat(constm4(mat4().orthoBox()))

private val renderIn = unifs()
private val sandRender = ShadingFlat(constm4(mat4().orthoBox()), sampler(renderIn))

private val rect = glMeshCreateRect()
private val startTexture = libTextureCreate("textures/font.png")

private fun sandUse(callback: Callback) {
    glRttUse(buffer0) {
        glRttUse(buffer1) {
            glShadingFlatUse(sandPhysics) {
                glShadingFlatUse(sandRender) {
                    glMeshUse(rect) {
                        glTextureUse(startTexture) {
                            callback.invoke()
                        }
                    }
                }
            }
        }
    }
}

private fun sandStart() {
    glRttDraw(buffer0) {
        glClear(col3().black())
    }
    glRttDraw(buffer1) {
        glClear(col3().black())
    }
    sandPopulate(startTexture, buffer0)
}

private fun sandFrame() {
    val buffIn: TechniqueRtt
    val buffOut: TechniqueRtt
    if (currentBuffer % 2 == 0) {
        buffIn = buffer0
        buffOut = buffer1
    } else {
        buffIn = buffer1
        buffOut = buffer0
    }
    sandPopulate(buffIn.color, buffOut)
    sandDraw(buffOut.color)
    currentBuffer++
}

private fun sandPopulate(from: GlTexture, to: TechniqueRtt) {
    glRttDraw(to) {
        glTextureBind(from) {
            glShadingFlatDraw(sandPhysics) {
                physicsIn.value = from
                glShadingFlatInstance(sandPhysics, rect)
            }
        }
    }
}

private fun sandDraw(buffer: GlTexture) {
    glTextureBind(buffer) {
        glShadingFlatDraw(sandRender) {
            renderIn.value = buffer
            glShadingFlatInstance(sandRender, rect)
        }
    }
}

fun main() {
    window.create {
        sandUse {
            sandStart()
            window.show {
                sandFrame()
            }
        }
    }
}