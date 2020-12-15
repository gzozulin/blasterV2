package com.gzozulin.alch

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
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

private fun constv4(givenName: String) = object : Expression<vec4>() {
    override val name = givenName
    override val type = "vec4"
}

private fun constv2(givenName: String) = object : Expression<vec2>() {
    override val name = givenName
    override val type = "vec2"
}

private abstract class Uniform<R> : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
}

private fun unifmat4() = object : Uniform<mat4>() {
    override val type = "mat4"
}

private fun unifvec4() = object : Uniform<vec4>() {
    override val type = "vec4"
}

private interface Sampler
private fun unifsampler() = object : Uniform<Sampler>() {
    override val type = "sampler2D"
}

private abstract class Add<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type add($type left, $type right) { return left + right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = add(${left.name}, ${right.name});")
}

private fun addf(left: Expression<Float>, right: Expression<Float>) = object : Add<Float>(left, right) {
    override val type = "float"
}

private fun addv4(left: Expression<vec4>, right: Expression<vec4>) = object : Add<vec4>(left, right) {
    override val type = "vec4"
}

private fun tex(texCoord: Expression<vec2>, sampler: Expression<Sampler>) = object : Expression<vec4>() {
    override fun decl() = texCoord.decl() + sampler.decl() +
            listOf("$type tex(${texCoord.type} texCoord, ${sampler.type} sampler) { return texture(sampler, texCoord); }")
    override fun expr() = texCoord.expr() + sampler.expr() +
            listOf("$type $name = tex(${texCoord.name}, ${sampler.name});")

    override val type = "vec4"
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

%DECL%

layout (location = 0) out vec4 oFragColor;

void main() {
    %EXPR%
    oFragColor = %COLOR%;
}
"""

private fun List<String>.toSrc() = distinct().joinToString("\n")

private fun simple(modelM: Expression<mat4>, viewM: Expression<mat4>, projM: Expression<mat4>,
                   color: Expression<vec4>): GlProgram {
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
    return GlProgram(
        GlShader(GlShaderType.VERTEX_SHADER, vertSrc),
        GlShader(GlShaderType.FRAGMENT_SHADER, fragSrc))
}

// endregion

// region --------------------------- Window ---------------------------

private val window = GlWindow()

private val matrixStack = MatrixStack()
private val camera = Camera()
private val controller = Controller(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = SkyboxTechnique("textures/snowy")
private val diffuse = texturesLib.loadTexture("textures/utah.jpg")
private val rectangle = GlMesh.rect()

private var mouseLook = false

private val constTexCoord = constv2("vTexCoord")

private val unifModelM = unifmat4()
private val unifViewM = unifmat4()
private val unifProjM = unifmat4()
private val unifDiffuse = unifsampler()
private val unifColor = unifvec4()

private val simpleProgram = simple(
    unifModelM, unifViewM, unifProjM,
    addv4(
        tex(constTexCoord, unifDiffuse),
        unifColor
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
        glUse(simpleProgram, skyboxTechnique, rectangle, diffuse) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glBind(simpleProgram, rectangle, diffuse) {
                    simpleProgram.setArbitraryUniform(unifModelM.name, matrixStack.peekMatrix())
                    simpleProgram.setArbitraryUniform(unifViewM.name, camera.calculateViewM())
                    simpleProgram.setArbitraryUniform(unifProjM.name, camera.projectionM)
                    simpleProgram.setArbitraryUniform(unifDiffuse.name, diffuse.unit)
                    simpleProgram.setArbitraryUniform(unifColor.name, vec4(1f, 0f, 0f, 0f))
                    simpleProgram.draw(indicesCount = rectangle.indicesCount)
                }
            }
        }
    }
}

// endregion