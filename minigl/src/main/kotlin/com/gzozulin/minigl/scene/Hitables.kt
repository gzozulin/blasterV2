package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.vec3

enum class HitableType { HITABLE, SPHERE }
enum class MaterialType { LAMBERTIAN, METALLIC }

object Hitable // placeholder
object HitRecord // placeholder
object ScatterResult // placeholder

interface RtMaterial
data class LambertianMaterial(val albedo: vec3) : RtMaterial
data class MetallicMaterial(val albedo: vec3) : RtMaterial

data class Sphere(val center: vec3, val radius: Float, val material: RtMaterial)
