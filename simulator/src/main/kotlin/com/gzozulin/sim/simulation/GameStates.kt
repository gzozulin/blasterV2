package com.gzozulin.sim.simulation

open class State {
    open fun enter() {}
    open fun leave() {}
    open fun tick() {}
}

class StateMachine {
    private var current: State =
        State()
    private var next: State? = null

    fun next(next: State) {
        this.next = next
    }

    fun tick() {
        current.tick()
    }

    fun switch() {
        if (next != null) {
            current.leave()
            current = next!!
            next = null
            current.enter()
        }
    }
}
