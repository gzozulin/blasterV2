package com.gzozulin.minigl.api

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val FULL_WIDTH: Int = 1920
private const val FULL_HEIGHT: Int = 1080

private const val MULTISAMPLING_HINT = 4

private val xbuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asDoubleBuffer()
private val ybuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asDoubleBuffer()

enum class MouseButton { LEFT, RIGHT }

typealias KeyCallback = (key: Int, pressed: Boolean) -> Unit
typealias ButtonCallback = (button: MouseButton, pressed: Boolean) -> Unit
typealias PositionCallback = (position: vec2) -> Unit
typealias DeltaCallback = (delta: vec2) -> Unit

class GlWindow(private val winWidth: Int = 1024, private val winHeight: Int = 768) {
    var handle: Long? = null

    private var isHoldingCursor: Boolean = false
    private var isFullscreen: Boolean = false
    private var isMultisampling: Boolean = false

    var keyCallback: KeyCallback? = null
    var buttonCallback: ButtonCallback? = null
    var positionCallback: PositionCallback? = null
    var deltaCallback: DeltaCallback? = null

    val width: Int
        get() = if (isFullscreen) FULL_WIDTH else winWidth
    val height: Int
        get() = if (isFullscreen) FULL_HEIGHT else winHeight

    val frameBuffer: ByteBuffer by lazy {
        ByteBuffer.allocateDirect(FULL_WIDTH * FULL_HEIGHT * 4) // RGBA, 1 byte, fullscreen
    }

    private val cursorPos = vec2()
    private val lastCursorPos = vec2()

    private var fps = 0
    private var last = System.currentTimeMillis()

    private val keyCallbackInternal = object : GLFWKeyCallback() {
        override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
            if (action == GLFW_PRESS) {
                when (key) {
                    GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true)
                }
            }
            keyCallback?.invoke(key, action == GLFW_PRESS || action == GLFW_REPEAT)
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

    fun create(isHoldingCursor: Boolean = false, isFullscreen: Boolean = false,
               isMultisampling: Boolean = false, onCreated: () -> Unit) {
        this.isHoldingCursor = isHoldingCursor
        this.isFullscreen = isFullscreen
        this.isMultisampling = isMultisampling
        glfwSetErrorCallback { error, description -> error("$error, $description") }
        check(glfwInit())
        recreateWindow()
        glCheck { backend.glViewport(0, 0, width, height) }
        onCreated.invoke()
        glfwDestroyWindow(handle!!)
    }

    fun show(onFrame: () -> Unit) {
        check(handle != null) { "Window is not yet created!" }
        if (isMultisampling) {
            backend.glEnable(backend.GL_MULTISAMPLE)
        } else {
            backend.glDisable(backend.GL_MULTISAMPLE)
        }
        while (!glfwWindowShouldClose(handle!!)) {
            updateCursor(handle!!)
            onFrame.invoke()
            glfwSwapBuffers(handle!!)
            glfwPollEvents()
            updateFps()
        }
    }

    private fun recreateWindow() {
        if (isMultisampling) {
            glfwWindowHint(GLFW_SAMPLES, MULTISAMPLING_HINT)
        }
        handle = glfwCreateWindow(width, height, "Blaster!",
            if (isFullscreen) glfwGetPrimaryMonitor() else NULL, handle ?: NULL)
        if (!isFullscreen) {
            glfwSetWindowPos(handle!!, (FULL_WIDTH - width)/2, (FULL_HEIGHT - height)/2)
        }
        if (isHoldingCursor) {
            glfwSetInputMode(handle!!, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        glfwSetMouseButtonCallback(handle!!, buttonCallbackInternal)
        glfwSetKeyCallback(handle!!, keyCallbackInternal)
        glfwMakeContextCurrent(handle!!)
        GL.createCapabilities();
        glfwSwapInterval(1)
        glfwShowWindow(handle!!)
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
            glfwSetWindowTitle(handle!!, "Blaster! $fps fps")
            last = current
            fps = 0
        }
    }

    fun copyWindowBuffer() {
        GL11.glReadBuffer(GL11.GL_FRONT)
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, frameBuffer)
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