package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GlBuffer(val target: Int = backend.GL_ARRAY_BUFFER, val usage: Int = backend.GL_STATIC_DRAW,
                    internal val data: ByteBuffer, internal var handle: Int? = null)

internal fun glBufferUpload(buffer: GlBuffer) {
    check(buffer.handle == null) { "GlBuffer already in use!" }
    buffer.handle = backend.glGenBuffers()
    backend.glBindBuffer(buffer.target, buffer.handle!!)
    backend.glBufferData(buffer.target, buffer.data, buffer.usage)
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