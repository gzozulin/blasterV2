//
// Created by greg on 2021-07-30.
//

// todo: naming: sinf, mulv3, mulv3f
// todo: glsl refract
// todo: glsl reflect
// todo: math library

#pragma once

#include <assert.h>
#include <math.h>
#include <stdbool.h>
#include <float.h>
#include <stdlib.h>
#include <stdio.h>

#define public      // define and add handle
#define custom      // ops: handle only, definition is custom
#define protected   // ops: definiton only, no handle

// region ------------------- DEFINE -------------------

#define MAX_LIGHTS              128
#define MAX_BVH                 512
#define MAX_SPHERES             256
#define MAX_LAMBERTIANS         16
#define MAX_METALS              16
#define MAX_DIELECTRICS         16

#define HITABLE_BVH             0
#define HITABLE_SPHERE          1

#define MATERIAL_LAMBERTIAN     0
#define MATERIAL_METALIIC       1
#define MATERIAL_DIELECTRIC     2

// endregion ------------------- DEFINE -------------------

// region ------------------- TYPES -------------------

typedef struct vec2 {
    float x;
    float y;
} vec2;

typedef struct ivec2 {
    int x;
    int y;
} ivec2;

typedef struct vec3 {
    float x;
    float y;
    float z;
} vec3;

typedef struct vec4 {
    float x;
    float y;
    float z;
    float w;
} vec4;

typedef struct mat2 {
    float value[4];
} mat2;

typedef struct mat4 {
    float value[16];
} mat4;

public
typedef struct Ray {
    vec3 origin;
    vec3 direction;
} Ray;

public
typedef struct AABB {
    vec3 pointMin;
    vec3 pointMax;
} AABB;

public
typedef struct RtCamera {
    vec3 origin;
    vec3 lowerLeft;
    vec3 horizontal;
    vec3 vertical;
    vec3 w, u, v;
    float lensRadius;
} RtCamera;

public
typedef struct Light {
    vec3 vector;
    vec3 color;
    float attenConstant;
    float attenLinear;
    float attenQuadratic;
} Light;

public
typedef struct PhongMaterial {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shine;
    float transparency;
} PhongMaterial;

public
typedef struct BvhNode {
    AABB aabb;
    int leftType;
    int leftIndex;
    int rightType;
    int rightIndex;
} BvhNode;

public
typedef struct Sphere {
    vec3 center;
    float radius;
    int materialType;
    int materialIndex;
} Sphere;

public
typedef struct LambertianMaterial {
    vec3 albedo;
} LambertianMaterial;

public
typedef struct MetallicMaterial {
    vec3 albedo;
} MetallicMaterial;

public
typedef struct DielectricMaterial {
    float reflectiveIndex;
} DielectricMaterial;

public
typedef struct HitRecord {
    float t;
    vec3 point;
    vec3 normal;
    int materialType;
    int materialIndex;
} HitRecord;

public
typedef struct ScatterResult {
    vec3 attenuation;
    Ray scattered;
} ScatterResult;

public
typedef struct RefractResult {
    bool isRefracted;
    vec3 refracted;
} RefractResult;

// endregion ------------------- TYPES -------------------

float error();
void printv3(vec3 v);

// region ------------------- CONST -------------------

extern const float                  PI;
extern const float                  BOUNCE_ERR;

extern const HitRecord              NO_HIT;
extern const ScatterResult          NO_SCATTER;
extern const RefractResult          NO_REFRACT;

extern const int                    uLightsPointCnt;
extern const int                    uLightsDirCnt;
extern const Light                  uLights[];

extern const BvhNode                uBvhNodes[];
extern int                          bvhStack[];
extern int                          bvhTop;

extern const Sphere                 uSpheres[];

extern const LambertianMaterial     uLambertianMaterials[];
extern const MetallicMaterial       uMetallicMaterials  [];
extern const DielectricMaterial     uDielectricMaterials[];

