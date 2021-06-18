package com.gzozulin.minigl.api

import java.util.concurrent.atomic.AtomicInteger

const val VERSION = "#version 460\n"
const val PRECISION_HIGH = "precision highp float;\n"

const val MAX_LIGHTS = 128

const val V_TEX_COORD = "vTexCoord"

private const val EXPR_PI = "const float PI = 3.14159265359;\n"

private const val LIGHT_DECL = """
    struct Light {
        vec3 vector;
        vec3 color;
        float attenConstant;
        float attenLinear;
        float attenQuadratic;
    };
"""

private const val MAT_DECL = """
    struct PhongMaterial {
        vec3 ambient;
        vec3 diffuse;
        vec3 specular;
        float shine;
        float transparency;
    };
"""

private const val RAY_DECL = """
    struct ray {
        vec3 origin;
        vec3 direction;
    };
"""

private const val SPHERE_DECL = """
    struct Sphere {
        vec3 center;
        float radius;
    };
"""

private const val HIT_RECORD_DECL = """
    struct HitRecord {
        float t;
        vec3 point;
        vec3 normal;
    };
"""

private const val LIGHTS = """
    const int MAX_LIGHTS = 128;
    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];
"""

private const val EXPR_DISCARD =
    "vec4 expr_discard() {\n" +
            "    discard;\n" +
            "    return vec4(1.0);\n" +
            "}\n"

private const val EXPR_ITOF = """
    float itof(int i) {
        return float(i);
    }
"""

private const val EXPR_FTOI = """
    float ftoi(float f) {
        return int(f);
    }
"""

private const val EXPR_V2 = """
    vec2 v2(float x, float y) {
        return vec2(x, y);
    }
"""

private const val EXPR_V2I = """
    ivec2 v2i(int x, int y) {
        return ivec2(x, y);
    }
"""

private const val EXPR_V3 = """
    vec3 v3(float x, float y, float z) {
        return vec3(x, y, z);
    }
"""

private const val EXPR_V4 = """
    vec4 v4(float x, float y, float z, float w) {
        return vec4(x, y, z, w);
    }
"""

private const val EXPR_GET_NORMAL = """
    vec3 getNormalFromMap(vec3 normal, vec3 worldPos, vec2 texCoord, vec3 vnormal) {
        vec3 tangentNormal = normal * 2.0 - 1.0;

        vec3 Q1  = dFdx(worldPos);
        vec3 Q2  = dFdy(worldPos);
        vec2 st1 = dFdx(texCoord);
        vec2 st2 = dFdy(texCoord);

        vec3 N   = normalize(vnormal);
        vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
        vec3 B  = -normalize(cross(N, T));
        mat3 TBN = mat3(T, B, N);

        return normalize(TBN * tangentNormal);
    }
"""

private const val PRIVATE_DEFINITIONS = "$EXPR_PI\n$LIGHT_DECL\n$MAT_DECL\n$RAY_DECL\n$SPHERE_DECL\n$HIT_RECORD_DECL\n$LIGHTS\n"

private const val CUSTOM_DEFINITIONS = EXPR_ITOF + EXPR_FTOI + EXPR_V2 + EXPR_V2I + EXPR_V3 + EXPR_V4

private const val MAIN_DECL = "void main() {"

const val VERT_SHADER_HEADER = "$VERSION\n$PRECISION_HIGH\n" +
        "$PRIVATE_DEFINITIONS\n$CUSTOM_DEFINITIONS\n$PUBLIC_DEFINITIONS\n"
const val FRAG_SHADER_HEADER = VERT_SHADER_HEADER + "$EXPR_GET_NORMAL\n$EXPR_DISCARD\n"

private var next = AtomicInteger()
private fun nextName() = "_v${next.incrementAndGet()}"

abstract class Expression<T> {
    open val name: String = nextName()
    abstract fun expr(): String
    open fun roots(): List<Expression<*>> = emptyList()
    open fun submit(program: GlProgram) {
        roots().forEach { it.submit(program) }
    }
}

// ----------------------------- Substitution -----------------------------

fun glExprSubstitute(source: String, expressions: Map<String, Expression<*>>): String {
    var result = source
    var uniforms = ""
    var constants = ""
    var cache = ""
    fun search(expression: Expression<*>) {
        if (expression is Cache) {
            cache += "${expression.declare()}\n"
        }
        when (expression) {
            is Constant -> constants    += "${expression.declare()}\n"
            is Uniform  -> uniforms     += "${expression.declare()}\n"
            else        -> expression.roots().forEach { search(it) }
        }
    }
    expressions.forEach { (name, expr) ->
        search(expr)
        check(source.contains("%$name%")) { "Expression $name was not found in source!" }
        result = result.replace("%$name%", expr.expr())
    }
    uniforms = uniforms.lines().distinct().joinToString("\n")
    constants = constants.lines().distinct().joinToString("\n")
    cache = cache.lines().distinct().joinToString("\n")
    check(result.contains(MAIN_DECL)) { "Main is not declared properly: $MAIN_DECL\n$source" }
    result = result.replace(MAIN_DECL, "$uniforms\n$MAIN_DECL")
    result = result.replace(MAIN_DECL, "$constants\n$MAIN_DECL")
    result = result.replace(MAIN_DECL, "$MAIN_DECL\n$cache\n")
    return result
}

// ----------------------------- Expressions -----------------------------

data class Named<T>(val given: String) : Expression<T>() {
    override fun expr() = given
}

