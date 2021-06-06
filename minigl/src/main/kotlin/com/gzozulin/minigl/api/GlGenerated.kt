package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light
import com.gzozulin.minigl.scene.PhongMaterial

private const val DEF_V3VAL = "vec3 v3val ( float v ) { return v3 ( v , v , v ) ; }\n\n"
private const val DEF_V3ZERO = "vec3 v3zero ( ) { return v3val ( 0.0f ) ; }\n\n"
private const val DEF_EQV3 = "bool eqv3 ( vec3 left , vec3 right ) { return left . x == right . x && left . y == right . y && left . z == right . z ; }\n\n"
private const val DEF_NEGV3 = "vec3 negv3 ( vec3 v ) { return v3 ( - v . x , - v . y , - v . z ) ; }\n\n"
private const val DEF_DOTV3 = "float dotv3 ( vec3 left , vec3 right ) { return left . x * right . x + left . y * right . y + left . z * right . z ; }\n\n"
private const val DEF_CROSSV3 = "vec3 crossv3 ( vec3 left , vec3 right ) { return v3 ( left . y * right . z - left . z * right . y , left . z * right . x - left . x * right . z , left . x * right . y - left . y * right . x ) ; }\n\n"
private const val DEF_ADDV3 = "vec3 addv3 ( vec3 left , vec3 right ) { return v3 ( left . x + right . x , left . y + right . y , left . z + right . z ) ; }\n\n"
private const val DEF_SUBV3 = "vec3 subv3 ( vec3 left , vec3 right ) { return v3 ( left . x - right . x , left . y - right . y , left . z - right . z ) ; }\n\n"
private const val DEF_MULV3 = "vec3 mulv3 ( vec3 left , vec3 right ) { return v3 ( left . x * right . x , left . y * right . y , left . z * right . z ) ; }\n\n"
private const val DEF_MULV3F = "vec3 mulv3f ( vec3 left , float right ) { return v3 ( left . x * right , left . y * right , left . z * right ) ; }\n\n"
private const val DEF_DIVV3F = "vec3 divv3f ( vec3 left , float right ) { return v3 ( left . x / right , left . y / right , left . z / right ) ; }\n\n"
private const val DEF_LENV3 = "float lenv3 ( vec3 v ) { return sqrt ( v . x * v . x + v . y * v . y + v . z * v . z ) ; }\n\n"
private const val DEF_NORMV3 = "vec3 normv3 ( vec3 v ) { return divv3f ( v , lenv3 ( v ) ) ; }\n\n"
private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { vec2 result = v2 ( 0.0f , 0.0f ) ; float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; result . x = tileStartX + texCoord . x * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; result . y = tileStartY + texCoord . y * tileSideY ; return result ; }\n\n"
private const val DEF_LUMINOSITY = "float luminosity ( float distance , Light light ) { return 1.0f / ( light . attenConstant + light . attenLinear * distance + light . attenQuadratic * distance * distance ) ; }\n\n"
private const val DEF_DIFFUSECONTRIB = "vec3 diffuseContrib ( vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { float diffuseTerm = dotv3 ( fragNormal , lightDir ) ; if ( diffuseTerm > 0.0 ) { return mulv3f ( material . diffuse , diffuseTerm ) ; } return v3zero ( ) ; }\n\n"
private const val DEF_SPECULARCONTRIB = "vec3 specularContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { vec3 halfVector = normv3 ( addv3 ( viewDir , lightDir ) ) ; float specularTerm = dotv3 ( halfVector , fragNormal ) ; if ( specularTerm > 0.0 ) { return mulv3f ( material . specular , pow ( specularTerm , material . shine ) ) ; } return v3zero ( ) ; }\n\n"
private const val DEF_LIGHTCONTRIB = "vec3 lightContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , float attenuation , Light light , PhongMaterial material ) { vec3 lighting = v3 ( 0.0f , 0.0f , 0.0f ) ; lighting = addv3 ( lighting , diffuseContrib ( lightDir , fragNormal , material ) ) ; lighting = addv3 ( lighting , specularContrib ( viewDir , lightDir , fragNormal , material ) ) ; return mulv3 ( mulv3f ( light . color , attenuation ) , lighting ) ; }\n\n"
private const val DEF_POINTLIGHTCONTRIB = "vec3 pointLightContrib ( vec3 viewDir , vec3 fragPosition , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 direction = subv3 ( light . vector , fragPosition ) ; float distance = lenv3 ( direction ) ; float lum = luminosity ( distance , light ) ; vec3 lightDir = normv3 ( direction ) ; return lightContrib ( viewDir , lightDir , fragNormal , lum , light , material ) ; }\n\n"
private const val DEF_DIRLIGHTCONTRIB = "vec3 dirLightContrib ( vec3 viewDir , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 lightDir = negv3 ( normv3 ( light . vector ) ) ; return lightContrib ( viewDir , lightDir , fragNormal , 1.0f , light , material ) ; }\n\n"
private const val DEF_GETXV4 = "float getxv4 ( vec4 v ) { return v . x ; }\n\n"
private const val DEF_GETYV4 = "float getyv4 ( vec4 v ) { return v . y ; }\n\n"
private const val DEF_GETZV4 = "float getzv4 ( vec4 v ) { return v . z ; }\n\n"
private const val DEF_GETWV4 = "float getwv4 ( vec4 v ) { return v . w ; }\n\n"
private const val DEF_GETRV4 = "float getrv4 ( vec4 v ) { return v . x ; }\n\n"
private const val DEF_GETGV4 = "float getgv4 ( vec4 v ) { return v . y ; }\n\n"
private const val DEF_GETBV4 = "float getbv4 ( vec4 v ) { return v . z ; }\n\n"
private const val DEF_GETAV4 = "float getav4 ( vec4 v ) { return v . w ; }\n\n"
private const val DEF_SETXV4 = "vec4 setxv4 ( vec4 v , float f ) { return v4 ( f , v . y , v . z , v . w ) ; }\n\n"
private const val DEF_SETYV4 = "vec4 setyv4 ( vec4 v , float f ) { return v4 ( v . x , f , v . z , v . w ) ; }\n\n"
private const val DEF_SETZV4 = "vec4 setzv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , f , v . w ) ; }\n\n"
private const val DEF_SETWV4 = "vec4 setwv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n\n"
private const val DEF_SETRV4 = "vec4 setrv4 ( vec4 v , float f ) { return v4 ( f , v . y , v . z , v . w ) ; }\n\n"
private const val DEF_SETGV4 = "vec4 setgv4 ( vec4 v , float f ) { return v4 ( v . x , f , v . z , v . w ) ; }\n\n"
private const val DEF_SETBV4 = "vec4 setbv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , f , v . w ) ; }\n\n"
private const val DEF_SETAV4 = "vec4 setav4 ( vec4 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n\n"