// endregion ------------------- CONST -------------------

// region ------------------- MATH -------------------

#define sqrt    sqrtf
#define pow     powf
#define tan     tanf
#define min     fminf
#define max     fmaxf
#define cos     cosf
#define sin     cosf

#define floor _erased_

float sqrtv(float value);
float sinv(float rad);
float cosv(float rad);
float tanv(float rad);
float powv(float base, float power);
float minv(float left, float right);
float maxv(float left, float right);
float clamp(float x, float lowerlimit, float upperlimit);
float smoothstep(float edge0, float edge1, float x);
float floor(float value);
float fract(float value);
float schlick(float cosine, float ri);
float length(vec2 v);
float remap(float a, float b, float c, float d, float t);

// endregion ------------------- MATH -------------------

// region ------------------- FLOAT -------------------

float itof(int i);
int ftoi(float f);
float dtof(double d);
float addf(float left, float right);
float subf(float left, float right);
float mulf(float left, float right);
float divf(float left, float right);

// endregion ------------------- FLOAT -------------------

// region ------------------- BOOL -------------------

bool eqv2(vec2 left, vec2 right);
bool eqv3(vec3 left, vec3 right);
bool eqv4(vec4 left, vec4 right);

// endregion ------------------- BOOL -------------------

// region ------------------- VEC2 -------------------

vec2 v2(float x, float y);
vec2 ftov2(float v);
vec2 v2zero();
vec2 mulv2f(vec2 vec, float v);
vec2 addv2f(vec2 left, float right);
vec2 subv2f(vec2 left, float right);
float getxv2(vec2 v);
float getyv2(vec2 v);

// endregion ------------------- VEC2 -------------------

// region ------------------- IVEC2 -------------------

ivec2 iv2(int x, int y);
float getxiv2(vec2 v);
float getyiv2(vec2 v);
float getuiv2(vec2 v);
float getviv2(vec2 v);
vec2 tile(vec2 texCoord, ivec2 uv, ivec2 cnt);

// endregion ------------------- IVEC2 -------------------

// region ------------------- VEC3 -------------------

float indexv3(vec3 v, int index);
vec3 v3(float x, float y, float z);
vec3 v2tov3(vec2 v, float f);
vec3 ftov3(float v);
vec3 v3zero();
vec3 v3one();
vec3 v3front();
vec3 v3back();
vec3 v3left();
vec3 v3right();
vec3 v3up();
vec3 v3down();
vec3 v3white();
vec3 v3black();
vec3 v3ltGrey();
vec3 v3grey();
vec3 v3dkGrey();
vec3 v3red();
vec3 v3green();
vec3 v3blue();
vec3 v3yellow();
vec3 v3magenta();
vec3 v3cyan();
vec3 v3orange();
vec3 v3rose();
vec3 v3violet();
vec3 v3azure();
vec3 v3aquamarine();
vec3 negv3(vec3 v);
float dotv3(vec3 left, vec3 right);
vec3 crossv3(vec3 left, vec3 right);
vec3 addv3(vec3 left, vec3 right);
vec3 subv3(vec3 left, vec3 right);
vec3 mulv3(vec3 left, vec3 right);
vec3 mulv3f(vec3 left, float right);
vec3 divv3f(vec3 left, float right);
vec3 divv3(vec3 left, vec3 right);
vec3 powv3(vec3 left, vec3 right);
vec3 mixv3(vec3 left, vec3 right, float proportion);
float lenv3(vec3 v);
float lensqv3(vec3 v);
vec3 normv3(vec3 v);
vec3 lerpv3(vec3 from, vec3 to, float t);
vec3 reflectv3(vec3 v, vec3 n);
RefractResult refractv3(vec3 v, vec3 n, float niOverNt);

// endregion ------------------- VEC3 -------------------

// region ------------------- VEC4 -------------------

