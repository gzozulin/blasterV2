package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.*

private val window = GlWindow()
private val constWH = constv2i(window.width, window.height)

private var currentBuffer = 0
private val buffer0 = TechniqueRtt(window, internalFormat = backend.GL_RGBA32F)
private val buffer1 = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F)
private val deltas = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F)

private val physicsIn = unifs()
private val sandPhysics = ShadingFlat(constm4(mat4().orthoBox()),
    sandPhysics(physicsIn, namedTexCoordsV2(), constWH))

private val solverOrigin = unifs()
private val solverDeltas = unifs()
private val sandSolver = ShadingFlat(constm4(mat4().orthoBox()),
    sandSolver(solverOrigin, solverDeltas, namedTexCoordsV2(), constWH))

private val renderIn = unifs()
private val sandRender = ShadingFlat(constm4(mat4().orthoBox()), sampler(renderIn))

private val rect = glMeshCreateRect()
private val startTexture = libTextureCreate("textures/font.png")

private fun sandUse(callback: Callback) {
    glShadingFlatUse(sandPhysics) {
        glShadingFlatUse(sandRender) {
            glShadingFlatUse(sandSolver) {
                glRttUse(buffer0) {
                    glRttUse(buffer1) {
                        glRttUse(deltas) {
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
    }
}

private fun sandPopulate() {
    glRttDraw(buffer0) {
        glClear(col3().black())
    }
    glRttDraw(buffer1) {
        glClear(col3().black())
    }
    glRttDraw(deltas) {
        glClear(col3().black())
    }
    sandPhysics(startTexture)
}

private fun sandFrame() {
    val (buffIn, buffOut) = sandSelectBuffer()
    sandPhysics(buffIn.color)
    sandSolve(buffIn, buffOut)
    sandDraw(buffOut.color)
    currentBuffer++
}

private fun sandSelectBuffer(): Pair<TechniqueRtt, TechniqueRtt> {
    val buffIn: TechniqueRtt
    val buffOut: TechniqueRtt
    if (currentBuffer % 2 == 0) {
        buffIn = buffer0
        buffOut = buffer1
    } else {
        buffIn = buffer1
        buffOut = buffer0
    }
    return buffIn to buffOut
}

private fun sandPhysics(from: GlTexture) {
    glRttDraw(deltas) {
        glTextureBind(from) {
            glShadingFlatDraw(sandPhysics) {
                physicsIn.value = from
                glShadingFlatInstance(sandPhysics, rect)
            }
        }
    }
}

private fun sandSolve(buffIn: TechniqueRtt, buffOut: TechniqueRtt) {
    glRttDraw(buffOut) {
        glTextureBind(buffIn.color) {
            glTextureBind(deltas.color) {
                glShadingFlatDraw(sandSolver) {
                    solverOrigin.value = buffIn.color
                    solverDeltas.value = deltas.color
                    glShadingFlatInstance(sandSolver, rect)
                }
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
            sandPopulate()
            window.show {
                sandFrame()
            }
        }
    }
}