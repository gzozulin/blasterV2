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

    private var handle: Int = -1;

    private val uniformLocations = HashMap<GlUniform, Int>()
    private val unsatisfiedUniforms = HashSet<GlUniform>()

    private val arrayUniformLocations = HashMap<String, Int>()

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
        GlUniform.values().forEach {
            val location = backend.glGetUniformLocation(handle, it.label)
            if (location != -1) {
                uniformLocations[it] = location
            }
        }
        unsatisfiedUniforms.addAll(uniformLocations.keys)
    }

    fun setTexture(uniform: GlUniform, texture: GlTexture) {
        checkReady()
        texture.checkReady()
        setUniform(uniform, texture.unit)
        unsatisfiedUniforms.remove(uniform)
    }

    // TODO - ???
    fun setArbitraryUniform(name: String, value: mat4) {
        checkReady()
        value.get(bufferMat4)
        val location = backend.glGetUniformLocation(handle, name)
        backend.glUniformMatrix4fv(location, false, bufferMat4)
        unsatisfiedUniforms.clear()
    }

    fun setUniform(uniform: GlUniform, value: Matrix4f) {
        checkReady()
        value.get(bufferMat4)
        backend.glUniformMatrix4fv(uniformLocations[uniform]!!, false, bufferMat4)
        unsatisfiedUniforms.remove(uniform)
    }

    fun setUniform(uniform: GlUniform, value: Int) {
        checkReady()
        backend.glUniform1i(uniformLocations[uniform]!!, value)
        unsatisfiedUniforms.remove(uniform)
    }

    fun setUniform(uniform: GlUniform, value: Float) {
        checkReady()
        backend.glUniform1f(uniformLocations[uniform]!!, value)
        unsatisfiedUniforms.remove(uniform)
    }

    fun setUniform(uniform: GlUniform, value: Vector2f) {
        checkReady()
        value.get(bufferVec2)
        backend.glUniform2fv(uniformLocations[uniform]!!, bufferVec2)
        unsatisfiedUniforms.remove(uniform)
    }

    fun setUniform(uniform: GlUniform, value: Vector3f) {
        checkReady()
        value.get(bufferVec3)
        backend.glUniform3fv(uniformLocations[uniform]!!, bufferVec3)
        unsatisfiedUniforms.remove(uniform)
    }

    private fun arrayLocation(uniform: GlUniform, index: Int): Int {
        val label = uniform.label.format(index)
        var location: Int? = arrayUniformLocations[label]
        if (location == null) {
            location = backend.glGetUniformLocation(handle, label)
            arrayUniformLocations[label] = location
        }
        return location
    }

    fun setArrayUniform(uniform: GlUniform, index: Int, value: vec3) {
        checkReady()
        value.get(bufferVec3)
        backend.glUniform3fv(arrayLocation(uniform, index), bufferVec3)
    }

    fun setArrayTexture(uniform: GlUniform, index: Int, texture: GlTexture) {
        checkReady()
        texture.checkReady()
        backend.glUniform1i(arrayLocation(uniform, index), texture.unit)
    }

    fun draw(mode: Int = backend.GL_TRIANGLES, indicesCount: Int) {
        checkReady()
        checkUnsatisfiedUniforms()
        backend.glDrawElements(mode, indicesCount, backend.GL_UNSIGNED_INT, 0)
    }

    fun drawInstanced(mode: Int = backend.GL_TRIANGLES, indicesCount: Int, instances: Int) {
        checkReady()
        checkUnsatisfiedUniforms()
        backend.glDrawElementsInstanced(mode, indicesCount, backend.GL_UNSIGNED_INT, 0, instances)
    }

    fun draw(mesh: GlMesh) {
        draw(mode = backend.GL_TRIANGLES, indicesCount = mesh.indicesCount)
    }

    private fun checkUnsatisfiedUniforms() {
        check(unsatisfiedUniforms.isEmpty()) {
            var unsatisfied = ""
            unsatisfiedUniforms.forEach { unsatisfied += it.label + "\n" }
            "Uniforms aren't satisfied:\n$unsatisfied"
        }
    }
}