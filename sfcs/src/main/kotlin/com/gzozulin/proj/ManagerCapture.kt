package com.gzozulin.proj

import com.gzozulin.minigl.gl.GlCapturer
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.global.opencv_videoio.VIDEOWRITER_PROP_QUALITY
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.kodein.di.instance
import org.opencv.core.CvType
import java.io.File

class ManagerCapture {
    private val capturer: GlCapturer by ProjectorApp.injector.instance()
    private val videoWriter: VideoWriter by ProjectorApp.injector.instance()

    private val framePointer = BytePointer(capturer.frameBuffer)
    private val originalFrame by lazy { Mat(capturer.height, capturer.width, CvType.CV_8UC4, framePointer) }
    private val flippedFrame by lazy { Mat(capturer.height, capturer.width, CvType.CV_8UC4) }

    fun capture(frames: () -> Unit) {
        if (IS_CAPTURING) {
            val fourcc = VideoWriter.fourcc('M'.toByte(), 'J'.toByte(), 'P'.toByte(), 'G'.toByte())
            videoWriter.open(File("1vid.avi").absolutePath, fourcc, 60.0, Size(capturer.width, capturer.height))
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