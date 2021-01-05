package com.gzozulin.minigl.gl

import sun.rmi.server.InactiveGroupException
import java.nio.ByteBuffer

data class GlTexData(
    val internalFormat: Int = backend.GL_RGBA,
    val pixelFormat: Int = backend.GL_RGBA,
    val pixelType: Int = backend.GL_UNSIGNED_BYTE,
    val width: Int, val height: Int,
    val pixels: ByteBuffer?)

class GlTexture(
    val target: Int,
    var unit: Int = 0,
    private val texData: List<GlTexData>
) : GlBindable() {

    private var internalHandle: Int = -1
    val handle: Int
    get() {
        checkReady()
        return internalHandle
    }

    constructor(
        target: Int = backend.GL_TEXTURE_2D, unit: Int = 0,
        internalFormat: Int = backend.GL_RGBA, pixelFormat: Int = backend.GL_RGBA, pixelType: Int = backend.GL_UNSIGNED_BYTE,
        width: Int, height: Int, pixels: ByteBuffer? = null
    ) : this(target, unit, listOf(GlTexData(internalFormat, pixelFormat, pixelType, width, height, pixels)))

    override fun use() {
        super.use()
        internalHandle = backend.glGenTextures()
        glBind(this) {
            backend.glTexParameteri(target, backend.GL_TEXTURE_MIN_FILTER, backend.GL_NEAREST)
            backend.glTexParameteri(target, backend.GL_TEXTURE_MAG_FILTER, backend.GL_NEAREST)
            when (target) {
                backend.GL_TEXTURE_2D -> useTexture2D()
                backend.GL_TEXTURE_CUBE_MAP -> useTextureCubeMap()
                else -> TODO()
            }
        }
    }

    override fun release() {
        backend.glDeleteTextures(handle)
        super.release()
    }

    override fun bind() {
        super.bind()
        backend.glActiveTexture(backend.GL_TEXTURE0 + unit)
        backend.glBindTexture(target, handle)
    }

    override fun unbind() {
        backend.glActiveTexture(backend.GL_TEXTURE0 + unit)
        backend.glBindTexture(target, 0)
        super.unbind()
    }

    private fun useTexture2D() {
        check(texData.size == 1)
        val data = texData.first()
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_S, backend.GL_REPEAT)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_T, backend.GL_REPEAT)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_R, backend.GL_REPEAT)
        backend.glTexImage2D(target, 0, data.internalFormat,
            data.width, data.height, 0, data.pixelFormat, data.pixelType, data.pixels)
    }

    private fun useTextureCubeMap() {
        check(texData.size == 6)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_S, backend.GL_CLAMP_TO_EDGE)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_T, backend.GL_CLAMP_TO_EDGE)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_R, backend.GL_CLAMP_TO_EDGE)
        texData.forEachIndexed { index, side ->
            backend.glTexImage2D(
                backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + index, 0,
                side.internalFormat, side.width, side.height, 0,
                side.pixelFormat, side.pixelType, side.pixels
            )
        }
    }
}