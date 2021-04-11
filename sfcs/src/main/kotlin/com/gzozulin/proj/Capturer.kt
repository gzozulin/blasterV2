package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.global.opencv_videoio.VIDEOWRITER_PROP_QUALITY
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.opencv.core.CvType
import java.io.File

private const val IS_CAPTURING = true

private fun String.toFourcc() =
    VideoWriter.fourcc(this[0].toByte(), this[1].toByte(), this[2].toByte(), this[3].toByte())

class Capturer(window: GlWindow, private val width: Int, private val height: Int) {
    private val videoWriter = VideoWriter()

    private val framePointer = BytePointer(window.frameBuffer)
    private val originalFrame by lazy { Mat(height, width, CvType.CV_8UC4, framePointer) }
    private val flippedFrame by lazy { Mat(height, width, CvType.CV_8UC4) }

    fun capture(frames: () -> Unit) {
        if (IS_CAPTURING) {
            videoWriter.open(File("output.avi").absolutePath, "MJPG".toFourcc(), 60.0, Size(width, height))
            videoWriter.set(VIDEOWRITER_PROP_QUALITY, 100.0)
            check(videoWriter.isOpened)
        }
        frames.invoke()
        if (IS_CAPTURING) {
            videoWriter.release()
        }
    }

    fun onBuffer() {
        if (IS_CAPTURING) {
            flip(originalFrame, flippedFrame, 0) // vertical flip
            videoWriter.write(flippedFrame)
        }
    }
}

private val containers = listOf("avi", "mkv", "wmv")
private val fourcc = listOf(
    "uncompressed" to 0,
    "MP4V" to "MP4V".toFourcc(),
    "MJPG" to "MJPG".toFourcc())

fun main() {
    for (container in containers) {
        for (pair in fourcc) {
            try {
                val writer = VideoWriter()
                writer.open("123video123.$container", pair.second, 24.0, Size(100, 100))
                check(writer.isOpened)
                println("succeeded $container ${pair.first}")
            } catch (th: Throwable) {
                println("failed $container ${pair.first}")
            }
        }
    }
}