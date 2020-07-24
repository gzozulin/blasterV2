package com.gzozulin.minigl.gl

import com.gzozulin.minigl.SharedLibraryLoader
import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val winWidth: Int = 1024
private const val winHeight: Int = 768

private const val fullWidth: Int = 1920
private const val fullHeight: Int = 1080

private const val winX: Int = 448
private const val winY: Int = 156

private typealias ResizeCallback = (width: Int, height: Int) -> Unit
private typealias KeyCallback = (key: Int, pressed: Boolean) -> Unit
private typealias ButtonCallback = (key: Int, pressed: Boolean) -> Unit
private typealias PositionCallback = (position: vec2) -> Unit
private typealias DeltaCallback = (delta: vec2) -> Unit

class GlWindow {
    init { SharedLibraryLoader.load() }

    var resizeCallback: ResizeCallback? = null
    var keyCallback: KeyCallback? = null
    var buttonCallback: ButtonCallback? = null
    var positionCallback: PositionCallback? = null
    var deltaCallback: DeltaCallback? = null

    private var isFullscreen: Boolean = false
    private var window = NULL

    private val width: Int
        get() = if (isFullscreen) fullWidth else winWidth
    private val height: Int
        get() = if (isFullscreen) fullHeight else winHeight

    private val xbuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val xbufDouble = xbuf.asDoubleBuffer()
    private val ybuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val ybufDouble = ybuf.asDoubleBuffer()
    private val currentPos = vec2()
    private val lastCursorPos = vec2()

    private var fps = 0
    private var last = System.currentTimeMillis()

    private val errorCallback = errorCallbackPrint(System.err)

    private val resizeCallbackInternal = object : GLFWWindowSizeCallback() {
        override fun invoke(window: kotlin.Long, width: kotlin.Int, height: kotlin.Int) {
            glCheck { backend.glViewport(0, 0, width, height) }
            resizeCallback?.invoke(width, height)
        }
    }

    private val keyCallbackInternal = object : GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (key == GLFW_KEY_ESCAPE) {
                glfwSetWindowShouldClose(window, GL11.GL_TRUE)
            }
            if (action == GLFW_PRESS) {
                keyCallback?.invoke(key, true)
            } else if (action == GLFW_RELEASE) {
                keyCallback?.invoke(key, false)
            }
        }
    }

    private val buttonCallbackInternal = object : GLFWMouseButtonCallback() {
        override fun invoke(window: kotlin.Long, button: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            buttonCallback?.invoke(button, action == GLFW_PRESS)
        }
    }

    private fun updateCursor(window: Long) {
        xbuf.rewind()
        xbufDouble.rewind()
        ybuf.rewind()
        ybufDouble.rewind()
        glfwGetCursorPos(window, xbuf, ybuf)
        currentPos.set(xbufDouble.get().toFloat(), ybufDouble.get().toFloat())
        if (lastCursorPos.x == 0f && lastCursorPos.y == 0f) {
            lastCursorPos.set(currentPos.x, currentPos.y)
        }
        positionCallback?.invoke(currentPos)
        currentPos.sub(lastCursorPos, lastCursorPos)
        deltaCallback?.invoke(lastCursorPos)
        lastCursorPos.set(currentPos)
    }

    private fun updateFps() {
        fps++
        val current = System.currentTimeMillis()
        if (current - last >= 1000L) {
            glfwSetWindowTitle(window, "Blaster! $fps fps")
            last = current
            fps = 0
        }
    }

    fun create(isHoldingCursor: Boolean = true, isFullscreen: Boolean = false, onCreated: () -> Unit) {
        this.isFullscreen = isFullscreen
        glfwSetErrorCallback(errorCallback)
        check(glfwInit() == GL11.GL_TRUE)
        val result = if (isFullscreen) {
            glfwCreateWindow(fullWidth, fullHeight, "Blaster!", glfwGetPrimaryMonitor(), window)
        } else {
            glfwCreateWindow(winWidth, winHeight, "Blaster!", NULL, window)
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
        GLContext.createFromCurrent()
        window = result
        onCreated.invoke()
        glfwDestroyWindow(window)
        keyCallbackInternal.release()
    }

    fun show(onFrame: () -> Unit) {
        check(window != NULL) { "Window is not yet created!" }
        glCheck { backend.glViewport(0, 0, width, height) }
        resizeCallback?.invoke(width, height)
        glfwShowWindow(window)
        while (glfwWindowShouldClose(window) == GL11.GL_FALSE) {
            updateCursor(window)
            onFrame.invoke()
            glfwSwapBuffers(window)
            glfwPollEvents()
            updateFps()
        }
    }
}