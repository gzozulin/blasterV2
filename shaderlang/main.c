#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#pragma ide diagnostic ignored "readability-non-const-parameter"

#include <assert.h>
#include <math.h>
#include <stdbool.h>
#include <float.h>
#include <stdlib.h>
#include <stdio.h>

#define public
#define custom

#define sqrt sqrtf
#define pow powf
#define max fmaxf

#define PI 3.14159265359f

#define WIDTH           1024
#define HEIGHT          768
#define SAMPLES         4096
#define BOUNCES         4
#define BOUNCE_ERR      0.001f

#define MAX_LIGHTS      128
#define MAX_HITABLES    128
#define MAX_SPHERES     128

// Corresponds to HitableType
#define HITABLE_HITABLE 0
#define HITABLE_SPHERE  1

// ------------------- TYPES -------------------

struct vec2 {
    float x;
    float y;
};

struct ivec2 {
    int x;
    int y;
};

struct vec3 {
    float x;
    float y;
    float z;
};

struct vec4 {
    float x;
    float y;
    float z;
    float w;
};

struct mat3 {
    float value;
};

struct mat4 {
    float value;
};

struct Ray {
    struct vec3 origin;
    struct vec3 direction;
};

struct Light {
    struct vec3 vector;
    struct vec3 color;
    float attenConstant;
    float attenLinear;
    float attenQuadratic;
};

struct PhongMaterial {
    struct vec3 ambient;
    struct vec3 diffuse;
    struct vec3 specular;
    float shine;
    float transparency;
};

struct Sphere {
    struct vec3 center;
    float radius;
};

struct HitRecord {
    float t;
    struct vec3 point;
    struct vec3 normal;
};

struct Hitable {
    int type;
    int index;
};

// ------------------- LIGHTS -------------------

#define LIGHT_X1 { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 1.0f }, 1.0f, 1.0f, 1.0f }
#define LIGHT_X4 LIGHT_X1, LIGHT_X1, LIGHT_X1, LIGHT_X1
#define LIGHT_X16 LIGHT_X4, LIGHT_X4, LIGHT_X4, LIGHT_X4
#define LIGHT_X64 LIGHT_X16, LIGHT_X16, LIGHT_X16, LIGHT_X16

const int uLightsPointCnt = 1;
const int uLightsDirCnt = 0;
const struct Light uLights[MAX_LIGHTS] = { LIGHT_X64, LIGHT_X64 };

// ------------------- HITABLES -------------------

const struct HitRecord NO_HIT = { -1, { 0, 0, 0 }, { 1, 0, 0 } };

#define HITABLE_X1 { 1, 0 }
#define HITABLE_X4 HITABLE_X1, HITABLE_X1, HITABLE_X1, HITABLE_X1
#define HITABLE_X16 HITABLE_X4, HITABLE_X4, HITABLE_X4, HITABLE_X4
#define HITABLE_X64 HITABLE_X16, HITABLE_X16, HITABLE_X16, HITABLE_X16

const int uHitablesCnt = 1;
const struct Hitable uHitables[MAX_HITABLES] = { HITABLE_X64, HITABLE_X64 };

// ------------------- SPHERES -------------------

#define SPHERE_X1 { { 0, 0, -1 }, 3 }
#define SPHERE_X4 SPHERE_X1, SPHERE_X1, SPHERE_X1, SPHERE_X1
#define SPHERE_X16 SPHERE_X4, SPHERE_X4, SPHERE_X4, SPHERE_X4
const struct Sphere uSpheres[MAX_SPHERES] = { SPHERE_X16 };

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

// ------------------- CTORS -------------------

custom
struct vec2 v2(const float x, const float y) {
    return (struct vec2) {x, y};
}

public
struct vec2 ftov2(const float v) {
    return v2(v, v);
}

public
struct vec2 v2zero() {
    return ftov2(0.0f);
}

custom
struct ivec2 iv2(const int x, const int y) {
    return (struct ivec2) {x, y};
}

