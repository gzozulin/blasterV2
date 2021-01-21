package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.gl.*

const val VERSION = "#version 300 es"
const val PRECISION_HIGH = "precision highp float;"

const val DECLARATION = ""

abstract class Expression<R> {
    open val name: String = nextName()

    open fun decl(): List<String> = listOf()
    open fun expr(): List<String> = listOf()

    open fun submit(program: GlProgram) {}

    abstract val type: String

    companion object {
        private var next = 0
        private fun nextName() = "_v${next++}"
    }
}

// ------------------------- Constant -------------------------

fun constf(givenName: String) = object : Expression<Float>() {
    override val name = givenName
    override val type = "float"
}

fun constv4(givenName: String) = object : Expression<vec4>() {
    override val name = givenName
    override val type = "vec4"
}

fun constv2(givenName: String) = object : Expression<vec2>() {
    override val name = givenName
    override val type = "vec2"
}

// ------------------------- Uniforms -------------------------

abstract class Uniform<R>(var value: R?) : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
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

// ------------------------- Properties -------------------------

fun propi(value: Int) = object : Expression<Int>() {
    override val type = "int"
    override fun decl() = listOf("const $type $name = $value;")
}

fun propf(value: Float) = object : Expression<Float>() {
    override val type = "float"
    override fun decl() = listOf("const $type $name = $value;")
}

fun propb(value: Boolean) = object : Expression<Boolean>() {
    override val type = "bool"
    override fun decl() = listOf("const $type $name = $value;")
}

fun propv4(value: vec4) = object : Expression<vec4>() {
    override val type = "vec4"
    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
}

fun propv2i(value: vec2i) = object : Expression<vec2i>() {
    override val type = "ivec2"
    override fun decl() = listOf("const $type $name = ivec2(${value.x}, ${value.y});")
}

fun propm4(value: mat4) = object : Expression<mat4>() {
    override val type = "mat4"
    override fun decl() = listOf("const $type $name = mat4(" +
            "${value.get(0, 0)}, ${value.get(0, 1)}, ${value.get(0, 2)}, ${value.get(0, 3)}, " +
            "${value.get(1, 0)}, ${value.get(1, 1)}, ${value.get(1, 2)}, ${value.get(1, 3)}, " +
            "${value.get(2, 0)}, ${value.get(2, 1)}, ${value.get(2, 2)}, ${value.get(2, 3)}, " +
            "${value.get(3, 0)}, ${value.get(3, 1)}, ${value.get(3, 2)}, ${value.get(3, 3)});")
}

// ------------------------- Addition -------------------------

abstract class Add<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type expr_add($type left, $type right) { return left + right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = expr_add(${left.name}, ${right.name});")
    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun addf(left: Expression<Float>, right: Expression<Float>) = object : Add<Float>(left, right) {
    override val type = "float"
}

fun addv4(left: Expression<vec4>, right: Expression<vec4>) = object : Add<vec4>(left, right) {
    override val type = "vec4"
}

fun addv2(left: Expression<vec2>, right: Expression<vec2>) = object : Add<vec2>(left, right) {
    override val type = "vec2"
}

// ------------------------- Multiplication -------------------------

abstract class Mul<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type expr_mul($type left, $type right) { return left * right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = expr_mul(${left.name}, ${right.name});")
    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun mulv4(left: Expression<vec4>, right: Expression<vec4>) = object : Mul<vec4>(left, right) {
    override val type = "vec4"
}

// ------------------------- Textures -------------------------

fun texv4(texCoord: Expression<vec2>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun decl() = texCoord.decl() + sampler.decl() +
            listOf("$type expr_tex(${texCoord.type} texCoord, ${sampler.type} sampler) { return texture(sampler, texCoord); }")
    override fun expr() = texCoord.expr() + sampler.expr() +
            listOf("$type $name = expr_tex(${texCoord.name}, ${sampler.name});")

    override val type = "vec4"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        sampler.submit(program)
    }
}

// ------------------------- If -------------------------

abstract class If<R>(val check: Expression<Boolean>, val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = check.decl() + left.decl() + right.decl() +
            listOf("$type expr_if(bool check, $type left, $type right) { if(check) return left; else return right; }")
    override fun expr() = check.expr() + left.expr() + right.expr() + listOf("$type $name = expr_if(${check.name}, ${left.name}, ${right.name});")
    override fun submit(program: GlProgram) {
        check.submit(program)
        left.submit(program)
        right.submit(program)
    }
}

fun ifv4(check: Expression<Boolean>, left: Expression<vec4>, right: Expression<vec4>) = object : If<vec4>(check, left, right) {
    override val type = "vec4"
}

// ------------------------- Tile -------------------------

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun decl() = texCoord.decl() + uv.decl() + cnt.decl() +
            listOf("$type expr_tile(vec2 texCoord, ivec2 uv, ivec2 cnt) { " +
                    "    vec2 result;" +
                    "    float tileSideX = 1.0 / float(cnt.x);\n" +
                    "    float tileStartX = float(uv.x) * tileSideX;\n" +
                    "    result.x = tileStartX + texCoord.x * tileSideX;\n" +
                    "    \n" +
                    "    float tileSideY = 1.0 / float(cnt.y);\n" +
                    "    float tileStartY = float(uv.y) * tileSideY;\n" +
                    "    result.y = tileStartY + texCoord.y * tileSideY;" +
                    "    return result;" +
                    " }")

    override fun expr() = texCoord.expr() + uv.expr() + cnt.expr() +
            listOf("$type $name = expr_tile(${texCoord.name}, ${uv.name}, ${cnt.name});")

    override val type = "vec2"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        uv.submit(program)
        cnt.submit(program)
    }
}

// ------------------------- Filter -------------------------

// todo:
fun discardv4() = object : Expression<vec4>() {
    override val type = "vec4"
    override fun expr() = listOf("discard;")
}

abstract class Filter<R>(val check: Expression<Boolean>, val input: Expression<R>) : Expression<R>() {
    override fun decl() = check.decl() + input.decl() +
            listOf("$type expr_filter(bool check, $type inp) { if(check) return inp; else discard; }")
    override fun expr() = check.expr() + input.expr() +
            listOf("$type $name = expr_filter(${check.name}, ${input.name});")
    override fun submit(program: GlProgram) {
        check.submit(program)
        input.submit(program)
    }
}

fun filterv4(check: Expression<Boolean>, input: Expression<vec4>) = object : Filter<vec4>(check, input) {
    override val type = "vec4"
}

// ------------------------- Boolean -------------------------

// eq/not/more/less

fun <R> eq(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl() +
            listOf("$type expr_eq(${left.type} left, ${right.type} right) { return left == right; }")
    override fun expr() = left.expr() + right.expr() +
            listOf("bool $name = expr_eq(${left.name}, ${right.name});")

    override val type = "bool"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun not(expr: Expression<Boolean>) = object : Expression<Boolean>() {
    override val type = "bool"

    override fun decl() = expr.decl() + listOf("$type expr_not(bool value) { return !value; }")
    override fun expr() = expr.expr() + listOf("bool $name = expr_not(${expr.name});")

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

// ------------------------- Accessors -------------------------

// x, y, z, w, u, v, swizzles