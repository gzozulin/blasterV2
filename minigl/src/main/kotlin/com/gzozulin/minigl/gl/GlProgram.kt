package com.gzozulin.minigl.gl

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    override fun use() {
        super.use()
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

    override fun release() {
        backend.glDeleteProgram(handle)
        super.release()
    }

    override fun bind() {
        super.bind()
        backend.glUseProgram(handle)
    }

    override fun unbind() {
        backend.glUseProgram(0)
        super.unbind()
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

    private fun satisfyUniformLocation(uniform: String): Int {
        val location = uniformLocations[uniform]
        checkNotNull(location) { "Location for uniform $uniform not found!" }
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

    fun setUniform(uniform: String, value: Matrix4f) {
        checkReady()
        value.get(bufferMat4)
        backend.glUniformMatrix4fv(satisfyUniformLocation(uniform), false, bufferMat4)
    }

    fun setUniform(uniform: String, value: Int) {
        checkReady()
        backend.glUniform1i(satisfyUniformLocation(uniform), value)
    }

    fun setUniform(uniform: String, value: Float) {
        checkReady()
        backend.glUniform1f(satisfyUniformLocation(uniform), value)
    }

    fun setUniform(uniform: String, value: Vector2f) {
        checkReady()
        value.get(bufferVec2)
        backend.glUniform2fv(satisfyUniformLocation(uniform), bufferVec2)
    }

    fun setUniform(uniform: String, value: Vector3f) {
        checkReady()
        value.get(bufferVec3)
        backend.glUniform3fv(satisfyUniformLocation(uniform), bufferVec3)
    }

    fun setUniform(name: String, value: vec4) {
        checkReady()
        value.get(bufferVec4)
        backend.glUniform4fv(satisfyUniformLocation(name), bufferVec4)
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
        draw(mode = backend.GL_TRIANGLES, indicesCount = mesh.indicesCount)
    }
}