custom
struct vec3 v3(const float x, const float y, const float z) {
    return (struct vec3) {x, y, z};
}

public
struct vec3 v2tov3(struct vec2 v, const float f) {
    return v3(v.x, v.y, f);
}

public
struct vec3 ftov3(const float v) {
    return v3(v, v, v);
}

public
struct vec3 v3zero() {
    return ftov3(0.0f);
}

public
struct vec3 v3one() {
    return ftov3(1.0f);
}

public
struct vec3 v3front() {
    return v3(0, 0, 1);
}

public
struct vec3 v3back() {
    return v3(0, 0, -1);
}

public
struct vec3 v3left() {
    return v3(-1, 0, 0);
}

public
struct vec3 v3right() {
    return v3(1, 0, 0);
}

public
struct vec3 v3up() {
    return v3(0, 1, 0);
}

public
struct vec3 v3down() {
    return v3(0, -1, 0);
}

public
struct vec3 v3white() {
    return v3(1.0f, 1.0f, 1.0f);
}

public
struct vec3 v3black() {
    return v3(0.0f, 0.0f, 0.0f);
}
public
struct vec3 v3ltGrey() {
    return ftov3(0.3f);
}

public
struct vec3 v3grey() {
    return ftov3(0.5f);
}

public
struct vec3 v3dkGrey() {
    return ftov3(0.7f);
}

public
struct vec3 v3red() {
    return v3(1.0f, 0.0f, 0.0f);
}

public
struct vec3 v3green() {
    return v3(0.0f, 1.0f, 0.0f);
}

public
struct vec3 v3blue() {
    return v3(0.0f, 0.0f, 1.0f);
}

public
struct vec3 v3yellow() {
    return v3(1.0f, 1.0f, 0.0f);
}

public
struct vec3 v3magenta() {
    return v3(1.0f, 0.0f, 1.0f);
}

public
struct vec3 v3cyan() {
    return v3(0.0f, 1.0f, 1.0f);
}

public
struct vec3 v3orange() {
    return v3(1.0f, 0.5f, 0.0f);
}
public
struct vec3 v3rose() {
    return v3(1.0f, 0.0f, 0.5f);
}

public
struct vec3 v3violet() {
    return v3(0.5f, 0.0f, 1.0f);
}

public
struct vec3 v3azure() {
    return v3(0.0f, 0.5f, 1.0f);
}

public
struct vec3 v3aquamarine() {
    return v3(0.0f, 1.0f, 0.5f);
}

public
struct vec3 v3chartreuse() {
    return v3(0.5f, 1.0f, 0.0f);
}

custom
struct vec4 v4(const float x, const float y, const float z, const float w) {
    return (struct vec4) {x, y, z, w};
}

