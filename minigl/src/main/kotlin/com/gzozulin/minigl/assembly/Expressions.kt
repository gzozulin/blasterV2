package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.gl.*

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

fun unifmat4(v: mat4? = null) = object : Uniform<mat4>(v) {
    override val type = "mat4"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifvec4(v: vec4? = null) = object : Uniform<vec4>(v) {
    override val type = "vec4"
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

fun propv4(value: vec4) = object : Expression<vec4>() {
    override val type = "vec4"
    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
}

// ------------------------- Addition -------------------------

abstract class Add<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type add($type left, $type right) { return left + right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = add(${left.name}, ${right.name});")
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

// ------------------------- Multiplication -------------------------

abstract class Mul<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type mul($type left, $type right) { return left * right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = mul(${left.name}, ${right.name});")
    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun mulv4(left: Expression<vec4>, right: Expression<vec4>) = object : Mul<vec4>(left, right) {
    override val type = "vec4"
}

// ------------------------- Textures -------------------------

fun tex(texCoord: Expression<vec2>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun decl() = texCoord.decl() + sampler.decl() +
            listOf("$type tex(${texCoord.type} texCoord, ${sampler.type} sampler) { return texture(sampler, texCoord); }")
    override fun expr() = texCoord.expr() + sampler.expr() +
            listOf("$type $name = tex(${texCoord.name}, ${sampler.name});")

    override val type = "vec4"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        sampler.submit(program)
    }
}