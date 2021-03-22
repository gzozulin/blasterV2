package com.gzozulin.sim.entity

import com.gzozulin.sim.system.Partition
import com.gzozulin.minigl.api.vec3

private const val MAX_CACHED_COMPONENTS = 100

enum class ComponentType {
    SPATIAL,
    TILE_MAP,
    BILLBOARD,
    MESH,
    TTL,
    PHYSICS
}

interface Component

data class SpatialComponent(
    val position: vec3, val euler: vec3) : Component {
    var partition: Partition? = null
}

class TileMapComponent(val tileMap: Array<Int> = arrayOf()) : Component
enum class BillboardType { SOCCER }
data class BillboardComponent(var billboardType: BillboardType) : Component
enum class MeshType { FIELD_PATCH }
data class MeshComponent(var meshType: MeshType) : Component
data class TtlComponent(var ttl: Long) : Component
data class PhysicsComponent(val velocity: vec3) : Component

fun Actor.readSpatial() = readComponent<SpatialComponent>(ComponentType.SPATIAL)
fun Actor.writeSpatial() = writeComponent<SpatialComponent>(ComponentType.SPATIAL)

fun Actor.readTileMap() = readComponent<TileMapComponent>(ComponentType.TILE_MAP)
fun Actor.writeTileMap() = writeComponent<TileMapComponent>(ComponentType.TILE_MAP)

fun Actor.readBillboard() = readComponent<BillboardComponent>(ComponentType.BILLBOARD)
fun Actor.writeBillboard() = writeComponent<BillboardComponent>(ComponentType.BILLBOARD)

fun Actor.readMesh() = readComponent<MeshComponent>(ComponentType.MESH)
fun Actor.writeMesh() = writeComponent<MeshComponent>(ComponentType.MESH)

fun Actor.readTtl() = readComponent<TtlComponent>(ComponentType.TTL)
fun Actor.writeTtl() = writeComponent<TtlComponent>(ComponentType.TTL)

fun Actor.readPhysics() = readComponent<PhysicsComponent>(ComponentType.PHYSICS)
fun Actor.writePhysics() = writeComponent<PhysicsComponent>(ComponentType.PHYSICS)

class ComponentFactory {
    private val cache = Array<MutableList<Component>>(
        ComponentType.values().size) { mutableListOf() }

    fun create(actor: Actor, type: ComponentType) {
        val list = cache[type.ordinal]
        if (list.isEmpty()) {
            for (i in 0 until MAX_CACHED_COMPONENTS) {
                list.add(createComponent(type))
            }
        }
        actor.components[type.ordinal] = list.removeAt(0)
    }

    fun store(type: ComponentType, component: Component) {
        cache[type.ordinal].add(component)
    }

    private fun createComponent(type: ComponentType) = when(type) {
        ComponentType.SPATIAL -> SpatialComponent(vec3(), vec3())
        ComponentType.TILE_MAP -> TileMapComponent()
        ComponentType.BILLBOARD -> BillboardComponent(BillboardType.SOCCER)
        ComponentType.MESH -> MeshComponent(MeshType.FIELD_PATCH)
        ComponentType.TTL -> TtlComponent(100L)
        ComponentType.PHYSICS -> PhysicsComponent(vec3())
    }
}
