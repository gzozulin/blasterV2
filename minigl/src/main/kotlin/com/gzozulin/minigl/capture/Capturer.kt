package com.gzozulin.minigl.capture

import com.gzozulin.minigl.api.GlWindow
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_RAWVIDEO
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGBA
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.opencv_core.Mat
import org.opencv.core.CvType

// code > raw, avi > ffmpeg > h264, mp4, lossless and H264, mp4 low profile for handling

private val converter = OpenCVFrameConverter.ToMat()

class Capturer(private val window: GlWindow, private val filename: String = "output") : AutoCloseable {
    var isCapturing = true

    private val recorder by lazy {
        FFmpegFrameRecorder("$filename.avi", window.width, window.height, 0)
            .apply {
                videoCodec = AV_CODEC_ID_RAWVIDEO
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

    fun addFrame() {
        if (isCapturing) {
            window.copyWindowBuffer()
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