package com.gzozulin.minigl.scene

import com.gzozulin.minigl.api.vec2
import org.lwjgl.glfw.GLFW

class WasdInput(private val controller: ControllerFirstPerson) {
    fun onCursorDelta(delta: vec2) {
        controller.yaw(delta.x)
        controller.pitch(-delta.y)
    }

    fun onKeyPressed(key: Int, pressed: Boolean) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.moveForward = pressed
            GLFW.GLFW_KEY_A -> controller.moveLeft = pressed
            GLFW.GLFW_KEY_S -> controller.moveBack = pressed
            GLFW.GLFW_KEY_D -> controller.moveRight = pressed
            GLFW.GLFW_KEY_E -> controller.moveUp = pressed
            GLFW.GLFW_KEY_Q -> controller.moveDown = pressed
        }
    }
}