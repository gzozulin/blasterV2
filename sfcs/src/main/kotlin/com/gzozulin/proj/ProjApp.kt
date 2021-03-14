package com.gzozulin.proj

import com.gzozulin.minigl.gl.GlCapturer
import com.gzozulin.minigl.gl.glUse
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

class ProjApp {
    companion object {
        val injector = DI {
            bind<Tracker>()                 with singleton { Tracker() }
            bind<GlCapturer>()              with singleton { GlCapturer(1920, 1080, isFullscreen = true) }
            bind<VideoWriter>()             with singleton { VideoWriter() }
            bind<ManagerCapture>()          with singleton { ManagerCapture() }
            bind<Repository>()              with singleton { Repository() }
            bind<MechanicScenario>()            with singleton { MechanicScenario() }
            bind<MechanicPlayback>()            with singleton { MechanicPlayback() }
            bind<ProjScene>()               with singleton { ProjScene() }
            bind<ProjModel>()               with singleton { ProjModel() }
        }
    }
}

private val tracker: Tracker by ProjApp.injector.instance()
private val capturer: GlCapturer by ProjApp.injector.instance()
private val managerCapture: ManagerCapture by ProjApp.injector.instance()
private val mechanicScenario: MechanicScenario by ProjApp.injector.instance()
private val mechanicPlayback: MechanicPlayback by ProjApp.injector.instance()
private val scene: ProjScene by ProjApp.injector.instance()

fun main() {
    mechanicScenario.renderScenario()
    tracker.mark("Scenario rendered")
    mechanicPlayback.prepareNextOrder()
    tracker.mark("Order prepared")
    capturer.create {
        tracker.mark("Capturer created")
        capturer.keyCallback = { key, isPressed ->
            if (key == GLFW.GLFW_KEY_SPACE && isPressed) {
                mechanicPlayback.proceed()
            }
        }
        managerCapture.capture {
            tracker.mark("Video capturing")
            glUse(scene) {
                capturer.show(scene::onFrame, managerCapture::onBuffer)
            }
        }
    }
}