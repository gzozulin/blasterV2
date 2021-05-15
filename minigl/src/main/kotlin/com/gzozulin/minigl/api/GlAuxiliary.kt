package com.gzozulin.minigl.api

import org.lwjgl.opengl.GL11

fun <T> glCheck(action: () -> T): T {
    val result = action.invoke()
    val errorCode = GL11.glGetError()
    if (errorCode != GL11.GL_NO_ERROR) {
        throw GlError(errorCode)
    }
    return result
}

fun glClear(color: col3 = col3().cyan()) {
    backend.glClearColor(color.x, color.y, color.z, 0f)
    backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT)
}

fun glClear(color: vec4) {
    backend.glClearColor(color.x, color.y, color.z, color.w)
    backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT)
}

fun glDepthTest(depthFunc: Int = backend.GL_LEQUAL, action: () -> Unit) {
    backend.glEnable(backend.GL_DEPTH_TEST)
    backend.glDepthFunc(depthFunc)
    action.invoke()
    backend.glDisable(backend.GL_DEPTH_TEST)
}

fun glCulling(frontFace: Int = backend.GL_CCW, action: () -> Unit) {
    backend.glEnable(backend.GL_CULL_FACE)
    backend.glFrontFace(frontFace)
    action.invoke()
    backend.glDisable(backend.GL_CULL_FACE)
}

fun glBlend(sfactor: Int = backend.GL_SRC_ALPHA, dfactor: Int = backend.GL_ONE_MINUS_SRC_ALPHA, action: () -> Unit) {
    backend.glBlendFunc(sfactor, dfactor)
    backend.glEnable(backend.GL_BLEND)
    action.invoke()
    backend.glDisable(backend.GL_BLEND)
}

private val prevViewport = IntArray(4)
fun glViewportBindPrev(callback: Callback) {
    backend.glGetIntegerv(backend.GL_VIEWPORT, prevViewport)
    callback.invoke()
    backend.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3])
}
