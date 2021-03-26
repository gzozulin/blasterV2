package com.gzozulin.minigl.api

import java.util.*

private val bindStack = Stack<Int>()

class GlFrameBuffer : GlBindable() {
    private var handle: Int = -1

    override fun onUse() {
        handle = backend.glGenFramebuffers()
    }

    override fun onRelease() {
        backend.glDeleteFramebuffers(handle)
    }

    override fun onBound() {
        backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, handle)
        bindStack.push(handle)
    }

    override fun onUnbound() {
        bindStack.pop()
        if (bindStack.empty()) {
            backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, 0)
        } else {
            backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, bindStack.peek())
        }
    }

    fun setTexture(attachement: Int, texture: GlTexture) {
        checkReady()
        backend.glFramebufferTexture2D(backend.GL_FRAMEBUFFER, attachement, texture.target, texture.accessHandle(), 0) // 0 - level
    }

    fun setRenderBuffer(attachement: Int, renderBuffer: GlRenderBuffer) {
        checkReady()
        backend.glFramebufferRenderbuffer(backend.GL_FRAMEBUFFER, attachement, backend.GL_RENDERBUFFER, renderBuffer.accessHandle())
    }

    fun setOutputs(outputs: IntArray) {
        checkReady()
        backend.glDrawBuffers(outputs)
    }

    fun checkIsComplete() {
        checkReady()
        check(backend.glCheckFramebufferStatus(backend.GL_FRAMEBUFFER) == backend.GL_FRAMEBUFFER_COMPLETE)
    }
}