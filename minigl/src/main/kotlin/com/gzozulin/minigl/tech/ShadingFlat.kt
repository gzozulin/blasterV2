package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.WasdInput

private val vertexSrc = """
    $VERT_SHADER_HEADER
    
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
    $FRAG_SHADER_HEADER
    
    in vec2 vTexCoord;

    layout (location = 0) out vec4 oFragColor;

    void main() {
        oFragColor = shadingFlat(%COLOR%);
    }
""".trimIndent()

data class ShadingFlat(
    val matrix: Expression<mat4> = constm4(mat4().orthoBox()),
    val color: Expression<col4> = constv4(vec4(1f))) {

    private val vertShader = GlShader(backend.GL_VERTEX_SHADER,
        glExprSubstitute(vertexSrc, mapOf("MATRIX" to matrix)))

    private val fragShader = GlShader(backend.GL_FRAGMENT_SHADER,
        glExprSubstitute(fragmentSrs, mapOf("COLOR" to color)))

    internal val program = GlProgram(vertShader, fragShader)
}

fun glShadingFlatUse(shadingFlat: ShadingFlat, callback: Callback) =
    glProgramUse(shadingFlat.program, callback)

fun glShadingFlatDraw(shadingFlat: ShadingFlat, callback: Callback) {
    glProgramBind(shadingFlat.program) {
        callback.invoke()
    }
}

fun glShadingFlatInstance(shadingFlat: ShadingFlat, mesh: GlMesh) {
    shadingFlat.matrix.submit(shadingFlat.program)
    shadingFlat.color.submit(shadingFlat.program)
    glMeshBind(mesh) {
        glDrawTriangles(shadingFlat.program, mesh)
    }
}

private val window = GlWindow()

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val mesh = glMeshCreateRect()

private val matrix = unifm4 { camera.calculateFullM() }
private val color = constv4(vec4(col3().red(), 1f))
private val shadingFlat = ShadingFlat(matrix, color)

private fun useScene(callback: Callback) {
    glShadingFlatUse(shadingFlat) {
        glMeshUse(mesh) {
           callback.invoke()
        }
    }
}

private fun drawScene() {
    glDepthTest {
        glShadingFlatDraw(shadingFlat) {
            glShadingFlatInstance(shadingFlat, mesh)
        }
    }
}

fun main() {
    window.create {
        window.buttonCallback = { button, pressed ->
            if (button == MouseButton.LEFT) {
                mouseLook = pressed
            }
        }
        window.deltaCallback = { delta ->
            if (mouseLook) {
                wasdInput.onCursorDelta(delta)
            }
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        useScene {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                drawScene()
            }
        }
    }
}