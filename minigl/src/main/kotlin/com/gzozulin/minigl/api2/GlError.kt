package com.gzozulin.minigl.api2

import java.lang.Error

class GlError(private val errorCode: Int) : Error() {
    override fun toString(): String {
        val msg = when (errorCode) {
            0x0   -> "GL_NO_ERROR"
            0x500 -> "GL_INVALID_ENUM"
            0x501 -> "GL_INVALID_VALUE"
            0x502 -> "GL_INVALID_OPERATION"
            0x503 -> "GL_STACK_OVERFLOW"
            0x504 -> "GL_STACK_UNDERFLOW"
            0x505 -> "GL_OUT_OF_MEMORY"
            else -> throw TODO("Unknown error code: $errorCode")
        }
        return "OpenGL error: $msg($errorCode)"
    }
}