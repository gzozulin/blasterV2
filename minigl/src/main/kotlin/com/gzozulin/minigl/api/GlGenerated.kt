package com.gzozulin.minigl.api

private const val DEF_MUL = "generate int mul ( int left , int right ) { return left * right ; }"

private const val DEF_BOTH = DEF_MUL

fun mul(left: Expression<Int>, right: Expression<Int>) = object : Expression<Int>() {
    override fun expr() = "mul(left, right)"
    override fun roots() = listOf(left, right)
}

