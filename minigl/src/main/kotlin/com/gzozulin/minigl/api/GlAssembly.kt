package com.gzozulin.minigl.api

import java.util.concurrent.atomic.AtomicInteger

const val VERSION = "#version 450\n"
const val PRECISION_HIGH = "precision highp float;\n"

const val V_TEX_COORD = "vTexCoord"
const val GL_FRAG_COORD = "gl_FragCoord.xy"

private const val EXPR_PI = "const float PI = 3.14159265359;\n"

const val MAX_LIGHTS        = 128
const val MAX_BVH           = 128
const val MAX_SPHERES       = 128
const val MAX_LAMBERTIANS   = 16
const val MAX_METALLICS     = 16
const val MAX_DIELECTRICS   = 16

private const val GENERAL_DECL = """
    #define FLT_MAX 3.402823466e+38
    #define FLT_MIN 1.175494351e-38
    #define DBL_MAX 1.7976931348623158e+308
    #define DBL_MIN 2.2250738585072014e-308
    
    #define WIDTH           1024
    #define HEIGHT          768
    #define BOUNCE_ERR      0.001f
"""

private const val ERR_DECL = """
    bool errorFlag = false;
"""

private const val RANDOM_DECL = """
    // https://stackoverflow.com/questions/4200224/random-noise-functions-for-glsl
    // A single iteration of Bob Jenkins' One-At-A-Time hashing algorithm.
    uint hash(uint x) {
        x += (x << 10u);
        x ^= (x >>  6u);
        x += (x <<  3u);
        x ^= (x >> 11u);
        x += (x << 15u);
        return x;
    }
    
    // Compound versions of the hashing algorithm I whipped together.
    uint hash(uvec2 v) { return hash(v.x ^ hash(v.y)                        ); }
    uint hash(uvec3 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z)            ); }
    uint hash(uvec4 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w)); }

    // Construct a float with half-open range [0:1] using low 23 bits.
    // All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
    float floatConstruct(uint m) {
        const uint ieeeMantissa = 0x007FFFFFu; // binary32 mantissa bitmask
        const uint ieeeOne      = 0x3F800000u; // 1.0 in IEEE binary32
        m &= ieeeMantissa;                     // Keep only mantissa bits (fractional part)
        m |= ieeeOne;                          // Add fractional part to 1.0
        float  f = uintBitsToFloat(m);         // Range [1:2]
        return f - 1.0;                        // Range [0:1]
    }
    
    // Pseudo-random value in half-open range [0:1].
    float random(float x) { return floatConstruct(hash(floatBitsToUint(x))); }
    float random(vec2  v) { return floatConstruct(hash(floatBitsToUint(v))); }
    float random(vec3  v) { return floatConstruct(hash(floatBitsToUint(v))); }
    float random(vec4  v) { return floatConstruct(hash(floatBitsToUint(v))); }
    
    vec4 seed = vec4(0.0f, 0.0f, 0.0f, 0.0f);
    vec3 seedRandom(vec3 s) {
        seed.x = s.x;
        seed.y = s.y;
        seed.z = s.z;
        return s;
    }
    
    float randf() {
        seed.w += FLT_MIN;
        return random(seed);
    }
"""

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
    struct Ray {
        vec3 origin;
        vec3 direction;
    };
"""

private const val CAMERA_DECL = """
    struct RtCamera {
        vec3 origin;
        vec3 lowerLeft;
        vec3 horizontal;
        vec3 vertical;
        vec3 w, u, v;
        float lensRadius;
    };
"""

private const val SPHERE_DECL = """
    struct Sphere {
        vec3 center;
        float radius;
        int materialType;
        int materialIndex;
    };
"""

private const val HIT_RECORD_DECL = """
    struct HitRecord {
        float t;
        vec3 point;
        vec3 normal;
        int materialType;
        int materialIndex;
    };
    const HitRecord NO_HIT = { -1, { 0, 0, 0 }, { 1, 0, 0 }, 0, 0 };
