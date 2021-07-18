#include <assert.h>
#include <math.h>
#include <stdbool.h>
#include <float.h>
#include <stdlib.h>
#include <stdio.h>

#define public      // define and add handle
#define custom      // ops: handle only, definition is custom
#define protected   // ops: definiton only, no handle

// ------------------- TYPES -------------------

typedef struct {
    float x;
    float y;
} vec2;

typedef struct {
    int x;
    int y;
} ivec2;

typedef struct {
    float x;
    float y;
    float z;
} vec3;

typedef struct {
    float x;
    float y;
    float z;
    float w;
} vec4;

typedef struct {
    float value;
} mat3;

typedef struct {
    float value;
} mat4;

public
typedef struct {
    vec3 origin;
    vec3 direction;
} Ray;

public
typedef struct {
    vec3 pointMin;
    vec3 pointMax;
} AABB;

public
typedef struct {
    vec3 origin;
    vec3 lowerLeft;
    vec3 horizontal;
    vec3 vertical;
    vec3 w, u, v;
    float lensRadius;
} RtCamera;

public
typedef struct {
    vec3 vector;
    vec3 color;
    float attenConstant;
    float attenLinear;
    float attenQuadratic;
} Light;

public
typedef struct {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shine;
    float transparency;
} PhongMaterial;

public
typedef struct {
    AABB aabb;
    int leftType;
    int leftIndex;
    int rightType;
    int rightIndex;
} BvhNode;

public
typedef struct {
    vec3 center;
    float radius;
    int materialType;
    int materialIndex;
} Sphere;

public
typedef struct {
    vec3 albedo;
} LambertianMaterial;

public
typedef struct {
    vec3 albedo;
} MetallicMaterial;

public
typedef struct {
    float reflectiveIndex;
} DielectricMaterial;

public
typedef struct {
    float t;
    vec3 point;
    vec3 normal;
    int materialType;
    int materialIndex;
} HitRecord;

public
typedef struct {
    vec3 attenuation;
    Ray scattered;
} ScatterResult;

public
typedef struct {
    bool isRefracted;
    vec3 refracted;
} RefractResult;

// ------------------- DEFINE -------------------

#define MAX_LIGHTS              128
#define MAX_BVH                 512
#define MAX_SPHERES             256
#define MAX_LAMBERTIANS         16
#define MAX_METALS              16
#define MAX_DIELECTRICS         16

// Corresponds to HitableType
#define HITABLE_BVH 0
#define HITABLE_SPHERE 1

// Corresponds to MaterialType
#define MATERIAL_LAMBERTIAN 0
#define MATERIAL_METALIIC 1
#define MATERIAL_DIELECTRIC 2

// ------------------- PUBLIC CONST -------------------

public
const float PI = 3.14159265359f;

public
const float BOUNCE_ERR = 0.001f;

public
const HitRecord NO_HIT = { -1, { 0, 0, 0 }, { 1, 0, 0 }, 0, 0 };

public
const ScatterResult NO_SCATTER = { { -1, -1, -1 }, { { 0, 0, 0 }, { 0, 0, 0 } } };

public
const RefractResult NO_REFRACT = { false, { 0, 0, 0 } };

// ------------------- PRIVATE CONST -------------------

const int uLightsPointCnt = 1;
const int uLightsDirCnt = 0;
const Light uLights[MAX_LIGHTS] = {
        { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 1.0f }, 1.0f, 1.0f, 1.0f }
};

const BvhNode uBvhNodes[MAX_BVH] = {
        { { { -100, -100,  -100 }, { 100, 100, 100 } }, HITABLE_BVH,     1, HITABLE_BVH,   2 },
        { { { -100, -100,  -100 }, {  0,   0,    0 } }, HITABLE_SPHERE,  0,   -1,         -1 },
        { { {    0,    0,     0 }, { 100, 100, 100 } }, HITABLE_SPHERE,  1,   -1,         -1 }
};

int bvhStack[MAX_BVH];
int bvhTop = 0;

const Sphere uSpheres[MAX_SPHERES] = {
        { { -50, -50, -50 }, 50, 0, 0 },
        { {  50,  50,  50 }, 50, 0, 1 }
};

const LambertianMaterial uLambertianMaterials[MAX_LAMBERTIANS] = {
        { { 1, 0, 0 } },
        { { 0, 1, 0 } }
};
const MetallicMaterial   uMetallicMaterials  [MAX_METALS] = { { { 0, 1, 0 } } };
const DielectricMaterial uDielectricMaterials[MAX_DIELECTRICS] = { { 2 } };

// ------------------- ERROR -------------------

bool errorFlag = false;

public
int flagError() {
    // assert(1);
    errorFlag = true;
    return 1;
}

// ------------------- CASTS -------------------

custom
float itof(const int i) {
    return (float) i;
}

custom
int ftoi(const float f) {
    return (int) f;
}

custom
float dtof(const double d) {
    return (float) d;
}

// ------------------- VEC2 -------------------

