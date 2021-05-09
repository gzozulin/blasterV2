package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend

typealias Callback = () -> Unit

data class GlShader(internal val type: Int, internal var source: String, internal var handle: Int? = null)

private fun glCreateShader(shader: GlShader) {
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

internal fun glUseShader(shader: GlShader, callback: Callback) {
    check(shader.handle == null) { "Shader is already in use!" }
    glCreateShader(shader)
    callback.invoke()
    backend.glDeleteShader(shader.handle!!)
    shader.handle = null
}

private val testShader = GlShader(1, """
    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;
    
    out vec2 vTexCoord;
    
    void main() {
        vTexCoord = aTexCoord;
        gl_Position = vec4(aPosition, 1.0);
    }
""".trimIndent())

internal fun testGlShader() {
    glUseShader(testShader) {
        println("testsing")
    }
}