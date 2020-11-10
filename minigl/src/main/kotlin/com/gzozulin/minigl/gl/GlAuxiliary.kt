package com.gzozulin.minigl.gl

import org.joml.*
import org.lwjgl.opengl.GL11
import java.lang.Math
import java.util.Random
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

const val STRICT_MODE = true

private val random = Random()

typealias vec3 = Vector3f
typealias vec4i = Vector4i
typealias euler3 = Vector3f
typealias color = Vector3f
typealias vec2 = Vector2f
typealias mat3 = Matrix3f
typealias mat4 = Matrix4f
typealias quat = Quaternionf
typealias aabb = AABBf
typealias ray = Rayf
typealias sphere = Spheref

fun radf(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
fun degf(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()
fun sinf(value: Float) = sin(value.toDouble()).toFloat()
fun cosf(value: Float) = cos(value.toDouble()).toFloat()
fun lerpf(from: Float, to: Float, t: Float) = (1f - t) * from + t * to
fun powf(base: Float, p: Float) = base.pow(p)

fun randi(bound: Int) = random.nextInt(bound)
fun randf(min: Float = 0f, max: Float = 1f) = min + random.nextFloat() * (max - min)
fun vec3.rand(min: vec3 = vec3(0f), max: vec3 = vec3(1f)): vec3 {
    x = randf(min.x, max.x)
    y = randf(min.y, max.y)
    z = randf(min.z, max.z)
    return this
}

fun vec3.up()       = set(0f, 1f, 0f)
fun vec3.down()     = set(0f, -1f, 0f)
fun vec3.left()     = set(-1f, 0f, 0f)
fun vec3.right()    = set(1f, 0f, 0f)
fun vec3.front()    = set(0f, 0f, 1f)
fun vec3.back()     = set(0f, 0f, -1f)

fun color.white()   = set(1f, 1f, 1f)
fun color.red()     = set(1f, 0f, 0f)
fun color.green()   = set(0f, 1f, 0f)
fun color.blue()    = set(0f, 0f, 1f)
fun color.yellow()  = set(1f, 1f, 0f)
fun color.magenta() = set(1f, 0f, 1f)
fun color.cyan()    = set(0f, 1f, 1f)
fun color.grey()    = set(0.5f)

fun color.parseColor(hex: String): color {
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

fun aabb.scaleTo(other: aabb) =
    vec3(other.width() / width(), other.height() / height(), other.depth() / depth())

data class Version(private var version: Long = 0L, private var last: Long = Long.MAX_VALUE) {
    fun increment() { version++ }

    fun check(): Boolean {
        return if (version != last) {
            last = version
            true
        } else {
            false
        }
    }
}

abstract class GlResource {
    private var isUsed = false

    private val childResource = mutableListOf<GlResource>()

    open fun addChildren(vararg children: GlResource) {
        childResource.addAll(children)
    }

    open fun addChildren(children: Collection<GlResource>) {
        childResource.addAll(children)
    }

    open fun use() {
        childResource.forEach { it.use() }
        if (STRICT_MODE) {
            check(!isUsed) { "Already used!" }
        }
        isUsed = true
    }

    open fun release() {
        isUsed = false
        childResource.reversed().forEach { it.release() }
    }

    open fun checkReady() {
        if (STRICT_MODE) {
            check(isUsed) { "Is not used!" }
        }
    }
}

abstract class GlBindable : GlResource() {
    private var isBound = false

    private val childBindable = mutableListOf<GlBindable>()

    fun addChild(vararg children: GlBindable) {
        super.addChildren(*children)
        childBindable.addAll(children)
    }

    open fun bind() {
        super.checkReady()
        childBindable.forEach { it.bind() }
        if (STRICT_MODE) {
            check(!isBound) { "Already bound!" }
        }
        isBound = true
    }

    open fun unbind() {
        isBound = false
        childBindable.reversed().forEach { it.unbind() }
    }

    override fun checkReady() {
        super.checkReady()
        if (STRICT_MODE) {
            check(isBound) { "Is not bound!" }
        }
    }
}

fun glBind(vararg bindables: GlBindable, action: () -> Unit) {
    bindables.forEach { it.bind() }
    action.invoke()
    bindables.reversed().forEach { it.unbind() }
}

fun glUse(vararg usables: GlResource, action: () -> Unit) {
    usables.forEach { it.use() }
    action.invoke()
    usables.forEach { it.release() }
}

fun <T> glCheck(action: () -> T): T {
    val result = action.invoke()
    if (STRICT_MODE) {
        val errorCode = GL11.glGetError()
        if (errorCode != GL11.GL_NO_ERROR) {
            throw GlError(errorCode)
        }
    }
    return result
}

fun glClear(color: color = color().cyan()) {
    backend.glClearColor(color.x, color.y, color.z, 0f)
    backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT)
}

fun glDepthTest(depthFunc: Int = backend.GL_LEQUAL, action: () -> Unit) {
    backend.glEnable(backend.GL_DEPTH_TEST)
    backend.glDepthFunc(depthFunc)
    action.invoke()
    backend.glDisable(backend.GL_DEPTH_TEST)
}

fun glCulling(frontFace: Int = backend.GL_CCW, action: () -> Unit) {
    backend.glEnable(backend.GL_CULL_FACE)
    backend.glFrontFace(frontFace)
    action.invoke()
    backend.glDisable(backend.GL_CULL_FACE)
}

fun glBlend(sfactor: Int = backend.GL_SRC_ALPHA, dfactor: Int = backend.GL_ONE_MINUS_SRC_ALPHA, action: () -> Unit) {
    backend.glBlendFunc(sfactor, dfactor)
    backend.glEnable(backend.GL_BLEND)
    action.invoke()
    backend.glDisable(backend.GL_BLEND)
}