package com.gzozulin.minigl.api

private const val DEF_ITOF = "float itof ( int i ) { return ( float ) i ; }"
private const val DEF_EXPR_TILE = "vec2 expr_tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { vec2 result = v2 ( ) ; float tileSideX = 1.0f / itof ( cnt [ 0 ] ) ; float tileStartX = itof ( uv [ 0 ] ) * tileSideX ; result [ 0 ] = tileStartX + texCoord [ 0 ] * tileSideX ; float tileSideY = 1.0f / itof ( cnt [ 0 ] ) ; float tileStartY = itof ( uv [ 1 ] ) * tileSideY ; result [ 1 ] = tileStartY + texCoord [ 1 ] * tileSideY ; return result ; }"
const val PUBLIC_DEFINITIONS = DEF_ITOF+DEF_EXPR_TILE

fun itof(i: Expression<Int>) = object : Expression<Float>() {
    override fun expr() = "itof(i)"
    override fun roots() = listOf(i)
}

fun expr_tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun expr() = "expr_tile(texCoord, uv, cnt)"
    override fun roots() = listOf(texCoord, uv, cnt)
}

