package com.gzozulin.minigl.assets2

import com.gzozulin.minigl.api2.GlTexture
import com.gzozulin.minigl.api2.glTextureCreate2D
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