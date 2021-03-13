package com.gzozulin.proj

import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.lwjgl.glfw.GLFW

const val LINES_TO_SHOW = 22
const val IS_CAPTURING = false
const val IS_TRACING = true

// todo: scenario to nodes
// todo: center on function (stop - start)/2
// todo: basic scene arrangement
// todo: example project + video

class Tracker {
    private val startup = System.currentTimeMillis()
    private var last = startup

    fun mark(section: String) {
        if (IS_TRACING) {
            val current = System.currentTimeMillis()
            val elapsedFromLast = (current - last).toFloat() / 1000f
            val elapsedFromStartup = (current - startup).toFloat() / 1000f
            last = current
            println("$section: elapsed from last: $elapsedFromLast, from startup: $elapsedFromStartup")
        }
    }
}

private val fontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.4f, fontScaleV = 0.5f,
    fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

class ProjApp {
    companion object {
        val injector = DI {
            bind<Tracker>()                 with singleton { Tracker() }
            bind<GlCapturer>()              with singleton { GlCapturer(1920, 1080, isFullscreen = true) }
            bind<StaticSkyboxTechnique>()   with singleton { StaticSkyboxTechnique("textures/darkskies") }
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

private val tracker: Tracker by ProjApp.injector.instance()
private val capturer: GlCapturer by ProjApp.injector.instance()
private val skyboxTechnique: StaticSkyboxTechnique by ProjApp.injector.instance()
private val simpleTextTechnique: SimpleTextTechnique by ProjApp.injector.instance()
private val managerCapture: ManagerCapture by ProjApp.injector.instance()
private val caseScenario: CaseScenario by ProjApp.injector.instance()
private val casePlayback: CasePlayback by ProjApp.injector.instance()
private val scene: ProjScene by ProjApp.injector.instance()

val backgroundModelM = constm4(mat4().identity().translate(capturer.width.toFloat()/2f, 0f, 0f))
val backgroundViewM = constm4(mat4().identity())
val backgroundProjM = constm4(mat4().ortho(0f, capturer.width.toFloat(), 0f, capturer.height.toFloat(), -1f, 1f))
val backgroundMesh = GlMesh.rect(1600f, capturer.height.toFloat() * 2)
val backgroundTech = SimpleTechnique(backgroundModelM, backgroundViewM, backgroundProjM, constv4(vec4(0f, 0f, 0f, 0.8f)))

fun main() {
    caseScenario.renderScenario()
    tracker.mark("Scenario rendered")
    casePlayback.prepareNextOrder()
    tracker.mark("Order prepared")
    capturer.create {
        tracker.mark("Capturer created")
        capturer.keyCallback = { key, isPressed ->
            if (key == GLFW.GLFW_KEY_SPACE && isPressed) {
                casePlayback.proceed()
            }
        }
        managerCapture.capture {
            tracker.mark("Video capturing")
            glUse(skyboxTechnique, simpleTextTechnique, backgroundTech, backgroundMesh) {
                capturer.show(scene::onFrame, managerCapture::onBuffer)
            }
        }
    }
}