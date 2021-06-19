package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.vec3

enum class HitableType { HITABLE, SPHERE }

data class HitRecord(val t: Float, val point: vec3, val normal: vec3)
data class Hitable(val type: Int, val index: Int)
data class Sphere(val center: vec3, val radius: Float)
