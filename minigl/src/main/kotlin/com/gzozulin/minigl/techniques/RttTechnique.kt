package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api.GlFrameBuffer
import com.gzozulin.minigl.api.GlTexture
import com.gzozulin.minigl.api.glFrameBufferUse

private val outputs = listOf(backend.GL_COLOR_ATTACHMENT0)

data class TechniqueRtt(val width: Int, val height: Int) {
    constructor(window: GlWindow): this(window.width, window.height)

    internal val frameBuffer = GlFrameBuffer()

    val color = GlTexture(images = listOf(GlTextureImage(backend.GL_TEXTURE_2D, width, height)))
    val depth = GlRenderBuffer(width, height)
}

fun glTechRttUse(techniqueRtt: TechniqueRtt, callback: Callback) {
    glFrameBufferUse(techniqueRtt.frameBuffer) {
        glTextureUse(techniqueRtt.color) {
            glRenderBufferUse(techniqueRtt.depth) {
                callback.invoke()
            }
        }
    }
}

fun glTechRttDraw(techniqueRtt: TechniqueRtt, callback: Callback) {
    glFrameBufferBind(techniqueRtt.frameBuffer) {
        glTextureBind(techniqueRtt.color) {
            glRenderBufferBind(techniqueRtt.depth) {
                glViewportBindPrev {
                    backend.glViewport(0, 0, techniqueRtt.width, techniqueRtt.height)
                    glFrameBufferAttachment(techniqueRtt.frameBuffer, backend.GL_COLOR_ATTACHMENT0, techniqueRtt.color)
                    glFrameBufferAttachment(techniqueRtt.frameBuffer, backend.GL_DEPTH_ATTACHMENT, techniqueRtt.depth)
                    glFrameBufferOutputs(techniqueRtt.frameBuffer, outputs)
                    glFrameBufferIsComplete(techniqueRtt.frameBuffer)
                    backend.glGenerateMipmap(techniqueRtt.color.target)
                    callback.invoke()
                }
            }
        }
    }
}

private val techniqueRtt = TechniqueRtt(10, 10)

private val unifTexture = sampler(unift(techniqueRtt.color))
private val shadingFlat = ShadingFlat(constm4(mat4().orthoBox(1.1f)), unifTexture)

private val rect = glMeshCreateRect()

private val window = GlWindow()

fun main() {
    window.create {
        glTechRttUse(techniqueRtt) {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    window.show {
                        glClear(vec3().rose())
                        glTechRttDraw(techniqueRtt) {
                            glClear(vec3().chartreuse())
                        }
                        glShadingFlatDraw(shadingFlat) {
                            glTextureBind(techniqueRtt.color) {
                                glShadingFlatInstance(shadingFlat, rect)
                            }
                        }
                    }
                }
            }
        }
    }
}
