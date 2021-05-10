package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GlBuffer(internal val data: ByteBuffer, internal var handle: Int? = null)

internal fun glUseBuffer(buffer: GlBuffer, target: Int, usage: Int, callback: Callback) {
    check(buffer.handle == null) { "GlBuffer already in use!" }
    buffer.handle = backend.glGenBuffers()
    glBindBuffer(buffer, target) {
        backend.glBufferData(target, buffer.data, usage)
        callback.invoke()
    }
    backend.glDeleteBuffers(buffer.handle!!)
    buffer.handle = null
}

private val currBinding = mutableMapOf<Int, Int?>()
internal fun glBindBuffer(buffer: GlBuffer, target: Int, callback: Callback) {
    check(buffer.handle != null) { "GlBuffer is not used!" }
    val prev = currBinding[target]
    backend.glBindBuffer(target, buffer.handle!!)
    currBinding[target] = buffer.handle!!
    callback.invoke()
    backend.glBindBuffer(target, prev ?: 0)
    currBinding[target] = prev
}

fun glCreateBuffer(floats: FloatArray): GlBuffer {
    val data = ByteBuffer.allocateDirect(floats.size * 4).order(ByteOrder.nativeOrder())
    data.asFloatBuffer().put(floats).position(0)
    return GlBuffer(data)
}

fun glCreateBuffer(ints: IntArray): GlBuffer {
    val data = ByteBuffer.allocateDirect(ints.size * 4).order(ByteOrder.nativeOrder())
    data.asIntBuffer().put(ints).position(0)
    return GlBuffer(data)
}