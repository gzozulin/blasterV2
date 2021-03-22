package com.gzozulin.minigl.api

import java.nio.ByteBuffer
import java.nio.ByteOrder

class GlBuffer(
    private val target: Int,
    private val buffer: ByteBuffer,
    private val usage: Int = backend.GL_STATIC_DRAW) : GlBindable() {

    private var handle: Int = -1

    init {
        check(target == backend.GL_ARRAY_BUFFER || target == backend.GL_ELEMENT_ARRAY_BUFFER)
    }

    override fun use() {
        super.use()
        handle = backend.glGenBuffers()
        glBind(this) {
            backend.glBufferData(target, buffer, usage)
        }
    }

    override fun release() {
        backend.glDeleteBuffers(handle)
        super.release()
    }

    override fun bind() {
        super.bind()
        backend.glBindBuffer(target, handle)
    }

    override fun unbind() {
        backend.glBindBuffer(target, 0)
        super.unbind()
    }

    fun updateBuffer(access : Int = backend.GL_WRITE_ONLY, update: (mapped: ByteBuffer) -> Unit) {
        checkReady()
        val mapped = backend.glMapBuffer(target, access, buffer)
        update.invoke(mapped)
        backend.glUnapBuffer(target)
    }

    companion object {
        fun create(type: Int, floats: FloatArray): GlBuffer {
            val buffer = ByteBuffer.allocateDirect(floats.size * 4).order(ByteOrder.nativeOrder())
            buffer.asFloatBuffer().put(floats).position(0)
            return GlBuffer(type, buffer)
        }

        fun create(type: Int, ints: IntArray): GlBuffer {
            val buffer = ByteBuffer.allocateDirect(ints.size * 4).order(ByteOrder.nativeOrder())
            buffer.asIntBuffer().put(ints).position(0)
            return GlBuffer(type, buffer)
        }
    }
}