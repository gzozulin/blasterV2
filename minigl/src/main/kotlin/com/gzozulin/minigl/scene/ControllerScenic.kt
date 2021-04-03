package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.*
import org.joml.Vector3f

private const val TIMEOUT_FLIGHT = 150000L
private const val TIMEOUT_HOVER = 0L
private const val TIMEOUT_FRAME = 16L

private val upVec = vec3().up()

data class PointOfInterest(val position: vec3, val direction: vec3)

class ControllerScenic(private val points: List<PointOfInterest>) {

    private var isHovering = true
    private var timeout = TIMEOUT_HOVER

    private var currPoi: PointOfInterest = points.first()
    private var nextPoi: PointOfInterest = points.first()

    private val position = vec3(points.first().position)
    private val direction = vec3(points.first().direction)

    private val rotationFrom = quat().identity()
    private val rotationTo = quat().identity()
    private val rotation = quat().identity()

    fun apply(apply: (position: Vector3f, direction: Vector3f) -> Unit) {
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
        currPoi = nextPoi
        val noCurrent = points.toMutableList()
        noCurrent.remove(currPoi)
        nextPoi = noCurrent.random()
        rotationFrom.identity().lookAlong(currPoi.direction, upVec)
        rotationTo.identity().lookAlong(nextPoi.direction, upVec)
    }

    private fun interpolate() {
        val progress = 1f - (timeout.toFloat() / TIMEOUT_FLIGHT.toFloat())
        position.set(currPoi.position).lerp(nextPoi.position, progress)
        rotationFrom.slerp(rotationTo, progress, rotation)
        direction.set(vec3().back())
        rotation.transformInverse(direction)
    }
}