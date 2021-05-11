package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend
import com.gzozulin.minigl.api.col4
import java.nio.ByteBuffer

// At least 80, see https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glActiveTexture.xhtml
private const val MAX_ACTIVE_TEXTURES = 80
private val availableActiveTextures = (0 until MAX_ACTIVE_TEXTURES).toMutableList()

data class GlTexture(val target: Int = backend.GL_TEXTURE_2D, val images: List<GlTextureImage> = emptyList(),
                     internal var handle: Int? = null, internal var unit: Int? = null)

data class GlTextureImage(val target: Int, val width: Int, val height: Int, val pixels: ByteBuffer, val pixelType: Int = backend.GL_UNSIGNED_BYTE,
                          val internalFormat: Int = backend.GL_RGBA, val pixelFormat: Int = backend.GL_RGBA,
                          val minFilter: Int = backend.GL_NEAREST_MIPMAP_LINEAR, val magFilter: Int = backend.GL_LINEAR,
                          val wrapS: Int = backend.GL_REPEAT, val wrapT: Int = backend.GL_REPEAT, val wrapR: Int = backend.GL_REPEAT)

private fun glTextureUnitHold(): Int {
    synchronized(availableActiveTextures) {
        check(availableActiveTextures.isNotEmpty())
        return availableActiveTextures.removeFirst()
    }
}

private fun glTextureUnitRelease(unit: Int) {
    synchronized(availableActiveTextures) {
        availableActiveTextures.add(unit)
    }
}

private fun glTextureImage(image: GlTextureImage) {
    backend.glTexImage2D(image.target, 0, image.internalFormat,
        image.width, image.height, 0, image.pixelFormat, image.pixelType, image.pixels)
    backend.glTexParameteri(image.target, backend.GL_TEXTURE_MIN_FILTER, image.minFilter)
    backend.glTexParameteri(image.target, backend.GL_TEXTURE_MAG_FILTER, image.magFilter)
    backend.glTexParameteri(image.target, backend.GL_TEXTURE_WRAP_S, image.wrapS)
    backend.glTexParameteri(image.target, backend.GL_TEXTURE_WRAP_T, image.wrapT)
    backend.glTexParameteri(image.target, backend.GL_TEXTURE_WRAP_R, image.wrapR)
    backend.glGenerateMipmap(image.target)
}

internal fun glTextureUse(texture: GlTexture, callback: Callback) {
    check(texture.handle == null) { "GlTexture is already in use" }
    texture.handle = backend.glGenTextures()
    texture.unit = glTextureUnitHold()
    glTextureBind(texture) {
        texture.images.forEach { glTextureImage(it) }
        callback.invoke()
    }
    callback.invoke()
    backend.glDeleteTextures(texture.handle!!)
    glTextureUnitRelease(texture.unit!!)
    texture.handle = null
    texture.unit = null
}

private val currBinding = HashSet<Int>()
internal fun glTextureBind(texture: GlTexture, callback: Callback) {
    check(texture.handle != null) { "GlTexture is not used!" }
    backend.glActiveTexture(backend.GL_TEXTURE0 + texture.unit!!)
    backend.glBindTexture(texture.target, texture.handle!!)
    currBinding.add(texture.unit!!)
    callback.invoke()
    backend.glActiveTexture(backend.GL_TEXTURE0 + texture.unit!!)
    backend.glBindTexture(texture.target, 0) // Each texture has a unique unit
    currBinding.remove(texture.unit)
}

internal fun glTextureCreate2D(width: Int, height: Int, pixels: ByteBuffer): GlTexture {
    return GlTexture(backend.GL_TEXTURE_2D,
        images = listOf(GlTextureImage(backend.GL_TEXTURE_2D, width, height, pixels)))
}

internal fun glTextureCreate2D(width: Int, height: Int, pixels: ByteArray): GlTexture {
    val data = ByteBuffer.allocateDirect(width * height * 4).put(pixels)
    return glTextureCreate2D(width, height, data)
}

internal fun glTextureCreate2D(width: Int, height: Int, pixels: List<col4>): GlTexture {
    val data = ByteBuffer.allocateDirect(width * height * 4)
    pixels.forEach {
        data.put((it.x * 255f).toInt().toByte())
        data.put((it.y * 255f).toInt().toByte())
        data.put((it.z * 255f).toInt().toByte())
        data.put((it.w * 255f).toInt().toByte())
    }
    return glTextureCreate2D(width, height, data)
}