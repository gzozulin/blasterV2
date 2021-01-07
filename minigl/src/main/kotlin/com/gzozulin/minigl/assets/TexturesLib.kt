package com.gzozulin.minigl.assets

import com.gzozulin.minigl.gl.GlTexData
import com.gzozulin.minigl.gl.GlTexture
import com.gzozulin.minigl.gl.backend
import com.gzozulin.minigl.scene.PbrMaterial
import java.awt.Color
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

private val pixelDecoder = PixelDecoder()

open class PixelDecoder internal constructor() {
    data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

    open fun decodePixels(inputStream: InputStream, mirrorX: Boolean = false, mirrorY: Boolean = false): Decoded {
        val bufferedImage = ImageIO.read(inputStream)
        val pixelNum = bufferedImage.width * bufferedImage.height
        val byteBuffer = ByteBuffer.allocateDirect(pixelNum * 4).order(ByteOrder.nativeOrder())
        val xRange = if (mirrorX) {
            bufferedImage.width - 1 downTo 0
        } else {
            0 until bufferedImage.width
        }
        val yRange = if (mirrorY) {
            0 until bufferedImage.height
        } else {
            bufferedImage.height - 1 downTo 0
        }
        for (y in yRange) {
            for (x in xRange) {
                val color = Color(bufferedImage.getRGB(x, y), true)
                addColor(color, byteBuffer)
            }
        }
        byteBuffer.position(0)
        return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
    }

    private fun addColor(color: Color, byteBuffer: ByteBuffer) {
        byteBuffer.put(color.red.toByte())
        byteBuffer.put(color.green.toByte())
        byteBuffer.put(color.blue.toByte())
        byteBuffer.put(color.alpha.toByte())
    }
}

val texturesLib = TexturesLib()

class TexturesLib internal constructor() {
    fun loadTexture(filename: String, mirror: Boolean = false): GlTexture {
        val decoded = pixelDecoder.decodePixels(assetStream.openAsset(filename), mirror)
        return GlTexture(width = decoded.width, height = decoded.height, pixels = decoded.pixels)
    }

    fun loadSkybox(filename: String, unit: Int = 0): GlTexture {
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

    fun loadPbr(directory: String, extension: String = "png",
                albedo: String = "$directory/albedo.$extension",
                normal: String = "$directory/normal.$extension",
                metallic: String = "$directory/metallic.$extension",
                roughness: String = "$directory/roughness.$extension",
                ao: String = "$directory/ao.$extension"): PbrMaterial {
        val decodedAlbedo   = pixelDecoder.decodePixels(assetStream.openAsset(albedo))
        val decodedNormal   = pixelDecoder.decodePixels(assetStream.openAsset(normal))
        val decodedMetallic = pixelDecoder.decodePixels(assetStream.openAsset(metallic))
        val decodedRoughness = pixelDecoder.decodePixels(assetStream.openAsset(roughness))
        val decodedAo       = pixelDecoder.decodePixels(assetStream.openAsset(ao))
        return PbrMaterial(
            GlTexture(width = decodedAlbedo.width, height = decodedAlbedo.height, pixels = decodedAlbedo.pixels),
            GlTexture(width = decodedNormal.width, height = decodedNormal.height, pixels = decodedNormal.pixels),
            GlTexture(width = decodedMetallic.width, height = decodedMetallic.height, pixels = decodedMetallic.pixels),
            GlTexture(width = decodedRoughness.width, height = decodedRoughness.height, pixels = decodedRoughness.pixels),
            GlTexture(width = decodedAo.width, height = decodedAo.height, pixels = decodedAo.pixels)
        )
    }
}