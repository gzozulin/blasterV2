package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.vec3

interface Light {
    val vector: vec3
    val intensity: vec3
}

data class PointLight(val position: vec3, override val intensity: vec3) : Light {
    override val vector: vec3
        get() = position
}

data class DirectionLight(val direction: vec3, override val intensity: vec3) : Light {
    override val vector: vec3
        get() = direction
}