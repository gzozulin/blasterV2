package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.api.*
import java.util.concurrent.atomic.AtomicInteger

const val VERSION = "#version 460"
const val PRECISION_HIGH = "precision highp float;"

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
            "    return abs(left - right) < 0.001;\n" +
            "}\n"

private const val EXPR_NEAR_V4 =
    "bool expr_near(vec4 left, vec4 right) {\n" +
            "    return " +
            "       expr_near(left.x, right.x) && " +
            "       expr_near(left.y, right.y) && " +
            "       expr_near(left.z, right.z) && " +
            "       expr_near(left.w, right.w);\n" +
            "}\n"

private const val EXPR_ATTEN = """
    const float lightConstantAtt    = 0.9;
    const float lightLinearAtt      = 0.7;
    const float lightQuadraticAtt   = 0.3;
    
    float expr_atten(float distance) {
        return 1.0 / (lightConstantAtt + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
    }
"""

private const val EXPR_LIGHT_CONTRIB = """
    // todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
    // https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/
    
    vec3 expr_lightContrib(vec3 viewDir, vec3 lightDir, vec3 fragNormal, vec3 lightIntensity, float attenuation,
                           vec3 matDiffuse, vec3 matSpecular, float shine) {
        vec3 contribution = vec3(0.0);
        if (attenuation < 0.01) {
            return contribution;
        }
        vec3 attenuatedLight = lightIntensity * attenuation;
        // diffuse
        float diffuseTerm = dot(fragNormal, lightDir);
        if (diffuseTerm > 0.0) {
            contribution += diffuseTerm * attenuatedLight * matDiffuse;
        }
        // specular
        vec3 reflectDir = reflect(-lightDir, fragNormal);
        float specularTerm = dot(viewDir, reflectDir);
        if (specularTerm > 0.0) {
            contribution += pow(specularTerm, shine) * attenuatedLight * matSpecular;
        }
        return contribution;
    }
"""

private const val EXPR_POINT_LIGHT_CONTRIB = """
    vec3 expr_pointLightContrib(vec3 viewDir, vec3 fragPosition, vec3 fragNormal, vec3 lightVector, vec3 lightIntensity,
                                vec3 matDiffuse, vec3 matSpecular, float shine) {
        vec3 direction = lightVector - fragPosition;
        float attenuation = expr_atten(length(direction));
        vec3 lightDir = normalize(direction);
        return expr_lightContrib(viewDir, lightDir, fragNormal, lightIntensity, attenuation, matDiffuse, matSpecular, shine);
    }
"""

private const val EXPR_DIR_LIGHT_CONTRIB = """
    vec3 expr_dirLightContrib(vec3 viewDir, vec3 fragNormal, vec3 lightVector, vec3 lightIntensity,
                              vec3 matDiffuse, vec3 matSpecular, float shine) {
        float attenuation = 1.0; // no attenuation
        vec3 lightDir = -normalize(lightVector);
        return expr_lightContrib(viewDir, lightDir, fragNormal, lightIntensity, attenuation, matDiffuse, matSpecular, shine);
    }
"""

const val DECLARATIONS_VERT = EXPR_TILE + EXPR_NEAR + EXPR_NEAR_V4 + EXPR_ATTEN +
        EXPR_LIGHT_CONTRIB + EXPR_POINT_LIGHT_CONTRIB + EXPR_DIR_LIGHT_CONTRIB
const val DECLARATIONS_FRAG = EXPR_TILE + EXPR_DISCARD + EXPR_NEAR + EXPR_NEAR_V4 + EXPR_ATTEN +
        EXPR_LIGHT_CONTRIB + EXPR_POINT_LIGHT_CONTRIB + EXPR_DIR_LIGHT_CONTRIB

private var next = AtomicInteger()
private fun nextName() = "_v${next.incrementAndGet()}"

abstract class Expression<R> {
    open val name: String = nextName()
    open val type: String = "Override!"

    open fun decl(): List<String> = listOf()
    open fun vrbl(): List<String> = listOf()
    open fun expr(): String = ""

    open fun submit(program: GlProgram) {}
}

// ------------------------- Varrying -------------------------

fun <R> varying(givenName: String) = object : Expression<R>() {
    override fun expr() = givenName
}

// ------------------------- Uniforms -------------------------

abstract class Uniform<R>(var value: R?) : Expression<R>() {
    override fun decl() = listOf("uniform $type $name;")
    override fun expr() = name
}

