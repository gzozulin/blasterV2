package com.gzozulin.proj

import com.gzozulin.minigl.assembly.SimpleTextTechnique
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_core.flip
import org.bytedeco.opencv.global.opencv_videoio
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.kodein.di.instance
import org.opencv.core.CvType
import java.io.File

class ProjScene {
    private val model: ProjModel by ProjApp.injector.instance()
    private val repo: Repository by ProjApp.injector.instance()
    private val casePlayback: CasePlayback by ProjApp.injector.instance()

    private val capturer = GlCapturer(1024, 1024, isFullscreen = false)

    private var videoWriter = VideoWriter()

    private val framePointer = BytePointer(capturer.frameBuffer)
    private val originalFrame by lazy { Mat(capturer.height, capturer.width, CvType.CV_8UC4, framePointer) }
    private val flippedFrame by lazy { Mat(capturer.height, capturer.width, CvType.CV_8UC4) }

    private val camera = Camera()

    private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")
    private val simpleTextTechnique = SimpleTextTechnique(capturer.width, capturer.height)

    fun loop() {
        openVideo()
        capturer.create {
            glUse(skyboxTechnique, simpleTextTechnique) {
                capturer.show(::onFrame, ::onBuffer)
            }
        }
        videoWriter.release()
    }

    private fun openVideo() {
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
    }

    private fun onFrame() {
        glClear(col3().ltGrey())
        casePlayback.updateSpans()
        camera.tick()
        skyboxTechnique.skybox(camera)
        simpleTextTechnique.pageCentered(casePlayback.currentPage, casePlayback.currentCenter, LINES_TO_SHOW)
    }

    private fun onBuffer() {
        flip(originalFrame, flippedFrame, 0) // vertical flip
        videoWriter.write(flippedFrame)
    }
}