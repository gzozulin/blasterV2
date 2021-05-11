package com.gzozulin.minigl.assembly

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

fun glFlatTechniqueUse(flatTechnique: FlatTechnique2, callback: Callback) =
    glProgramUse(flatTechnique.program, callback)

fun glFlatTechniqueBind(flatTechnique: FlatTechnique2, callback: Callback) =
    glProgramBind(flatTechnique.program, callback)

fun glFlatTechniqueDraw(flatTechnique: FlatTechnique2, mesh: GlMesh) {
    flatTechnique.matrix.submit(flatTechnique.program)
    flatTechnique.color.submit(flatTechnique.program)
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
private val flatTechnique = FlatTechnique2(matrix, color)

private val window = GlWindow()

private fun useScene(callback: Callback) {
    glFlatTechniqueUse(flatTechnique) {
        glMeshUse(obj.mesh) {
            glTextureUse(obj.material.mapDiffuse!!) {
                callback.invoke()
            }
        }
    }
}

private fun drawScene() {
    glDepthTest {
        glFlatTechniqueBind(flatTechnique) {
            glMeshBind(obj.mesh) {
                glTextureBind(obj.material.mapDiffuse!!) {
                    glFlatTechniqueDraw(flatTechnique, obj.mesh)
                }
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