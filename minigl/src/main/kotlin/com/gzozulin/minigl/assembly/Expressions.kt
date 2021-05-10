package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.api.*
import java.util.concurrent.atomic.AtomicInteger

private var next = AtomicInteger()
private fun nextName() = "_v${next.incrementAndGet()}"

abstract class Expression<R> {
    open val name: String = nextName()
    open val type: String = "Override!"

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

abstract class Uniform<R>(private val p: (() -> R)?, private var v: R?) : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
    override fun expr() = name

    var value: R
        get() = if (p != null) { p.invoke() } else { v!! }
        set(new) {
            check(p == null) { "This uniform already has a provider!" }
            v = new
        }
}

fun uniff(v: Float? = null) = object : Uniform<Float>(null, v) {
    override val type = "float"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifi(v: Int? = null) = object : Uniform<Int>(null, v) {
    override val type = "int"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifv2(v: vec2? = null) = object : Uniform<vec2>(null, v) {
    override val type = "vec2"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifv2i(v: vec2i? = null) = object : Uniform<vec2i>(null, v) {
    override val type = "ivec2"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifv3(v: vec3? = null) = object : Uniform<vec3>(null, v) {
    override val type = "vec3"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifv3(p: () -> vec3) = object : Uniform<vec3>(p, null) {
    override val type = "vec3"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifv4(v: vec4? = null) = object : Uniform<vec4>(null, v) {
    override val type = "vec4"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifm4(v: mat4? = null) = object : Uniform<mat4>(null, v) {
    override val type = "mat4"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifm4(p: () -> mat4) = object : Uniform<mat4>(p, null) {
    override val type = "mat4"
    override fun submit(program: GlProgram) { program.setUniform(name, value) }
}

fun unifsampler(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override val type = "sampler2D"
    override fun submit(program: GlProgram) { program.setTexture(name, value)}
}

// ------------------------- Constants -------------------------

abstract class Const<R>(private var value: R?) : Expression<R>() {
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

fun constv2i(value: vec2i) = object : Const<vec2i>(value) {
    override val type = "ivec2"
    override fun decl() = listOf("const $type $name = ivec2(${value.x}, ${value.y});")
}

fun constv3(value: vec3) = object : Const<vec3>(value) {
    override val type = "vec3"
    override fun decl() = listOf("const $type $name = vec3(${value.x}, ${value.y}, ${value.z});")
}

fun constv4(value: vec4) = object : Const<vec4>(value) {
    override val type = "vec4"
    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
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

abstract class Variable<R>(private val expr: Expression<R>) : Expression<R>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl() + listOf("$type $name = ${expr.expr()};")
    override fun expr() = name

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun cachev2(expr: Expression<vec2>) = object : Variable<vec2>(expr) {
    override val type = "vec2"
}

fun cachev4(expr: Expression<vec4>) = object : Variable<vec4>(expr) {
    override val type = "vec4"
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

// ------------------------- Subtraction -------------------------

fun <R> sub(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} - ${right.expr()})"

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

// ------------------------- Division -------------------------

fun <R> div(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} / ${right.expr()})"

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

fun <R> discard() = object : Expression<R>() {
    override fun expr() = "expr_discard()"
}

// ------------------------- Boolean -------------------------

fun <R> ifexp(check: Expression<Boolean>, left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = check.decl() + left.decl() + right.decl()
    override fun vrbl() = check.vrbl() + left.vrbl() + right.vrbl()
    override fun expr() = "((${check.expr()}) ? ${left.expr()} : ${right.expr()})"

    override fun submit(program: GlProgram) {
        check.submit(program)
        left.submit(program)
        right.submit(program)
    }
}

fun <R> eq(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} == ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun <R> more(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} > ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun <R> near(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "expr_near(${left.expr()}, ${right.expr()})"

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

// todo: more, less, boundary

// ------------------------- Accessors -------------------------

fun getx(expr: Expression<vec4>) = object : Expression<Float>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl()
    override fun expr() = "expr_x(${expr.expr()})"

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun gety(expr: Expression<vec4>) = object : Expression<Float>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl()
    override fun expr() = "expr_y(${expr.expr()})"

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun getz(expr: Expression<vec4>) = object : Expression<Float>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl()
    override fun expr() = "expr_z(${expr.expr()})"

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun getw(expr: Expression<vec4>) = object : Expression<Float>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl()
    override fun expr() = "expr_w(${expr.expr()})"

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun getr(expr: Expression<vec4>) = getx(expr)
fun getg(expr: Expression<vec4>) = gety(expr)
fun getb(expr: Expression<vec4>) = getz(expr)
fun geta(expr: Expression<vec4>) = getw(expr)

fun setx(vec: Expression<vec4>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun decl() = vec.decl() + w.decl()
    override fun vrbl() = vec.vrbl() + w.vrbl()
    override fun expr() = "expr_set_x(${vec.expr()}, ${w.expr()})"

    override fun submit(program: GlProgram) {
        vec.submit(program)
        w.submit(program)
    }
}

fun sety(vec: Expression<vec4>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun decl() = vec.decl() + w.decl()
    override fun vrbl() = vec.vrbl() + w.vrbl()
    override fun expr() = "expr_set_y(${vec.expr()}, ${w.expr()})"

    override fun submit(program: GlProgram) {
        vec.submit(program)
        w.submit(program)
    }
}

fun setz(vec: Expression<vec4>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun decl() = vec.decl() + w.decl()
    override fun vrbl() = vec.vrbl() + w.vrbl()
    override fun expr() = "expr_set_z(${vec.expr()}, ${w.expr()})"

    override fun submit(program: GlProgram) {
        vec.submit(program)
        w.submit(program)
    }
}

fun setw(vec: Expression<vec4>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun decl() = vec.decl() + w.decl()
    override fun vrbl() = vec.vrbl() + w.vrbl()
    override fun expr() = "expr_set_w(${vec.expr()}, ${w.expr()})"

    override fun submit(program: GlProgram) {
        vec.submit(program)
        w.submit(program)
    }
}

fun setr(vec: Expression<vec4>, r: Expression<Float>) = setx(vec, r)
fun setg(vec: Expression<vec4>, g: Expression<Float>) = sety(vec, g)
fun setb(vec: Expression<vec4>, b: Expression<Float>) = setz(vec, b)
fun seta(vec: Expression<vec4>, a: Expression<Float>) = setw(vec, a)