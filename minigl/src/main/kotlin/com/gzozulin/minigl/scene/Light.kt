package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.vec3

interface Light {
    val vector: vec3
    val color: vec3
    val attenConstant: Float
    val attenLinear: Float
    val attenQuadratic: Float
}

// Based on:
// http://wiki.ogre3d.org/Light+Attenuation+Shortcut
// http://wiki.ogre3d.org/tiki-index.php?page=-Point+Light+Attenuation

private const val LINEAR_COEFF = 4.5f
private const val QUADRATIC_COEFF = 75f

data class PointLight(var position: vec3,
                      override var color: vec3,
                      var range: Float) : Light {

    override val vector: vec3
        get() = position

    override val attenConstant: Float = 1f

    override val attenLinear: Float
        get() = LINEAR_COEFF / range

    override val attenQuadratic: Float
        get() = QUADRATIC_COEFF / (range * range)
}

data class DirectionalLight(var direction: vec3,
                      override var color: vec3,
                      var range: Float) : Light {

    override val vector: vec3
        get() = direction

    override val attenConstant: Float = 1f

    override val attenLinear: Float
        get() = LINEAR_COEFF / range

    override val attenQuadratic: Float
        get() = QUADRATIC_COEFF / (range * range)
}