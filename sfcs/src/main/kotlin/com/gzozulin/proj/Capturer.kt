package com.gzozulin.proj

import com.gzozulin.minigl.api.GlWindow
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacv.DC1394FrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.opencv_core.Mat
import org.opencv.core.CvType

private const val IS_CAPTURING = true

class Capturer(window: GlWindow, private val width: Int, private val height: Int) {
    private val recorder = FFmpegFrameRecorder("out.avi", width, height, 0)
    private val converter = OpenCVFrameConverter.ToMat()

    init {
        recorder.videoCodec = avcodec.AV_CODEC_ID_RAWVIDEO
        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
        recorder.frameRate = 60.0
    }

    private val bufferPointer = BytePointer(window.frameBuffer)
    private val originalFrame by lazy { Mat(height, width, CvType.CV_8UC4, bufferPointer) }
    private val flippedFrame by lazy { Mat(height, width, CvType.CV_8UC4) }
    private val frame = converter.convert(flippedFrame)

    fun capture(frames: () -> Unit) {
        if (IS_CAPTURING) {
            recorder.start()
        }
        frames.invoke()
        if (IS_CAPTURING) {
            recorder.release()
            bufferPointer.close()
            originalFrame.release()
            flippedFrame.release()
            frame.close()
        }
    }

    fun onBuffer() {
        if (IS_CAPTURING) {
            flip(originalFrame, flippedFrame, 0) // vertical flip
            recorder.record(frame)
        }
    }
}