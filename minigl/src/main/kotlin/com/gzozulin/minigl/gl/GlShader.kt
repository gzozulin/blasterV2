package com.gzozulin.minigl.gl

enum class GlShaderType(val type: Int) {
    VERTEX_SHADER(backend.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(backend.GL_FRAGMENT_SHADER)
}

class GlShader(val type: GlShaderType, private val source: String): GlResource() {
    private var internalHandle: Int = -1
    val handle: Int
    get() {
        checkReady()
        return internalHandle
    }

    override fun use() {
        super.use()
        internalHandle = backend.glCreateShader(type.type)
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

    override fun release() {
        backend.glDeleteShader(handle)
        super.release()
    }
}