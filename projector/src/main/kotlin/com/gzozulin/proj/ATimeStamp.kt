package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import org.lwjgl.glfw.GLFW

private val window = GlWindow()

private var color = vec3().rand()

private var prev: Long? = null

fun main() = window.create {
    window.keyCallback = { key, pressed ->
        if (key == GLFW.GLFW_KEY_SPACE && pressed) {
            if (prev == null) {
                prev = System.currentTimeMillis()
            } else {
                val current = System.currentTimeMillis()
                val seconds = (current - prev!!).toFloat() / 1000.0f
                val out = String.format("%.2f", seconds)
                println(out + "s")
                color = col3().rand()
            }
        }
    }
    window.show {
        glClear(color)
    }
}