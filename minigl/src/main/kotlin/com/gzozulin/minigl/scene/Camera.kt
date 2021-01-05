package com.gzozulin.minigl.scene

import com.gzozulin.minigl.gl.*

private val vecUp = vec3().up()

data class Camera(
    val projectionM: mat4 = mat4(),
    val position: vec3 = vec3(),
    val rotation: quat = quat()
) {
    private val viewVersion: Version = Version()
    private val viewM: mat4 = mat4()
    private val negatedBuf: vec3 = vec3()

    init {
        setPerspective(4f/3f)
    }

    fun calculateViewM(): mat4 {
        if (viewVersion.check()) {
            position.negate(negatedBuf)
            viewM.identity().rotate(rotation).translate(negatedBuf)
        }
        return viewM
    }

    fun setPerspective(aspectRatio: Float): Camera {
        projectionM.identity().perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 0.01f, 1000f)
        viewVersion.increment()
        return this
    }

    fun setPerspective(width: Int, height: Int) {
        setPerspective(width.toFloat() / height.toFloat())
    }

    fun setPosition(newPosition: vec3) {
        position.set(newPosition)
        viewVersion.increment()
    }

    fun lookAlong(direction: vec3) {
        rotation.identity().lookAlong(direction, vecUp)
        viewVersion.increment()
    }
}