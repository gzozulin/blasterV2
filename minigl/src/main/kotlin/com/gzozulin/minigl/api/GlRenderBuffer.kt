package com.gzozulin.minigl.api

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
    }

    override fun onUnbound() {
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0)
    }

    fun accessHandle(): Int {
        checkReady()
        return handle
    }
}