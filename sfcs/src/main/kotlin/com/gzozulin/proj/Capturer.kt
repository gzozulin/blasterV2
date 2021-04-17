package com.gzozulin.proj

import com.gzozulin.minigl.api.GlResizable
import com.gzozulin.minigl.api.GlWindow
import org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGBA
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.opencv_core.Mat
import org.opencv.core.CvType

private val converter = OpenCVFrameConverter.ToMat()

class Capturer(private val window: GlWindow) : GlResizable, AutoCloseable {

    var isCapturing = true

    private var recorder: FFmpegFrameRecorder? = null

    private lateinit var bufferPointer: BytePointer
    private lateinit var originalFrame: Mat
    private lateinit var flippedFrame: Mat
    private lateinit var frame: Frame

    override fun resize(width: Int, height: Int) {
        if (recorder != null) {
            close()
        }
        recorder = FFmpegFrameRecorder("out.mp4", width, height, 0)
            .apply {
                videoCodec = AV_CODEC_ID_H264
                pixelFormat = AV_PIX_FMT_YUV420P
                format = "mp4"
                frameRate = 60.0
                videoQuality = 22.0
            }
        bufferPointer = BytePointer(window.frameBuffer)
        originalFrame = Mat(height, width, CvType.CV_8UC4, bufferPointer)
        flippedFrame = Mat(height, width, CvType.CV_8UC4)
        frame = converter.convert(flippedFrame)
        recorder!!.start()
    }

    fun capture(frames: () -> Unit) {
        frames.invoke()
        close()
    }

    fun frame() {
        if (isCapturing) {
            flip(originalFrame, flippedFrame, 0) // vertical flip
            recorder!!.record(frame, AV_PIX_FMT_RGBA)
        }
    }

    override fun close() {
        recorder!!.release()
        bufferPointer.close()
        originalFrame.release()
        flippedFrame.release()
        frame.close()
        recorder = null
    }
}