fun uniff(v: Float? = null) = object : Uniform<Float>(v) {
    override val type = "float"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifi(v: Int? = null) = object : Uniform<Int>(v) {
    override val type = "int"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifm4(v: mat4? = null) = object : Uniform<mat4>(v) {
    override val type = "mat4"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifv4(v: vec4? = null) = object : Uniform<vec4>(v) {
    override val type = "vec4"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifv2(v: vec2? = null) = object : Uniform<vec2>(v) {
    override val type = "vec2"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifv2i(v: vec2i? = null) = object : Uniform<vec2i>(v) {
    override val type = "ivec2"
    override fun submit(program: GlProgram) { program.setUniform(name, checkNotNull(value)) }
}

fun unifsampler(v: GlTexture? = null) = object : Uniform<GlTexture>(v) {
    override val type = "sampler2D"
    override fun submit(program: GlProgram) { program.setTexture(name, checkNotNull(value))}
}

// ------------------------- Constants -------------------------

abstract class Const<R>(private var value: R?) : Expression<R>() {
    override fun decl() = listOf("const $type $name = $value;")
    override fun expr() = name
}

fun consti(value: Int) = object : Const<Int>(value) {
    override val type = "int"
}

fun constf(value: Float) = object : Const<Float>(value) {
    override val type = "float"
}

fun constb(value: Boolean) = object : Const<Boolean>(value) {
    override val type = "bool"
}

fun constv4(value: vec4) = object : Const<vec4>(value) {
    override val type = "vec4"
    override fun decl() = listOf("const $type $name = vec4(${value.x}, ${value.y}, ${value.z}, ${value.w});")
}

fun constv2i(value: vec2i) = object : Const<vec2i>(value) {
    override val type = "ivec2"
    override fun decl() = listOf("const $type $name = ivec2(${value.x}, ${value.y});")
}

fun constm4(value: mat4) = object : Const<mat4>(value) {
    override val type = "mat4"
    override fun decl() = listOf("const $type $name = mat4(" +
            "${value.get(0, 0)}, ${value.get(0, 1)}, ${value.get(0, 2)}, ${value.get(0, 3)}, " +
            "${value.get(1, 0)}, ${value.get(1, 1)}, ${value.get(1, 2)}, ${value.get(1, 3)}, " +
            "${value.get(2, 0)}, ${value.get(2, 1)}, ${value.get(2, 2)}, ${value.get(2, 3)}, " +
            "${value.get(3, 0)}, ${value.get(3, 1)}, ${value.get(3, 2)}, ${value.get(3, 3)});")
}

// ------------------------- Variable -------------------------

abstract class Variable<R>(private val expr: Expression<R>) : Expression<R>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl() + listOf("$type $name = ${expr.expr()};")
    override fun expr() = name

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

fun cachev2(expr: Expression<vec2>) = object : Variable<vec2>(expr) {
    override val type = "vec2"
}

fun cachev4(expr: Expression<vec4>) = object : Variable<vec4>(expr) {
    override val type = "vec4"
}

// ------------------------- Addition -------------------------

fun <R> add(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} + ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Subtraction -------------------------

fun <R> sub(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} - ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Multiplication -------------------------

fun <R> mul(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} * ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Division -------------------------

fun <R> div(left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} / ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

// ------------------------- Textures -------------------------

fun tex(texCoord: Expression<vec2>, sampler: Expression<GlTexture>) = object : Expression<vec4>() {
    override fun decl() = texCoord.decl() + sampler.decl()
    override fun vrbl() = texCoord.vrbl() + sampler.vrbl()
    override fun expr() = "texture(${sampler.expr()}, ${texCoord.expr()})"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        sampler.submit(program)
    }
}

// ------------------------- Tile -------------------------

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun decl() = texCoord.decl() + uv.decl() + cnt.decl()
    override fun vrbl() = texCoord.vrbl() + uv.vrbl() + cnt.vrbl()
    override fun expr() = "expr_tile(${texCoord.expr()}, ${uv.expr()}, ${cnt.expr()})"

    override fun submit(program: GlProgram) {
        texCoord.submit(program)
        uv.submit(program)
        cnt.submit(program)
    }
}

// ------------------------- Discard -------------------------

fun <R> discard() = object : Expression<R>() {
    override fun expr() = "expr_discard()"
}

// ------------------------- Boolean -------------------------

fun <R> ifexp(check: Expression<Boolean>, left: Expression<R>, right: Expression<R>) = object : Expression<R>() {
    override fun decl() = check.decl() + left.decl() + right.decl()
    override fun vrbl() = check.vrbl() + left.vrbl() + right.vrbl()
    override fun expr() = "((${check.expr()}) ? ${left.expr()} : ${right.expr()})"

    override fun submit(program: GlProgram) {
        check.submit(program)
        left.submit(program)
        right.submit(program)
    }
}

fun <R> eq(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "(${left.expr()} == ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun <R> near(left: Expression<R>, right: Expression<R>) = object : Expression<Boolean>() {
    override fun decl() = left.decl() + right.decl()
    override fun vrbl() = left.vrbl() + right.vrbl()
    override fun expr() = "expr_near(${left.expr()}, ${right.expr()})"

    override fun submit(program: GlProgram) {
        left.submit(program)
        right.submit(program)
    }
}

fun not(expr: Expression<Boolean>) = object : Expression<Boolean>() {
    override fun decl() = expr.decl()
    override fun vrbl() = expr.vrbl()
    override fun expr() = expr.expr() + listOf("(!${expr.expr()})")

    override fun submit(program: GlProgram) {
        expr.submit(program)
    }
}

// todo: more, less, boundary

// ------------------------- Accessors -------------------------

// x, y, z, w, u, v, swizzles