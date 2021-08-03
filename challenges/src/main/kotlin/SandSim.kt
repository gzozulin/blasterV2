package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.tech.*

private val window = GlWindow(isFullscreen = true)
private val capturer = Capturer(window)
private val constWH = constv2i(window.width, window.height)

private var isSimulating = true

private var currentBuffer = 0
private val buffer0 = TechniqueRtt(window, internalFormat = backend.GL_RGBA32F, minFilter = backend.GL_LINEAR) // non-normalized, linear
private val buffer1 = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F, minFilter = backend.GL_LINEAR)// non-normalized, linear
private val deltas = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F, minFilter = backend.GL_LINEAR)// non-normalized, linear

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
        glTextureBind(startTexture) {
            glShadingFlatDraw(sandRender) {
                renderIn.value = startTexture
                glShadingFlatInstance(sandRender, rect)
            }
        }
    }
    glRttDraw(buffer1) {
        glClear(col3().black())
    }
    glRttDraw(deltas) {
        glClear(col3().black())
    }
}

private fun sandFrame() {
    val (buffIn, buffOut) = sandSelectBuffer()
    if (isSimulating) {
        sandPhysics(buffIn.color)
        sandSolve(buffIn, buffOut)
        currentBuffer++
    }
    sandDraw(buffOut.color)
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

private fun sandSolve(origin: TechniqueRtt, result: TechniqueRtt) {
    glRttDraw(result) {
        glTextureBind(origin.color) {
            glTextureBind(deltas.color) {
                glShadingFlatDraw(sandSolver) {
                    solverOrigin.value = origin.color
                    solverDeltas.value = deltas.color
                    glShadingFlatInstance(sandSolver, rect)
                }
            }
        }
    }
}

private fun sandDraw(result: GlTexture) {
    glTextureBind(result) {
        glShadingFlatDraw(sandRender) {
            renderIn.value = result
            glShadingFlatInstance(sandRender, rect)
        }
    }
}

fun main() {
    window.create {
        window.keyCallback = { key, pressed ->
            isSimulating = pressed
        }
        sandUse {
            sandPopulate()
            //capturer.capture {
                window.show {
                    sandFrame()
                    //capturer.addFrame()
                }
            //}
        }
    }
}