package com.gzozulin.sim.entity

import com.gzozulin.sim.system.reactiveBus
import java.util.concurrent.atomic.AtomicInteger

typealias ActorUid = Int
private const val DUMMY_ACTOR_ID = 0
private val nextId = AtomicInteger(DUMMY_ACTOR_ID)

private const val MAX_CACHED_ACTORS = 100

class Actor internal constructor() {
    val uid: ActorUid = nextId.incrementAndGet()

    internal val components: Array<Component?> = arrayOfNulls(ComponentType.values().size)

    fun hasComponent(type: ComponentType) = components[type.ordinal] != null

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> readComponent(type: ComponentType) = components[type.ordinal]!! as T
    fun <T : Component> writeComponent(type: ComponentType): T {
        reactiveBus.updated(type, this)
        return readComponent(type)
    }
}

class ActorFactory {
    private val cache = mutableListOf<Actor>()

    fun create(): Actor {
        if (cache.isEmpty()) {
            for (i in 0 until MAX_CACHED_ACTORS) {
                cache.add(Actor())
            }
        }
        return cache.removeAt(0)
    }

    fun store(actor: Actor) {
        cache.add(actor)
    }
}