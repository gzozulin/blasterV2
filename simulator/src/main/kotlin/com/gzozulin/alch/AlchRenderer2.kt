package com.gzozulin.alch

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.SimpleTechnique
import com.gzozulin.minigl.techniques.SkyboxTechnique

// region --------------------------- Expression ---------------------------

private abstract class Expression<R> {
    open val name: String = nextName()

    open fun decl(): List<String> = listOf()
    open fun expr(): List<String> = listOf()

    abstract val type: String

    companion object {
        private var next = 0
        private fun nextName() = "_v${next++}"
    }
}

private fun constf(givenName: String) = object : Expression<Float>() {
    override val name = givenName
    override val type = "float"
}

private abstract class Uniform<R> : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
}

private fun uniff() = object : Uniform<Float>() {
    override val type = "float"
}

private abstract class Add<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type add($type left, $type right) { return left + right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = add(${left.name}, ${right.name});")
}

private fun addf(left: Expression<Float>, right: Expression<Float>) = object : Add<Float>(left, right) {
    override val type = "float"
}

// endregion

// region --------------------------- Technique ---------------------------

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

uniform sampler2D uTexDiffuse;
uniform vec3 uColor;

layout (location = 0) out vec4 oFragC olor;

void main() {
    oFragColor = texture(uTexDiffuse, vTexCoord);
    if (oFragColor.a < 0.1) {
        discard;
    }
    oFragColor.rgb *= uColor;
}
"""

private fun shader(type: GlShaderType, source: String) = GlShader(type, source)

private fun technique() = GlProgram(
    shader(GlShaderType.VERTEX_SHADER, TEMPL_SIMPLE_VERT),
    shader(GlShaderType.FRAGMENT_SHADER, TEMPL_SIMPLE_FRAG))

// endregion

// region --------------------------- Window ---------------------------

private val window = GlWindow()

private val matrixStack = MatrixStack()
private val camera = Camera()
private val controller = Controller(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val technique = technique()
private val simpleTechnique = SimpleTechnique()
private val skyboxTechnique = SkyboxTechnique("textures/snowy")
private val diffuse = texturesLib.loadTexture("textures/utah.jpg")
private val rectangle = GlMesh.rect()

private var mouseLook = false

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
        glUse(technique, simpleTechnique, skyboxTechnique, rectangle, diffuse) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                simpleTechnique.draw(camera.calculateViewM(), camera.projectionM) {
                    matrixStack.pushMatrix(mat4().identity().translate(vec3().left())) {
                        simpleTechnique.instance(rectangle, diffuse, matrixStack.peekMatrix())
                    }
                }
            }
        }
    }
}

// endregion