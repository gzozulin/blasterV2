package com.gzozulin.sim.entity

import com.gzozulin.minigl.gl.front
import com.gzozulin.minigl.gl.vec3

data class Template(
    private var withSpatial: Boolean = false,
    private var position: vec3 = vec3(0f),
    private var euler: vec3 = vec3(1f),

    private var withBillboard: Boolean = false,
    private var billboardType: BillboardType = BillboardType.SOCCER,

    private var withMesh: Boolean = false,
    private var meshType: MeshType = MeshType.FIELD_PATCH,

    private var withTtl: Boolean = false,
    private var ttl: Long = 0L,

    private var withPhysics: Boolean = false,
    private var velocity: vec3 = vec3().front()
) {
    fun set(actor: Actor, componentFactory: ComponentFactory) {
        if (withSpatial) {
            componentFactory.create(actor,
                ComponentType.SPATIAL
            )
            val spatial = actor.writeSpatial()
            spatial.position.set(position)
            spatial.euler.set(euler)
        }
        if (withBillboard) {
            componentFactory.create(actor,
                ComponentType.BILLBOARD
            )
            actor.writeBillboard().billboardType = billboardType
        }
        if (withMesh) {
            componentFactory.create(actor,
                ComponentType.MESH
            )
            actor.writeMesh().meshType = meshType
        }
        if (withTtl) {
            componentFactory.create(actor,
                ComponentType.TTL
            )
            actor.writeTtl().ttl = ttl
        }
        if (withPhysics) {
            componentFactory.create(actor,
                ComponentType.PHYSICS
            )
            actor.writePhysics().velocity.set(velocity)
        }
    }
}

private val TEMPLATE_BASE = Template(withSpatial = true)
val TEMPLATE_FIELD_PATCH = TEMPLATE_BASE.copy(withMesh = true, meshType = MeshType.FIELD_PATCH)
val TEMPLATE_SOCCER_BALL = TEMPLATE_BASE.copy(withBillboard = true, billboardType = BillboardType.SOCCER, withTtl = true, withPhysics = true)