package com.gzozulin.minigl.gl

import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer

private const val width: Int = 640
private const val height: Int = 480

private const val winX: Int = 448
private const val winY: Int = 156

private const val BPP = 4
private val FRAME_BUFFER by lazy { BufferUtils.createByteBuffer(width * height * BPP) }

private val keyCallbackInternal = object : GLFWKeyCallback() {
    override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW_KEY_ESCAPE) {
            glfwSetWindowShouldClose(window, true)
        }
    }
}

class GlCapturer {
    var handle = NULL

    private var fps = 0
    private var last = System.currentTimeMillis()

    private fun updateFps() {
        fps++
        val current = System.currentTimeMillis()
        if (current - last >= 1000L) {
            glfwSetWindowTitle(handle, "Blaster! $fps fps")
            last = current
            fps = 0
        }
    }

    fun create(onCreated: () -> Unit) {
        glfwSetErrorCallback { error, description -> error("$error, $description") }
        check(glfwInit())
        val result = glfwCreateWindow(width, height, "Blaster!", NULL, handle)
        glfwSetWindowPos(result, winX, winY)
        glfwSetKeyCallback(result, keyCallbackInternal)
        glfwMakeContextCurrent(result)
        glfwSwapInterval(1)
        handle = result
        GL.createCapabilities();
        onCreated.invoke()
        glfwDestroyWindow(handle)
    }

    fun show(onFrame: () -> Unit, onBuffer: (byteBuffer: ByteBuffer) -> Unit) {
        check(handle != NULL) { "Window is not yet created!" }
        glCheck { backend.glViewport(0, 0, width, height) }
        glfwShowWindow(handle)
        while (!glfwWindowShouldClose(handle)) {
            onFrame.invoke()
            copyWindowBuffer()
            onBuffer.invoke(FRAME_BUFFER)
            glfwSwapBuffers(handle)
            glfwPollEvents()
            updateFps()
            GlProgram.stopComplaining()
        }
    }

    private fun copyWindowBuffer() {
        GL11.glReadBuffer(GL11.GL_FRONT)
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, FRAME_BUFFER)
    }
}

private val capturer = GlCapturer()

fun main() {
    capturer.create {
        capturer.show(
            onFrame = {
                glClear(col3().cyan())
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
            },
            onBuffer = { buffer ->
                println(buffer[4444])
            })
    }
}