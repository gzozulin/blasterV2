package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING

private val binding = IntArray(1)

data class GlFrameBuffer(internal val target: Int = backend.GL_FRAMEBUFFER, internal var handle: Int? = null)

private fun glFrameBufferBindPrev(frameBuffer: GlFrameBuffer, callback: Callback) {
    when (frameBuffer.target) {
        backend.GL_FRAMEBUFFER -> backend.glGetIntegerv(GL_FRAMEBUFFER_BINDING, binding)
        else -> error("Unknown GlFrameBuffer type!")
    }
    callback.invoke()
    backend.glBindFramebuffer(frameBuffer.target, binding[0])
}

internal fun glFrameBufferUse(frameBuffer: GlFrameBuffer, callback: Callback) {
    check(frameBuffer.handle == null) { "GlFrameBuffer is already in use!" }
    frameBuffer.handle = backend.glGenFramebuffers()
    callback.invoke()
    backend.glDeleteFramebuffers(frameBuffer.handle!!)
    frameBuffer.handle = null
}

internal fun glFrameBufferBind(frameBuffer: GlFrameBuffer, callback: Callback) {
    check(frameBuffer.handle != null) { "FrameBuffer is not used!" }
    glFrameBufferBindPrev(frameBuffer) {
        backend.glBindFramebuffer(frameBuffer.target, frameBuffer.handle!!)
        callback.invoke()
    }
}

internal fun glFrameBufferCheck(frameBuffer: GlFrameBuffer) {
    check(frameBuffer.handle != null) { "FrameBuffer is not used!" }
    when (frameBuffer.target) {
        backend.GL_FRAMEBUFFER -> backend.glGetIntegerv(GL_FRAMEBUFFER_BINDING, binding)
        else -> error("Unknown GlFrameBuffer type!")
    }
    check(binding[0] == frameBuffer.handle) { "GlFrameBuffer is not bound!" }
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