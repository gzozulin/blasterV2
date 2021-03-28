package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.vec3

interface Light {
    val vector: vec3
    val color: vec3
    val attenConstant: Float
    val attenLinear: Float
    val attenQuadratic: Float
}

data class PointLight(val position: vec3,
                      override val color: vec3,
                      override val attenConstant: Float = 0.9f,
                      override val attenLinear: Float = 0.7f,
                      override val attenQuadratic: Float = 0.3f
) : Light {
    override val vector: vec3
        get() = position
}

data class DirectionLight(val direction: vec3,
                          override val color: vec3,
                          override val attenConstant: Float = 0.9f,
                          override val attenLinear: Float = 0.7f,
                          override val attenQuadratic: Float = 0.3f
) : Light {
    override val vector: vec3
        get() = direction
}