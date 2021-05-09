package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import java.nio.ByteBuffer

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

private val prevBinding = mutableMapOf<Int, Int?>()
internal fun glBindBuffer(buffer: GlBuffer, target: Int, callback: Callback) {
    check(buffer.handle != null) { "GlBuffer is not used!" }
    val prev = prevBinding[target]
    backend.glBindBuffer(target, buffer.handle!!)
    prevBinding[target] = buffer.handle!!
    callback.invoke()
    backend.glBindBuffer(target, prev ?: 0)
    prevBinding[target] = prev
}