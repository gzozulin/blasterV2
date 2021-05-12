package com.gzozulin.minigl.techniques2

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api2.*
import com.gzozulin.minigl.api2.Expression
import com.gzozulin.minigl.api2.GlMesh
import com.gzozulin.minigl.api2.GlProgram
import com.gzozulin.minigl.api2.GlShader
import com.gzozulin.minigl.api2.constv4
import com.gzozulin.minigl.api2.unifm4
import com.gzozulin.minigl.assets2.libWavefrontCreate
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
        oFragColor = %COLOR%;
    }
""".trimIndent()

data class ShadingFlat(
    val matrix: Expression<mat4>, val color: Expression<col4> = constv4(vec4(1f))) {

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
        glDrawTriangles(mesh)
    }
}

private var mouseLook = false
private val camera = Camera()
private val controller = ControllerFirstPerson(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val group = libWavefrontCreate("models/pcjr/pcjr")
private val obj = group.objects.first()

private val uniformSampler = unift(obj.material.mapDiffuse!!)
private val namedTexCoords = namedv2("vTexCoord")
private val matrix = unifm4 { camera.calculateFullM() }
private val color = tex(namedTexCoords, uniformSampler)
private val shadingFlat = ShadingFlat(matrix, color)

private val window = GlWindow()

private fun useScene(callback: Callback) {
    glShadingFlatUse(shadingFlat) {
        glMeshUse(obj.mesh) {
            glTextureUse(obj.material.mapDiffuse!!) {
                callback.invoke()
            }
        }
    }
}

private fun drawScene() {
    glDepthTest {
        glTextureBind(obj.material.mapDiffuse!!) {
            glShadingFlatDraw(shadingFlat) {
                glShadingFlatInstance(shadingFlat, obj.mesh)
            }
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