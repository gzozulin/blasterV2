package com.gzozulin.minigl.api

import java.util.*

private val bindStack = Stack<Int>()

class GlRenderBuffer(
    private val component: Int = backend.GL_DEPTH_COMPONENT24,
    private val width: Int, private val height: Int) : GlBindable() {

    private var handle: Int = -1

    override fun onUse() {
        handle = backend.glGenRenderbuffers()
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle)
        backend.glRenderbufferStorage(backend.GL_RENDERBUFFER, component, width, height)
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0)
    }

    override fun onRelease() {
        backend.glDeleteRenderBuffers(handle)
    }

    override fun onBound() {
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle)
        bindStack.push(handle)
    }

    override fun onUnbound() {
        bindStack.pop()
        if (bindStack.empty()) {
            backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0)
        } else {
            backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, bindStack.peek())
        }
    }

    fun accessHandle(): Int {
        checkReady()
        return handle
    }
}