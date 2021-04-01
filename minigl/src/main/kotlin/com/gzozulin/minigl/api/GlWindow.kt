package com.gzozulin.minigl.api

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val winWidth: Int = 1024
private const val winHeight: Int = 768

private const val fullWidth: Int = 1920
private const val fullHeight: Int = 1080

private const val winX: Int = 448
private const val winY: Int = 156

private const val MULTISAMPLING_HINT = 4

enum class MouseButton { LEFT, RIGHT }

typealias ResizeCallback = (width: Int, height: Int) -> Unit
typealias KeyCallback = (key: Int, pressed: Boolean) -> Unit
typealias ButtonCallback = (button: MouseButton, pressed: Boolean) -> Unit
typealias PositionCallback = (position: vec2) -> Unit
typealias DeltaCallback = (delta: vec2) -> Unit

class GlWindow {
    var resizeCallback: ResizeCallback? = null
    var keyCallback: KeyCallback? = null
    var buttonCallback: ButtonCallback? = null
    var positionCallback: PositionCallback? = null
    var deltaCallback: DeltaCallback? = null

    private var isFullscreen: Boolean = false
    private var isCapturing: Boolean = false

    var handle = NULL

    val width: Int
        get() = if (isFullscreen) fullWidth else winWidth
    val height: Int
        get() = if (isFullscreen) fullHeight else winHeight

    private val xbuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asDoubleBuffer()
    private val ybuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asDoubleBuffer()

    var frameBuffer = ByteBuffer.allocateDirect(width * height * 4) // RGBA, 1 byte each

    private val cursorPos = vec2()
    private val lastCursorPos = vec2()

    private var fps = 0
    private var last = System.currentTimeMillis()

    private val resizeCallbackInternal = object : GLFWWindowSizeCallback() {
        override fun invoke(window: Long, width: Int, height: Int) {
            glCheck { backend.glViewport(0, 0, width, height) }
            frameBuffer = ByteBuffer.allocateDirect(width * height * 4)
            resizeCallback?.invoke(width, height)
        }
    }

    private val keyCallbackInternal = object : GLFWKeyCallback() {
        override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
            if (key == GLFW_KEY_ESCAPE) {
                glfwSetWindowShouldClose(window, true)
            }
            if (action == GLFW_PRESS) {
                keyCallback?.invoke(key, true)
            } else if (action == GLFW_RELEASE) {
                keyCallback?.invoke(key, false)
            }
        }
    }

    private val buttonCallbackInternal = object : GLFWMouseButtonCallback() {
        override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
            val converted = when (button) {
                GLFW_MOUSE_BUTTON_LEFT -> MouseButton.LEFT
                GLFW_MOUSE_BUTTON_RIGHT -> MouseButton.RIGHT
                else -> error("Unknown mouse button!")
            }
            buttonCallback?.invoke(converted, action == GLFW_PRESS)
        }
    }

    fun create(
        isHoldingCursor: Boolean = true, isFullscreen: Boolean = false,
        isMultisampling: Boolean = false, isCapturing: Boolean = false,
        onCreated: () -> Unit
    ) {
        this.isFullscreen = isFullscreen
        this.isCapturing = isCapturing
        glfwSetErrorCallback { error, description -> error("$error, $description") }
        check(glfwInit())
        if (isMultisampling) {
            glfwWindowHint(GLFW_SAMPLES, MULTISAMPLING_HINT)
        }
        val result = if (isFullscreen) {
            glfwCreateWindow(fullWidth, fullHeight, "Blaster!", glfwGetPrimaryMonitor(), handle)
        } else {
            glfwCreateWindow(winWidth, winHeight, "Blaster!", NULL, handle)
        }
        if (!isFullscreen) {
            glfwSetWindowPos(result, winX, winY)
        }
        if (isHoldingCursor) {
            glfwSetInputMode(result, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        glfwSetWindowSizeCallback(result, resizeCallbackInternal)
        glfwSetMouseButtonCallback(result, buttonCallbackInternal)
        glfwSetKeyCallback(result, keyCallbackInternal)
        glfwMakeContextCurrent(result)
        glfwSwapInterval(1)
        handle = result
        GL.createCapabilities();
        onCreated.invoke()
        glfwDestroyWindow(handle)
    }

    fun show(onFrame: () -> Unit) {
        check(handle != NULL) { "Window is not yet created!" }
        glCheck { backend.glViewport(0, 0, width, height) }
        resizeCallback?.invoke(width, height)
        glfwShowWindow(handle)
        while (!glfwWindowShouldClose(handle)) {
            updateCursor(handle)
            onFrame.invoke()
            glfwSwapBuffers(handle)
            copyWindowBuffer()
            glfwPollEvents()
            updateFps()
            GlProgram.stopComplaining()
        }
    }

    private fun updateCursor(window: Long) {
        xbuf.rewind()
        ybuf.rewind()
        glfwGetCursorPos(window, xbuf, ybuf)
        cursorPos.set(xbuf.get().toFloat(), ybuf.get().toFloat())
        if (lastCursorPos.x == 0f && lastCursorPos.y == 0f) {
            lastCursorPos.set(cursorPos.x, cursorPos.y)
        }
        positionCallback?.invoke(cursorPos)
        cursorPos.sub(lastCursorPos, lastCursorPos)
        deltaCallback?.invoke(lastCursorPos)
        lastCursorPos.set(cursorPos)
    }

    private fun updateFps() {
        fps++
        val current = System.currentTimeMillis()
        if (current - last >= 1000L) {
            glfwSetWindowTitle(handle, "Blaster! $fps fps")
            last = current
            fps = 0
        }
    }

    private fun copyWindowBuffer() {
        if (isCapturing) {
            GL11.glReadBuffer(GL11.GL_FRONT)
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, frameBuffer)
        }
    }
}

private val window = GlWindow()

fun main() {
    window.create {
        window.show {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
        }
    }
}