package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

data class GlProgram(internal val vertexShader: GlShader, internal val fragmentShader: GlShader,
                     internal var handle: Int? = null)

private fun glProgramCreate(program: GlProgram) {
    program.handle = backend.glCreateProgram()
    check(program.vertexShader.type == backend.GL_VERTEX_SHADER)
    check(program.fragmentShader.type == backend.GL_FRAGMENT_SHADER)
    backend.glAttachShader(program.handle!!, program.vertexShader.handle!!)
    backend.glAttachShader(program.handle!!, program.fragmentShader.handle!!)
    backend.glLinkProgram(program.handle!!)
    val isLinked = backend.glGetProgrami(program.handle!!, backend.GL_LINK_STATUS)
    if (isLinked == backend.GL_FALSE) {
        error(backend.glGetProgramInfoLog(program.handle!!))
    }
}

internal fun glProgramUse(program: GlProgram, callback: Callback) {
    check(program.handle == null) { "GlProgram is already in use!" }
    glShaderUpload(program.vertexShader)
    glShaderUpload(program.fragmentShader)
    glProgramCreate(program)
    callback.invoke()
    backend.glDeleteProgram(program.handle!!)
    program.handle = null
    glShaderDelete(program.vertexShader)
    glShaderDelete(program.fragmentShader)
}

private var currBinding: Int? = null
internal fun glProgramBind(program: GlProgram, callback: Callback) {
    check(program.handle != null) { "GlProgram is not used!" }
    val prev = currBinding
    backend.glUseProgram(program.handle!!)
    currBinding = program.handle
    callback.invoke()
    backend.glUseProgram(prev ?: 0)
    currBinding = prev
}

internal fun glProgramCheckBound() {
    check(currBinding != null) { "No GlProgram is bound!" }
}

private fun glProgramUniformLocation(program: GlProgram, name: String): Int {
    check(program.handle != null) { "GlProgram is not used!" }
    val location = backend.glGetUniformLocation(program.handle!!, name)
    check(location >= 0) { "Location $name is not found in GlProgram" }
    return location
}

internal fun glProgramUniform(program: GlProgram, name: String, value: Float) {
    backend.glUniform1f(glProgramUniformLocation(program, name), value)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: Int) {
    backend.glUniform1i(glProgramUniformLocation(program, name), value)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec2i) {
    value.get(bufferVec2i)
    backend.glUniform2iv(glProgramUniformLocation(program, name), bufferVec2i)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec2) {
    value.get(bufferVec2)
    backend.glUniform2fv(glProgramUniformLocation(program, name), bufferVec2)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec3) {
    value.get(bufferVec3)
    backend.glUniform3fv(glProgramUniformLocation(program, name), bufferVec3)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec4) {
    value.get(bufferVec4)
    backend.glUniform4fv(glProgramUniformLocation(program, name), bufferVec4)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: mat4) {
    value.get(bufferMat4)
    backend.glUniformMatrix4fv(glProgramUniformLocation(program, name), false, bufferMat4)
}

internal fun glProgramUniform(program: GlProgram, name: String, texture: GlTexture) {
    glTextureCheckBound(texture)
    backend.glUniform1i(glProgramUniformLocation(program, name), texture.unit!!)
}

internal fun glProgramArrayUniform(program: GlProgram, name: String, index: Int, value: Int) {
    val location = glProgramUniformLocation(program, name.format(index))
    backend.glUniform1i(location, value)
}

internal fun glProgramArrayUniform(program: GlProgram, name: String, index: Int, value: Float) {
    val location = glProgramUniformLocation(program, name.format(index))
    backend.glUniform1f(location, value)
}

internal fun glProgramArrayUniform(program: GlProgram, name: String, index: Int, value: vec3) {
    val location = glProgramUniformLocation(program, name.format(index))
    value.get(bufferVec3)
    backend.glUniform3fv(location, bufferVec3)
}

internal fun glDrawTriangles(mesh: GlMesh) {
    glProgramCheckBound()
    glMeshCheckBound(mesh)
    backend.glDrawElements(backend.GL_TRIANGLES, mesh.indicesCnt, backend.GL_UNSIGNED_INT, 0)
}