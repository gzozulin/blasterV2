package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.*
import org.joml.Vector3f

private const val TIMEOUT_FLIGHT = 7000L
private const val TIMEOUT_HOVER = 5000L
private const val TIMEOUT_FRAME = 16L

data class PointOfInterest(val position: vec3, val direction: vec3)

class ControllerScenic(private val points: List<PointOfInterest>) {
    private var current = 0
    private var next = current + 1

    private lateinit var from: PointOfInterest
    private lateinit var to: PointOfInterest

    private var isHovering = true
    private var timeout = TIMEOUT_HOVER

    private val position = points.first().position
    private val direction = points.first().direction

    fun apply(apply: (position: Vector3f, direction: Vector3f) -> Unit) {
        println("$current $next")
        if (isHovering) {
            if (timeout < 0) {
                selectNext()
                timeout = TIMEOUT_FLIGHT
                isHovering = false
            }
        } else {
            interpolate()
            if (timeout < 0) {
                timeout = TIMEOUT_HOVER
                isHovering = true
            }
        }
        apply.invoke(position, direction)
        timeout -= TIMEOUT_FRAME
    }

    private fun selectNext() {
        current++
        if (current == points.size) {
            current = 0
        }
        next++
        if (next == points.size) {
            next = 0
        }
        from = points[current]
        to = points[next]
    }

    private fun interpolate() {
        val progress = 1f - (timeout.toFloat() / TIMEOUT_FLIGHT.toFloat())
        position.lerp(to.position, progress)
        direction.lerp(to.direction, progress) // yaw, pitch, roll instead
    }
}