"""

private const val SCATTER_RESULT_DECL = """
    struct ScatterResult {
        vec3 attenuation;
        Ray scattered;
    };
    const ScatterResult NO_SCATTER = { { -1, 0, 0 }, { { 0, 0, 0 }, { 0, 0, 0 } } };
"""

private const val REFRACT_RESULT_DECL = """
    struct RefractResult {
        bool isRefracted;
        vec3 refracted;
    };

    const RefractResult NO_REFRACT = { false, { 0, 0, 0 } };
"""

private const val BVH_DECL = """
    struct AABB {
        vec3 pointMin;
        vec3 pointMax;
    };

    struct BvhNode {
        AABB aabb;
        int leftType;
        int leftIndex;
        int rightType;
        int rightIndex;
    };
"""

private const val MATERIALS_DECL = """
    struct LambertianMaterial {
        vec3 albedo;
    };

    struct MetallicMaterial {
        vec3 albedo;
    };
    
    struct DielectricMaterial {
        float reflectiveIndex;
    };
"""

private const val LIGHTS = """
    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];
"""

private const val HITABLES = """
    #define HITABLE_NONE             0
    #define HITABLE_BVH              1
    #define HITABLE_SPHERE           2
    #define MAX_BVH                 128
    
    uniform BvhNode uBvhNodes[$MAX_BVH];
"""

private const val SPHERES = """
    uniform int uSpheresCnt;
    uniform Sphere uSpheres[$MAX_SPHERES];
"""

private const val MATERIALS = """
    #define MATERIAL_LAMBERTIAN     0
    #define MATERIAL_METALIIC       1
    #define MATERIAL_DIELECTRIC     2
    uniform LambertianMaterial     uLambertianMaterials[$MAX_LAMBERTIANS];
    uniform MetallicMaterial       uMetallicMaterials  [$MAX_METALLICS];
    uniform DielectricMaterial     uDielectricMaterials[$MAX_DIELECTRICS];
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

private const val EXPR_DTOF = """
    float dtof(double d) {
        return float(d);
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

private const val PRIVATE_DEFINITIONS =
    "$EXPR_PI\n$GENERAL_DECL\n$ERR_DECL\n$RANDOM_DECL\n$LIGHT_DECL\n$MAT_DECL\n$RAY_DECL\n$CAMERA_DECL\n$SPHERE_DECL\n" +
    "$HIT_RECORD_DECL\n$SCATTER_RESULT_DECL$REFRACT_RESULT_DECL\n\n$BVH_DECL\n$MATERIALS_DECL\n$LIGHTS\n" +
    "$HITABLES\n$SPHERES\n$MATERIALS\n"

private const val CUSTOM_DEFINITIONS = EXPR_ITOF + EXPR_FTOI + EXPR_DTOF + EXPR_V2 + EXPR_V2I + EXPR_V3 + EXPR_V4

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
fun namedGlFragCoordV2() = Named<vec2>(GL_FRAG_COORD)

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

fun unifi(v: Int? = null) = object : Uniform<Int>(null, v) {
    override fun declare() = "uniform int $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifi(p: () -> Int) = object : Uniform<Int>(p, null) {
    override fun declare() = "uniform int $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

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

fun unifs1(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override fun declare() = "uniform sampler1D $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifsb(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override fun declare() = "uniform samplerBuffer $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifsq(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override fun declare() = "uniform samplerCube $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

// ----------------------------- Constants -----------------------------

fun consti(value: Int) = object : Constant<Int>(value) {
    override fun declare() = "const int $name = $value;"
}

fun constf(value: Float) = object : Constant<Float>(value) {
    override fun declare() = "const float $name = $value;"
}

fun constv2(value: vec2) = object : Constant<vec2>(value) {
    override fun declare() = "const vec2 $name = vec2(${value.x}, ${value.y});"
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

fun texel(sampler: Expression<GlTexture>, index: Expression<Int>) = object : Expression<vec4>() {
    override fun expr() = "texelFetch(${sampler.expr()}, ${index.expr()})"
    override fun roots() = listOf(index, sampler)
}

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