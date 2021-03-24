package com.gzozulin.minigl.api

import org.lwjgl.opengl.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

val backend = GlBackend()

class GlBackend {
    val GL_TRUE:               Int get() = GL11.GL_TRUE
    val GL_FALSE:              Int get() = GL11.GL_FALSE
    val GL_DEPTH_TEST:         Int get() = GL11.GL_DEPTH_TEST
    val GL_COLOR_BUFFER_BIT:   Int get() = GL11.GL_COLOR_BUFFER_BIT
    val GL_DEPTH_BUFFER_BIT:   Int get() = GL11.GL_DEPTH_BUFFER_BIT
    val GL_CCW:                Int get() = GL11.GL_CCW
    val GL_CW:                 Int get() = GL11.GL_CW
    val GL_CULL_FACE:          Int get() = GL11.GL_CULL_FACE
    val GL_ARRAY_BUFFER:       Int get() = GL15.GL_ARRAY_BUFFER
    val GL_ELEMENT_ARRAY_BUFFER: Int get() = GL15.GL_ELEMENT_ARRAY_BUFFER
    val GL_STATIC_DRAW:        Int get() = GL15.GL_STATIC_DRAW
    val GL_STREAM_DRAW:        Int get() = GL15.GL_STREAM_DRAW
    val GL_FRAMEBUFFER:        Int get() = ARBFramebufferObject.GL_FRAMEBUFFER
    val GL_FRAMEBUFFER_COMPLETE: Int get() = ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE
    val GL_FLOAT:              Int get() = GL11.GL_FLOAT
    val GL_INT:                Int get() = GL11.GL_INT
    val GL_UNSIGNED_INT:       Int get() = GL11.GL_UNSIGNED_INT
    val GL_UNSIGNED_BYTE:      Int get() = GL11.GL_UNSIGNED_BYTE
    val GL_BYTE:               Int get() = GL11.GL_BYTE
    val GL_TRIANGLES:          Int get() = GL11.GL_TRIANGLES
    val GL_LINES:              Int get() = GL11.GL_LINES
    val GL_MULTISAMPLE:        Int get() = ARBMultisample.GL_MULTISAMPLE_ARB
    val GL_POINTS:             Int get() = GL11.GL_POINTS
    val GL_VERTEX_SHADER:      Int get() = GL20.GL_VERTEX_SHADER
    val GL_FRAGMENT_SHADER:    Int get() = GL20.GL_FRAGMENT_SHADER
    val GL_COMPILE_STATUS:     Int get() = GL20.GL_COMPILE_STATUS
    val GL_LINK_STATUS:        Int get() = GL20.GL_LINK_STATUS
    val GL_ACTIVE_UNIFORMS:    Int get() = GL20.GL_ACTIVE_UNIFORMS
    val GL_RENDERBUFFER:       Int get() = GL30.GL_RENDERBUFFER
    val GL_DEPTH_COMPONENT24:  Int get() = GL14.GL_DEPTH_COMPONENT24
    val GL_RED:                Int get() = GL11.GL_RED
    val GL_R32UI:              Int get() = ARBTextureRG.GL_R32UI
    val GL_R32I:               Int get() = ARBTextureRG.GL_R32I
    val GL_R8UI:               Int get() = ARBTextureRG.GL_R8UI
    val GL_RED_INTEGER:        Int get() = GL30.GL_RED_INTEGER
    val GL_RGB:                Int get() = GL11.GL_RGB
    val GL_RGBA:               Int get() = GL11.GL_RGBA
    val GL_TEXTURE_2D:         Int get() = GL11.GL_TEXTURE_2D
    val GL_TEXTURE_CUBE_MAP:   Int get() = GL13.GL_TEXTURE_CUBE_MAP
    val GL_TEXTURE_CUBE_MAP_POSITIVE_X:   Int get() = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X
    val GL_TEXTURE0:           Int get() = GL13.GL_TEXTURE0
    val GL_TEXTURE_MIN_FILTER: Int get() = GL11.GL_TEXTURE_MIN_FILTER
    val GL_TEXTURE_MAG_FILTER: Int get() = GL11.GL_TEXTURE_MAG_FILTER
    val GL_NEAREST:            Int get() = GL11.GL_NEAREST
    val GL_LINEAR:             Int get() = GL11.GL_LINEAR
    val GL_NEAREST_MIPMAP_LINEAR: Int get() = GL11.GL_NEAREST_MIPMAP_LINEAR
    val GL_TEXTURE_WRAP_S:     Int get() = GL11.GL_TEXTURE_WRAP_S
    val GL_TEXTURE_WRAP_T:     Int get() = GL11.GL_TEXTURE_WRAP_T
    val GL_TEXTURE_WRAP_R:     Int get() = GL12.GL_TEXTURE_WRAP_R
    val GL_TEXTURE_MAX_LEVEL:     Int get() = GL12.GL_TEXTURE_MAX_LEVEL
    val GL_REPEAT:             Int get() = GL11.GL_REPEAT
    val GL_CLAMP_TO_EDGE:      Int get() = GL12.GL_CLAMP_TO_EDGE
    val GL_RGBA16F:            Int get() = GL30.GL_RGBA16F
    val GL_RGB16F:             Int get() = GL30.GL_RGB16F
    val GL_COLOR_ATTACHMENT0:  Int get() = GL30.GL_COLOR_ATTACHMENT0
    val GL_COLOR_ATTACHMENT1:  Int get() = GL30.GL_COLOR_ATTACHMENT1
    val GL_COLOR_ATTACHMENT2:  Int get() = GL30.GL_COLOR_ATTACHMENT2
    val GL_COLOR_ATTACHMENT3:  Int get() = GL30.GL_COLOR_ATTACHMENT3
    val GL_COLOR_ATTACHMENT4:  Int get() = GL30.GL_COLOR_ATTACHMENT4
    val GL_COLOR_ATTACHMENT5:  Int get() = GL30.GL_COLOR_ATTACHMENT5
    val GL_DEPTH_ATTACHMENT:   Int get() = GL30.GL_DEPTH_ATTACHMENT
    val GL_MAP_WRITE_BIT:      Int get() = GL30.GL_MAP_WRITE_BIT
    val GL_MAP_UNSYNCHRONIZED_BIT: Int get() = GL30.GL_MAP_UNSYNCHRONIZED_BIT
    val GL_READ_ONLY:          Int get() = GL15.GL_READ_ONLY
    val GL_READ_WRITE:         Int get() = GL15.GL_READ_WRITE
    val GL_WRITE_ONLY:         Int get() = GL15.GL_READ_WRITE
    val GL_BLEND:              Int get() = GL11.GL_BLEND
    val GL_ONE_MINUS_SRC_ALPHA: Int get() = GL11.GL_ONE_MINUS_SRC_ALPHA
    val GL_SRC_ALPHA:          Int get() = GL11.GL_SRC_ALPHA
    val GL_LEQUAL:             Int get() = GL11.GL_LEQUAL
    val GL_GEQUAL:             Int get() = GL11.GL_GEQUAL
    val GL_LESS:               Int get() = GL11.GL_LESS
    val GL_MODELVIEW:          Int get() = GL11.GL_MODELVIEW
    val GL_PROJECTION:         Int get() = GL11.GL_PROJECTION
    val GL_VIEWPORT:           Int get() = GL11.GL_VIEWPORT