custom
vec2 v2(const float x, const float y) {
    return (vec2) { x, y };
}

public
vec2 ftov2(const float v) {
    return v2(v, v);
}

public
vec2 v2zero() {
    return ftov2(0.0f);
}

// ------------------- IVEC2 -------------------

custom
ivec2 iv2(const int x, const int y) {
    return (ivec2) { x, y };
}

// ------------------- VEC3 -------------------

custom
vec3 v3(const float x, const float y, const float z) {
    return (vec3) { x, y, z };
}

public
vec3 v2tov3(vec2 v, const float f) {
    return v3(v.x, v.y, f);
}

public
vec3 ftov3(const float v) {
    return v3(v, v, v);
}

public
vec3 v3zero() {
    return ftov3(0.0f);
}

public
vec3 v3one() {
    return ftov3(1.0f);
}

public
vec3 v3front() {
    return v3(0, 0, 1);
}

public
vec3 v3back() {
    return v3(0, 0, -1);
}

public
vec3 v3left() {
    return v3(-1, 0, 0);
}

public
vec3 v3right() {
    return v3(1, 0, 0);
}

public
vec3 v3up() {
    return v3(0, 1, 0);
}

public
vec3 v3down() {
    return v3(0, -1, 0);
}

public
vec3 v3white() {
    return v3(1.0f, 1.0f, 1.0f);
}

public
vec3 v3black() {
    return v3(0.0f, 0.0f, 0.0f);
}
public
vec3 v3ltGrey() {
    return ftov3(0.3f);
}

public
vec3 v3grey() {
    return ftov3(0.5f);
}

public
vec3 v3dkGrey() {
    return ftov3(0.7f);
}

public
vec3 v3red() {
    return v3(1.0f, 0.0f, 0.0f);
}

public
vec3 v3green() {
    return v3(0.0f, 1.0f, 0.0f);
}

public
vec3 v3blue() {
    return v3(0.0f, 0.0f, 1.0f);
}

public
vec3 v3yellow() {
    return v3(1.0f, 1.0f, 0.0f);
}

public
vec3 v3magenta() {
    return v3(1.0f, 0.0f, 1.0f);
}

public
vec3 v3cyan() {
    return v3(0.0f, 1.0f, 1.0f);
}

public
vec3 v3orange() {
    return v3(1.0f, 0.5f, 0.0f);
}
public
vec3 v3rose() {
    return v3(1.0f, 0.0f, 0.5f);
}

public
vec3 v3violet() {
    return v3(0.5f, 0.0f, 1.0f);
}

public
vec3 v3azure() {
    return v3(0.0f, 0.5f, 1.0f);
}

public
vec3 v3aquamarine() {
    return v3(0.0f, 1.0f, 0.5f);
}

public
vec3 v3chartreuse() {
    return v3(0.5f, 1.0f, 0.0f);
}

// ------------------- VEC4 -------------------

custom
vec4 v4(const float x, const float y, const float z, const float w) {
    return (vec4) { x, y, z, w };
}

