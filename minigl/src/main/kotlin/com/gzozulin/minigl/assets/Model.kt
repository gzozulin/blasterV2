package com.gzozulin.minigl.assets

import com.gzozulin.minigl.api.GlBindable
import com.gzozulin.minigl.api.GlMesh
import com.gzozulin.minigl.api.GlResource
import com.gzozulin.minigl.api.aabb
import com.gzozulin.minigl.scene.PbrMaterial
import com.gzozulin.minigl.scene.PhongMaterial

open class Material : GlBindable()

data class Object(val mesh: GlMesh, val material: Material, val aabb: aabb): GlBindable() {
    init {
        addChildren(mesh, material)
    }

    fun phong(): PhongMaterial = material as PhongMaterial
    fun pbr(): PbrMaterial = material as PbrMaterial
}

data class Model(val objects: List<Object>, val aabb: aabb) : GlResource() {
    init {
        addChildren(objects)
    }

    fun first() = objects.first()
}