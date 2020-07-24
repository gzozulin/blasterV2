package com.gzozulin.minigl.scene

import com.gzozulin.minigl.gl.mat4
import java.util.*

class MatrixStack {
    private val matrixStack = Stack<mat4>()

    init {
        matrixStack.push(mat4().identity())
    }

    fun pushMatrix(m: mat4, action: () -> Unit) {
        matrixStack.push(mat4().set(peekMatrix()).mul(m))
        action.invoke()
        matrixStack.pop()
    }

    fun peekMatrix(): mat4 = matrixStack.peek()
}