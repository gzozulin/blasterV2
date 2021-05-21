package com.gzozulin.minigl.assets

import com.gzozulin.minigl.api.GlTexture
import com.gzozulin.minigl.api.GlTextureImage
import com.gzozulin.minigl.api.backend
import com.gzozulin.minigl.api.glTextureCreate2D
import com.gzozulin.minigl.scene.PbrMaterial
import java.awt.Color
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

private data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

private fun libTextureDecodePixels(inputStream: InputStream, mirrorX: Boolean = false, mirrorY: Boolean = false): Decoded {
    val bufferedImage = ImageIO.read(inputStream)
    val pixelNum = bufferedImage.width * bufferedImage.height
    val byteBuffer = ByteBuffer.allocateDirect(pixelNum * 4).order(ByteOrder.nativeOrder())
    val xRange = if (mirrorX) bufferedImage.width - 1 downTo 0 else 0 until bufferedImage.width
    val yRange = if (mirrorY) 0 until bufferedImage.height else bufferedImage.height - 1 downTo 0
    for (y in yRange) {
        for (x in xRange) {
            val color = Color(bufferedImage.getRGB(x, y), true)
            byteBuffer.addColor(color)
        }
    }
    byteBuffer.position(0)
    return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
}

private fun ByteBuffer.addColor(color: Color) {
    put(color.red.toByte())
    put(color.green.toByte())
    put(color.blue.toByte())
    put(color.alpha.toByte())
}

fun libTextureCreate(file: File, mirrorX: Boolean = false, mirrorY: Boolean = false): GlTexture {
    val inputStream = file.inputStream()
    val decoded = libTextureDecodePixels(inputStream, mirrorX, mirrorY)
    return glTextureCreate2D(file.absolutePath, decoded.width, decoded.height, decoded.pixels)
}

fun libTextureCreate(asset: String, mirrorX: Boolean = false, mirrorY: Boolean = false) =
    libTextureCreate(libAssetCreate(asset), mirrorX, mirrorY)

fun libTextureCreateCubeMap(filename: String): GlTexture {
    val file = File(filename)
    val right   = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_rt.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val left    = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_lf.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val top     = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_up.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val bottom  = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_dn.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val front   = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_ft.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val back    = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_bk.jpg").inputStream(), mirrorX = true, mirrorY = true)
    return GlTexture(
        label = filename,
        target = backend.GL_TEXTURE_CUBE_MAP,
        wrapS = backend.GL_CLAMP_TO_EDGE, wrapT = backend.GL_CLAMP_TO_EDGE, wrapR = backend.GL_CLAMP_TO_EDGE,
        images = listOf(
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 0, width = right.width,    height = right.height,  pixels = right.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 1, width = left.width,     height = left.height,   pixels = left.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 2, width = top.width,      height = top.height,    pixels = top.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 3, width = bottom.width,   height = bottom.height, pixels = bottom.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 4, width = front.width,    height = front.height,  pixels = front.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 5, width = back.width,     height = back.height,   pixels = back.pixels)
        )
    )
}

fun libTextureCreatePbr(directory: String, extension: String = "png",
                        albedo: String = "$directory/albedo.$extension",
                        normal: String = "$directory/normal.$extension",
                        metallic: String = "$directory/metallic.$extension",
                        roughness: String = "$directory/roughness.$extension",
                        ao: String = "$directory/ao.$extension"): PbrMaterial {
    val decodedAlbedo   = libTextureDecodePixels(libAssetCreate(albedo).inputStream())
    val decodedNormal   = libTextureDecodePixels(libAssetCreate(normal).inputStream())
    val decodedMetallic = libTextureDecodePixels(libAssetCreate(metallic).inputStream())
    val decodedRoughness = libTextureDecodePixels(libAssetCreate(roughness).inputStream())
    val decodedAo       = libTextureDecodePixels(libAssetCreate(ao).inputStream())
    return PbrMaterial(
        glTextureCreate2D(albedo, decodedAlbedo.width, decodedAlbedo.height, decodedAlbedo.pixels),
        glTextureCreate2D(normal, decodedNormal.width, decodedNormal.height, decodedNormal.pixels),
        glTextureCreate2D(metallic, decodedMetallic.width, decodedMetallic.height, decodedMetallic.pixels),
        glTextureCreate2D(roughness, decodedRoughness.width, decodedRoughness.height, decodedRoughness.pixels),
        glTextureCreate2D(ao, decodedAo.width, decodedAo.height, decodedAo.pixels),
    )
}