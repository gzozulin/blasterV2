package com.gzozulin.sim.system

import com.gzozulin.sim.entity.*
import com.gzozulin.sim.simulation.*
import com.gzozulin.minigl.gl.vec3
import kotlinx.coroutines.*

private const val BODY_RADIUS_SQR = 1f
private const val BODY_SPEED = 0.1f

class PhysicsSystem {
    fun updatePhysics() = runBlocking {
        val toUpdate = repository.existingPartitions
        val deferred = mutableListOf<Deferred<Unit>>()
        for (partitionId in toUpdate) {
            deferred.add(async(Dispatchers.Default) { updatePartition(partitionId) })
        }
        deferred.awaitAll()
    }

    private fun updatePartition(partitionId: PartitionId) {
        val all = repository.withPartitionId(partitionId)
            .filter { it.hasComponent(ComponentType.PHYSICS) }
            .toList()
        for (actor in all) {
            updatePosition(actor, all)
        }
    }

    private fun updatePosition(actor: Actor, all: Collection<Actor>) {
        val physics = actor.writePhysics()
        val updated = vec3(physics.velocity).mul(BODY_SPEED).add(actor.readSpatial().position)
        val collided = collisionBounds(updated, physics) || collisionOthers(actor, updated, physics, all)
        if (collided) {
            updated.set(physics.velocity).mul(BODY_SPEED).add(actor.readSpatial().position)
        }
        actor.writeSpatial().position.set(updated)
    }

    private fun collisionBounds(updated: vec3, physics: PhysicsComponent): Boolean {
        var collided = false
        if (updated.x < WORLD_LEFT || updated.x > WORLD_RIGHT) {
            physics.velocity.x = -physics.velocity.x
            collided = true
        }
        if (updated.z < WORLD_BACK || updated.z > WORLD_FRONT) {
            physics.velocity.z = -physics.velocity.z
            collided = true
        }
        return collided
    }

    private fun collisionOthers(actor: Actor, updated: vec3, physics: PhysicsComponent,
                                all: Collection<Actor>): Boolean {
        for (other in all) {
            if (actor != other) {
                val pos = other.readSpatial().position
                val dx = updated.x - pos.x
                val dz = updated.z - pos.z
                val distSq = dx * dx + dz * dz
                if (distSq <= BODY_RADIUS_SQR) {
                    handleCollision(updated, physics, other.writeSpatial(), other.writePhysics())
                    return true
                }
            }
        }
        return false
    }

    private fun handleCollision(updated: vec3, leftPhysics: PhysicsComponent,
                                rightSpatial: SpatialComponent, rightPhysics: PhysicsComponent
    ) {
        // http://flatredball.com/documentation/tutorials/math/circle-collision
        val tangent = vec3(rightSpatial.position.z - updated.z, 0f,
            -(rightSpatial.position.x - updated.x)).normalize()
        val relativeVelocity = vec3(leftPhysics.velocity.x - rightPhysics.velocity.x, 0f,
            leftPhysics.velocity.z - rightPhysics.velocity.z)
        val length = relativeVelocity.dot(tangent)
        tangent.mul(length)
        relativeVelocity.sub(tangent)
        leftPhysics.velocity.x -= relativeVelocity.x
        leftPhysics.velocity.z -= relativeVelocity.z
        rightPhysics.velocity.x += relativeVelocity.x
        rightPhysics.velocity.z += relativeVelocity.z
    }
}