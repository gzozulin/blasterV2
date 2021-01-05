package com.gzozulin.sim.system

import com.gzozulin.sim.entity.Actor
import com.gzozulin.sim.entity.ComponentType

val reactiveBus = ReactiveBus()

class ReactiveBus {
    private val subscriptions = mutableMapOf<ComponentType, MutableList<Subscriber>>()

    fun register(type: ComponentType, subscriber: Subscriber) {
        val subscribers = subscriptions.getOrPut(type) { mutableListOf() }
        subscribers.add(subscriber)
    }

    fun updated(type: ComponentType, actor: Actor) {
        subscriptions[type]?.forEach { it.changed(actor) }
    }
}

class Subscriber(type: ComponentType) {
    init {
        reactiveBus.register(type, this)
    }

    private val changes = HashSet<Actor>()

    fun changed(actor: Actor) {
        changes.add(actor)
    }

    fun invokeOnChanges(action: (Collection<Actor>) -> Unit) {
        action.invoke(changes)
        changes.clear()
    }
}