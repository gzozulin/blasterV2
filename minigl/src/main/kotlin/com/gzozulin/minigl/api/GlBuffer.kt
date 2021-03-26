package com.gzozulin.minigl.api

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private val bindStack = Stack<Int>()

class GlBuffer(
    private val target: Int,
    private val buffer: ByteBuffer,
    private val usage: Int = backend.GL_STATIC_DRAW) : GlBindable() {

    private var handle: Int = -1

    init {
        check(target == backend.GL_ARRAY_BUFFER || target == backend.GL_ELEMENT_ARRAY_BUFFER)
    }

    override fun onUse() {
        handle = backend.glGenBuffers()
        glBind(this) {
            backend.glBufferData(target, buffer, usage)
        }
    }

    override fun onRelease() {
        backend.glDeleteBuffers(handle)
    }

    override fun onBound() {
        backend.glBindBuffer(target, handle)
        bindStack.push(handle)
    }

    override fun onUnbound() {
        bindStack.pop()
        if (bindStack.empty()) {
            backend.glBindBuffer(target, 0)
        } else {
            backend.glBindBuffer(target, bindStack.peek())
        }
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