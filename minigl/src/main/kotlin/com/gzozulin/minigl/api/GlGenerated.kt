package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light

private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { vec2 result = v2 ( 0.0f , 0.0f ) ; float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; result . x = tileStartX + texCoord . x * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; result . y = tileStartY + texCoord . y * tileSideY ; return result ; }\n\n"
private const val DEF_LUMINOSITY = "float luminosity ( float distance , Light light ) { return 1.0f / ( light . attenConstant + light . attenLinear * distance + light . attenQuadratic * distance * distance ) ; }\n\n"

const val PUBLIC_DEFINITIONS = DEF_TILE+DEF_LUMINOSITY

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun expr() = "tile(${texCoord.expr()}, ${uv.expr()}, ${cnt.expr()})"
    override fun roots() = listOf(texCoord, uv, cnt)
}

fun luminosity(distance: Expression<Float>, light: Expression<Light>) = object : Expression<Float>() {
    override fun expr() = "luminosity(${distance.expr()}, ${light.expr()})"
    override fun roots() = listOf(distance, light)
}

