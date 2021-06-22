package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.down
import com.gzozulin.minigl.api.up
import com.gzozulin.minigl.api.vec3
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

private val vecUp = vec3().up()
private val vecDown = vec3().down()

private const val maxPitch = Math.PI.toFloat() / 2f - 0.1f
private const val minPitch = -Math.PI.toFloat() / 2f + 0.1f

data class ControllerFirstPerson(
    val position: vec3 = vec3(),
    var yaw: Float = Math.toRadians(-90.0).toFloat(),
    var pitch: Float = 0f,
    var roll: Float = 0f,
    private val sensitivity: Float = 0.005f,
    private val velocity: Float = 0.01f) {

    var moveForward: Boolean     = false
    var moveLeft: Boolean        = false
    var moveBack: Boolean        = false
    var moveRight: Boolean       = false
    var moveDown: Boolean        = false
    var moveUp: Boolean          = false

    private val delta: vec3 = Vector3f()
    private val back: vec3 = Vector3f()
    private val right: vec3 = Vector3f()
    private val left: vec3 = Vector3f()

    val direction = Vector3f(0f, 0f, -1f)

    fun yaw(radians: Float) {
        yaw += (radians * sensitivity)
    }

    fun pitch(radians: Float) {
        pitch += (radians * sensitivity)
        if (pitch > maxPitch) {
            pitch = maxPitch
        }
        if (pitch < minPitch) {
            pitch = minPitch
        }
    }

    fun roll(radians: Float) {
        roll += (radians * sensitivity)
    }

    fun apply(apply: (position: Vector3f, direction: Vector3f) -> Unit) {
        updatePosition()
        updateDirection()
        apply.invoke(position, direction)
    }

    fun updatePosition() {
        delta.zero()
        right.set(vecUp).cross(direction, right)
        right.normalize()
        direction.negate(back)
        right.negate(left)
        if (moveForward) {
            delta.add(direction)
        }
        if (moveLeft) {
            delta.add(right)
        }
        if (moveBack) {
            delta.add(back)
        }
        if (moveRight) {
            delta.add(left)
        }
        if (moveDown) {
            delta.add(vecDown)
        }
        if (moveUp) {
            delta.add(vecUp)
        }
        delta.mul(velocity)
        position.add(delta)
    }

    fun updateDirection() {
        direction.x = cos(yaw) * cos(pitch)
        direction.y = sin(pitch)
        direction.z = sin(yaw) * cos(pitch)
        direction.normalize()
    }
}

// todo: 0 - starting point
// todo: 1-9 - teleports
// todo: some inertia would be cool