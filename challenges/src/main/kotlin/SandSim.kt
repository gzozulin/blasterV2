package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.*

private val window = GlWindow()

private var currentBuffer = 0
private val buffer0 = TechniqueRtt(window, internalFormat = backend.GL_RGBA32F)
private val buffer1 = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F)

private val physicsIn = unifs()
private val processPhysics = ShadingFlat(constm4(mat4().orthoBox()), sampler(physicsIn))

private val renderIn = unifs()
private val renderWorld = ShadingFlat(constm4(mat4().orthoBox()), sampler(renderIn))

private val rect = glMeshCreateRect()
private val startTexture = libTextureCreate("textures/font.png")

private fun sandUse(callback: Callback) {
    glRttUse(buffer0) {
        glRttUse(buffer1) {
            glShadingFlatUse(processPhysics) {
                glShadingFlatUse(renderWorld) {
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
    val from: TechniqueRtt
    val to: TechniqueRtt
    if (currentBuffer % 2 == 0) {
        from = buffer0
        to = buffer1
    } else {
        from = buffer1
        to = buffer0
    }
    sandPopulate(from.color, to)
    sandDraw(to.color)
    currentBuffer++
}

private fun sandPopulate(from: GlTexture, to: TechniqueRtt) {
    glRttDraw(to) {
        glTextureBind(from) {
            glShadingFlatDraw(processPhysics) {
                physicsIn.value = from
                glShadingFlatInstance(processPhysics, rect)
            }
        }
    }
}

private fun sandDraw(buffer: GlTexture) {
    glTextureBind(buffer) {
        glShadingFlatDraw(renderWorld) {
            renderIn.value = buffer
            glShadingFlatInstance(renderWorld, rect)
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