public
struct vec4 v3tov4(const struct vec3 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

public
struct vec4 ftov4(const float v) {
    return v4(v, v, v, v);
}

public
struct vec4 v4zero() {
    return ftov4(0.0f);
}

custom
struct mat3 m3ident() {
    return (struct mat3) { 0.0f };
}

public
struct Ray rayBack() {
    const struct Ray result = {v3zero(), v3back() };
    return result;
}

// ------------------- GET ---------------

public
float getxv4(const struct vec4 v) {
    return v.x;
}

public
float getyv4(const struct vec4 v) {
    return v.y;
}

public
float getzv4(const struct vec4 v) {
    return v.z;
}

public
float getwv4(const struct vec4 v) {
    return v.w;
}

public
float getrv4(const struct vec4 v) {
    return v.x;
}

public
float getgv4(const struct vec4 v) {
    return v.y;
}

public
float getbv4(const struct vec4 v) {
    return v.z;
}

public
float getav4(const struct vec4 v) {
    return v.w;
}

// ------------------- SET ---------------

public
struct vec4 setxv4(const struct vec4 v, const float f) {
    return v4(f, v.y, v.z, v.w);
}

public
struct vec4 setyv4(const struct vec4 v, const float f) {
    return v4(v.x, f, v.z, v.w);
}

public
struct vec4 setzv4(const struct vec4 v, const float f) {
    return v4(v.x, v.y, f, v.w);
}

public
struct vec4 setwv4(const struct vec4 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

public
struct vec4 setrv4(const struct vec4 v, const float f) {
    return v4(f, v.y, v.z, v.w);
}

public
struct vec4 setgv4(const struct vec4 v, const float f) {
    return v4(v.x, f, v.z, v.w);
}

public
struct vec4 setbv4(const struct vec4 v, const float f) {
    return v4(v.x, v.y, f, v.w);
}

public
struct vec4 setav4(const struct vec4 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

// ------------------- BOOL -------------------

public
bool eqv2(const struct vec2 left, const struct vec2 right) {
    return left.x == right.x && left.y == right.y;
}

public
bool eqv3(const struct vec3 left, const struct vec3 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z;
}

public
bool eqv4(const struct vec4 left, const struct vec4 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z && left.w == right.w;
}

// ------------------- MATH -------------------

public
struct vec3 negv3(const struct vec3 v) {
    return v3(-v.x, -v.y, -v.z);
}

public
float dotv3(const struct vec3 left, const struct vec3 right) {
    return left.x * right.x + left.y * right.y + left.z * right.z;
}

public
struct vec3 crossv3(const struct vec3 left, const struct vec3 right) {
    return v3(
            left.y * right.z - left.z * right.y,
            left.z * right.x - left.x * right.z,
            left.x * right.y - left.y * right.x);
}

public
struct vec3 addv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x + right.x, left.y + right.y, left.z + right.z);
}

public
struct vec3 subv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x - right.x, left.y - right.y, left.z - right.z);
}

public
struct vec3 mulv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x * right.x, left.y * right.y, left.z * right.z);
}

public
struct vec3 mulv3f(const struct vec3 left, const float right) {
    return v3(left.x * right, left.y * right, left.z * right);
}

public
struct vec3 powv3(const struct vec3 left, const struct vec3 right) {
    return v3(pow(left.x, right.x), pow(left.y, right.y), pow(left.z, right.z));
}

public
struct vec3 divv3f(const struct vec3 left, const float right) {
    return v3(left.x / right, left.y / right, left.z / right);
}

public
struct vec3 divv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x / right.x, left.y / right.y, left.z / right.z);
}

public
struct vec3 mixv3(const struct vec3 left, const struct vec3 right, const float proportion) {
    return addv3(mulv3(left, ftov3(1.0f - proportion)), mulv3(right, ftov3(proportion)));
}

