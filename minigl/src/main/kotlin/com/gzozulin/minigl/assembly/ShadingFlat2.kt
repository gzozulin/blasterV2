package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api2.*
import com.gzozulin.minigl.api2.Expression
import com.gzozulin.minigl.api2.GlProgram
import com.gzozulin.minigl.api2.GlShader
import com.gzozulin.minigl.api2.constv4

private val vertexSrc = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_VERT
    $CONST_UNIF
    
    layout (location = 0) in vec3 aPosition;
    layout (location = 1) in vec2 aTexCoord;
    layout (location = 2) in vec3 aNormal;

    out vec2 vTexCoord;

    void main() {
        vTexCoord = aTexCoord;
        gl_Position = %MATRIX% * vec4(aPosition, 1.0);
    }
    
""".trimIndent()

private val fragmentSrs = """
    $VERSION
    $PRECISION_HIGH
    $DECLARATIONS_FRAG
    $CONST_UNIF
    
    in vec2 vTexCoord;

    layout (location = 0) out vec4 oFragColor;

    void main() {
        oFragColor = %COLOR%;
    }
""".trimIndent()

data class FlatTechnique2(
    val matrix: Expression<mat4>, val color: Expression<col4> = constv4(vec4(1f))) {

    private val vertShader = GlShader(backend.GL_VERTEX_SHADER,
        glExprSubstitute(vertexSrc, mapOf("MATRIX" to matrix)))

    private val fragShader = GlShader(backend.GL_FRAGMENT_SHADER,
        glExprSubstitute(fragmentSrs, mapOf("COLOR" to color)))

    internal val program = GlProgram(vertShader, fragShader)
}

fun glUseFlatTechnique(flatTechnique: FlatTechnique2, callback: Callback) =
    glUseProgram(flatTechnique.program, callback)

fun glBindFlatTechnique(flatTechnique: FlatTechnique2, callback: Callback) =
    glBindProgram(flatTechnique.program, callback)

private val window = GlWindow()

fun main() {
    window.create {
        window.show {

        }
    }
}