    fun glEnable(cap: Int) = glCheck { GL11.glEnable(cap) }
    fun glDisable(cap: Int) = glCheck { GL11.glDisable(cap) }
    fun glDepthFunc(func: Int) = glCheck { GL11.glDepthFunc(func) }
    fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)  = glCheck { GL11.glClearColor(red, green, blue, alpha) }
    fun glViewport(x: Int, y: Int, width: Int, height: Int) = glCheck { GL11.glViewport(x, y, width, height) }
    fun glFrontFace(mode: Int) = glCheck { GL11.glFrontFace(mode) }
    fun glClear(mask: Int) = glCheck { GL11.glClear(mask) }
    fun glGenBuffers(): Int = glCheck { GL15.glGenBuffers() }
    fun glDeleteBuffers(handle: Int) = glCheck { GL15.glDeleteBuffers(handle) }
    fun glBindBuffer(target: Int, buffer: Int) = glCheck { GL15.glBindBuffer(target, buffer) }
    fun glBufferData(target: Int, data: ByteBuffer, usage: Int) = glCheck { GL15.glBufferData(target, data, usage) }
    fun glGenFramebuffers() = glCheck { ARBFramebufferObject.glGenFramebuffers() }
    fun glDeleteFramebuffers(handle: Int) = glCheck { ARBFramebufferObject.glDeleteFramebuffers(handle) }
    fun glBindFramebuffer(target: Int, framebuffer: Int) = glCheck { ARBFramebufferObject.glBindFramebuffer(target, framebuffer) }
    fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) = glCheck { ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level) }
    fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) = glCheck { ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer) }
    fun glDrawBuffers(bufs: IntArray) {
        glCheck {
            // todo: remove allocation
            val buffer = ByteBuffer.allocateDirect(bufs.size * 4).order(ByteOrder.nativeOrder())
                .asIntBuffer()
            buffer.put(bufs).position(0)
            GL20.glDrawBuffers(buffer)
        }
    }
    fun glCheckFramebufferStatus(target: Int): Int = glCheck { ARBFramebufferObject.glCheckFramebufferStatus(target) }
    fun glEnableVertexAttribArray(index: Int) = glCheck { GL20.glEnableVertexAttribArray(index) }
    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long) = glCheck { GL20.glVertexAttribPointer(indx, size, type, normalized, stride, offset) }
    fun glGenVertexArrays() = glCheck { GL30.glGenVertexArrays() }
    fun glDeleteVertexArrays(handle: Int) = glCheck { GL30.glDeleteVertexArrays(handle) }
    fun glBindVertexArray(handle: Int) = glCheck { GL30.glBindVertexArray(handle) }
    fun glVertexAttribDivisor(indx: Int, divisor: Int) = glCheck { GL33.glVertexAttribDivisor(indx, divisor) }
    fun glDisableVertexAttribArray(index: Int) = glCheck { GL20.glDisableVertexAttribArray(index) }
    fun glDrawElements(mode: Int, count: Int, type: Int, offset: Long) = glCheck { GL11.glDrawElements(mode, count, type, offset) }
    fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, offset: Long, instances: Int) = glCheck { GL31.glDrawElementsInstanced(mode, count, type, offset, instances) }
    fun glCreateShader(type: Int): Int = glCheck { GL20.glCreateShader(type) }
    fun glShaderSource(shader: Int, string: String) = glCheck { GL20.glShaderSource(shader, string) }
    fun glCompileShader(shader: Int) = glCheck { GL20.glCompileShader(shader) }
    fun glGetActiveUniform(program: Int, index: Int, size: IntBuffer, type: IntBuffer) = glCheck { GL20.glGetActiveUniform(program, index, size, type) }
    fun glGetShaderi(shader: Int, pname: Int): Int = glCheck { GL20.glGetShaderi(shader, pname) }
    fun glGetShaderInfoLog(shader: Int) = glCheck { GL20.glGetShaderInfoLog(shader) }
    fun glDeleteShader(shader: Int) = glCheck { GL20.glDeleteShader(shader) }
    fun glCreateProgram() = glCheck { GL20.glCreateProgram() }
    fun glAttachShader(program: Int, shader: Int) = glCheck { GL20.glAttachShader(program, shader) }
    fun glLinkProgram(program: Int) = glCheck { GL20.glLinkProgram(program) }
    fun glGetProgrami(program: Int, pname: Int) = glCheck { GL20.glGetProgrami(program, pname) }
    fun glGetProgramInfoLog(program: Int) = glCheck { GL20.glGetProgramInfoLog(program) }
    fun glGetUniformLocation(program: Int, name: String) = glCheck { GL20.glGetUniformLocation(program, name) }
    fun glDeleteProgram(program: Int) = glCheck { GL20.glDeleteProgram(program) }
    fun glUseProgram(program: Int) = glCheck { GL20.glUseProgram(program) }
    fun glGetIntegerv(pname: Int, value: IntArray) = glCheck { GL11.glGetIntegerv(pname, value) }
    fun glUniform1i(location: Int, x: Int) = glCheck { GL20.glUniform1i(location, x) }
    fun glUniform1f(location: Int, x: Float) = glCheck { GL20.glUniform1f(location, x) }
    fun glUniform2fv(location: Int, v: FloatBuffer) = glCheck { GL20.glUniform2fv(location, v) }
    fun glUniform2iv(location: Int, v: IntBuffer) = glCheck { GL20.glUniform2iv(location, v) }
    fun glUniform3fv(location: Int, v: FloatBuffer) = glCheck { GL20.glUniform3fv(location, v) }
    fun glUniform4fv(location: Int, v: FloatBuffer) = glCheck { GL20.glUniform4fv(location, v) }
    fun glUniformMatrix4fv(location: Int, transpose: Boolean, value: FloatBuffer) =
        glCheck { GL20.glUniformMatrix4fv(location, transpose, value) }
    fun glGenRenderbuffers() = glCheck { GL30.glGenRenderbuffers() }
    fun glDeleteRenderBuffers(handle: Int) = glCheck { GL30.glDeleteRenderbuffers(handle) }
    fun glBindRenderbuffer(target: Int, renderbuffer: Int) = glCheck { GL30.glBindRenderbuffer(target, renderbuffer) }
    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) =
        glCheck { GL30.glRenderbufferStorage(target, internalformat, width, height) }
    fun glGenTextures() = glCheck { GL11.glGenTextures() }
    fun glDeleteTextures(handle: Int) = glCheck { GL11.glDeleteTextures(handle) }
    fun glBindTexture(target: Int, texture: Int) = glCheck { GL11.glBindTexture(target, texture) }
    fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: ByteBuffer?) = glCheck { GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels) }
    fun glGenerateMipmap(target: Int) = glCheck { GL30.glGenerateMipmap(target) }
    fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: ByteBuffer) =
        glCheck { GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels) }
    fun glTexParameteri(target: Int, pname: Int, param: Int) = glCheck { GL11.glTexParameteri(target, pname, param) }
    fun glActiveTexture(texture: Int) = glCheck { GL13.glActiveTexture(texture) }
    fun glMapBuffer(target: Int, access: Int, oldBuffer: ByteBuffer) = glCheck { GL15.glMapBuffer(target, access, oldBuffer) }!!
    fun glUnapBuffer(target: Int) = glCheck { GL15.glUnmapBuffer(target) }
    fun glMapBufferRange(target: Int, offset: Long, length: Long, access: Int, oldBuffer: ByteBuffer): ByteBuffer = glCheck { GL30.glMapBufferRange(target, offset, length, access, oldBuffer) }!!
    fun glBegin(mode: Int) = glCheck { GL11.glBegin(mode) }
    fun glEnd() = glCheck { GL11.glEnd() }
    fun glColor3f(rgb: col3) = glCheck { GL11.glColor3f(rgb.x, rgb.y, rgb.z) }
    fun glColor3f(r: Float, g: Float, b: Float) = glCheck { GL11.glColor3f(r, g, b) }
    fun glVertex3f(x: Float, y: Float, z: Float) = glCheck { GL11.glVertex3f(x, y, z) }
    fun glVertex3f(xyz: vec3) = glCheck { GL11.glVertex3f(xyz.x, xyz.y, xyz.z) }
    fun glBlendFunc(sfactor: Int, dfactor: Int) = glCheck { GL11.glBlendFunc(sfactor, dfactor) }
    fun glMatrixMode(mode: Int) = glCheck { GL11.glMatrixMode(mode) }
    fun glLoadMatrix(matrix: FloatBuffer) = glCheck { GL11.glLoadMatrixf(matrix) }
}