public
vec4 v3tov4(const vec3 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

public
vec4 ftov4(const float v) {
    return v4(v, v, v, v);
}

public
vec4 v4zero() {
    return ftov4(0.0f);
}

// ------------------- MAT4, RAY -------------------

custom
mat3 m3ident() {
    return (mat3) { 0.0f };
}

public
Ray rayBack() {
    const Ray result = { v3zero(), v3back() };
    return result;
}

// ------------------- GETRS ---------------

public
float indexv3(const vec3 v, const int index) {
    switch (index) {
        case 0: return v.x;
        case 1: return v.y;
        case 2: return v.z;
        default:
            flagError(); return v.x;
    }
}

public
float getxv4(const vec4 v) {
    return v.x;
}

public
float getyv4(const vec4 v) {
    return v.y;
}

public
float getzv4(const vec4 v) {
    return v.z;
}

public
float getwv4(const vec4 v) {
    return v.w;
}

public
float getrv4(const vec4 v) {
    return v.x;
}

public
float getgv4(const vec4 v) {
    return v.y;
}

public
float getbv4(const vec4 v) {
    return v.z;
}

public
float getav4(const vec4 v) {
    return v.w;
}

public
float getxv2(const vec2 v) {
    return v.x;
}

public
float getyv2(const vec2 v) {
    return v.y;
}

public
float getuv2(const vec2 v) {
    return v.x;
}

public
float getvv2(const vec2 v) {
    return v.y;
}

// ------------------- SETRS ---------------

public
vec4 setxv4(const vec4 v, const float f) {
    return v4(f, v.y, v.z, v.w);
}

public
vec4 setyv4(const vec4 v, const float f) {
    return v4(v.x, f, v.z, v.w);
}

public
vec4 setzv4(const vec4 v, const float f) {
    return v4(v.x, v.y, f, v.w);
}

public
vec4 setwv4(const vec4 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

public
vec4 setrv4(const vec4 v, const float f) {
    return v4(f, v.y, v.z, v.w);
}

public
vec4 setgv4(const vec4 v, const float f) {
    return v4(v.x, f, v.z, v.w);
}

public
vec4 setbv4(const vec4 v, const float f) {
    return v4(v.x, v.y, f, v.w);
}

public
vec4 setav4(const vec4 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

// ------------------- BOOL -------------------

public
bool eqv2(const vec2 left, const vec2 right) {
    return left.x == right.x && left.y == right.y;
}

public
bool eqv3(const vec3 left, const vec3 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z;
}

public
bool eqv4(const vec4 left, const vec4 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z && left.w == right.w;
}

// ------------------- MATH -------------------

#define sqrt sqrtf
#define pow powf
#define tan tanf
#define min fminf
#define max fmaxf
#define cos cosf
#define sin cosf

public
float sqrtv(const float value) {
    return sqrt(value);
}

public
float sinv(const float rad) {
    return sin(rad);
}

public
float cosv(const float rad) {
    return cos(rad);
}

public
float tanv(const float rad) {
    return tan(rad);
}

public
float powv(const float base, const float power) {
    return pow(base, power);
}

public
float minv(const float left, const float right) {
    return min(left, right);
}

public
float maxv(const float left, const float right) {
    return max(left, right);
}

public
vec3 negv3(const vec3 v) {
    return v3(-v.x, -v.y, -v.z);
}

public
float addf(const float left, const float right) {
    return left + right;
}

public
float subf(const float left, const float right) {
    return left - right;
}

public
float mulf(const float left, const float right) {
    return left * right;
}

public
float divf(const float left, const float right) {
    return left / right;
}

public
float dotv3(const vec3 left, const vec3 right) {
    return left.x * right.x + left.y * right.y + left.z * right.z;
}

public
vec3 crossv3(const vec3 left, const vec3 right) {
    return v3(
            left.y * right.z - left.z * right.y,
            left.z * right.x - left.x * right.z,
            left.x * right.y - left.y * right.x);
}

public
vec3 addv3(const vec3 left, const vec3 right) {
    return v3(left.x + right.x, left.y + right.y, left.z + right.z);
}

public
vec3 subv3(const vec3 left, const vec3 right) {
    return v3(left.x - right.x, left.y - right.y, left.z - right.z);
}

public
vec3 mulv3(const vec3 left, const vec3 right) {
    return v3(left.x * right.x, left.y * right.y, left.z * right.z);
}

public
vec3 mulv3f(const vec3 left, const float right) {
    return v3(left.x * right, left.y * right, left.z * right);
}

public
vec3 powv3(const vec3 left, const vec3 right) {
    return v3(pow(left.x, right.x), pow(left.y, right.y), pow(left.z, right.z));
}

public
vec3 divv3f(const vec3 left, const float right) {
    return v3(left.x / right, left.y / right, left.z / right);
}

public
vec3 divv3(const vec3 left, const vec3 right) {
    return v3(left.x / right.x, left.y / right.y, left.z / right.z);
}

public
vec3 mixv3(const vec3 left, const vec3 right, const float proportion) {
    return addv3(mulv3(left, ftov3(1.0f - proportion)), mulv3(right, ftov3(proportion)));
}

public
vec4 addv4(const vec4 left, const vec4 right) {
    return v4(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
}

public
vec4 subv4(const vec4 left, const vec4 right) {
    return v4(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
}

public
vec4 mulv4(const vec4 left, const vec4 right) {
    return v4(left.x * right.x, left.y * right.y, left.z * right.z, left.w * right.w);
}

public
vec4 mulv4f(const vec4 left, const float right) {
    return v4(left.x * right, left.y * right, left.z * right, left.w * right);
}

public
vec4 divv4(const vec4 left, const vec4 right) {
    return v4(left.x / right.x, left.y / right.y, left.z / right.z, left.w / right.w);
}

public
vec4 divv4f(const vec4 left, const float right) {
    return v4(left.x / right, left.y / right, left.z / right, left.z / right);
}

public
float lenv3(const vec3 v) {
    return sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
}

public
float lensqv3(const vec3 v) {
    return (v.x*v.x + v.y*v.y + v.z*v.z);
}

public
vec3 normv3(const vec3 v) {
    return divv3f(v, lenv3(v));
}

public
vec3 lerpv3(const vec3 from, const vec3 to, const float t) {
    return addv3(mulv3f(from, 1.0f - t), mulv3f(to, t));
}

public
vec3 rayPoint(const Ray ray, const float t) {
    return addv3(ray.origin, mulv3f(ray.direction, t));
}

public
float schlick(float cosine, float ri) {
    float r0 = (1 - ri) / (1 + ri);
    r0 = r0*r0;
    return r0 + (1 - r0) * pow((1 - cosine), 5);
}

public
vec3 reflectv3(const vec3 v, const vec3 n) {
    return subv3(v, mulv3f(n, 2.0f * dotv3(v, n)));
}

protected
RefractResult refractv3(const vec3 v, const vec3 n, const float niOverNt) {
    const vec3 unitV = normv3(v);
    const float dt = dotv3(unitV, n);
    const float D = 1.0f - niOverNt*niOverNt*(1.0f - dt*dt);
    if (D > 0) {
        const vec3 left = mulv3f(subv3(unitV, mulv3f(n, dt)), niOverNt);
        const vec3 right = mulv3f(n, sqrt(D));
        const RefractResult result = { true, subv3(left, right) };
        return result;
    } else {
        return NO_REFRACT;
    }
}

// ------------------- RAND -------------------

custom
vec3 seedRandom(const vec3 s) {
    return s;
}

custom
float randomFloat() {
    return dtof(drand48());
}

public
vec3 randomInUnitSphere() {
    vec3 result;
    for (int i = 0; i < 10; i++) {
        result = v3(randomFloat() * 2.0f - 1.0f, randomFloat() * 2.0f - 1.0f, randomFloat() * 2.0f - 1.0f);
        if (lensqv3(result) >= 1.0f) {
            return result;
        }
    }
    return normv3(result);
}

public
vec3 randomInUnitDisk() {
    vec3 result;
    for (int i = 0; i < 10; i++) {
        result = subv3(mulv3f(v3(randomFloat(), randomFloat(), 0.0f), 2.0f), v3(1.0f, 1.0f, 0.0f));
        if (dotv3(result, result) >= 1.0f) {
            return result;
        }
    }
    return normv3(result); // wrong, but should not happen
}

// ------------------- ERR_HANDLER ---------------

public
vec4 errorHandler(vec4 color) {
    if (errorFlag) {
        vec3 signal;
        float check = randomFloat();
        if (check > 0.6f) {
            signal = v3red();
        } else if (check > 0.3f) {
            signal = v3blue();
        } else {
            signal = v3green();
        }
        return v3tov4(signal, 1.0f);
    } else {
        return color;
    }
}

// ------------------- TILE ---------------

public
vec2 tile(const vec2 texCoord, const ivec2 uv, const ivec2 cnt) {
    float tileSideX = 1.0f / itof(cnt.x);
    float tileStartX = itof(uv.x) * tileSideX;
    float tileSideY = 1.0f / itof(cnt.y);
    float tileStartY = itof(uv.y) * tileSideY;
    return v2(tileStartX + texCoord.x * tileSideX, tileStartY + texCoord.y * tileSideY);
}

// ------------------- SHADING ---------------

// todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
// https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/

public
float luminosity(const float distance, const Light light) {
    return 1.0f / (light.attenConstant + light.attenLinear * distance + light.attenQuadratic * distance * distance);
}

public
vec3 diffuseContrib(const vec3 lightDir, const vec3 fragNormal, const PhongMaterial material) {
    float diffuseTerm = dotv3(fragNormal, lightDir);
    return diffuseTerm > 0.0f ? mulv3f(material.diffuse, diffuseTerm) : v3zero();
}

public vec3 halfVector(const vec3 left, const vec3 right) {
    return normv3(addv3(left, right));
}

public
vec3 specularContrib(const vec3 viewDir, const vec3 lightDir, const vec3 fragNormal,
                     const PhongMaterial material) {
    vec3 hv = halfVector(viewDir, lightDir);
    float specularTerm = dotv3(hv, fragNormal);
    return specularTerm > 0.0f ? mulv3f(material.specular, pow(specularTerm, material.shine)) : v3zero();
}

public
vec3 lightContrib(const vec3 viewDir, const vec3 lightDir, const vec3 fragNormal,
                  const float attenuation, const Light light, const PhongMaterial material) {
    vec3 lighting = v3zero();
    lighting = addv3(lighting, diffuseContrib(lightDir, fragNormal, material));
    lighting = addv3(lighting, specularContrib(viewDir, lightDir, fragNormal, material));
    return mulv3(mulv3f(light.color, attenuation), lighting);
}

public
vec3 pointLightContrib(const vec3 viewDir, const vec3 fragPosition, const vec3 fragNormal,
                       const Light light, const PhongMaterial material) {
    vec3 direction = subv3(light.vector, fragPosition);
    vec3 lightDir = normv3(direction);
    if (dotv3(lightDir, fragNormal) < 0.0f) {
        return v3zero();
    }
    float distance = lenv3(direction);
    float lum = luminosity(distance, light);
    return lightContrib(viewDir, lightDir, fragNormal, lum, light, material);
}

public
vec3 dirLightContrib(const vec3 viewDir, const vec3 fragNormal, const Light light, const PhongMaterial material) {
    vec3 lightDir = negv3(normv3(light.vector));
    return lightContrib(viewDir, lightDir, fragNormal, 1.0f, light, material);
}

public
vec4 shadingFlat(vec4 color) {
    return color;
}

public
vec4 shadingPhong(const vec3 fragPosition, const vec3 eye, const vec3 fragNormal, const vec3 fragAlbedo,
                  const PhongMaterial material) {
    vec3 viewDir = normv3(subv3(eye, fragPosition));
    vec3 color = material.ambient;
    for (int i = 0; i < uLightsPointCnt; ++i) {
        color = addv3(color, pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i], material));
    }
    for (int i = uLightsPointCnt; i < uLightsPointCnt + uLightsDirCnt; ++i) {
        color = addv3(color, dirLightContrib(viewDir, fragNormal, uLights[i], material));
    }
    color = mulv3(color, fragAlbedo);
    return v3tov4(color, material.transparency);
}

custom
vec3 getNormalFromMap(const vec3 normal, const vec3 worldPos, const vec2 texCoord, const vec3 vnormal) {

    const vec3 result = addv3(addv3(addv3(normal, worldPos), vnormal), v2tov3(texCoord, 1));
    assert(dotv3(result, worldPos) && "Some chicken shit happens here..");
    /*vec3 tangentNormal = fromMap * 2.0 - 1.0;
    vec3 Q1  = dFdx(vWorldPos);
    vec3 Q2  = dFdy(vWorldPos);
    vec2 st1 = dFdx(vTexCoord);
    vec2 st2 = dFdy(vTexCoord);
    vec3 N   = normalize(vNormal);
    vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
    vec3 B  = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);
    return normalize(TBN * tangentNormal);*/
    return result;
}

public
float distributionGGX(const vec3 N, const vec3 H, const float a) {
    float a2        = a * a;
    float NdotH     = max(dotv3(N, H), 0.0f);
    float NdotH2    = NdotH*NdotH;
    float nom       = a2;
    float denom     = (NdotH2 * (a2 - 1.0f) + 1.0f);
    denom           = PI * denom * denom;
    return nom / denom;
}

public
float geometrySchlickGGX(const float NdotV, const float roughness) {
    float r         = (roughness + 1.0f);
    float k         = (r*r) / 8.0f;
    float nom       = NdotV;
    float denom     = NdotV * (1.0f - k) + k;
    return nom / denom;
}

public
float geometrySmith(const vec3 N, const vec3 V, const vec3 L, const float roughness) {
    float NdotV     = max(dotv3(N, V), 0.0f);
    float NdotL     = max(dotv3(N, L), 0.0f);
    float ggx2      = geometrySchlickGGX(NdotV, roughness);
    float ggx1      = geometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

public
vec3 fresnelSchlick(const float cosTheta, const vec3 F0) {
    return addv3(F0, mulv3(subv3(ftov3(1.0f), F0), ftov3(pow(1.0f - cosTheta, 5.0f))));
}

public
vec4 shadingPbr(const vec3 eye, const vec3 worldPos, const vec3 albedo, const vec3 N,
                const float metallic, const float roughness, const float ao) {

    const vec3 alb = powv3(albedo, ftov3(2.2f));
    const vec3 V   = normv3(subv3(eye, worldPos));

    vec3 F0  = ftov3(0.04f);
    F0 = mixv3(F0, alb, metallic);

    vec3 Lo = v3zero();

    for(int i = 0; i < uLightsPointCnt; ++i) {
        const vec3 toLight = subv3(uLights[i].vector, worldPos);
        const vec3 L = normv3(toLight);
        const vec3 H = normv3(addv3(V, L));

        const float distance          = lenv3(toLight);
        const float lum               = luminosity(distance, uLights[i]);
        const vec3 radiance    = mulv3(uLights[i].color, ftov3(lum));

        const float NDF = distributionGGX(N, H, roughness);
        const float G   = geometrySmith(N, V, L, roughness);
        const vec3 F    = fresnelSchlick(max(dotv3(H, V), 0.0f), F0);

        const vec3 nominator = mulv3(F, ftov3(NDF * G));
        const float denominator = 4.0f * max(dotv3(N, V), 0.0f) * max(dotv3(N, L), 0.0f) + 0.001f;

        const vec3 specular = divv3f(nominator, denominator);

        vec3 kD = subv3(ftov3(1.0f), F);
        kD = mulv3(kD, ftov3(1.0f - metallic));
        const float NdotL = max(dotv3(N, L), 0.0f);
        Lo = addv3(Lo, mulv3(mulv3(addv3(divv3(mulv3(kD, alb), ftov3(PI)), specular), radiance), ftov3(NdotL)));
    }

    const vec3 ambient = mulv3(ftov3(0.1f * ao), alb);
    vec3 color = addv3(ambient, Lo);
    color = divv3(color, addv3(color, ftov3(1.0f)));
    color = powv3(color, ftov3(1.0f/2.2f));
    return v3tov4(color, 1.0f);
}

// ------------------- RAYTRACING ---------------

protected
RtCamera cameraLookAt(const vec3 eye, const vec3 center, const vec3 up,const float vfoy, const float aspect,
                      const float aperture, const float focusDist) {
    const float lensRadius = aperture / 2.0f;

    const float halfHeight = tan(vfoy/2.0f);
    const float halfWidth = aspect * halfHeight;

    const vec3 w = normv3(subv3(eye, center));
    const vec3 u = normv3(crossv3(up, w));
    const vec3 v = crossv3(w, u);

    const vec3 hwu = mulv3f(u, halfWidth * focusDist);
    const vec3 hhv = mulv3f(v, halfHeight * focusDist);
    const vec3 wf = mulv3f(w, focusDist);
    const vec3 lowerLeft = subv3(subv3(subv3(eye, hwu), hhv), wf);

    const vec3 horizontal = mulv3f(u, halfWidth * focusDist * 2.0f);
    const vec3 vertical  = mulv3f(v, halfHeight * focusDist * 2.0f);

    const RtCamera result = { eye, lowerLeft, horizontal, vertical, w, u, v, lensRadius};
    return result;
}

protected
Ray rayFromCamera(const RtCamera camera, const float u, const float v) {
    const vec3 horShift = mulv3f(camera.horizontal, u);
    const vec3 verShift = mulv3f(camera.vertical, v);

    vec3 origin;
    vec3 direction;

    if (camera.lensRadius > 0.0f) {
        const vec3 rd = mulv3f(randomInUnitDisk(), camera.lensRadius);
        const vec3 offset = addv3(mulv3f(camera.u, rd.x), mulv3f(camera.v, rd.y));
        origin = addv3(camera.origin, offset);
        direction = normv3(subv3(subv3(addv3(camera.lowerLeft, addv3(horShift, verShift)), camera.origin), offset));
    } else {
        origin = camera.origin;
        direction = normv3(subv3(addv3(camera.lowerLeft, addv3(horShift, verShift)), camera.origin));
    }

    const Ray result = { origin, direction };
    return result;
}

protected
vec3 background(const Ray ray) {
    const float t = (ray.direction.y + 1.0f) * 0.5f;
    const vec3 gradient = lerpv3(v3one(), v3(0.5f, 0.7f, 1.0f), t);
    return gradient;
}

protected
bool rayHitAabb(const Ray ray, const AABB aabb, const float tMin, const float tMax) {
    for (int i = 0; i < 3; i++) {
        const float invD = 1.0f / indexv3(ray.direction, i);
        float t0 = (indexv3(aabb.pointMin, i) - indexv3(ray.origin, i)) * invD;
        float t1 = (indexv3(aabb.pointMax, i) - indexv3(ray.origin, i)) * invD;

        if (invD < 0.0f) {
            float temp = t0;
            t0 = t1;
            t1 = temp;
        }

        const float tmin = t0 > tMin ? t0 : tMin;
        const float tmax = t1 < tMax ? t1 : tMax;
        if (tmax <= tmin) {
            return false;
        }
    }
    return true;
}

protected
HitRecord raySphereHitRecord(const Ray ray, const float t, const Sphere sphere) {
    const vec3 point = rayPoint(ray, t);
    const vec3 N = normv3(divv3f(subv3(point, sphere.center), sphere.radius));
    const HitRecord result = { t, point, N, sphere.materialType, sphere.materialIndex };
    return result;
}

protected
HitRecord rayHitSphere(const Ray ray, const float tMin, const float tMax, const Sphere sphere) {
    const vec3 oc = subv3(ray.origin, sphere.center);
    const float a = dotv3(ray.direction, ray.direction);
    const float b = 2 * dotv3(oc, ray.direction);
    const float c = dotv3(oc, oc) - sphere.radius * sphere.radius;
    const float D = b*b - 4*a*c;

    if (D > 0) {
        float t = (-b - sqrt(D)) / 2 * a;
        if (t < tMax && t > tMin) {
            return raySphereHitRecord(ray, t, sphere);
        }

        t = (-b + sqrt(D)) / 2 * a;
        if (t < tMax && t > tMin) {
            return raySphereHitRecord(ray, t, sphere);
        }
    }
    return NO_HIT;
}

protected
HitRecord rayHitObject(const Ray ray,const float tMin, const float tMax, const int type, const int index) {
    if (type != HITABLE_SPHERE) {
        flagError(); // spheres only
        return NO_HIT;
    }
    return rayHitSphere(ray, tMin, tMax, uSpheres[index]);
}

protected
HitRecord rayHitBvh(const Ray ray, const float tMin, const float tMax, const int index) {
    bvhTop = 0;
    float closest = tMax;
    HitRecord result = NO_HIT;
    int curr = index;

    while (curr >= 0) {
        while (curr >= 0 && rayHitAabb(ray, uBvhNodes[curr].aabb, tMin, closest)) {
            if (uBvhNodes[curr].leftType == HITABLE_BVH) {
                bvhStack[bvhTop] = curr;
                bvhTop++;
                curr = uBvhNodes[curr].leftIndex;
            } else {
                const HitRecord hit = rayHitObject(
                        ray, tMin, closest, uBvhNodes[curr].leftType, uBvhNodes[curr].leftIndex);
                if (hit.t > 0 && hit.t < closest) {
                    result = hit;
                    closest = hit.t;
                }
                break;
            }
        }

        bvhTop--;
        if (bvhTop < 0) {
            break;
        }
        curr = bvhStack[bvhTop];
        curr = uBvhNodes[curr].rightIndex;
    }

    return result;
}

protected
HitRecord rayHitWorld(const Ray ray, const float tMin, const float tMax) {
    return rayHitBvh(ray, tMin, tMax, 0);
}

protected
ScatterResult materialScatterLambertian(const HitRecord record, const LambertianMaterial material) {
    const vec3 tangent = addv3(record.point, record.normal);
    const vec3 direction = addv3(tangent, randomInUnitSphere());
    const ScatterResult result = { material.albedo, { record.point, subv3(direction, record.point) } };
    return result;
}

protected
ScatterResult materialScatterMetalic(const Ray ray, const HitRecord record, const MetallicMaterial material) {
    const vec3 reflected = reflectv3(ray.direction, record.normal);
    if (dotv3(reflected, record.normal) > 0) {
        const ScatterResult result = { material.albedo, { record.point, reflected } };
        return result;
    } else {
        return NO_SCATTER;
    }
}

protected
ScatterResult materialScatterDielectric(const Ray ray, const HitRecord record, const DielectricMaterial material) {
    float niOverNt;
    float cosine;
    vec3 outwardNormal;

    const float rdotn = dotv3(ray.direction, record.normal);
    const float dirlen = lenv3(ray.direction);

    if (rdotn > 0) {
        outwardNormal = negv3(record.normal);
        niOverNt = material.reflectiveIndex;
        cosine = material.reflectiveIndex * rdotn / dirlen;
    } else {
        outwardNormal = record.normal;
        niOverNt = 1.0f / material.reflectiveIndex;
        cosine = -rdotn / dirlen;
    }

    float reflectProbe;
    const RefractResult refractResult = refractv3(ray.direction, outwardNormal, niOverNt);
    if (refractResult.isRefracted) {
        reflectProbe = schlick(cosine, material.reflectiveIndex);
    } else {
        reflectProbe = 1.0f;
    }

    vec3 scatteredDir;
    if (randomFloat() < reflectProbe) {
        scatteredDir = reflectv3(ray.direction, record.normal);
    } else {
        scatteredDir = refractResult.refracted;
    }

    const ScatterResult scatterResult = { v3one(), { record.point, scatteredDir } };
    return scatterResult;
}

protected
ScatterResult materialScatter(const Ray ray, const HitRecord record) {
    switch (record.materialType) {
        case MATERIAL_LAMBERTIAN:
            return materialScatterLambertian(record, uLambertianMaterials[record.materialIndex]);
        case MATERIAL_METALIIC:
            return materialScatterMetalic(ray, record, uMetallicMaterials[record.materialIndex]);
        case MATERIAL_DIELECTRIC:
            return materialScatterDielectric(ray, record, uDielectricMaterials[record.materialIndex]);
        default:
            return NO_SCATTER;
    }
}

protected
vec3 sampleColor(const int rayBounces, const RtCamera camera, const float u, const float v) {
    Ray ray = rayFromCamera(camera, u, v);
    vec3 fraction = ftov3(1.0f);
    for (int i = 0; i < rayBounces; i++) {
        const HitRecord record = rayHitWorld(ray, BOUNCE_ERR, FLT_MAX);
        if (record.t < 0) {
            break;
        } else {
            const ScatterResult scatterResult = materialScatter(ray, record);
            if (scatterResult.attenuation.x < 0) {
                return v3zero();
            }
            fraction = mulv3(fraction, scatterResult.attenuation);
            ray = scatterResult.scattered;
        }
    }
    return mulv3(background(ray), fraction);
}

public
vec4 fragmentColorRt(const int width, const int height,
                     const float random, int sampleCnt, int rayBounces,
                     const vec3 eye, const vec3 center, const vec3 up,
                     const float fovy, const float aspect,
                     const float aperture, const float focusDist,
                     const vec2 texCoord) {

    seedRandom(v2tov3(texCoord, random));

    const float DU = 1.0f / itof(width);
    const float DV = 1.0f / itof(height);

    const RtCamera camera = cameraLookAt(eye, center, up, fovy, aspect, aperture, focusDist);
    vec3 result = v3zero();
    for (int i = 0; i < sampleCnt; i++) {
        const float du = DU * randomFloat();
        const float dv = DV * randomFloat();
        const float sampleU = texCoord.x + du;
        const float sampleV = texCoord.y + dv;
        result = addv3(result, sampleColor(rayBounces, camera, sampleU, sampleV));
    }
    return v3tov4(result, 1.0f);
}

public
vec4 gammaSqrt(const vec4 result) {
    return v4(sqrt(result.x), sqrt(result.y), sqrt(result.z), 1.0f);
}

void raytracer() {
    const int WIDTH = 1024;
    const int HEIGHT = 768;
    const int SAMPLES = 8;

    FILE *f = fopen("out.ppm", "w");
    if (f == NULL) {
        printf("Error opening file!\n");
        exit(1);
    }
    fprintf(f, "P3\n%d %d\n255\n", WIDTH, HEIGHT);

    const float all = itof(WIDTH) * itof(HEIGHT);
    int current = 0;

    for (int v = HEIGHT - 1; v >= 0; v--) {
        for (int u = 0; u < WIDTH; u++) {
            const float s = (float) u / (float) WIDTH;
            const float t = (float) v / (float) WIDTH;

            const vec4 added = fragmentColorRt(
                    WIDTH, HEIGHT,
                    randomFloat(), SAMPLES, 4,
                    v3(0, 0, 250.0f), v3zero(), v3up(),
                    90.0f * PI / 180.0f, 4.0f / 3.0f, 0, 1,
                    v2(s, t));
            const vec4 color = divv4f(added, itof(SAMPLES));

            const int r = (int) (255.9f * color.x);
            const int g = (int) (255.9f * color.y);
            const int b = (int) (255.9f * color.z);
            fprintf(f, "%d %d %d ", r, g, b);

            static float prevReport = 0.0f;
            float progress = (float) (current++) / all;
            if (progress - prevReport > 0.01f) {
                printf("progress: %.2f\n", progress);
                prevReport = progress;
            }
        }
        fprintf(f, "\n");
    }
    fclose(f);
}

// ------------------- LOGGING ---------------

void printv3(const vec3 v) {
    printf("v3 = {%f, %f, %f}\n", v.x, v.y, v.z);
}

// ------------------- MAIN ---------------

int main() {
    assert(eqv3(v3(1, 1, 1), v3(1, 1, 1)));
    assert(!eqv3(v3(1, 1, 1), v3(1, 0, 1)));
    assert(eqv3(negv3(ftov3(1)), ftov3(-1)));
    assert(dotv3(v3(1, 0, 0), v3(0, 1, 0)) == 0.0);
    assert(dotv3(v3(1, 0, 0), v3(1, 0, 0)) == 1.0);
    assert(eqv3(crossv3(v3(1, 0, 0), v3(0, 1, 0)), v3(0, 0, 1)));
    assert(eqv3(addv3(v3(1, 1, 1), v3(2, 2, 2)), v3(3, 3, 3)));
    assert(eqv3(subv3(v3(1, 1, 1), v3(2, 2, 2)), v3(-1, -1, -1)));
    assert(eqv3(mulv3(v3(1, 1, 1), v3(2, 2, 2)), v3(2, 2, 2)));
    assert(eqv3(mulv3f(v3(1, 1, 1), 2.5f), v3(2.5f, 2.5f, 2.5f)));
    assert(eqv3(powv3(ftov3(2.0f), ftov3(2.0f)), ftov3(4.0f)));
    assert(eqv3(divv3f(v3(10, 10, 10), 5.0f), v3(2.0f, 2.0f, 2.0f)));
    assert(eqv3(divv3(ftov3(4.0f), ftov3(2.0f)), ftov3(2.0f)));
    assert(eqv3(mixv3(ftov3(1.0f), ftov3(1.0f), 0.5f), ftov3(1.0f)));
    assert(eqv4(addv4(v4(1, 1, 1, 1), v4(2, 2, 2, 2)), v4(3, 3, 3, 3)));
    assert(eqv4(subv4(v4(1, 1, 1, 1), v4(2, 2, 2, 2)), v4(-1, -1, -1, -1)));
    assert(eqv4(mulv4(v4(1, 1, 1, 1), v4(2, 2, 2, 2)), v4(2, 2, 2, 2)));
    assert(eqv4(mulv4f(v4(1, 1, 1, 1), 2.5f), v4(2.5f, 2.5f, 2.5f, 2.5f)));
    assert(eqv4(divv4(ftov4(4.0f), ftov4(2.0f)), ftov4(2.0f)));
    assert(eqv4(divv4f(v4(10, 10, 10, 10), 5.0f), v4(2.0f, 2.0f, 2.0f, 2.0f)));
    assert(itof(123) == 123.0f);
    assert(ftoi(123.5f) == 123);
    assert(eqv2(tile(v2(1.0f, 1.0f), iv2(1, 1), iv2(2, 2)), ftov2(1.0f)));
    assert(lenv3(v3(0, 0, 0)) == 0);
    assert(lenv3(v3(1, 0, 0)) == 1);
    assert(lenv3(v3(0, 1, 0)) == 1);
    assert(lenv3(v3(0, 0, 1)) == 1);
    assert(lenv3(normv3(v3(10, 10, 10))) - 1.0f < FLT_EPSILON);
    assert(eqv3(lerpv3(v3zero(), v3one(), 0.5f), ftov3(0.5f)));
    assert(eqv3(rayPoint(rayBack(), 10.0f), v3(0, 0, -10)));
    raytracer();
    return 0;
}
