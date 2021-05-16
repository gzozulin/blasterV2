package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light
import com.gzozulin.minigl.scene.PointLight
import org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM
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

private val binding = IntArray(1)
private fun glProgramGetBound(): Int {
    backend.glGetIntegerv(GL_CURRENT_PROGRAM, binding)
    return binding[0]
}

private fun glProgramBindPrev(callback: Callback) {
    val prev = glProgramGetBound()
    callback.invoke()
    backend.glUseProgram(prev)
}

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

internal fun glProgramBind(program: GlProgram, callback: Callback) {
    check(program.handle != null) { "GlProgram is not used!" }
    glProgramBindPrev {
        backend.glUseProgram(program.handle!!)
        callback.invoke()
    }
}

internal fun glProgramCheckBound(program: GlProgram) {
    check(glProgramGetBound() == program.handle) { "GlProgram is not bound!" }
}

private fun glProgramUniformLocation(program: GlProgram, name: String): Int {
    check(program.handle != null) { "GlProgram is not used!" }
    val location = backend.glGetUniformLocation(program.handle!!, name)
    check(location >= 0) { "Location $name is not found in GlProgram" }
    return location
}

internal fun glProgramUniform(program: GlProgram, name: String, value: Float) {
    glProgramCheckBound(program)
    backend.glUniform1f(glProgramUniformLocation(program, name), value)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: Int) {
    glProgramCheckBound(program)
    backend.glUniform1i(glProgramUniformLocation(program, name), value)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec2i) {
    glProgramCheckBound(program)
    value.get(bufferVec2i)
    backend.glUniform2iv(glProgramUniformLocation(program, name), bufferVec2i)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec2) {
    glProgramCheckBound(program)
    value.get(bufferVec2)
    backend.glUniform2fv(glProgramUniformLocation(program, name), bufferVec2)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec3) {
    glProgramCheckBound(program)
    value.get(bufferVec3)
    backend.glUniform3fv(glProgramUniformLocation(program, name), bufferVec3)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: vec4) {
    glProgramCheckBound(program)
    value.get(bufferVec4)
    backend.glUniform4fv(glProgramUniformLocation(program, name), bufferVec4)
}

internal fun glProgramUniform(program: GlProgram, name: String, value: mat4) {
    glProgramCheckBound(program)
    value.get(bufferMat4)
    backend.glUniformMatrix4fv(glProgramUniformLocation(program, name), false, bufferMat4)
}

internal fun glProgramUniform(program: GlProgram, name: String, texture: GlTexture) {
    glProgramCheckBound(program)
    glTextureCheckBound(texture)
    backend.glUniform1i(glProgramUniformLocation(program, name), texture.unit!!)
}

internal fun glProgramArrayUniform(program: GlProgram, name: String, index: Int, value: Int) {
    glProgramCheckBound(program)
    val location = glProgramUniformLocation(program, name.format(index))
    backend.glUniform1i(location, value)
}

internal fun glProgramArrayUniform(program: GlProgram, name: String, index: Int, value: Float) {
    glProgramCheckBound(program)
    val location = glProgramUniformLocation(program, name.format(index))
    backend.glUniform1f(location, value)
}

internal fun glProgramArrayUniform(program: GlProgram, name: String, index: Int, value: vec3) {
    glProgramCheckBound(program)
    val location = glProgramUniformLocation(program, name.format(index))
    value.get(bufferVec3)
    backend.glUniform3fv(location, bufferVec3)
}

internal fun glProgramSubmitLights(program: GlProgram, lights: List<Light>) {
    check(lights.size <= MAX_LIGHTS) { "More lights than defined in shader!" }
    glProgramCheckBound(program)
    val sorted = lights.sortedBy { it is PointLight }
    var pointLightCnt = 0
    var dirLightCnt = 0
    sorted.forEachIndexed { index, light ->
        glProgramArrayUniform(program, "uLights[%d].vector",          index, light.vector)
        glProgramArrayUniform(program, "uLights[%d].color",           index, light.color)
        glProgramArrayUniform(program, "uLights[%d].attenConstant",   index, light.attenConstant)
        glProgramArrayUniform(program, "uLights[%d].attenLinear",     index, light.attenLinear)
        glProgramArrayUniform(program, "uLights[%d].attenQuadratic",  index, light.attenQuadratic)
        if (light is PointLight) {
            pointLightCnt++
        } else {
            dirLightCnt++
        }
    }
    glProgramUniform(program, "uLightsPointCnt", pointLightCnt)
    glProgramUniform(program, "uLightsDirCnt",   dirLightCnt)
}

internal fun glDrawTriangles(program: GlProgram, mesh: GlMesh) {
    glProgramCheckBound(program)
    glMeshCheckBound(mesh)
    backend.glDrawElements(backend.GL_TRIANGLES, mesh.indicesCnt, backend.GL_UNSIGNED_INT, 0)
}