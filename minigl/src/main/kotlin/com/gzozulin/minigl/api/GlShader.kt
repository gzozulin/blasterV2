package com.gzozulin.minigl.api

enum class GlShaderType(val type: Int) {
    VERTEX_SHADER(backend.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(backend.GL_FRAGMENT_SHADER)
}

class GlShader(val type: GlShaderType, private val source: String): GlResource() {
    private var handle: Int = -1

    override fun onUse() {
        handle = backend.glCreateShader(type.type)
        backend.glShaderSource(handle, source)
        backend.glCompileShader(handle)
        val isCompiled = backend.glGetShaderi(handle, backend.GL_COMPILE_STATUS)
        if (isCompiled == backend.GL_FALSE) {
            var withLineNo = ""
            source.lines().forEachIndexed { index, line -> withLineNo += ("$index $line\n") }
            val reason = "Failed to compile shader:\n\n" +
                    "$withLineNo\n\n" +
                    "With reason:\n\n" +
                    backend.glGetShaderInfoLog(handle)
            error(reason)
        }
    }

    override fun onRelease() {
        backend.glDeleteShader(handle)
    }

    fun accessHandle(): Int {
        checkReady()
        return handle
    }
}