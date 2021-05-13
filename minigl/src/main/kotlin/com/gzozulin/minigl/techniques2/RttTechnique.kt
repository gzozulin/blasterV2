package com.gzozulin.minigl.techniques2

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api2.*
import com.gzozulin.minigl.api2.GlFrameBuffer
import com.gzozulin.minigl.api2.GlTexture
import com.gzozulin.minigl.api2.glFrameBufferUse

private val prevViewport = IntArray(4)
private val outputs = listOf(backend.GL_COLOR_ATTACHMENT0)

data class TechniqueRtt(val width: Int, val height: Int) {
    internal val frameBuffer = GlFrameBuffer()
    val output = GlTexture(images = listOf(GlTextureImage(backend.GL_TEXTURE_2D, width, height)))
}

fun glTechRttUse(techniqueRtt: TechniqueRtt, callback: Callback) {
    glFrameBufferUse(techniqueRtt.frameBuffer) {
        glTextureUse(techniqueRtt.output) {
            callback.invoke()
        }
    }
}

fun glTechRttDraw(techniqueRtt: TechniqueRtt, callback: Callback) {
    backend.glGetIntegerv(backend.GL_VIEWPORT, prevViewport)
    glFrameBufferBind(techniqueRtt.frameBuffer) {
        glTextureBind(techniqueRtt.output) {
            backend.glViewport(0, 0, techniqueRtt.width, techniqueRtt.height)
            glFrameBufferTexture(techniqueRtt.frameBuffer, backend.GL_COLOR_ATTACHMENT0, techniqueRtt.output)
            glFrameBufferOutputs(techniqueRtt.frameBuffer, outputs)
            glFrameBufferIsComplete(techniqueRtt.frameBuffer)
            callback.invoke()
        }
    }
    backend.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3])
}

private val techniqueRtt = TechniqueRtt(10, 10)

private val unifTexture = tex(namedTexCoords(), unift(techniqueRtt.output))
private val shadingFlat = ShadingFlat(constm4(mat4().orthoBox(3f)), unifTexture)

private val rect = glMeshCreateRect()

private val window = GlWindow()

fun main() {
    window.create {
        glTechRttUse(techniqueRtt) {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    window.show {
                        glTechRttDraw(techniqueRtt) {
                            glClear(vec3().chartreuse())
                        }
                        glShadingFlatDraw(shadingFlat) {
                            glTextureBind(techniqueRtt.output) {
                                glShadingFlatInstance(shadingFlat, rect)
                            }
                        }
                    }
                }
            }
        }
    }
}
