package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_HUFFYUV
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGBA
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.opencv_core.Mat
import org.opencv.core.CvType

// Code > HuffYUV, mkv
// Blender > H264, mp4

private val converter = OpenCVFrameConverter.ToMat()

class Capturer(window: GlWindow) : AutoCloseable {
    var isCapturing = true

    private val recorder by lazy {
        FFmpegFrameRecorder("output.mvk", window.width, window.height, 0)
            .apply {
                videoCodec = AV_CODEC_ID_HUFFYUV
                frameRate = 60.0
            }
    }

    private val bufferPointer by lazy { BytePointer(window.frameBuffer) }
    private val originalFrame by lazy { Mat(window.height, window.width, CvType.CV_8UC4, bufferPointer) }
    private val flippedFrame by lazy { Mat(window.height, window.width, CvType.CV_8UC4) }
    private val frame by lazy { converter.convert(flippedFrame) }

    fun capture(frames: () -> Unit) {
        recorder.start()
        frames.invoke()
        close()
    }

    fun frame() {
        if (isCapturing) {
            flip(originalFrame, flippedFrame, 0) // vertical flip
            recorder.record(frame, AV_PIX_FMT_RGBA)
        }
    }

    override fun close() {
        recorder.release()
        bufferPointer.close()
        originalFrame.release()
        flippedFrame.release()
        frame.close()
    }
}