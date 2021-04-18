package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.*
import org.joml.Vector3f
import kotlin.math.abs

private const val EPSILON = 1f
private const val SPEED = 0.05f
private const val INERTIA = 0.02f

private const val DTAU = 0.0001f
private const val TAU_EPSILON = 0.01f

class ControllerScenic(private val positions: List<vec3>, private val points: List<vec3>) {

    // ----------------- Position ----------------

    private val position = vec3(positions.random())
    private val destination = vec3(positions.random())
    private val velocity = vec3(destination).sub(position).normalize()
    private val ideal = vec3(destination).sub(position).normalize()
    private val deltaPosition = vec3()
    private val deltaInertia = vec3()

    // ----------------- Direction ----------------

    private var tau = 0f
    private val curr = vec3(points.random())
    private val next = vec3(points.random())
    private val center = vec3()
    private val direction = vec3()

    fun apply(apply: (position: Vector3f, direction: Vector3f) -> Unit) {
        updateVelocity()
        updatePosition()
        updateDirection()
        apply.invoke(position, direction)
    }

    private fun updateVelocity() {
        ideal.set(destination).sub(position).normalize()
        deltaInertia.set(ideal).sub(velocity).mul(INERTIA)
        velocity.add(deltaInertia).normalize()
    }

    private fun updatePosition() {
        deltaPosition.set(velocity).mul(SPEED)
        position.add(deltaPosition)
        checkDestination()
    }

    private fun checkDestination() {
        if (position.distance(destination) < EPSILON) {
            destination.set(positions.filter { it != destination }.random())
        }
    }

    private fun updateDirection() {
        tau += DTAU
        checkTau()
        println(tau)
        center.set(curr).lerp(next, tau)
        direction.set(center).sub(position).normalize()
    }

    private fun checkTau() {
        if (abs(1f - tau) <= TAU_EPSILON) {
            tau = 0f
            curr.set(next)
            next.set(points.filter { it != curr }.random())
        }
    }
}