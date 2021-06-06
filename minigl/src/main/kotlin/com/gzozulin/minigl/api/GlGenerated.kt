package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light
import com.gzozulin.minigl.scene.PhongMaterial

private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { vec2 result = v2 ( 0.0f , 0.0f ) ; float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; result . x = tileStartX + texCoord . x * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; result . y = tileStartY + texCoord . y * tileSideY ; return result ; }\n\n"
private const val DEF_LUMINOSITY = "float luminosity ( float distance , Light light ) { return 1.0f / ( light . attenConstant + light . attenLinear * distance + light . attenQuadratic * distance * distance ) ; }\n\n"
private const val DEF_DIFFUSECONTRIB = "vec3 diffuseContrib ( vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { float diffuseTerm = dotv3 ( fragNormal , lightDir ) ; if ( diffuseTerm > 0.0 ) { return mulv3f ( material . diffuse , diffuseTerm ) ; } return v3 ( 0.0f , 0.0f , 0.0f ) ; }\n\n"
private const val DEF_SPECULARCONTRIB = "vec3 specularContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { vec3 halfVector = normv3 ( addv3 ( viewDir , lightDir ) ) ; float specularTerm = dotv3 ( halfVector , fragNormal ) ; if ( specularTerm > 0.0 ) { return mulv3f ( material . specular , powf ( specularTerm , material . shine ) ) ; } return v3 ( 0.0f , 0.0f , 0.0f ) ; }\n\n"
private const val DEF_LIGHTCONTRIB = "vec3 lightContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , float attenuation , Light light , PhongMaterial material ) { vec3 lighting = v3 ( 0.0f , 0.0f , 0.0f ) ; lighting = addv3 ( lighting , diffuseContrib ( lightDir , fragNormal , material ) ) ; lighting = addv3 ( lighting , specularContrib ( viewDir , lightDir , fragNormal , material ) ) ; return mulv3 ( mulv3f ( light . color , attenuation ) , lighting ) ; }\n\n"
private const val DEF_POINTLIGHTCONTRIB = "vec3 pointLightContrib ( vec3 viewDir , vec3 fragPosition , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 direction = subv3 ( light . vector , fragPosition ) ; float distance = lenv3 ( direction ) ; float lum = luminosity ( distance , light ) ; vec3 lightDir = normv3 ( direction ) ; return lightContrib ( viewDir , lightDir , fragNormal , lum , light , material ) ; }\n\n"
private const val DEF_DIRLIGHTCONTRIB = "vec3 dirLightContrib ( vec3 viewDir , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 lightDir = negv3 ( normv3 ( light . vector ) ) ; return lightContrib ( viewDir , lightDir , fragNormal , 1.0f , light , material ) ; }\n\n"

const val PUBLIC_DEFINITIONS = DEF_TILE+DEF_LUMINOSITY+DEF_DIFFUSECONTRIB+DEF_SPECULARCONTRIB+DEF_LIGHTCONTRIB+DEF_POINTLIGHTCONTRIB+DEF_DIRLIGHTCONTRIB

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

