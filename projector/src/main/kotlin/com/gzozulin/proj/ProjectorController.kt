package com.gzozulin.proj

import com.gzozulin.minigl.capture.Capturer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private var scenarioLoaded = false
private val exceptionHandler = Thread.currentThread().uncaughtExceptionHandler

open class State(
    protected val parent: ProjectorController,
    protected val model: ProjectorModel,
    protected val view: ProjectorView,
    protected val capturer: Capturer
) {
    open fun onEnter() {}
    open fun onLeave() {}

    open fun onFrame() {}

    open fun onKey(key: Int, pressed: Boolean) {}
}

class ProjectorController(model: ProjectorModel, view: ProjectorView, capturer: Capturer) {

    private lateinit var current: State
    private var next: State? = null

    init {
        switch(StateCozyRoomIntro(this, model, view, capturer))
    }

    fun keyPressed(key: Int, pressed: Boolean) {
        current.onKey(key, pressed)
    }

    fun onFrame() {
        if (next != null) {
            if (::current.isInitialized) {
                current.onLeave()
            }
            current = next!!
            current.onEnter()
            next = null
        }
        current.onFrame()
    }

    fun switch(next: State) {
        this.next = next
    }
}

class StateCozyRoomIntro(parent: ProjectorController, model: ProjectorModel, view: ProjectorView,
                         capturer: Capturer) : State(parent, model, view, capturer) {

    override fun onEnter() {
        super.onEnter()
        GlobalScope.launch {
            try {
                model.renderScenario()
            } catch (th: Throwable) {
                exceptionHandler.uncaughtException(Thread.currentThread(), th)
            }
            scenarioLoaded = true
        }
    }

    override fun onFrame() {
        view.renderScene()
        if (scenarioLoaded) {
            parent.switch(StateCozyRoomTyping(parent, model, view, capturer))
        }
    }
}

class StateCozyRoomTyping(parent: ProjectorController, model: ProjectorModel, view: ProjectorView,
                          capturer: Capturer) : State(parent, model, view, capturer) {

    override fun onFrame() {
        super.onFrame()
        model.advanceScenario()
        view.renderScene()
        view.renderOverlays()
        if (model.animationState == AnimationState.FINISHED) {
            parent.switch(StateFinished(parent, model, view, capturer))
        }
    }
}

class StateFinished(parent: ProjectorController, model: ProjectorModel, view: ProjectorView,
                    capturer: Capturer) : State(parent, model, view, capturer) {

    override fun onEnter() {
        capturer.isCapturing = false
    }

    override fun onFrame() {
        view.renderScene()
    }
}