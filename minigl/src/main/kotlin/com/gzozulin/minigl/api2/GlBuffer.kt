package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER_BINDING
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val binding = IntArray(1)

data class GlBuffer(val target: Int = backend.GL_ARRAY_BUFFER, val usage: Int = backend.GL_STATIC_DRAW,
                    internal val data: ByteBuffer, internal var handle: Int? = null)

private fun glBufferBindPrev(buffer: GlBuffer, callback: Callback) {
    when (buffer.target) {
        backend.GL_ARRAY_BUFFER -> backend.glGetIntegerv(GL_ARRAY_BUFFER_BINDING, binding)
        backend.GL_ELEMENT_ARRAY_BUFFER -> backend.glGetIntegerv(GL_ELEMENT_ARRAY_BUFFER_BINDING, binding)
    }
    callback.invoke()
    backend.glBindBuffer(buffer.target, binding[0])
}

internal fun glBufferUpload(buffer: GlBuffer) {
    check(buffer.handle == null) { "GlBuffer already in use!" }
    buffer.handle = backend.glGenBuffers()
    glBufferBindPrev(buffer) {
        backend.glBindBuffer(buffer.target, buffer.handle!!)
        backend.glBufferData(buffer.target, buffer.data, buffer.usage)
    }
}

internal fun glBufferDelete(buffer: GlBuffer) {
    check(buffer.handle != null) { "GlBuffer is not in use!" }
    backend.glDeleteBuffers(buffer.handle!!)
    buffer.handle = null
}

fun glBufferCreate(target: Int, usage: Int, floats: FloatArray): GlBuffer {
    val data = ByteBuffer.allocateDirect(floats.size * 4).order(ByteOrder.nativeOrder())
    data.asFloatBuffer().put(floats).position(0)
    return GlBuffer(target, usage, data)
}

fun glBufferCreate(target: Int, usage: Int, ints: IntArray): GlBuffer {
    val data = ByteBuffer.allocateDirect(ints.size * 4).order(ByteOrder.nativeOrder())
    data.asIntBuffer().put(ints).position(0)
    return GlBuffer(target, usage, data)
}