package com.gzozulin.minigl.api

import org.lwjgl.opengl.GL30

data class GlRenderBuffer(val width: Int, val height: Int,
                          val target: Int = backend.GL_RENDERBUFFER, val component: Int = backend.GL_DEPTH_COMPONENT32,
                          var handle: Int? = null)

private val binding = IntArray(1)
private fun glRenderBufferGetBound(renderBuffer: GlRenderBuffer): Int {
    when (renderBuffer.target) {
        backend.GL_RENDERBUFFER -> backend.glGetIntegerv(GL30.GL_RENDERBUFFER_BINDING, binding)
        else -> error("Unknown target for GlRenderBuffer!")
    }
    return binding[0]
}

private fun glRenderBufferBindPrev(renderBuffer: GlRenderBuffer, callback: Callback) {
    val prev = glRenderBufferGetBound(renderBuffer)
    callback.invoke()
    backend.glBindRenderbuffer(renderBuffer.target, prev)
}

fun glRenderBufferUse(renderBuffer: GlRenderBuffer, callback: Callback) {
    check(renderBuffer.handle == null) { "GlRenderBuffer is already in use!" }
    try {
        renderBuffer.handle = backend.glGenRenderbuffers()
        glRenderBufferBindPrev(renderBuffer) {
            backend.glBindRenderbuffer(renderBuffer.target, renderBuffer.handle!!)
            backend.glRenderbufferStorage(renderBuffer.target, renderBuffer.component, renderBuffer.width, renderBuffer.height)
        }
        callback.invoke()
    } finally {
        backend.glDeleteRenderBuffers(renderBuffer.handle!!)
        renderBuffer.handle = null
    }
}

fun glRenderBufferBind(renderBuffer: GlRenderBuffer, callback: Callback) {
    check(renderBuffer.handle != null) { "GlRenderBuffer is not used!" }
    glRenderBufferBindPrev(renderBuffer) {
        backend.glBindRenderbuffer(renderBuffer.target, renderBuffer.handle!!)
        callback.invoke()
    }
}

fun glRenderBufferCheckBound(renderBuffer: GlRenderBuffer) {
    check(renderBuffer.handle != null) { "GlRenderBuffer is not used!" }
    check(glRenderBufferGetBound(renderBuffer) == renderBuffer.handle) { "GlRenderBuffer is not bound!" }
}