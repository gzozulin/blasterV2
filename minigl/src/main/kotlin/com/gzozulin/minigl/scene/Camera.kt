package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.*

private val vecUp = vec3().up()

data class Camera(
    val projectionM: mat4 = mat4(),
    val position: vec3 = vec3(),
    val rotation: quat = quat()
) : GlResizable {
    private val viewVersion: Version = Version()
    private val viewM: mat4 = mat4().identity()
    private val negatedBuf: vec3 = vec3()
    private val fullM: mat4 = mat4().identity()

    init {
        setPerspective(4f/3f)
    }

    override fun resize(width: Int, height: Int) {
        setPerspective(width, height)
    }

    fun calculateViewM(): mat4 {
        if (viewVersion.check()) {
            position.negate(negatedBuf)
            viewM.identity().rotate(rotation).translate(negatedBuf)
        }
        return viewM
    }

    fun calculateFullM() = fullM.set(projectionM).mul(calculateViewM())

    fun setPerspective(aspectRatio: Float): Camera {
        projectionM.identity().perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 0.1f, 500f)
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