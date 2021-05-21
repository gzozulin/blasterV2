package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.*

private val vecUp = vec3().up()

class Camera(aspectRatio: Float) {

    constructor(window: GlWindow) : this(window.width, window.height)
    constructor(winWidth: Int, winHeight: Int) : this(winWidth.toFloat() / winHeight.toFloat())

    val projectionM: mat4 = mat4().identity()
        .perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 0.1f, 500f)

    val position: vec3 = vec3()
    val rotation: quat = quat()

    val viewVersion: Version = Version()
    val viewM: mat4 = mat4().identity()
    val negatedBuf: vec3 = vec3()
    val fullM: mat4 = mat4().identity()

    fun calculateViewM(): mat4 {
        if (viewVersion.check()) {
            position.negate(negatedBuf)
            viewM.identity().rotate(rotation).translate(negatedBuf)
        }
        return viewM
    }

    fun calculateFullM(): mat4 =
        fullM.set(projectionM).mul(calculateViewM())

    fun setPosition(newPosition: vec3) {
        position.set(newPosition)
        viewVersion.increment()
    }

    fun lookAlong(direction: vec3) {
        rotation.identity().lookAlong(direction, vecUp)
        viewVersion.increment()
    }
}