vec4 v4(float x, float y, float z, float w);
vec4 v3tov4(vec3 v, float f) ;
vec4 ftov4(float v);
vec4 v4zero();
vec4 addv4(vec4 left, vec4 right);
vec4 subv4(vec4 left, vec4 right);
vec4 mulv4(vec4 left, vec4 right);
vec4 mulv4f(vec4 left, float right);
vec4 divv4(vec4 left, vec4 right);
vec4 divv4f(vec4 left, float right);
float getxv4(vec4 v);
float getyv4(vec4 v);
float getzv4(vec4 v);
float getwv4(vec4 v);
float getrv4(vec4 v);
float getgv4(vec4 v);
float getbv4(vec4 v);
float getav4(vec4 v);
vec4 setxv4(vec4 v, float f);
vec4 setyv4(vec4 v, float f);
vec4 setzv4(vec4 v, float f);
vec4 setwv4(vec4 v, float f);
vec4 setrv4(vec4 v, float f);
vec4 setgv4(vec4 v, float f);
vec4 setbv4(vec4 v, float f);
vec4 setav4(vec4 v, float f);

// endregion ------------------- VEC4 -------------------

// region ------------------- MAT2 -------------------

mat2 scalem2(vec2 scale);
vec2 transformv2(vec2 vec, mat2 mat);

// endregion ------------------- MAT2 -------------------

// region ------------------- MAT4 -------------------

mat4 m4ident();
mat4 mulm4(mat4 left, mat4 right);
vec4 transformv4(vec4 vec, mat4 mat);
mat4 translatem4(vec3 vec);
mat4 rotatem4(vec3 axis, float angle);
mat4 scalem4(vec3 scale);

// endregion ------------------- MAT4 -------------------

// region ------------------- RAY -------------------

Ray rayBack();
vec3 rayPoint(Ray ray, float t);

// endregion ------------------- RAY -------------------

// region ------------------- RAND -------------------

float rndf (float x);
float rndv2(vec2  v);
float rndv3(vec3  v);
float rndv4(vec4  v);
vec3 seedRandom(vec3 s);
float seededRndf();
vec3 randomInUnitSphere() ;
vec3 randomInUnitDisk();

// endregion ------------------- RAND -------------------

// region ------------------- RAYTRACING ---------------

RtCamera cameraLookAt(vec3 eye, vec3 center, vec3 up,float vfoy, float aspect, float aperture, float focusDist);
Ray rayFromCamera(RtCamera camera, float u, float v);
vec3 background(Ray ray);
bool rayHitAabb(Ray ray, AABB aabb, float tMin, float tMax);
HitRecord raySphereHitRecord(Ray ray, float t, Sphere sphere);
HitRecord rayHitSphere(Ray ray, float tMin, float tMax, Sphere sphere);
HitRecord rayHitObject(Ray ray,float tMin, float tMax, int type, int index);
HitRecord rayHitBvh(Ray ray, float tMin, float tMax, int index);
HitRecord rayHitWorld(Ray ray, float tMin, float tMax);
ScatterResult materialScatterLambertian(HitRecord record, LambertianMaterial material);
ScatterResult materialScatterMetalic(Ray ray, HitRecord record, MetallicMaterial material);
ScatterResult materialScatterDielectric(Ray ray, HitRecord record, DielectricMaterial material);
ScatterResult materialScatter(Ray ray, HitRecord record);
vec3 sampleColor(int rayBounces, RtCamera camera, float u, float v);
vec4 fragmentColorRt(int width, int height,float random, int sampleCnt, int rayBounces, vec3 eye, vec3 center, vec3 up,
                     float fovy, float aspect, float aperture, float focusDist, vec2 texCoord);
vec4 gammaSqrt(vec4 result);
void raytracer();

// endregion ------------------- RAYTRACING ---------------

// region ------------------- SANDSIM ---------------

vec4 sandPhysics(vec4 orig);
vec4 sandSolver(vec4 orig, vec4 deltas);

// endregion ------------------- SANDSIM ---------------
