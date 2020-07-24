package com.gzozulin.sim.entity

import com.gzozulin.sim.system.Partition
import com.gzozulin.sim.system.PartitionId
import com.gzozulin.sim.system.Subscriber
import com.gzozulin.sim.system.partitionId

internal val repository = Repository()

class Repository {
    private val actorFactory = ActorFactory()
    private val componentFactory = ComponentFactory()

    private val created = HashSet<Actor>()
    private val active = mutableListOf<Actor>()
    private val removed = HashSet<Actor>()

    internal val byPartition = mutableMapOf<PartitionId, Partition>()
    internal val byComponent = Array<MutableList<Actor>>(ComponentType.values().size) { mutableListOf() }

    val existingPartitions: Collection<PartitionId>
        get() = byPartition.keys

    fun createActor(template: Template): Actor {
        val actor = actorFactory.create()
        template.set(actor, componentFactory)
        created.add(actor)
        return actor
    }

    fun removeActor(actor: Actor) {
        removed.add(actor)
    }

    fun withComponent(type: ComponentType) = sequence {
        yieldAll(byComponent[type.ordinal])
    }

    fun withComponents(types: Collection<ComponentType>) = sequence {
        for (type in types) {
            yieldAll(withComponent(type))
        }
    }

    fun withPartitionId(partitionId: PartitionId) = sequence {
        val partition = byPartition[partitionId]
        if (partition != null) {
            yieldAll(partition.actors)
        }
    }

    fun withPartitionIds(partitionIds: Collection<PartitionId>) = sequence {
        for (partitionId in partitionIds) {
            yieldAll(withPartitionId(partitionId))
        }
    }

    class GcSystem {
        private val spatialSubscriber =
            Subscriber(ComponentType.SPATIAL)

        fun performGC() {
            handleRemovedActors()
            handleCreatedActors()
            updatePartitions()
        }

        private fun handleRemovedActors() {
            repository.removed.forEach { actor ->
                repository.active.remove(actor)
                removeFromCurrentPartition(actor)
                ComponentType.values().forEach { type ->
                    if (actor.hasComponent(type)) {
                        repository.byComponent[type.ordinal].remove(actor)
                        repository.componentFactory.store(type, actor.readComponent(type))
                    }
                }
                repository.actorFactory.store(actor)
            }
            repository.removed.clear()
        }

        private fun handleCreatedActors() {
            repository.created.forEach { actor ->
                ComponentType.values().forEach { type ->
                    if (actor.hasComponent(type)) {
                        repository.byComponent[type.ordinal].add(actor)
                    }
                }
                addToNewPartition(actor)
                repository.active.add(actor)
            }
            repository.created.clear()
        }

        private fun updatePartitions() {
            spatialSubscriber.invokeOnChanges { actors ->
                actors.forEach { actor ->
                    val spatial = actor.readSpatial()
                    val partition = spatial.partition
                    if (partition != null && partition.partitionId != spatial.position.partitionId()) {
                        removeFromCurrentPartition(actor)
                        addToNewPartition(actor)
                    }
                }
            }
        }

        private fun removeFromCurrentPartition(actor: Actor) {
            val spatial = actor.readSpatial()
            check(spatial.partition != null)
            check(spatial.partition!!.actors.remove(actor)) { "Actor was not registered?!" }
            spatial.partition = null
        }

        private fun addToNewPartition(actor: Actor) {
            val spatial = actor.readSpatial()
            val partitionId = spatial.position.partitionId()
            val partition = repository.byPartition.getOrPut(partitionId) { Partition(partitionId) }
            check(partition.actors.add(actor)) { "Actor is already registered?!" }
            spatial.partition = partition
        }
    }
}