const val PUBLIC_DEFINITIONS = DEF_V3VAL+DEF_V3ZERO+DEF_EQV3+DEF_NEGV3+DEF_DOTV3+DEF_CROSSV3+DEF_ADDV3+DEF_SUBV3+DEF_MULV3+DEF_MULV3F+DEF_DIVV3F+DEF_LENV3+DEF_NORMV3+DEF_TILE+DEF_LUMINOSITY+DEF_DIFFUSECONTRIB+DEF_SPECULARCONTRIB+DEF_LIGHTCONTRIB+DEF_POINTLIGHTCONTRIB+DEF_DIRLIGHTCONTRIB+DEF_GETXV4+DEF_GETYV4+DEF_GETZV4+DEF_GETWV4+DEF_GETRV4+DEF_GETGV4+DEF_GETBV4+DEF_GETAV4+DEF_SETXV4+DEF_SETYV4+DEF_SETZV4+DEF_SETWV4+DEF_SETRV4+DEF_SETGV4+DEF_SETBV4+DEF_SETAV4

fun v2(x: Expression<Float>, y: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "v2(${x.expr()}, ${y.expr()})"
    override fun roots() = listOf(x, y)
}

fun iv2(x: Expression<Int>, y: Expression<Int>) = object : Expression<vec2i>() {
    override fun expr() = "iv2(${x.expr()}, ${y.expr()})"
    override fun roots() = listOf(x, y)
}

fun v3(x: Expression<Float>, y: Expression<Float>, z: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "v3(${x.expr()}, ${y.expr()}, ${z.expr()})"
    override fun roots() = listOf(x, y, z)
}

fun v4(x: Expression<Float>, y: Expression<Float>, z: Expression<Float>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "v4(${x.expr()}, ${y.expr()}, ${z.expr()}, ${w.expr()})"
    override fun roots() = listOf(x, y, z, w)
}

fun v3val(v: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "v3val(${v.expr()})"
    override fun roots() = listOf(v)
}

fun v3zero() = object : Expression<vec3>() {
    override fun expr() = "v3zero()"
    override fun roots() = listOf<Expression<*>>()
}

fun eqv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<Boolean>() {
    override fun expr() = "eqv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun negv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "negv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun dotv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "dotv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun crossv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "crossv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun addv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "addv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun subv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "subv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "mulv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv3f(left: Expression<vec3>, right: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "mulv3f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divv3f(left: Expression<vec3>, right: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "divv3f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun lenv3(v: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "lenv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun normv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "normv3(${v.expr()})"
    override fun roots() = listOf(v)
}

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

fun luminosity(distance: Expression<Float>, light: Expression<Light>) = object : Expression<Float>() {
    override fun expr() = "luminosity(${distance.expr()}, ${light.expr()})"
    override fun roots() = listOf(distance, light)
}

fun diffuseContrib(lightDir: Expression<vec3>, fragNormal: Expression<vec3>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "diffuseContrib(${lightDir.expr()}, ${fragNormal.expr()}, ${material.expr()})"
    override fun roots() = listOf(lightDir, fragNormal, material)
}

fun specularContrib(viewDir: Expression<vec3>, lightDir: Expression<vec3>, fragNormal: Expression<vec3>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "specularContrib(${viewDir.expr()}, ${lightDir.expr()}, ${fragNormal.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, lightDir, fragNormal, material)
}

fun lightContrib(viewDir: Expression<vec3>, lightDir: Expression<vec3>, fragNormal: Expression<vec3>, attenuation: Expression<Float>, light: Expression<Light>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "lightContrib(${viewDir.expr()}, ${lightDir.expr()}, ${fragNormal.expr()}, ${attenuation.expr()}, ${light.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, lightDir, fragNormal, attenuation, light, material)
}

fun pointLightContrib(viewDir: Expression<vec3>, fragPosition: Expression<vec3>, fragNormal: Expression<vec3>, light: Expression<Light>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "pointLightContrib(${viewDir.expr()}, ${fragPosition.expr()}, ${fragNormal.expr()}, ${light.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, fragPosition, fragNormal, light, material)
}

fun dirLightContrib(viewDir: Expression<vec3>, fragNormal: Expression<vec3>, light: Expression<Light>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "dirLightContrib(${viewDir.expr()}, ${fragNormal.expr()}, ${light.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, fragNormal, light, material)
}

fun getxv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getxv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getyv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getyv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getzv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getzv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getwv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getwv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getrv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getrv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getgv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getgv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getbv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getbv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getav4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getav4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun setxv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setxv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setyv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setyv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setzv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setzv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setwv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setwv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setrv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setrv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setgv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setgv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setbv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setbv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setav4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setav4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

