package com.gzozulin.minigl.api

private const val DEF_DOTV3 = "generate float dotv3 ( vec3 left , vec3 right ) { return left [ 0 ] * right [ 0 ] + left [ 1 ] * right [ 1 ] + left [ 2 ] * right [ 2 ] ; }"

private const val DEF_BOTH = DEF_DOTV3

fun dotv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "dotv3(left, right)"
    override fun roots() = listOf(left, right)
}

