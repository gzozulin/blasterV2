package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GlBuffer(val target: Int = backend.GL_ARRAY_BUFFER, val usage: Int = backend.GL_STATIC_DRAW,
                    internal val data: ByteBuffer, internal var handle: Int? = null)

internal fun glBufferUse(buffer: GlBuffer, callback: Callback) {
    check(buffer.handle == null) { "GlBuffer already in use!" }
    buffer.handle = backend.glGenBuffers()
    glBufferBind(buffer) {
        backend.glBufferData(buffer.target, buffer.data, buffer.usage)
        callback.invoke()
    }
    backend.glDeleteBuffers(buffer.handle!!)
    buffer.handle = null
}

private val currBinding = mutableMapOf<Int, Int?>()
internal fun glBufferBind(buffer: GlBuffer, callback: Callback) {
    check(buffer.handle != null) { "GlBuffer is not used!" }
    val prev = currBinding[buffer.target]
    backend.glBindBuffer(buffer.target, buffer.handle!!)
    currBinding[buffer.target] = buffer.handle!!
    callback.invoke()
    backend.glBindBuffer(buffer.target, prev ?: 0)
    currBinding[buffer.target] = prev
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