package com.gzozulin.alch

/*

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.SkyboxTechnique

// todo: fix GlProgram
// todo: pass sampler
// todo: do not store expressions in technique
// todo: withTechnique { instance(mesh) }

private const val VERSION = "#version 300 es"
private const val PRECISION_HIGH = "precision highp float;"

//================================Expression================================

private abstract class Expr<R> {
    val name = nextName()

    open fun decl(): List<String> = listOf()

    open fun expr(): List<String> = listOf()

    abstract val type: String

    companion object {
        private var next = 0
        private fun nextName() = "_v${next++}"
    }
}

private fun List<String>.toSrc() = this.distinct().joinToString("\n")

//================================Constant================================

private abstract class Constant<T>(val value: T): Expr<T>() {
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

    override fun decl() = listOf("const $type $name = vec3(${value.x}, ${value.y}, ${value.z});")
}

private class ConstantV4(value: vec4) : Constant<vec4>(value) {
    override val type: String
        get() = "vec4"

    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
}

private fun consti(value: Int) = ConstantI(value)
private fun constf(value: Float) = ConstantF(value)
private fun constv3(value: vec3) = ConstantV3(value)
private fun constv4(value: vec4) = ConstantV4(value)

//================================Uniform================================

private class Sampler2D

private abstract class Uniform<T> : Expr<T>() {
    override fun decl() = listOf("uniform $type $name;")
}

private class UniformI : Uniform<Int>() {
    override val type: String
        get() = "int"
}

private class UniformV3 : Uniform<vec3>() {
    override val type: String
        get() = "vec3"
}

private class UniformM4 : Uniform<mat4>() {
    override val type: String
        get() = "mat4"
}

private class UniformS : Uniform<Sampler2D>() {
    override val type: String
        get() = "sampler2D"
}

private fun uniformi() = UniformI()
private fun uniformv3() = UniformV3()
private fun uniformm4() = UniformM4()
private fun uniforms() = UniformS()

//================================Attribute================================

private abstract class Attribute<T>(private val index: Int) : Expr<T>() {
    override fun decl() = listOf("layout (location = $index) in $type $name;")
}

private class AttributeV2(index: Int) : Attribute<vec2>(index) {
    override val type: String
        get() = "vec2"
}

private class AttributeV3(index: Int) : Attribute<vec3>(index) {
    override val type: String
        get() = "vec3"
}

private fun attributev2(index: Int) = AttributeV2(index)
private fun attributev3(index: Int) = AttributeV3(index)

//================================Add================================

private abstract class Add<T>(val left: Expr<T>,
                              val right: Expr<T>) : Expr<T>() {

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

private class AddI(left: Expr<Int>, right: Expr<Int>) : Add<Int>(left, right) {
    override val type: String
        get() = "int"
}

private class AddF(left: Expr<Float>, right: Expr<Float>) : Add<Float>(left, right) {
    override val type: String
        get() = "float"
}

private class AddV3(left: Expr<vec3>, right: Expr<vec3>) : Add<vec3>(left, right) {
    override val type: String
        get() = "vec3"
}

private fun addi(left: Expr<Int>, right: Expr<Int>) = AddI(left, right)
private fun addf(left: Expr<Float>, right: Expr<Float>) = AddF(left, right)

//================================Texture================================

private class Texture(private val sampler: Expr<Sampler2D>, private val texCoord: Expr<vec2>)
    : Expr<vec4>() {

    override val type: String
        get() = "vec4"

    override fun decl(): List<String> {
        val declarations = mutableListOf<String>()
        declarations.addAll(sampler.decl())
        declarations.addAll(texCoord.decl())
        return declarations
    }

    override fun expr(): List<String> {
        val expressions = mutableListOf<String>()
        expressions.addAll(sampler.expr())
        expressions.addAll(texCoord.expr())
        expressions.add("$type $name = texture(${sampler.name}, ${texCoord.name});")
        return expressions
    }
}

private fun texture(sampler: Expr<Sampler2D>, texCoord: Expr<vec2>) = Texture(sampler, texCoord)

//================================Technique================================

private abstract class Technique: GlResource() {
    abstract val program: GlProgram
}

private data class SimpleTechnique(
    override val program: GlProgram,
    val position: Expr<vec3>,
    val texCoord: Expr<vec2>,
    val albedo: Expr<vec4>,
    val modelM: Expr<mat4>,
    val viewM: Expr<mat4>,
    val projM: Expr<mat4>
): Technique() {
    init {
        addChildren(program)
    }
}

private fun simple(position: Expr<vec3>, texCoord: Expr<vec2>, albedo: Expr<vec4>,
                   modelM: Expr<mat4>, viewM: Expr<mat4>, projM: Expr<mat4>): SimpleTechnique {
    val vertDeclarations = mutableListOf<String>()
    vertDeclarations.addAll(position.decl())
    vertDeclarations.addAll(texCoord.decl())
    vertDeclarations.addAll(modelM.decl())
    vertDeclarations.addAll(viewM.decl())
    vertDeclarations.addAll(projM.decl())
    val vertExpressions = mutableListOf<String>()
    vertExpressions.addAll(position.expr())
    vertExpressions.addAll(texCoord.expr())
    vertExpressions.addAll(modelM.expr())
    vertExpressions.addAll(viewM.expr())
    vertExpressions.addAll(projM.expr())
    val vert = """
        $VERSION
        $PRECISION_HIGH
        ${vertDeclarations.toSrc()}
        out vec2 vTexCoord;
        void main() {
            ${vertExpressions.toSrc()}
            vTexCoord = ${texCoord.name};
            mat4 mvp =  ${projM.name} * ${viewM.name} * ${modelM.name};
            gl_Position = mvp * vec4(${position.name}, 1.0);
        }
    """.trimIndent()

    // todo: for variables in between, we need to declare them as out in vert and as in in frag

    val fragDeclarations = albedo.decl()
    val fragExpressions = albedo.expr()
    val frag = """
        $VERSION
        $PRECISION_HIGH
        ${fragDeclarations.toSrc()}
        in vec2 vTexCoord;
        layout (location = 0) out vec4 oFragColor;
        void main() {
            ${fragExpressions.toSrc()}
            oFragColor = ${albedo.name};
        }
    """.trimIndent()
    return SimpleTechnique(GlProgram(
        GlShader(GlShaderType.VERTEX_SHADER, vert), GlShader(GlShaderType.FRAGMENT_SHADER, frag)),
        position, texCoord, albedo, modelM, viewM, projM
    )
}

private fun <T : Technique> withTechnique(techinque: T, draw: T.() -> Unit) {
    glBind(techinque.program) {
        draw.invoke(techinque)
    }
}

//================================Variable================================

private val attributePosition = attributev3(0)
private val attributeTexCoord = attributev2(1)

private val uniformSampler = uniforms()

private val simpleTechnique = simple(
    attributePosition,
    attributeTexCoord,
    texture(uniformSampler, attributeTexCoord),
    uniformm4(),
    uniformm4(),
    uniformm4()
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
        glUse(simpleTechnique, skyboxTechnique, rectangle, diffuse) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                withTechnique(simpleTechnique) {
                    glBind(rectangle, diffuse) {
                        val color = when (randi(9)) {
                            0 -> vec3().red()
                            1 -> vec3().green()
                            2 -> vec3().blue()
                            3 -> vec3().yellow()
                            4 -> vec3().magenta()
                            5 -> vec3().cyan()
                            6 -> vec3().orange()
                            7 -> vec3().rose()
                            8 -> vec3().violet()
                            else -> error("")
                        }
                        program.setArbitraryUniform(albedo.name, color)
                        program.setArbitraryUniform(viewM.name, camera.calculateViewM())
                        program.setArbitraryUniform(projM.name, camera.projectionM)
                        program.setArbitraryUniform(modelM.name, matrixStack.peekMatrix())
                        program.setArbitraryUniform(uniformSampler.name, diffuse)
                        program.draw(indicesCount = rectangle.indicesCount)
                    }
                }
            }
        }
    }
}
*/