fun namedTexCoordsV2() = Named<vec2>(V_TEX_COORD)
fun namedTexCoordsV3() = Named<vec3>(V_TEX_COORD)

abstract class Uniform<T>(private val p: (() -> T)?, private var v: T?) : Expression<T>() {

    override fun expr() = name
    abstract fun declare(): String

    var value: T
        get() = if (p != null) { p.invoke() } else { v!! }
        set(new) {
            check(p == null) { "This uniform already has a provider!" }
            v = new
        }
}

abstract class Constant<T>(internal val value: T) : Expression<T>() {
    override fun expr() = name
    abstract fun declare(): String
}

abstract class Cache<T> : Expression<T>() {
    override fun expr() = name
    abstract fun declare(): String
}

// ----------------------------- Uniforms -----------------------------

fun uniff(v: Float? = null) = object : Uniform<Float>(null, v) {
    override fun declare() = "uniform float $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun uniff(p: () -> Float) = object : Uniform<Float>(p, null) {
    override fun declare() = "uniform float $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv2i(v: vec2i? = null) = object : Uniform<vec2i>(null, v) {
    override fun declare() = "uniform ivec2 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv2i(x: Int, y: Int) = object : Uniform<vec2i>(null, vec2i(x, y)) {
    override fun declare() = "uniform ivec2 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv2i(p: () -> vec2i) = object : Uniform<vec2i>(p, null) {
    override fun declare() = "uniform ivec2 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv2(v: vec2? = null) = object : Uniform<vec2>(null, v) {
    override fun declare() = "uniform vec2 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv3(v: vec3? = null) = object : Uniform<vec3>(null, v) {
    override fun declare() = "uniform vec3 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv3(p: () -> vec3) = object : Uniform<vec3>(p, null) {
    override fun declare() = "uniform vec3 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifv4(v: vec4? = null) = object : Uniform<vec4>(null, v) {
    override fun declare() = "uniform vec4 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifm4(v: mat4? = null) = object : Uniform<mat4>(null, v) {
    override fun declare() = "uniform mat4 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifm4(p: () -> mat4) = object : Uniform<mat4>(p, null) {
    override fun declare() = "uniform mat4 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifs(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override fun declare() = "uniform sampler2D $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifs(p: () -> GlTexture) = object : Uniform<GlTexture>(p, null) {
    override fun declare() = "uniform sampler2D $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifsq(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override fun declare() = "uniform samplerCube $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

// ----------------------------- Constants -----------------------------

fun constf(value: Float) = object : Constant<Float>(value) {
    override fun declare() = "const float $name = $value;"
}

fun constv2i(value: vec2i) = object : Constant<vec2i>(value) {
    override fun declare() = "const ivec2 $name = ivec2(${value.x}, ${value.y});"
}

fun constv2i(x: Int, y: Int) = object : Constant<vec2i>(vec2i(x, y)) {
    override fun declare() = "const ivec2 $name = ivec2(${value.x}, ${value.y});"
}

fun constv3(value: vec3) = object : Constant<vec3>(value) {
    override fun declare() = "const vec3 $name = vec3(${value.x}, ${value.y}, ${value.z});"
}

fun constv4(value: vec4) = object : Constant<vec4>(value) {
    override fun declare() = "const vec4 $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});"
}

fun constm4(value: mat4) = object : Constant<mat4>(value) {
    override fun declare() = "const mat4 $name = mat4(" +
            "${value.get(0, 0)}, ${value.get(0, 1)}, ${value.get(0, 2)}, ${value.get(0, 3)}, " +
            "${value.get(1, 0)}, ${value.get(1, 1)}, ${value.get(1, 2)}, ${value.get(1, 3)}, " +
            "${value.get(2, 0)}, ${value.get(2, 1)}, ${value.get(2, 2)}, ${value.get(2, 3)}, " +
            "${value.get(3, 0)}, ${value.get(3, 1)}, ${value.get(3, 2)}, ${value.get(3, 3)});"
}

// ----------------------------- Cache -----------------------------

fun cachev4(value: Expression<vec4>) = object : Cache<vec4>() {
    override fun declare() = "vec4 $name = ${value.expr()};"
    override fun roots() = listOf(value)
}

// ----------------------------- Sampler -----------------------------

fun sampler(sampler: Expression<GlTexture>, texCoord: Expression<vec2> = namedTexCoordsV2()) = object : Expression<vec4>() {
    override fun expr() = "texture(${sampler.expr()}, ${texCoord.expr()})"
    override fun roots() = listOf(texCoord, sampler)
}

fun samplerq(texCoord: Expression<vec3>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun expr() = "texture(${sampler.expr()}, ${texCoord.expr()})"
    override fun roots() = listOf(texCoord, sampler)
}

// ------------------------- Discard -------------------------

fun <R> discard() = object : Expression<R>() {
    override fun expr() = "expr_discard()"
}

// ------------------------- Boolean -------------------------

fun <R> ifexp(check: Expression<Boolean>, left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun expr() = "((${check.expr()}) ? ${left.expr()} : ${right.expr()})"
    override fun roots() = listOf(check, left, right)
}

fun <R> more(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun expr() = "(${left.expr()} > ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun not(expr: Expression<Boolean>) = object : Expression<Boolean>() {
    override fun expr() = expr.expr() + listOf("(!${expr.expr()})")
    override fun roots() = listOf(expr)
}