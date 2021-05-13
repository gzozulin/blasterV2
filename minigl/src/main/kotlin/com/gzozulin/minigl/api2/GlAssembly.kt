package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.mat4
import com.gzozulin.minigl.api.vec2
import com.gzozulin.minigl.api.vec3
import com.gzozulin.minigl.api.vec4
import java.util.concurrent.atomic.AtomicInteger

const val VERSION = "#version 460\n"
const val PRECISION_HIGH = "precision highp float;\n"
const val CONST_UNIF = "%CONST%\n%UNIF%\n"

const val V_TEX_COORD = "vTexCoord"

private const val EXPR_X = """
    float expr_x(vec4 v) {
        return v.x;
    }
"""

private const val EXPR_Y = """
    float expr_y(vec4 v) {
        return v.y;
    }
"""

private const val EXPR_Z = """
    float expr_z(vec4 v) {
        return v.z;
    }
"""

private const val EXPR_W = """
    float expr_w(vec4 v) {
        return v.w;
    }
"""

private const val EXPR_SET_X = """
    vec4 expr_set_x(vec4 vec, float x) {
        return vec4(x, vec.y, vec.z, vec.w);
    }
"""

private const val EXPR_SET_Y = """
    vec4 expr_set_y(vec4 vec, float y) {
        return vec4(vec.x, y, vec.z, vec.w);
    }
"""

private const val EXPR_SET_Z = """
    vec4 expr_set_z(vec4 vec, float z) {
        return vec4(vec.x, vec.y, z, vec.w);
    }
"""

private const val EXPR_SET_W = """
    vec4 expr_set_w(vec4 vec, float w) {
        return vec4(vec.x, vec.y, vec.z, w);
    }
"""

private const val EXPR_TILE =
    "vec2 expr_tile(vec2 texCoord, ivec2 uv, ivec2 cnt) {\n" +
            "    vec2 result;\n" +
            "    float tileSideX = 1.0 / float(cnt.x);\n" +
            "    float tileStartX = float(uv.x) * tileSideX;\n" +
            "    result.x = tileStartX + texCoord.x * tileSideX;\n" +
            "    \n" +
            "    float tileSideY = 1.0 / float(cnt.y);\n" +
            "    float tileStartY = float(uv.y) * tileSideY;\n" +
            "    result.y = tileStartY + texCoord.y * tileSideY;\n" +
            "    return result;\n" +
            "}\n"

private const val EXPR_DISCARD =
    "vec4 expr_discard() {\n" +
            "    discard;\n" +
            "    return vec4(1.0);\n" +
            "}\n"

private const val EXPR_NEAR =
    "bool expr_near(float left, float right) {\n" +
            "    return abs(left - right) < 0.00001;\n" +
            "}\n"

private const val EXPR_NEAR_V4 =
    "bool expr_near(vec4 left, vec4 right) {\n" +
            "    return " +
            "       expr_near(left.x, right.x) && " +
            "       expr_near(left.y, right.y) && " +
            "       expr_near(left.z, right.z) && " +
            "       expr_near(left.w, right.w);\n" +
            "}\n"

private const val EXPR_LIGHT_DECL = """
    struct Light {
        vec3 vector;
        vec3 color;
        float attenConstant;
        float attenLinear;
        float attenQuadratic;
    };
"""

private const val EXPR_LUMINOSITY = """
    float expr_luminosity(float distance, Light light) {
        return 1.0 / (light.attenConstant + light.attenLinear * distance + light.attenQuadratic * distance * distance);
    }
"""

private const val EXPR_PHONG_MATERIAL_DECL = """
    struct PhongMaterial {
        vec3 ambient;
        vec3 diffuse;
        vec3 specular;
        float shine;
        float transparency;
    };
"""

private const val EXPR_POINT_LIGHT_CONTRIB = """
    vec3 expr_pointLightContrib(vec3 viewDir, vec3 fragPosition, vec3 fragNormal, Light light, PhongMaterial material) {
        vec3 direction = light.vector - fragPosition;
        float distance = length(direction);
        float luminosity = expr_luminosity(distance, light);
        vec3 lightDir = normalize(direction);
        return expr_lightContrib(viewDir, lightDir, fragNormal, luminosity, light, material);
    }
"""

private const val EXPR_DIR_LIGHT_CONTRIB = """
    vec3 expr_dirLightContrib(vec3 viewDir, vec3 fragNormal, Light light, PhongMaterial material) {
        vec3 lightDir = -normalize(light.vector);
        return expr_lightContrib(viewDir, lightDir, fragNormal, 1.0, light, material);
    }
"""

private const val EXPR_LIGHT_CONTRIB = """
    vec3 expr_lightContrib(vec3 viewDir, vec3 lightDir, vec3 fragNormal, float attenuation, Light light, PhongMaterial material) {
        vec3 lighting = vec3(0.0);
        lighting += expr_diffuseContrib(lightDir, fragNormal, material);
        lighting += expr_specularContrib(viewDir, lightDir, fragNormal, material);
        return light.color * attenuation * lighting;
    }
"""

