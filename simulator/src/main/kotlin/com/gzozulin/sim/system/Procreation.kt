package com.gzozulin.sim.system

import com.gzozulin.sim.entity.*
import com.gzozulin.sim.simulation.*
import com.gzozulin.minigl.gl.randf
import java.util.*

private const val MAX_TTL = 1000
private const val MAX_BALLS = 30000

private val random = Random()

class ProcreationSystem {
    fun createFields() {
        for (x in -8..8) {
            for (y in -8..8) {
                createField(x, y)
            }
        }
    }

    private fun createField(x: Int, z: Int) {
        repository.createActor(TEMPLATE_FIELD_PATCH).writeSpatial()
            .position.set(x.toFloat() * PARTITION_SIDE, 0f, z.toFloat() * PARTITION_SIDE)
    }

    fun procreateSoccerBalls() {
        val count = removeOld()
        createNew(count)
    }

    private fun removeOld(): Int {
        val soccerBalls = repository.withComponent(ComponentType.TTL)
        var count = 0
        for (soccerBall in soccerBalls) {
            val ttl = soccerBall.writeTtl()
            ttl.ttl --
            if (ttl.ttl == 0L) {
                repository.removeActor(soccerBall)
            } else {
                count++
            }
        }
        return count
    }

    private fun createNew(count: Int) {
        val createCount = MAX_BALLS - count
        if (createCount > 0) {
            for (i in 0 until createCount) {
                val actor = repository.createActor(TEMPLATE_SOCCER_BALL)
                actor.writeSpatial().position.set(randf(WORLD_LEFT, WORLD_RIGHT), 0f, randf(
                    WORLD_BACK, WORLD_FRONT
                ))
                actor.writeTtl().ttl = random.nextInt(MAX_TTL).toLong()
                actor.writePhysics().velocity.set(randf(WORLD_LEFT, WORLD_RIGHT), 0f, randf(
                    WORLD_BACK, WORLD_FRONT
                ))
                    .normalize()
            }
        }
    }
}