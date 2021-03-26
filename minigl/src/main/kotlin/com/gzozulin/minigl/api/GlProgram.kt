package com.gzozulin.minigl.api

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.HashMap

private var complainAboutUniforms = true

private val bufferVec2i = ByteBuffer.allocateDirect(2 * 4)
    .order(ByteOrder.nativeOrder())
    .asIntBuffer()

private val bufferVec2 = ByteBuffer.allocateDirect(2 * 4)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()

private val bufferVec3 = ByteBuffer.allocateDirect(3 * 4)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()

private val bufferVec4 = ByteBuffer.allocateDirect(4 * 4)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()

private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()

private val bindStack = Stack<Int>()

class GlProgram(
    private val vertexShader: GlShader,
    private val fragmentShader: GlShader
) : GlBindable() {

    init {
        addChildren(vertexShader, fragmentShader)
    }

    private var handle: Int = -1

    private val uniformLocations = HashMap<String, Int>()
    private val unsatisfiedUniforms = mutableListOf<String>()

    override fun onUse() {
        handle = backend.glCreateProgram()
        check(vertexShader.type == GlShaderType.VERTEX_SHADER)
        check(fragmentShader.type == GlShaderType.FRAGMENT_SHADER)
        backend.glAttachShader(handle, vertexShader.accessHandle())
        backend.glAttachShader(handle, fragmentShader.accessHandle())
        backend.glLinkProgram(handle)
        val isLinked = backend.glGetProgrami(handle, backend.GL_LINK_STATUS)
        if (isLinked == backend.GL_FALSE) {
            error(backend.glGetProgramInfoLog(handle))
        }
        cacheUniforms()
    }

    override fun onRelease() {
        backend.glDeleteProgram(handle)
    }

    override fun onBound() {
        backend.glUseProgram(handle)
        bindStack.push(handle)
    }

    override fun onUnbound() {
        bindStack.pop()
        if (bindStack.empty()) {
            backend.glUseProgram(0)
        } else {
            backend.glUseProgram(bindStack.peek())
        }
    }

    private fun cacheUniforms() {
        val count = backend.glGetProgrami(handle, backend.GL_ACTIVE_UNIFORMS)
        val size = ByteBuffer.allocateDirect(4).asIntBuffer()
        val type = ByteBuffer.allocateDirect(4).asIntBuffer()
        for (i in 0 until count) {
            val uniform = backend.glGetActiveUniform(handle, i, size, type)
            val location = backend.glGetUniformLocation(handle, uniform)
            uniformLocations[uniform] = location
        }
        unsatisfiedUniforms.addAll(uniformLocations.keys)
    }

    private fun satisfyUniformLocation(uniform: String): Int? {
        val location = uniformLocations[uniform]
        if (location == null) {
            if (complainAboutUniforms) {
                println(
                    "Uniform location $uniform is not found! " +
                            "Removed by GLSL compiler?"
                )
            }
            return null
        }
        unsatisfiedUniforms.remove(uniform)
        return location
    }

    private fun getArrayUniformLocation(uniform: String, index: Int): Int {
        val location = backend.glGetUniformLocation(handle, uniform.format(index))
        checkNotNull(location != -1) { "Location for uniform $uniform not found!" }
        return location
    }

    fun setTexture(uniform: String, texture: GlTexture) {
        checkReady()
        setUniform(uniform, texture.accessUnit())
    }

    fun setUniform(uniform: String, value: mat4) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            value.get(bufferMat4)
            backend.glUniformMatrix4fv(it, false, bufferMat4)
        }
    }

    fun setUniform(uniform: String, value: Int) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            backend.glUniform1i(it, value)
        }
    }

    fun setUniform(uniform: String, value: Float) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            backend.glUniform1f(it, value)
        }
    }

    fun setUniform(uniform: String, value: vec2) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            value.get(bufferVec2)
            backend.glUniform2fv(it, bufferVec2)
        }
    }

    fun setUniform(uniform: String, value: vec2i) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            value.get(bufferVec2i)
            backend.glUniform2iv(it, bufferVec2i)
        }
    }

    fun setUniform(uniform: String, value: vec3) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            value.get(bufferVec3)
            backend.glUniform3fv(it, bufferVec3)
        }
    }

    fun setUniform(uniform: String, value: vec4) {
        checkReady()
        satisfyUniformLocation(uniform)?.let {
            value.get(bufferVec4)
            backend.glUniform4fv(it, bufferVec4)
        }
    }

    fun setArrayUniform(uniform: String, index: Int, value: vec3) {
        checkReady()
        value.get(bufferVec3)
        backend.glUniform3fv(getArrayUniformLocation(uniform, index), bufferVec3)
    }

    fun setArrayTexture(uniform: String, index: Int, texture: GlTexture) {
        checkReady()
        backend.glUniform1i(getArrayUniformLocation(uniform, index), texture.accessUnit())
    }

    fun draw(mode: Int = backend.GL_TRIANGLES, indicesCount: Int) {
        checkReady()
        backend.glDrawElements(mode, indicesCount, backend.GL_UNSIGNED_INT, 0)
    }

    fun drawInstanced(mode: Int = backend.GL_TRIANGLES, indicesCount: Int, instances: Int) {
        checkReady()
        backend.glDrawElementsInstanced(mode, indicesCount, backend.GL_UNSIGNED_INT, 0, instances)
    }

    fun draw(mesh: GlMesh) {
        checkReady()
        mesh.checkReady()
        draw(mode = backend.GL_TRIANGLES, indicesCount = mesh.indicesCount)
    }

    companion object {
        fun stopComplaining() {
            complainAboutUniforms = false
        }
    }
}