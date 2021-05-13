package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend

data class GlFrameBuffer(internal val target: Int = backend.GL_FRAMEBUFFER, internal var handle: Int? = null)

internal fun glFrameBufferUse(frameBuffer: GlFrameBuffer, callback: Callback) {
    check(frameBuffer.handle == null) { "GlFrameBuffer is already in use!" }
    frameBuffer.handle = backend.glGenFramebuffers()
    callback.invoke()
    backend.glDeleteFramebuffers(frameBuffer.handle!!)
    frameBuffer.handle = null
}

private val currBinding = mutableMapOf<Int, Int?>()
internal fun glFrameBufferBind(frameBuffer: GlFrameBuffer, callback: Callback) {
    check(frameBuffer.handle != null) { "FrameBuffer is not used!" }
    val prev = currBinding[frameBuffer.target]
    backend.glBindFramebuffer(frameBuffer.target, frameBuffer.handle!!)
    currBinding[frameBuffer.target] = frameBuffer.handle!!
    callback.invoke()
    backend.glBindFramebuffer(frameBuffer.target, prev ?: 0)
    currBinding[frameBuffer.target] = prev
}

internal fun glFrameBufferCheck(frameBuffer: GlFrameBuffer) {
    check(frameBuffer.handle != null) { "FrameBuffer is not used!" }
    check(currBinding[frameBuffer.target] != null) { "No GlFrameBuffer is bound!" }
}

internal fun glFrameBufferTexture(frameBuffer: GlFrameBuffer, attachment: Int, texture: GlTexture) {
    glFrameBufferCheck(frameBuffer)
    backend.glFramebufferTexture2D(backend.GL_FRAMEBUFFER, attachment, texture.target, texture.handle!!, 0)
}

/*internal fun glFrameBufferRenderBuffer(frameBuffer: GlFrameBuffer, callback: Callback) {
    glFrameBufferCheck(frameBuffer)
}*/

internal fun glFrameBufferOutputs(frameBuffer: GlFrameBuffer, outputs: List<Int>) {
    glFrameBufferCheck(frameBuffer)
    backend.glDrawBuffers(outputs.toIntArray())
}

internal fun glFrameBufferIsComplete(frameBuffer: GlFrameBuffer) {
    glFrameBufferCheck(frameBuffer)
    check(backend.glCheckFramebufferStatus(frameBuffer.target) == backend.GL_FRAMEBUFFER_COMPLETE)
        { "GlFrameBuffer is not complete!" }
}