private const val EXPR_DIFFUSE_CONTRIB = """
    vec3 expr_diffuseContrib(vec3 lightDir, vec3 fragNormal, PhongMaterial material) {
        float diffuseTerm = dot(fragNormal, lightDir);
        if (diffuseTerm > 0.0) {
            return material.diffuse * diffuseTerm;
        }
        return vec3(0.0);
    }
"""

private const val EXPR_SPECULAR_CONTRIB = """
    vec3 expr_specularContrib(vec3 viewDir, vec3 lightDir, vec3 fragNormal, PhongMaterial material) {
        vec3 halfVector = normalize(viewDir + lightDir);
        float specularTerm = dot(halfVector, fragNormal);
        if (specularTerm > 0.0) {
            return material.specular * pow(specularTerm, material.shine);
        }
        return vec3(0.0);
    }
"""

const val DECLARATIONS_VERT = EXPR_X + EXPR_Y + EXPR_Z + EXPR_W +
        EXPR_SET_X + EXPR_SET_Y + EXPR_SET_Z + EXPR_SET_W +
        EXPR_TILE + EXPR_NEAR + EXPR_NEAR_V4

const val DECLARATIONS_FRAG = EXPR_X + EXPR_Y + EXPR_Z + EXPR_W +
        EXPR_SET_X + EXPR_SET_Y + EXPR_SET_Z + EXPR_SET_W +
        EXPR_TILE + EXPR_DISCARD + EXPR_NEAR + EXPR_NEAR_V4 +
        EXPR_LIGHT_DECL + EXPR_LUMINOSITY + EXPR_PHONG_MATERIAL_DECL +
        EXPR_DIFFUSE_CONTRIB + EXPR_SPECULAR_CONTRIB + EXPR_LIGHT_CONTRIB +
        EXPR_POINT_LIGHT_CONTRIB + EXPR_DIR_LIGHT_CONTRIB

const val VERT_SHADER_HEADER = "$VERSION\n$PRECISION_HIGH\n$DECLARATIONS_VERT\n$CONST_UNIF"
const val FRAG_SHADER_HEADER = "$VERSION\n$PRECISION_HIGH\n$DECLARATIONS_FRAG\n$CONST_UNIF"

private var next = AtomicInteger()
private fun nextName() = "_v${next.incrementAndGet()}"

// ----------------------------- Expressions -----------------------------

abstract class Expression<T> {
    open val name: String = nextName()
    abstract fun expr(): String
    open fun roots(): List<Expression<*>> = emptyList()
    open fun submit(program: GlProgram) {
        roots().forEach { it.submit(program) }
    }
}

data class Named<T>(val given: String) : Expression<T>() {
    override fun expr() = given
}

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

fun namedv2(name: String) = Named<vec2>(name)
fun namedTexCoords() = Named<vec2>(V_TEX_COORD)

// ----------------------------- Uniforms -----------------------------

fun uniff(v: Float? = null) = object : Uniform<Float>(null, v) {
    override fun declare() = "uniform float $name;"
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

fun unifm4(v: mat4? = null) = object : Uniform<mat4>(null, v) {
    override fun declare() = "uniform mat4 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unifm4(p: () -> mat4) = object : Uniform<mat4>(p, null) {
    override fun declare() = "uniform mat4 $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

fun unift(v: GlTexture? = null) = object : Uniform<GlTexture>(null, v) {
    override fun declare() = "uniform sampler2D $name;"
    override fun submit(program: GlProgram) = glProgramUniform(program, name, value)
}

// ----------------------------- Constants -----------------------------

fun constf(value: Float) = object : Constant<Float>(value) {
    override fun declare() = "const float $name = $value;"
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

// cannot const texture as int
/*fun consts(value: GlTexture) = object : Constant<GlTexture>(value) {
    override fun declare() = "const int $name = ${value.unit};"
}*/

// ----------------------------- Arithmetics -----------------------------

fun <T> add(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} - ${left.expr()})"
    override fun roots() = listOf(left, right)
}

fun <T> sub(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} - ${left.expr()})"
    override fun roots() = listOf(left, right)
}

fun <T> mul(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} * ${left.expr()})"
    override fun roots() = listOf(left, right)
}
fun <T> div(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} / ${left.expr()})"
    override fun roots() = listOf(left, right)
}

// ----------------------------- Substitution -----------------------------

fun tex(texCoord: Expression<vec2>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun expr() = "texture(${sampler.expr()}, ${texCoord.expr()})"
    override fun roots() = listOf(texCoord, sampler)
}

// ----------------------------- Substitution -----------------------------

fun glExprSubstitute(source: String, expressions: Map<String, Expression<*>>): String {
    var result = source
    var uniforms = ""
    var constants = ""
    fun search(expression: Expression<*>) {
        when (expression) {
            is Constant -> constants += "${expression.declare()}\n"
            is Uniform  -> uniforms += "${expression.declare()}\n"
            else        -> expression.roots().forEach { search(it) }
        }
    }
    expressions.forEach { (name, expr) ->
        search(expr)
        result = result.replace("%$name%", expr.expr())
    }
    result = result.replace("%UNIF%", uniforms)
    result = result.replace("%CONST%", constants)
    return result
}