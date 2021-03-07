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
    private val casePlayback: CasePlayback by ProjApp.injector.instance()

    private val skyboxTechnique: StaticSkyboxTechnique by ProjApp.injector.instance()
    private val simpleTextTechnique: SimpleTextTechnique by ProjApp.injector.instance()

    private val camera = Camera()

    fun onFrame() {
        glClear(col3().ltGrey())
        casePlayback.updateSpans()
        camera.tick()
        skyboxTechnique.skybox(camera)
        simpleTextTechnique.pageCentered(model.page, model.center, LINES_TO_SHOW)
    }
}