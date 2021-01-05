package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.gl.GlTexture
import com.gzozulin.minigl.gl.mat4
import com.gzozulin.minigl.gl.vec2
import com.gzozulin.minigl.gl.vec4

abstract class Expression<R> {
    open val name: String = nextName()

    open fun decl(): List<String> = listOf()
    open fun expr(): List<String> = listOf()

    abstract val type: String

    companion object {
        private var next = 0
        private fun nextName() = "_v${next++}"
    }
}

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

abstract class Uniform<R> : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
}

fun unifmat4() = object : Uniform<mat4>() {
    override val type = "mat4"
}

fun unifvec4() = object : Uniform<vec4>() {
    override val type = "vec4"
}

fun unifsampler() = object : Uniform<GlTexture>() {
    override val type = "sampler2D"
}

fun propv4(value: vec4) = object : Expression<vec4>() {
    override val type = "vec4"
    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
}

abstract class Add<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type add($type left, $type right) { return left + right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = add(${left.name}, ${right.name});")
}

fun addf(left: Expression<Float>, right: Expression<Float>) = object : Add<Float>(left, right) {
    override val type = "float"
}

fun addv4(left: Expression<vec4>, right: Expression<vec4>) = object : Add<vec4>(left, right) {
    override val type = "vec4"
}

abstract class Mul<R>(val left: Expression<R>, val right: Expression<R>) : Expression<R>() {
    override fun decl() = left.decl() + right.decl() + listOf("$type mul($type left, $type right) { return left * right; }")
    override fun expr() = left.expr() + right.expr() + listOf("$type $name = mul(${left.name}, ${right.name});")
}

fun mulv4(left: Expression<vec4>, right: Expression<vec4>) = object : Mul<vec4>(left, right) {
    override val type = "vec4"
}

fun tex(texCoord: Expression<vec2>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun decl() = texCoord.decl() + sampler.decl() +
            listOf("$type tex(${texCoord.type} texCoord, ${sampler.type} sampler) { return texture(sampler, texCoord); }")
    override fun expr() = texCoord.expr() + sampler.expr() +
            listOf("$type $name = tex(${texCoord.name}, ${sampler.name});")

    override val type = "vec4"
}