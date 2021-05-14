package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api2.mat4
import com.gzozulin.minigl.api2.vec4

// Mproj * Mview * Mmodel * vert = Tproj(Tview(Tmodel(vert)))

data class Transform(val m: mat4 = mat4()) {
    fun apply(vec: vec4): vec4 = m.transform(vec)
    fun apply(other: Transform): Transform = Transform(mat4().set(m).mul(other.m))
}