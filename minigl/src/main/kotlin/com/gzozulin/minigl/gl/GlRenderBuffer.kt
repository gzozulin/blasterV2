package com.gzozulin.minigl.gl

class GlRenderBuffer(
    private val component: Int = backend.GL_DEPTH_COMPONENT24,
    private val width: Int, private val height: Int) : GlBindable() {

    private var handle: Int = -1

    override fun use() {
        super.use()
        handle = backend.glGenRenderbuffers()
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle)
        backend.glRenderbufferStorage(backend.GL_RENDERBUFFER, component, width, height)
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0)
    }

    override fun release() {
        backend.glDeleteRenderBuffers(handle)
        super.release()
    }

    override fun bind() {
        super.bind()
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle)
    }

    override fun unbind() {
        backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0)
        super.unbind()
    }

    fun accessHandle(): Int {
        checkReady()
        return handle
    }
}