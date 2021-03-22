package com.gzozulin.sim.system

import com.gzozulin.sim.entity.Actor
import com.gzozulin.minigl.api.vec3

const val PARTITION_SIDE = 16

class PartitionId private constructor(val x: Int, val z: Int) {
    companion object {
        private val partitions = mutableMapOf<Int, MutableMap<Int, PartitionId>>()

        fun fromPosition(position: vec3): PartitionId {
            val partitionX = (position.x / PARTITION_SIDE).toInt()
            val partitionZ = (position.z / PARTITION_SIDE).toInt()
            return fromXY(partitionX, partitionZ)
        }

        fun fromXY(x: Int, z: Int): PartitionId {
            synchronized(partitions) {
                val xPartitions = partitions.getOrPut(x) { mutableMapOf() }
                return xPartitions.getOrPut(z) { PartitionId(x, z) }
            }
        }
    }
}

fun vec3.partitionId() = PartitionId.fromPosition(this)

fun PartitionId.selectNeighbours(): List<PartitionId> {
    val partitionIds = mutableListOf<PartitionId>()
    for (dx in -4..4) {
        for (dz in -4..4) {
            partitionIds.add(PartitionId.fromXY(x - dx, z - dz))
        }
    }
    return partitionIds
}

class Partition(val partitionId: PartitionId) {
    val actors = mutableListOf<Actor>()
}