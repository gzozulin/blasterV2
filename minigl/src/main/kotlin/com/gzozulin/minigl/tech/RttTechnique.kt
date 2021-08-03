package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api.GlFrameBuffer
import com.gzozulin.minigl.api.GlTexture
import com.gzozulin.minigl.api.glFrameBufferUse

private val outputs = listOf(backend.GL_COLOR_ATTACHMENT0)

data class TechniqueRtt(val width: Int, val height: Int, val internalFormat: Int = backend.GL_RGBA,
                        val minFilter: Int = backend.GL_NEAREST_MIPMAP_LINEAR, val magFilter: Int = backend.GL_LINEAR) {

    constructor(window: GlWindow, internalFormat: Int = backend.GL_RGBA,
                minFilter: Int = backend.GL_NEAREST_MIPMAP_LINEAR, magFilter: Int = backend.GL_LINEAR):
            this(window.width, window.height, internalFormat, minFilter, magFilter)

    internal val frameBuffer = GlFrameBuffer()

    val color = GlTexture(
        minFilter = minFilter,
        magFilter = magFilter,
        data = listOf(GlTextureImage(backend.GL_TEXTURE_2D, width, height, internalFormat = internalFormat)))
    val depth = GlRenderBuffer(width, height)
}

fun glRttUse(techniqueRtt: TechniqueRtt, callback: Callback) {
    glFrameBufferUse(techniqueRtt.frameBuffer) {
        glTextureUse(techniqueRtt.color) {
            glRenderBufferUse(techniqueRtt.depth) {
                callback.invoke()
            }
        }
    }
}

fun glRttDraw(techniqueRtt: TechniqueRtt, callback: Callback) {
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

private val unifTexture = sampler(unifs(techniqueRtt.color))
private val shadingFlat = ShadingFlat(constm4(mat4().orthoBox(1.1f)), unifTexture)

private val rect = glMeshCreateRect()

private val window = GlWindow()

fun main() {
    window.create {
        glRttUse(techniqueRtt) {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    window.show {
                        glClear(vec3().rose())
                        glRttDraw(techniqueRtt) {
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
