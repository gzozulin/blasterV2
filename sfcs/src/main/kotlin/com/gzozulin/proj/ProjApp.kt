package com.gzozulin.proj

import com.gzozulin.minigl.assembly.FontDescription
import com.gzozulin.minigl.assembly.SimpleTextTechnique
import com.gzozulin.minigl.gl.GlCapturer
import com.gzozulin.minigl.gl.glUse
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.lwjgl.glfw.GLFW

// todo: scenario to nodes
// todo: basic scene arrangement
// todo: example project + video

private val fontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.3f, fontScaleV = 0.4f,
    fontStepScaleU = 0.55f, fontStepScaleV = 0.75f)

class ProjApp {
    companion object {
        val injector = DI {
            bind<GlCapturer>()              with singleton { GlCapturer(1024, 1024, isFullscreen = false) }
            bind<StaticSkyboxTechnique>()   with singleton { StaticSkyboxTechnique("textures/snowy") }
            bind<SimpleTextTechnique>()     with singleton { SimpleTextTechnique(fontDescription, capturer.width, capturer.height) }
            bind<VideoWriter>()             with singleton { VideoWriter() }
            bind<ManagerCapture>()          with singleton { ManagerCapture() }
            bind<Repository>()              with singleton { Repository() }
            bind<CaseScenario>()            with singleton { CaseScenario() }
            bind<CasePlayback>()            with singleton { CasePlayback() }
            bind<ProjScene>()               with singleton { ProjScene() }
            bind<ProjModel>()               with singleton { ProjModel() }
        }
    }
}

private val capturer: GlCapturer by ProjApp.injector.instance()
private val skyboxTechnique: StaticSkyboxTechnique by ProjApp.injector.instance()
private val simpleTextTechnique: SimpleTextTechnique by ProjApp.injector.instance()
private val managerCapture: ManagerCapture by ProjApp.injector.instance()
private val caseScenario: CaseScenario by ProjApp.injector.instance()
private val casePlayback: CasePlayback by ProjApp.injector.instance()
private val scene: ProjScene by ProjApp.injector.instance()

fun main() {
    caseScenario.renderScenario()
    casePlayback.prepareNextOrder()
    capturer.create {
        capturer.keyCallback = { key, isPressed ->
            if (key == GLFW.GLFW_KEY_SPACE && isPressed) {
                casePlayback.proceed()
            }
        }
        managerCapture.capture {
            glUse(skyboxTechnique, simpleTextTechnique) {
                capturer.show(scene::onFrame, managerCapture::onBuffer)
            }
        }
    }
}