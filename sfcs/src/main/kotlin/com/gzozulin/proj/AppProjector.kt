package com.gzozulin.proj

import com.gzozulin.minigl.gl.GlCapturer
import com.gzozulin.minigl.gl.glUse
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

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

private val tracker = Tracker()
private val controller = Controller()
private val capturer = GlCapturer(1920, 1080, isFullscreen = true)
private val videoWriter = VideoWriter()
private val managerCapture = ManagerCapture()
private val repoProjector = RepoProjector()
private val mechanicScenario = MechanicScenario()
private val mechanicPlayback = MechanicPlayback()
private val sceneCozyRoom = SceneCozyRoom()
private val modelCozyRoom = ModelCozyRoom()

class ProjectorApp {
    companion object {
        val injector = DI {
            bind<Tracker>()                 with singleton { tracker }
            bind<Controller>()              with singleton { controller }
            bind<GlCapturer>()              with singleton { capturer }
            bind<VideoWriter>()             with singleton { videoWriter }
            bind<ManagerCapture>()          with singleton { managerCapture }
            bind<RepoProjector>()           with singleton { repoProjector }
            bind<MechanicScenario>()        with singleton { mechanicScenario }
            bind<MechanicPlayback>()        with singleton { mechanicPlayback }
            bind<SceneCozyRoom>()           with singleton { sceneCozyRoom }
            bind<ModelCozyRoom>()           with singleton { modelCozyRoom }
        }
    }
}

fun main() {
    mechanicScenario.renderScenario()
    mechanicPlayback.prepareNextOrder()
    capturer.create {
        capturer.keyCallback = { key, pressed ->
            controller.keyPressed(key, pressed)
        }
        managerCapture.capture {
            glUse(sceneCozyRoom) {
                capturer.show(controller::frame, managerCapture::onBuffer)
            }
        }
    }
}