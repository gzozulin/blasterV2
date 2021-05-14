package com.gzozulin.minigl.api2

import org.joml.*
import java.lang.Math
import java.util.Random
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

typealias vec3 = Vector3f
typealias vec4 = Vector4f
typealias vec4i = Vector4i
typealias euler3 = Vector3f
typealias col3 = Vector3f
typealias col4 = Vector4f
typealias vec2 = Vector2f
typealias vec2i = Vector2i
typealias mat3 = Matrix3f
typealias mat4 = Matrix4f
typealias quat = Quaternionf
typealias aabb = AABBf
typealias ray = Rayf
typealias sphere = Spheref

private val random = Random()

fun radf(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
fun degf(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()
fun sinf(value: Float) = sin(value.toDouble()).toFloat()
fun cosf(value: Float) = cos(value.toDouble()).toFloat()
fun lerpf(from: Float, to: Float, t: Float) = (1f - t) * from + t * to
fun powf(base: Float, p: Float) = base.pow(p)

fun randi(bound: Int = Int.MAX_VALUE) = random.nextInt(bound)
fun randf(min: Float = 0f, max: Float = 1f) = min + random.nextFloat() * (max - min)
fun vec3.rand(min: vec3 = vec3(0f), max: vec3 = vec3(1f)): vec3 {
    x = randf(min.x, max.x)
    y = randf(min.y, max.y)
    z = randf(min.z, max.z)
    return this
}

fun vec3.up()           = set(0f, 1f, 0f)
fun vec3.down()         = set(0f, -1f, 0f)
fun vec3.left()         = set(-1f, 0f, 0f)
fun vec3.right()        = set(1f, 0f, 0f)
fun vec3.front()        = set(0f, 0f, 1f)
fun vec3.back()         = set(0f, 0f, -1f)

fun col3.white()        = set(1f, 1f, 1f)
fun col3.black()        = set(0f, 0f, 0f)
fun col3.ltGrey()       = set(0.3f)
fun col3.grey()         = set(0.5f)
fun col3.dkGrey()       = set(0.7f)

fun col3.red()          = set(1f, 0f, 0f)
fun col3.green()        = set(0f, 1f, 0f)
fun col3.blue()         = set(0f, 0f, 1f)

fun col3.yellow()       = set(1f, 1f, 0f)
fun col3.magenta()      = set(1f, 0f, 1f)
fun col3.cyan()         = set(0f, 1f, 1f)

fun col3.orange()       = set(1f, .5f, 0f)
fun col3.rose()         = set(1f, 0f, .5f)
fun col3.violet()       = set(.5f, 0f, 1f)
fun col3.azure()        = set(0f, .5f, 1f)
fun col3.aquamarine()   = set(0f, 1f, .5f)
fun col3.chartreuse()   = set(.5f, 1f, 0f)

val col3.r
    get() = x
val col3.g
    get() = y
val col3.b
    get() = z

fun col3.parseColor(hex: String): col3 {
    val integerHex = Integer.parseInt(hex, 16)
    val rIntValue = (integerHex / 256 / 256) % 256
    val gIntValue = (integerHex / 256      ) % 256
    val bIntValue = (integerHex            ) % 256
    x = rIntValue / 255.0f
    y = gIntValue / 255.0f
    z = bIntValue / 255.0f
    return this
}

fun aabb.width() = maxX - minX
fun aabb.height() = maxY - minY
fun aabb.depth() = maxZ - minZ
fun aabb.center() = Vector3f(
    minX + (maxX - minX) / 2f,
    minY + (maxY - minY) / 2f,
    minZ + (maxZ - minZ) / 2f)

fun aabb.scaleTo(to: Float): Float {
    var maxSide = Float.MIN_VALUE
    if (width() > maxSide) {
        maxSide = width()
    }
    if (height() > maxSide) {
        maxSide = height()
    }
    if (depth() > maxSide) {
        maxSide = depth()
    }
    return to / maxSide
}

fun mat4.orthoBox(side: Float) = identity().ortho(-side, side, -side, side, side, -side)