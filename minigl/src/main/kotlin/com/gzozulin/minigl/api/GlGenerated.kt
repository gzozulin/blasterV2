package com.gzozulin.minigl.api.test

import com.gzozulin.minigl.api.Expression
import com.gzozulin.minigl.api.vec2
import com.gzozulin.minigl.api.vec2i

private const val DEF_ITOF = "float itof ( int i ) { return ( float ) i ; }"
private const val DEF_FTOI = "float ftoi ( float f ) { return ( int ) f ; }"
private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { vec2 result = v2 ( 0.0f , 0.0f ) ; float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; result . x = tileStartX + texCoord . x * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; result . y = tileStartY + texCoord . y * tileSideY ; return result ; }"

const val PUBLIC_DEFINITIONS = DEF_ITOF+DEF_FTOI+DEF_TILE

fun itof(i: Expression<Int>) = object : Expression<Float>() {
    override fun expr() = "itof(${i.expr()})"
    override fun roots() = listOf(i)
}

fun ftoi(f: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "ftoi(${f.expr()})"
    override fun roots() = listOf(f)
}

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun expr() = "tile(${texCoord.expr()}, ${uv.expr()}, ${cnt.expr()})"
    override fun roots() = listOf(texCoord, uv, cnt)
}

