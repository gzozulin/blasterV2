package com.gzozulin.proj

import com.gzozulin.minigl.gl.black
import com.gzozulin.minigl.gl.col3
import com.gzozulin.minigl.gl.glClear
import org.kodein.di.instance

class Controller(private var current: State = StateIdle()) {

    private var next: State? = null

    fun keyPressed(key: Int, pressed: Boolean) {
        current.onKey(key, pressed)
    }

    fun frame() {
        if (next != null) {
            current.onLeave()
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

open class State {
    open fun onEnter() {}
    open fun onLeave() {}
    open fun onFrame() {}
    open fun onKey(key: Int, pressed: Boolean) {}
}

class StateIdle : State() {
    private val controller: Controller by ProjectorApp.injector.instance()

    override fun onFrame() {
        super.onFrame()
        glClear(col3().black())
    }

    override fun onKey(key: Int, pressed: Boolean) {
        super.onKey(key, pressed)
        if (!pressed) {
            controller.switch(StateCozyRoomIntro())
        }
    }
}

class StateCozyRoomIntro: State() {
    private val model: ModelCozyRoom by ProjectorApp.injector.instance()
    private val view: SceneCozyRoom by ProjectorApp.injector.instance()

    override fun onFrame() {
        super.onFrame()
        view.onFrame()
    }

    override fun onKey(key: Int, pressed: Boolean) {
        super.onKey(key, pressed)
        if (!pressed) {
            model.proceed()
        }
    }
}

class StateCozyRoomTyping: State() {

}

class StateCozyRoomImage: State() {

}