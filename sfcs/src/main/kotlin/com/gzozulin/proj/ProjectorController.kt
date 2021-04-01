package com.gzozulin.proj

import com.gzozulin.minigl.api.black
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.api.glClear
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class State(
    protected val parent: ProjectorController,
    protected val model: ProjectorModel,
    protected val view: ProjectorView) {

    open fun onEnter() {}
    open fun onLeave() {}

    open fun onFrame() {}

    open fun onKey(key: Int, pressed: Boolean) {}
}

class StateCozyRoomIntro(
    parent: ProjectorController, model: ProjectorModel, view: ProjectorView) : State(parent, model, view) {

    override fun onEnter() {
        super.onEnter()
        GlobalScope.launch { model.renderScenario() }
        view.fadeIn()
    }

    override fun onFrame() {
        glClear(col3().black())
        view.tickCamera()
        view.renderScene()
        view.renderCrossFade()
    }

    override fun onKey(key: Int, pressed: Boolean) {
        super.onKey(key, pressed)
        if (!pressed) { // todo: when loaded and faded in
            parent.switch(StateCozyRoomTyping(parent, model, view))
        }
    }
}

class StateCozyRoomTyping(
    parent: ProjectorController, model: ProjectorModel, view: ProjectorView) : State(parent, model, view) {

    override fun onFrame() {
        super.onFrame()
        model.updateSpans()
        view.tickCamera()
        view.prepareCode()
        view.renderScene()
        view.renderCode()
    }

    override fun onKey(key: Int, pressed: Boolean) {
        super.onKey(key, pressed)
        if (!pressed) {
            model.proceed()
        }
    }
}

class ProjectorController(model: ProjectorModel, view: ProjectorView) {

    private lateinit var current: State
    private var next: State? = null

    init {
        switch(StateCozyRoomIntro(this, model, view))
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