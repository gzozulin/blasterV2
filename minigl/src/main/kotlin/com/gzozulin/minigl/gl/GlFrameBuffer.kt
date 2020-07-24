package com.gzozulin.minigl.gl

class GlFrameBuffer : GlBindable() {
    private var handle: Int = -1

    override fun use() {
        super.use()
        handle = backend.glGenFramebuffers()
    }

    override fun release() {
        backend.glDeleteFramebuffers(handle)
        super.release()
    }

    override fun bind() {
        super.bind()
        backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, handle)
    }

    override fun unbind() {
        backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, 0)
        super.unbind()
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