public
struct vec4 addv4(const struct vec4 left, const struct vec4 right) {
    return v4(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
}

public
struct vec4 subv4(const struct vec4 left, const struct vec4 right) {
    return v4(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
}

public
struct vec4 mulv4(const struct vec4 left, const struct vec4 right) {
    return v4(left.x * right.x, left.y * right.y, left.z * right.z, left.w * right.w);
}

public
struct vec4 mulv4f(const struct vec4 left, const float right) {
    return v4(left.x * right, left.y * right, left.z * right, left.w * right);
}

public
struct vec4 divv4(const struct vec4 left, const struct vec4 right) {
    return v4(left.x / right.x, left.y / right.y, left.z / right.z, left.w / right.w);
}

public
struct vec4 divv4f(const struct vec4 left, const float right) {
    return v4(left.x / right, left.y / right, left.z / right, left.z / right);
}

public
float lenv3(const struct vec3 v) {
    return sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
}

public
float sqlenv3(const struct vec3 v) {
    return (v.x*v.x + v.y*v.y + v.z*v.z);
}

public
struct vec3 normv3(const struct vec3 v) {
    return divv3f(v, lenv3(v));
}

public
struct vec3 lerpv3(const struct vec3 from, const struct vec3 to, const float t) {
    return addv3(mulv3f(from, 1.0f - t), mulv3f(to, t));
}

public
struct vec3 pointOnRay(const struct Ray ray, const float t) {
    return addv3(ray.origin, mulv3f(ray.direction, t));
}

// ------------------- RAND -------------------

custom
struct vec3 seedRandom(const struct vec2 s) {
    return v2tov3(s, 1.0f);
}

custom
float randf() {
    return dtof(drand48());
}

public
struct vec3 randomInUnitSphere() {
    float d, x, y, z;
    for (int i = 0; i < 20; i++) {
        x = randf() * 2.0f - 1.0f;
        y = randf() * 2.0f - 1.0f;
        z = randf() * 2.0f - 1.0f;
        d = x*x + y*y + z*z;
        if (d >= 1.0f) {
            return v3(x, y, z);
        }
    }
    return normv3(v3(x, y, z));
}

// ------------------- TILE ---------------

public
struct vec2 tile(const struct vec2 texCoord, const struct ivec2 uv, const struct ivec2 cnt) {
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
float luminosity(const float distance, const struct Light light) {
    return 1.0f / (light.attenConstant + light.attenLinear * distance + light.attenQuadratic * distance * distance);
}

public
struct vec3 diffuseContrib(const struct vec3 lightDir, const struct vec3 fragNormal, const struct PhongMaterial material) {
    float diffuseTerm = dotv3(fragNormal, lightDir);
    return diffuseTerm > 0.0f ? mulv3f(material.diffuse, diffuseTerm) : v3zero();
}

public struct vec3 halfVector(const struct vec3 left, const struct vec3 right) {
    return normv3(addv3(left, right));
}

public
struct vec3 specularContrib(const struct vec3 viewDir, const struct vec3 lightDir, const struct vec3 fragNormal,
                            const struct PhongMaterial material) {
    struct vec3 hv = halfVector(viewDir, lightDir);
    float specularTerm = dotv3(hv, fragNormal);
    return specularTerm > 0.0f ? mulv3f(material.specular, pow(specularTerm, material.shine)) : v3zero();
}

public
struct vec3 lightContrib(const struct vec3 viewDir, const struct vec3 lightDir, const struct vec3 fragNormal,
                         const float attenuation, const struct Light light, const struct PhongMaterial material) {
    struct vec3 lighting = v3zero();
    lighting = addv3(lighting, diffuseContrib(lightDir, fragNormal, material));
    lighting = addv3(lighting, specularContrib(viewDir, lightDir, fragNormal, material));
    return mulv3(mulv3f(light.color, attenuation), lighting);
}

public
struct vec3 pointLightContrib(const struct vec3 viewDir, const struct vec3 fragPosition, const struct vec3 fragNormal,
                              const struct Light light, const struct PhongMaterial material) {
    struct vec3 direction = subv3(light.vector, fragPosition);
    struct vec3 lightDir = normv3(direction);
    if (dotv3(lightDir, fragNormal) < 0.0f) {
        return v3zero();
    }
    float distance = lenv3(direction);
    float lum = luminosity(distance, light);
    return lightContrib(viewDir, lightDir, fragNormal, lum, light, material);
}

public
struct vec3 dirLightContrib(const struct vec3 viewDir, const struct vec3 fragNormal, const struct Light light,
                            const struct PhongMaterial material) {
    struct vec3 lightDir = negv3(normv3(light.vector));
    return lightContrib(viewDir, lightDir, fragNormal, 1.0f, light, material);
}

public
struct vec4 shadingFlat(struct vec4 color) {
    return color;
}

public
struct vec4 shadingPhong(const struct vec3 fragPosition, const struct vec3 eye, const struct vec3 fragNormal,
                         const struct vec3 fragAlbedo, const struct PhongMaterial material) {
    struct vec3 viewDir = normv3(subv3(eye, fragPosition));
    struct vec3 color = material.ambient;
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
struct vec3 getNormalFromMap(const struct vec3 normal, const struct vec3 worldPos, const struct vec2 texCoord,
                             const struct vec3 vnormal) {
    assert(0 && "Some chicken shit happens here..");
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
}

public
float distributionGGX(const struct vec3 N, const struct vec3 H, const float a) {
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
float geometrySmith(const struct vec3 N, const struct vec3 V, const struct vec3 L, const float roughness) {
    float NdotV     = max(dotv3(N, V), 0.0f);
    float NdotL     = max(dotv3(N, L), 0.0f);
    float ggx2      = geometrySchlickGGX(NdotV, roughness);
    float ggx1      = geometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

public
struct vec3 fresnelSchlick(const float cosTheta, const struct vec3 F0) {
    return addv3(F0, mulv3(subv3(ftov3(1.0f), F0), ftov3(pow(1.0f - cosTheta, 5.0f))));
}

public
struct vec4 shadingPbr(const struct vec3 eye, const struct vec3 worldPos,
                       const struct vec3 albedo, const struct vec3 N, const float metallic, const float roughness, const float ao) {

    const struct vec3 alb = powv3(albedo, ftov3(2.2f));
    const struct vec3 V   = normv3(subv3(eye, worldPos));

    struct vec3 F0  = ftov3(0.04f);
    F0 = mixv3(F0, alb, metallic);

    struct vec3 Lo = v3zero();

    for(int i = 0; i < uLightsPointCnt; ++i) {
        const struct vec3 toLight = subv3(uLights[i].vector, worldPos);
        const struct vec3 L = normv3(toLight);
        const struct vec3 H = normv3(addv3(V, L));

        const float distance          = lenv3(toLight);
        const float lum               = luminosity(distance, uLights[i]);
        const struct vec3 radiance    = mulv3(uLights[i].color, ftov3(lum));

        const float NDF       = distributionGGX(N, H, roughness);
        const float G         = geometrySmith(N, V, L, roughness);
        const struct vec3 F   = fresnelSchlick(max(dotv3(H, V), 0.0f), F0);

        const struct vec3 nominator = mulv3(F, ftov3(NDF * G));
        const float denominator = 4.0f * max(dotv3(N, V), 0.0f) * max(dotv3(N, L), 0.0f) + 0.001f;

        const struct vec3 specular = divv3f(nominator, denominator);

        struct vec3 kD = subv3(ftov3(1.0f), F);
        kD = mulv3(kD, ftov3(1.0f - metallic));
        const float NdotL = max(dotv3(N, L), 0.0f);
        Lo = addv3(Lo, mulv3(mulv3(addv3(divv3(mulv3(kD, alb), ftov3(PI)), specular), radiance), ftov3(NdotL)));
    }

    const struct vec3 ambient = mulv3(ftov3(0.1f * ao), alb);
    struct vec3 color = addv3(ambient, Lo);
    color = divv3(color, addv3(color, ftov3(1.0f)));
    color = powv3(color, ftov3(1.0f/2.2f));
    return v3tov4(color, 1.0f);
}

// ------------------- RAYTRACING ---------------

public
struct vec3 background(const struct Ray ray) {
    const float t = (ray.direction.y + 1.0f) * 0.5f;
    const struct vec3 gradient = lerpv3(v3one(), v3(0.5f, 0.7f, 1.0f), t);
    return gradient;
}

public
struct Ray createRayFromTexCoord(const float u, const float v) {
    const struct vec3 lowerLeft   = { -1, -1, -1 };
    const struct vec3 origin      = {  0,  0,  0 };
    const struct vec3 horizontal  = {  2,  0,  0 };
    const struct vec3 vertical    = {  0,  2,  0 };
    const struct vec3 direction = normv3(
            addv3(lowerLeft,addv3(mulv3f(horizontal, u), mulv3f(vertical, v))));
    const struct Ray result = {origin, direction };
    return result;
}

public
struct HitRecord createRaySphereHitRecord(const struct Ray ray, const float t, const struct Sphere sphere) {
    const struct vec3 point = pointOnRay(ray, t);
    const struct vec3 N = normv3(divv3f(subv3(point, sphere.center), sphere.radius));
    const struct HitRecord result = { t, point, N };
    return result;
}

public
struct HitRecord hitRaySphere(const struct Ray ray, const float tMin, const float tMax, const struct Sphere sphere) {
    const struct vec3 oc = subv3(ray.origin, sphere.center);
    const float a = dotv3(ray.direction, ray.direction);
    const float b = 2 * dotv3(oc, ray.direction);
    const float c = dotv3(oc, oc) - sphere.radius * sphere.radius;
    const float D = b*b - 4*a*c;
    if (D > 0) {
        float temp = (-b - sqrt(D)) / 2 * a;
        if (temp < tMax && temp > tMin) {
            return createRaySphereHitRecord(ray, temp, sphere);
        }
        temp = (-b + sqrt(D)) / 2 * a;
        if (temp < tMax && temp > tMin) {
            return createRaySphereHitRecord(ray, temp, sphere);
        }
    }
    return NO_HIT;
}

public
struct HitRecord hitRayHitables(const struct Ray ray, const float tMin, const float tMax) {
    struct HitRecord result = NO_HIT;
    float closest = tMax;
    for (int i = 0; i < uHitablesCnt; i++) {
        const struct Hitable hitable = uHitables[i];
        struct HitRecord hitRecord;
        switch (hitable.type) {
            case HITABLE_SPHERE:
                hitRecord = hitRaySphere(ray, tMin, closest, uSpheres[hitable.index]);
                break;
            default:
                hitRecord = NO_HIT;
        }
        if (hitRecord.t > 0) {
            closest = hitRecord.t;
            result = hitRecord;
        }
    }
    return result;
}

public
struct vec3 sampleColor(const float u, const float v) {
    const struct Ray ray = createRayFromTexCoord(u, v);
    struct HitRecord record = hitRayHitables(ray, BOUNCE_ERR, FLT_MAX);
    float fraction = 1.0f;
    for (int i = 0; i < BOUNCES; i++) {
        if (record.t < 0 || fraction < 0.01f) {
            break;
        }
        const struct vec3 target = addv3(addv3(record.point, record.normal), randomInUnitSphere());
        const struct Ray scattered = { record.point, subv3(target, record.point) };
        record = hitRayHitables(scattered, BOUNCE_ERR, FLT_MAX);
        fraction *= 0.5f;
    }
    return mulv3f(background(ray), fraction);
}

public
struct vec4 shadingRt(const struct vec2 texCoord) {
    seedRandom(texCoord);
    struct vec3 result = v3zero();
    const float DU = 1.0f / WIDTH;
    const float DV = 1.0f / HEIGHT;
    for (int i = 0; i < SAMPLES; i++) {
        const float sampleU = texCoord.x - DU + 2 * DU * randf();
        const float sampleV = texCoord.y - DV + 2 * DV * randf();
        result = addv3(result, sampleColor(sampleU, sampleV));
    }
    result = divv3f(result, SAMPLES);
    result = v3(sqrt(result.x), sqrt(result.y), sqrt(result.z));
    return v3tov4(result, 1.0f);
}

void raytracer() {
    FILE *f = fopen("out.ppm", "w");
    if (f == NULL) {
        printf("Error opening file!\n");
        exit(1);
    }
    fprintf(f, "P3\n%d %d\n255\n", WIDTH, HEIGHT);
    const float all = WIDTH * HEIGHT;
    int current = 0;
    for (int v = 0; v < HEIGHT; v++) {
        for (int u = 0; u < WIDTH; u++) {
            const struct vec4 color = shadingRt(v2((float) u / (float) WIDTH, (float) v / (float) HEIGHT));
            const int r = (int) (255.9f * color.x);
            const int g = (int) (255.9f * color.y);
            const int b = (int) (255.9f * color.z);
            fprintf(f, "%d %d %d ", r, g, b);
            printf("progress: %.3f\n", (float) (current++) / all);
        }
        fprintf(f, "\n");
    }
    fclose(f);
}

// ------------------- LOGGING ---------------

void printv3(const struct vec3 v) {
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
    assert(eqv3(pointOnRay(rayBack(), 10.0f), v3(0, 0, -10)));
    raytracer();
    return 0;
}

#pragma clang diagnostic pop
