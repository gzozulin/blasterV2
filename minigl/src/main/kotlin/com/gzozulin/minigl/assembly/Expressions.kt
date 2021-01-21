package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.gl.*
import java.util.concurrent.atomic.AtomicInteger

const val VERSION = "#version 300 es"
const val PRECISION_HIGH = "precision highp float;"

private const val EXPR_TILE =
    "vec2 expr_tile(vec2 texCoord, ivec2 uv, ivec2 cnt) {\n" +
        "    vec2 result;\n" +
        "    float tileSideX = 1.0 / float(cnt.x);\n" +
        "    float tileStartX = float(uv.x) * tileSideX;\n" +
        "    result.x = tileStartX + texCoord.x * tileSideX;\n" +
        "    \n" +
        "    float tileSideY = 1.0 / float(cnt.y);\n" +
        "    float tileStartY = float(uv.y) * tileSideY;\n" +
        "    result.y = tileStartY + texCoord.y * tileSideY;\n" +
        "    return result;\n" +
        " }\n"

private const val EXPR_DISCARD =
    "vec4 expr_discard() {\n" +
            "    discard;\n" +
            "    return vec4(1.0);\n" +
            " }\n"

const val DECLARATIONS_VERT = EXPR_TILE
const val DECLARATIONS_FRAG = EXPR_TILE + EXPR_DISCARD

private var next = AtomicInteger()
private fun nextName() = "_v${next.incrementAndGet()}"

abstract class Expression<R> {
    open val type: String = "Override!"

    open val name: String = nextName()

    open fun decl(): List<String> = listOf()
    open fun vrbl(): List<String> = listOf()
    open fun expr(): String = ""

    open fun submit(program: GlProgram) {}
}

// ------------------------- Varrying -------------------------

fun <R> varying(givenName: String) = object : Expression<R>() {
    override fun expr() = givenName
}

// ------------------------- Uniforms -------------------------

abstract class Uniform<R>(var value: R?) : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
    override fun expr() = name
}

fun uniff(v: Float? = null) = object : Uniform<Float>(v) {
    override val type = "float"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifi(v: Int? = null) = object : Uniform<Int>(v) {
    override val type = "int"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifmat4(v: mat4? = null) = object : Uniform<mat4>(v) {
    override val type = "mat4"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifv4(v: vec4? = null) = object : Uniform<vec4>(v) {
    override val type = "vec4"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifv2(v: vec2? = null) = object : Uniform<vec2>(v) {
    override val type = "vec2"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifv2i(v: vec2i? = null) = object : Uniform<vec2i>(v) {
    override val type = "ivec2"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifsampler(v: GlTexture? = null) = object : Uniform<GlTexture>(v) {
    override val type = "sampler2D"
    override fun submit(program: GlProgram) { program.setTexture(name, checkNotNull(value))}
}

// ------------------------- Constants -------------------------

abstract class Const<R>(var value: R?) : Expression<R>() {
    override fun decl() = listOf("const $type $name = $value;")
    override fun expr() = name
}

fun consti(value: Int) = object : Const<Int>(value) {
    override val type = "int"
}

fun constf(value: Float) = object : Const<Float>(value) {
    override val type = "float"
}

fun constb(value: Boolean) = object : Const<Boolean>(value) {
    override val type = "bool"
}

fun constv4(value: vec4) = object : Const<vec4>(value) {
    override val type = "vec4"
    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
}

fun constv2i(value: vec2i) = object : Const<vec2i>(value) {
    override val type = "ivec2"
    override fun decl() = listOf("const $type $name = ivec2(${value.x}, ${value.y});")
}

fun constm4(value: mat4) = object : Const<mat4>(value) {
    override val type = "mat4"
    override fun decl() = listOf("const $type $name = mat4(" +
            "${value.get(0, 0)}, ${value.get(0, 1)}, ${value.get(0, 2)}, ${value.get(0, 3)}, " +
            "${value.get(1, 0)}, ${value.get(1, 1)}, ${value.get(1, 2)}, ${value.get(1, 3)}, " +
            "${value.get(2, 0)}, ${value.get(2, 1)}, ${value.get(2, 2)}, ${value.get(2, 3)}, " +
            "${value.get(3, 0)}, ${value.get(3, 1)}, ${value.get(3, 2)}, ${value.get(3, 3)});")
}

// ------------------------- Variable -------------------------

abstract class Variable<R>(val expr: Expression<R>) : Expression<R>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl() + listOf("$type $name = ${expr.expr()};")
    override fun expr() = name

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun varv2(expr: Expression<vec2>) = object : Variable<vec2>(expr) {
    override val type = "vec2"
}

// ------------------------- Addition -------------------------

fun <R> add(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} + ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Multiplication -------------------------

fun <R> mul(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} * ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Textures -------------------------

fun tex(texCoord: Expression<vec2>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun decl() = texCoord.decl() + sampler.decl()
    override fun vrbl() = texCoord.vrbl() + sampler.vrbl()
    override fun expr() = "texture(${sampler.expr()}, ${texCoord.expr()})"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        sampler.submit(program)
    }
}

// ------------------------- If -------------------------

fun <R> ifexp(check: Expression<Boolean>, left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = check.decl() + left.decl() + right.decl()
    override fun vrbl() = check.vrbl() + left.vrbl() + right.vrbl()
    override fun expr() = "(${check.expr()}) ? ${left.expr()} : ${right.expr()}"

    override fun submit(program: GlProgram) {
        check.submit(program)
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Tile -------------------------

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun decl() = texCoord.decl() + uv.decl() + cnt.decl()
    override fun vrbl() = texCoord.vrbl() + uv.vrbl() + cnt.vrbl()
    override fun expr() = "expr_tile(${texCoord.expr()}, ${uv.expr()}, ${cnt.expr()})"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        uv.submit(program)
        cnt.submit(program)
    }
}

// ------------------------- Discard -------------------------

fun discardv4() = object : Expression<vec4>() {
    override fun expr() = "expr_discard()"
}

// ------------------------- Boolean -------------------------

// eq/not/more/less

fun <R> eq(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} == ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun not(expr: Expression<Boolean>) = object : Expression<Boolean>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl()
    override fun expr() = expr.expr() + listOf("(!${expr.expr()})")

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

// ------------------------- Accessors -------------------------

// x, y, z, w, u, v, swizzles