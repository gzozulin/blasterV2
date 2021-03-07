package com.gzozulin.proj

import com.gzozulin.minigl.gl.GlCapturer
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.global.opencv_videoio
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.kodein.di.instance
import org.opencv.core.CvType
import java.io.File

class ManagerCapture {
    private val capturer: GlCapturer by ProjApp.injector.instance()
    private val videoWriter: VideoWriter by ProjApp.injector.instance()

    private val framePointer = BytePointer(capturer.frameBuffer)
    private val originalFrame by lazy { Mat(capturer.height, capturer.width, CvType.CV_8UC4, framePointer) }
    private val flippedFrame by lazy { Mat(capturer.height, capturer.width, CvType.CV_8UC4) }

    fun capture(frames: () -> Unit) {
        videoWriter.apply {
            open(
                File("1vid.avi").absolutePath,
                VideoWriter.fourcc('M'.toByte(), 'J'.toByte(), 'P'.toByte(), 'G'.toByte()),
                60.0,
                Size(capturer.width, capturer.height)
            )
            set(opencv_videoio.VIDEOWRITER_PROP_QUALITY, 100.0)
            check(isOpened)
        }
        frames.invoke()
        videoWriter.release()
    }

    fun onBuffer() {
        flip(originalFrame, flippedFrame, 0) // vertical flip
        videoWriter.write(flippedFrame)
    }
}