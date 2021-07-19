package com.gzozulin.minigl.api

const val VERSION = "#version 450\n"
const val PRECISION_HIGH = "precision highp float;\n"

internal const val MAIN_DECL = "void main() {"

const val V_TEX_COORD = "vTexCoord"
const val GL_FRAG_COORD = "gl_FragCoord.xy"

const val MAX_LIGHTS        = 128
const val MAX_BVH           = 512
const val MAX_SPHERES       = 256
const val MAX_LAMBERTIANS   = 16
const val MAX_METALLICS     = 16
const val MAX_DIELECTRICS   = 16

private const val CUSTOM_DEF = """
    #define FLT_MAX 3.402823466e+38
    #define FLT_MIN 1.175494351e-38
    
    #define DBL_MAX 1.7976931348623158e+308
    #define DBL_MIN 2.2250738585072014e-308
    
    #define HITABLE_BVH              0
    #define HITABLE_SPHERE           1
    
    #define MATERIAL_LAMBERTIAN      0
    #define MATERIAL_METALIIC        1
    #define MATERIAL_DIELECTRIC      2
    
    bool errorFlag = false;
    
    uniform int uLightsPointCnt;
    uniform int uLightsDirCnt;
    uniform Light uLights[$MAX_LIGHTS];
    
    #define MAX_BVH $MAX_BVH
    uniform BvhNode uBvhNodes[$MAX_BVH];
    int bvhStack[$MAX_BVH];
    int bvhTop = 0;
    
    uniform Sphere                 uSpheres[$MAX_SPHERES];
    
    uniform LambertianMaterial     uLambertianMaterials[$MAX_LAMBERTIANS];
    uniform MetallicMaterial       uMetallicMaterials  [$MAX_METALLICS];
    uniform DielectricMaterial     uDielectricMaterials[$MAX_DIELECTRICS];
"""

private const val CUSTOM_RANDOM_DEF = """
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
    
    float randomFloat() {
        seed.w += FLT_MIN;
        return random(seed);
    }
"""

private const val CUSTOM_FRAG_DEF = """
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
    
    vec4 expr_discard() { 
        discard; return vec4(1.0); 
    }
"""

private const val CUSTOM_MATH_DEF = """
    float itof(int i) {
        return float(i);
    }
    
    float ftoi(float f) {
        return int(f);
    }
    
    float dtof(double d) {
        return float(d);
    }
    
    vec2 v2(float x, float y) {
        return vec2(x, y);
    }
    
    ivec2 v2i(int x, int y) {
        return ivec2(x, y);
    }
    
    vec3 v3(float x, float y, float z) {
        return vec3(x, y, z);
    }
    
    vec4 v4(float x, float y, float z, float w) {
        return vec4(x, y, z, w);
    }
"""

private const val CUSTOM_MAT4_DEF = """
    mat4 m4ident() {
        return mat4(1.0);
    }
    
    mat4 mulm4(mat4 left, mat4 right) {
        return left * right;
    }
    
    mat4 translatem4(vec3 vec) {
        return mat4(1.0, 0.0, 0.0, 0.0,  0.0, 1.0, 0.0, 0.0,  0.0, 0.0, 1.0, 0.0,  vec.x, vec.y, vec.z, 1.0);
    }
    
    vec4 transformv4(vec4 vec, mat4 mat) {
        return mat * vec;
    }
"""


const val VERT_SHADER_HEADER = "$VERSION\n$PRECISION_HIGH\n$TYPES_DEF\n" +
        "$CUSTOM_DEF\n$CUSTOM_RANDOM_DEF\n$CUSTOM_MATH_DEF\n$CUSTOM_MAT4_DEF\n$CONST_DEF\n$OPS_DEF\n"
const val FRAG_SHADER_HEADER = VERT_SHADER_HEADER + "$CUSTOM_FRAG_DEF\n"