package com.gzozulin.proj

import org.kodein.di.instance

class Controller(private var initial: State = StateIdle()) {
    fun keyPressed(key: Int, pressed: Boolean) {

    }

    fun tick() {

    }
}

open class State {
    protected val model: ModelCozyRoom by ProjectorApp.injector.instance()

    open fun onEnter() {}
    open fun onLeave() {}
    open fun onFrame() {}
    open fun onKey(key: Int, pressed: Boolean) {}
}

class StateIdle : State() {

}

class StateCozyRoomIntro {

}

class StateCozyRoomTyping {

}

class StateCozyRoomImage {

}