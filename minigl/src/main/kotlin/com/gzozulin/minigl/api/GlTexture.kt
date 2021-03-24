package com.gzozulin.minigl.api

import java.nio.ByteBuffer

// At least 80, see https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glActiveTexture.xhtml
private const val MAX_ACTIVE_TEXTURES = 80
private val availableActiveTextures = (0 until MAX_ACTIVE_TEXTURES).toMutableList()

private fun holdTextureUnit(): Int {
    synchronized(availableActiveTextures) {
        check(availableActiveTextures.isNotEmpty())
        return availableActiveTextures.removeFirst()
    }
}

private fun releaseTextureUnit(unit: Int) {
    synchronized(availableActiveTextures) {
        availableActiveTextures.add(unit)
    }
}

data class GlTexData(
    val internalFormat: Int = backend.GL_RGBA,
    val pixelFormat: Int = backend.GL_RGBA,
    val pixelType: Int = backend.GL_UNSIGNED_BYTE,
    val width: Int, val height: Int,
    val pixels: ByteBuffer?)

class GlTexture(
    val target: Int,
    private val texData: List<GlTexData>
) : GlBindable() {

    private var handle: Int = -1
    private var unit: Int = -1

    constructor(
        target: Int = backend.GL_TEXTURE_2D,
        internalFormat: Int = backend.GL_RGBA, pixelFormat: Int = backend.GL_RGBA, pixelType: Int = backend.GL_UNSIGNED_BYTE,
        width: Int, height: Int, pixels: ByteBuffer? = null
    ) : this(target, listOf(GlTexData(internalFormat, pixelFormat, pixelType, width, height, pixels)))

    override fun use() {
        super.use()
        handle = backend.glGenTextures()
        unit = holdTextureUnit()
        glBind(this) {
            when (target) {
                backend.GL_TEXTURE_2D -> useTexture2D()
                backend.GL_TEXTURE_CUBE_MAP -> useTextureCubeMap()
                else -> TODO()
            }
        }
    }

    override fun release() {
        backend.glDeleteTextures(handle)
        releaseTextureUnit(unit)
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
        backend.glTexImage2D(target, 0, data.internalFormat, data.width, data.height,
            0, data.pixelFormat, data.pixelType, data.pixels)
        backend.glGenerateMipmap(target)
        backend.glTexParameteri(target, backend.GL_TEXTURE_MIN_FILTER, backend.GL_NEAREST_MIPMAP_LINEAR)
        backend.glTexParameteri(target, backend.GL_TEXTURE_MAG_FILTER, backend.GL_LINEAR)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_S, backend.GL_REPEAT)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_T, backend.GL_REPEAT)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_R, backend.GL_REPEAT)
    }

    private fun useTextureCubeMap() {
        check(texData.size == 6)
        texData.forEachIndexed { index, side ->
            backend.glTexImage2D(
                backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + index, 0,
                side.internalFormat, side.width, side.height, 0,
                side.pixelFormat, side.pixelType, side.pixels
            )
        }
        backend.glGenerateMipmap(target)
        backend.glTexParameteri(target, backend.GL_TEXTURE_MIN_FILTER, backend.GL_NEAREST_MIPMAP_LINEAR)
        backend.glTexParameteri(target, backend.GL_TEXTURE_MAG_FILTER, backend.GL_LINEAR)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_S, backend.GL_CLAMP_TO_EDGE)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_T, backend.GL_CLAMP_TO_EDGE)
        backend.glTexParameteri(target, backend.GL_TEXTURE_WRAP_R, backend.GL_CLAMP_TO_EDGE)
    }

    fun accessHandle(): Int {
        checkReady()
        return handle
    }

    fun accessUnit(): Int {
        checkReady()
        return unit
    }
}