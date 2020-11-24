package com.gzozulin.alch

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.SkyboxTechnique

private const val VERSION = "#version 300 es"
private const val PRECISION_HIGH = "precision highp float;"

//================================Expression================================

private abstract class Expression<R> {
    val name = nextName()

    open fun decl(): List<String> = listOf()

    open fun expr(): List<String> = listOf()

    abstract val type: String

    companion object {
        private var next = 0
        private fun nextName() = "_v${next++}"
    }
}

//================================Constant================================

private abstract class Constant<T>(private val value: T): Expression<T>() {
    override fun decl() = listOf("const $type $name = $value;")
}

private class ConstantI(value: Int) : Constant<Int>(value) {
    override val type: String
        get() = "int"
}

private class ConstantF(value: Float) : Constant<Float>(value) {
    override val type: String
        get() = "float"
}

private class ConstantV3(value: vec3) : Constant<vec3>(value) {
    override val type: String
        get() = "vec3"
}

private fun consti(value: Int) = ConstantI(value)
private fun constf(value: Float) = ConstantF(value)
private fun constv3(value: vec3) = ConstantV3(value)

//================================Uniform================================

private abstract class Uniform<T> : Expression<T>() {
    override fun decl() = listOf("uniform $type $name;")
}

private class UniformI : Uniform<Int>() {
    override val type: String
        get() = "int"
}

private fun uniformi() = UniformI()

//================================Attribute================================

private abstract class Attribute<T>(private val index: Int) : Expression<T>() {
    override fun decl() = listOf("layout (location = $index) in $type $name;")
}

private class AttributeI(index: Int) : Attribute<Int>(index) {
    override val type: String
        get() = "int"
}

private class AttributeV2(index: Int) : Attribute<vec2>(index) {
    override val type: String
        get() = "vec2"
}

private class AttributeV3(index: Int) : Attribute<vec3>(index) {
    override val type: String
        get() = "vec3"
}

private fun attributei(index: Int) = AttributeI(index)
private fun attributev2(index: Int) = AttributeV2(index)
private fun attributev3(index: Int) = AttributeV3(index)

//================================Add================================

private abstract class Add<T>(val left: Expression<T>,
                              val right: Expression<T>) : Expression<T>() {

    override fun decl(): List<String> {
        val declarations = mutableListOf<String>()
        declarations.addAll(left.decl())
        declarations.addAll(right.decl())
        declarations.add("$type addi($type left, $type right) { return left + right; }")
        return declarations
    }

    override fun expr(): List<String> {
        val expressions = mutableListOf<String>()
        expressions.addAll(left.expr())
        expressions.addAll(right.expr())
        expressions.add("$type $name = addi(${left.name}, ${right.name});")
        return expressions
    }
}

private class AddI(left: Expression<Int>, right: Expression<Int>) : Add<Int>(left, right) {
    override val type: String
        get() = "int"
}

private class AddF(left: Expression<Float>, right: Expression<Float>) : Add<Float>(left, right) {
    override val type: String
        get() = "float"
}

private class AddV3(left: Expression<vec3>, right: Expression<vec3>) : Add<vec3>(left, right) {
    override val type: String
        get() = "vec3"
}

private fun addi(left: Expression<Int>, right: Expression<Int>) = AddI(left, right)
private fun addf(left: Expression<Float>, right: Expression<Float>) = AddF(left, right)

//================================Technique================================

private abstract class Technique: GlResource() {
    fun instance() {}
}

private data class SimpleTechnique(
    val program: GlProgram,
    val position: Expression<vec3>,
    val texCoord: Expression<vec2>,
    val albedo: Expression<vec3>
): Technique()

private fun List<String>.toSrc() = this.distinct().joinToString("\n")

private fun simple(position: Expression<vec3>, texCoord: Expression<vec2>, albedo: Expression<vec3>): SimpleTechnique {
    val vertDeclarations = mutableListOf<String>()
    vertDeclarations.addAll(position.decl())
    vertDeclarations.addAll(texCoord.decl())
    val vertExpressions = mutableListOf<String>()
    vertExpressions.addAll(position.expr())
    vertExpressions.addAll(texCoord.expr())
    val vert = """
        $VERSION
        $PRECISION_HIGH
        ${vertDeclarations.toSrc()}
        out vec2 vTexCoord;
        void main() {
            ${vertExpressions.toSrc()}
            vTexCoord = ${texCoord.name};
            mat4 mvp =  uProjectionM * uViewM * uModelM;
            gl_Position = mvp * vec4(${position.name}, 1.0);
        }
    """.trimIndent()
    val fragDeclarations = albedo.decl()
    val fragExpressions = albedo.expr()
    val frag = """
        $VERSION
        $PRECISION_HIGH
        ${fragDeclarations.toSrc()}
        void main() {
            ${fragExpressions.toSrc()}
            oFragColor = ${albedo.name};
        }
    """.trimIndent()
    return SimpleTechnique(GlProgram(
        GlShader(GlShaderType.VERTEX_SHADER, vert), GlShader(GlShaderType.FRAGMENT_SHADER, frag)),
        position, texCoord, albedo
    )
}

private fun withTechnique(techinque: Technique, draw: Technique.() -> Unit) {
    draw.invoke(techinque)
}

//================================Variable================================

private val simpleTechnique = simple(
    attributev3(0),
    attributev2(1),
    constv3(vec3().magenta())
)

private val window = GlWindow()

private val matrixStack = MatrixStack()
private val camera = Camera()
private val controller = Controller(position = vec3().front())
private val wasdInput = WasdInput(controller)

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
        glUse(simpleTechnique, skyboxTechnique, diffuse, rectangle) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                /*simpleTechnique.draw(camera.calculateViewM(), camera.projectionM) {
                    matrixStack.pushMatrix(mat4().identity().translate(vec3().left())) {
                        simpleTechnique.instance(rectangle, diffuse, matrixStack.peekMatrix())
                    }
                }*/
            }
        }
    }
}