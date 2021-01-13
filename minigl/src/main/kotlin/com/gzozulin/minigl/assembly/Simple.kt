package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique

private const val VERSION = "#version 300 es"
private const val PRECISION_HIGH = "precision highp float;"

private const val TEMPL_SIMPLE_VERT = """
$VERSION
$PRECISION_HIGH

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

out vec2 vTexCoord;

%DECL%

void main() {
    %EXPR%
    vTexCoord = aTexCoord;
    mat4 mvp =  %PROJ% * %VIEW% * %MODEL%;
    gl_Position = mvp * vec4(aPosition, 1.0);
}
"""

private const val TEMPL_SIMPLE_FRAG = """
$VERSION
$PRECISION_HIGH

in vec2 vTexCoord;

%DECL%

layout (location = 0) out vec4 oFragColor;

void main() {
    %EXPR%
    oFragColor = %COLOR%;
}
"""

private fun List<String>.toSrc() = distinct().joinToString("\n")

private class SimpleTechnique(private val modelM: Expression<mat4>,
                              private val viewM: Expression<mat4>,
                              private val projM: Expression<mat4>,
                              private val color: Expression<vec4>) : GlResource() {

    private val program: GlProgram

    init {
        val vertDecl = modelM.decl() + viewM.decl() + projM.decl()
        val vertDeclSrc = vertDecl.toSrc()
        val vertExpr = modelM.expr() + modelM.expr() + modelM.expr()
        val vertExprSrc = vertExpr.toSrc()
        val vertSrc = TEMPL_SIMPLE_VERT
            .replace("%DECL%", vertDeclSrc)
            .replace("%EXPR%", vertExprSrc)
            .replace("%MODEL%", modelM.name)
            .replace("%VIEW%", viewM.name)
            .replace("%PROJ%", projM.name)
        val fragDecl = color.decl()
        val fragDeclSrc = fragDecl.toSrc()
        val fragExpr = color.expr()
        val fragExprSrc = fragExpr.toSrc()
        val fragSrc = TEMPL_SIMPLE_FRAG
            .replace("%DECL%", fragDeclSrc)
            .replace("%EXPR%", fragExprSrc)
            .replace("%COLOR%", color.name)
        program = GlProgram(
            GlShader(GlShaderType.VERTEX_SHADER, vertSrc),
            GlShader(GlShaderType.FRAGMENT_SHADER, fragSrc))
        addChildren(program)
    }

    fun draw(draw: () -> Unit) {
        glBind(program) {
            projM.submit(program)
            viewM.submit(program)
            draw.invoke()
        }
    }

    fun instance(mesh: GlMesh) {
        modelM.submit(program)
        color.submit(program)
        glBind(mesh) {
            program.draw(mesh)
        }
    }
}

private val window = GlWindow()

private val matrixStack = MatrixStack()
private val camera = Camera()
private val controller = Controller(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")
private val diffuse1 = texturesLib.loadTexture("textures/utah.jpg")
private val diffuse2 = texturesLib.loadTexture("textures/smoke.png")
private val diffuse3 = texturesLib.loadTexture("textures/blood_moss.jpg")
private val diffuse4 = texturesLib.loadTexture("textures/floor.jpg")
private val rectangle = GlMesh.rect()

private var mouseLook = false

private val constTexCoord = constv2("vTexCoord")

private var proportion = 0f
private var delta = 0.01f

private val unifModelM = unifmat4 { matrixStack.peekMatrix() }
private val unifViewM = unifmat4 { camera.calculateViewM() }
private val unifProjM = unifmat4 { camera.projectionM }
private val unifDiffuse1 = unifsampler { diffuse1 }
private val unifDiffuse2 = unifsampler { diffuse2 }
private val unifDiffuse3 = unifsampler { diffuse3 }
private val unifDiffuse4 = unifsampler { diffuse4 }
private val unifProp1 = unifvec4 { vec4(proportion) }
private val unifProp2 = unifvec4 { vec4(1f - proportion) }

private val simpleTechnique = SimpleTechnique(
    unifModelM, unifViewM, unifProjM,
    mulv4(
        addv4(
            mulv4(tex(constTexCoord, unifDiffuse1), unifProp1),
            mulv4(tex(constTexCoord, unifDiffuse2), unifProp2)
        ),
        addv4(
            mulv4(tex(constTexCoord, unifDiffuse3), unifProp1),
            mulv4(tex(constTexCoord, unifDiffuse4), unifProp2)
        )
    )
)

fun main() {
    window.create(isHoldingCursor = false) {
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
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        glUse(simpleTechnique, skyboxTechnique, rectangle, diffuse1, diffuse2, diffuse3, diffuse4) {
            window.show {
                glBind(diffuse1, diffuse2, diffuse3, diffuse4) {
                    glClear()
                    controller.apply { position, direction ->
                        camera.setPosition(position)
                        camera.lookAlong(direction)
                    }
                    skyboxTechnique.skybox(camera)
                    simpleTechnique.draw {
                        simpleTechnique.instance(rectangle)
                    }
                    proportion += delta
                    if (proportion < 0f || proportion > 1f) {
                        delta = -delta
                    }
                }
            }
        }
    }
}