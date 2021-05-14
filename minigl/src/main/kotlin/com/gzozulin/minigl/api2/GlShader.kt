package com.gzozulin.minigl.api2

typealias Callback = () -> Unit

data class GlShader(internal val type: Int, internal var source: String, internal var handle: Int? = null)

internal fun glShaderUpload(shader: GlShader) {
    check(shader.handle == null) { "Shader is already in use!" }
    shader.handle = backend.glCreateShader(shader.type)
    backend.glShaderSource(shader.handle!!, shader.source)
    backend.glCompileShader(shader.handle!!)
    if (backend.glGetShaderi(shader.handle!!, backend.GL_COMPILE_STATUS) == backend.GL_FALSE) {
        var withLineNo = ""
        shader.source.lines()
            .forEachIndexed { index, line -> withLineNo += ("$index $line\n") }
        error("Failed to compile shader:\n\n" +
                "$withLineNo\n\n" +
                "With reason:\n\n" +
                backend.glGetShaderInfoLog(shader.handle!!))
    }
}

internal fun glShaderDelete(shader: GlShader) {
    backend.glDeleteShader(shader.handle!!)
    shader.handle = null
}