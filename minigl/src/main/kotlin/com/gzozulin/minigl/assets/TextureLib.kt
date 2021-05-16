package com.gzozulin.minigl.assets

import com.gzozulin.minigl.api.GlTexture
import com.gzozulin.minigl.api.GlTextureImage
import com.gzozulin.minigl.api.backend
import com.gzozulin.minigl.api.glTextureCreate2D
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
    return glTextureCreate2D(decoded.width, decoded.height, decoded.pixels)
}

fun libTextureCreate(asset: String, mirrorX: Boolean = false, mirrorY: Boolean = false) =
    libTextureCreate(libAssetCreate(asset), mirrorX, mirrorY)

/*fun loadSkybox(filename: String, unit: Int = 0): GlTexture {
        val file = File(filename)
        val right   = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_rt.jpg"), mirrorX = true, mirrorY = true)
        val left    = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_lf.jpg"), mirrorX = true, mirrorY = true)
        val top     = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_up.jpg"), mirrorX = true, mirrorY = true)
        val bottom  = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_dn.jpg"), mirrorX = true, mirrorY = true)
        val front   = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_ft.jpg"), mirrorX = true, mirrorY = true)
        val back    = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_bk.jpg"), mirrorX = true, mirrorY = true)
        return GlTexture(
            target = backend.GL_TEXTURE_CUBE_MAP, texData = listOf(
                GlTexData(width = right.width, height = right.height, pixels = right.pixels),
                GlTexData(width = left.width, height = left.height, pixels = left.pixels),
                GlTexData(width = top.width, height = top.height, pixels = top.pixels),
                GlTexData(width = bottom.width, height = bottom.height, pixels = bottom.pixels),
                GlTexData(width = front.width, height = front.height, pixels = front.pixels),
                GlTexData(width = back.width, height = back.height, pixels = back.pixels)
            )
        )
    }
*/

fun libTextureCreateCubeMap(filename: String): GlTexture {
    val file = File(filename)
    val right   = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_rt.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val left    = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_lf.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val top     = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_up.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val bottom  = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_dn.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val front   = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_ft.jpg").inputStream(), mirrorX = true, mirrorY = true)
    val back    = libTextureDecodePixels(libAssetCreate(filename + "/" + file.name + "_bk.jpg").inputStream(), mirrorX = true, mirrorY = true)
    return GlTexture(
        target = backend.GL_TEXTURE_CUBE_MAP, images = listOf(
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 0, width = right.width, height = right.height, pixels = right.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 1, width = left.width, height = left.height, pixels = left.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 2, width = top.width, height = top.height, pixels = top.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 3, width = bottom.width, height = bottom.height, pixels = bottom.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 4, width = front.width, height = front.height, pixels = front.pixels),
            GlTextureImage(target = backend.GL_TEXTURE_CUBE_MAP_POSITIVE_X + 5, width = back.width, height = back.height, pixels = back.pixels)
        )
    )
}