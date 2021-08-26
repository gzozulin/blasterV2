//
// Created by greg on 2021-07-30.
//

// todo: glsl refract
// todo: glsl reflect
// todo: math library
// todo: functions, which are redefined via recipes: sceneDist function for example

#pragma once

#include <stdbool.h>

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

typedef struct sampler2D {
    int handle;
} sampler2D;

typedef struct samplerBuffer {
    int handle;
} samplerBuffer;

typedef struct samplerCube {
    int handle;
} samplerCube;

public
typedef struct ray {
    vec3 origin;
    vec3 direction;
} ray;

public
typedef struct aabb {
    vec3 pointMin;
    vec3 pointMax;
} aabb;

public
typedef struct Camera {
    vec3 origin;
    vec3 lowerLeft;
    vec3 horizontal;
    vec3 vertical;
    vec3 w, u, v;
    float lensRadius;
} Camera;

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
    aabb aabb;
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
    ray scattered;
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

float signf(float value);
float absf(float value);
float sqrtf(float value);
float sinf(float rad);
float cosf(float rad);
float tanf(float rad);
float powf(float base, float power);
float minf(float left, float right);
float maxf(float left, float right);
float clampf(float x, float lowerlimit, float upperlimit);
float smoothf(float edge0, float edge1, float x);
float floorf(float value);
float fractf(float value);
float schlickf(float cosine, float ri);
float remapf(float a, float b, float c, float d, float t);

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
bool eqiv2(ivec2 left, ivec2 right);
bool eqv3(vec3 left, vec3 right);
bool eqv4(vec4 left, vec4 right);

// endregion ------------------- BOOL -------------------

// region ------------------- VEC2 -------------------

vec2 v2(float x, float y);
vec2 ftov2(float v);
vec2 v2zero();

vec2 addv2(vec2 l, vec2 r);
vec2 subv2(vec2 left, vec2 right);
vec2 mulv2(vec2 left, vec2 right);
vec2 divv2(vec2 left, vec2 right);

vec2 mulv2f(vec2 vec, float v);
vec2 divv2f(vec2 v, float f);
vec2 addv2f(vec2 left, float right);
vec2 subv2f(vec2 left, float right);
float dotv2(vec2 left, vec2 right);

float getxv2(vec2 v);
float getyv2(vec2 v);

float lenv2(vec2 v);

// endregion ------------------- VEC2 -------------------

// region ------------------- IVEC2 -------------------

ivec2 iv2(int x, int y);
ivec2 iv2zero();
vec2 iv2tov2(ivec2 v);
vec4 iv2tov4(ivec2 vec, float z, float w);

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

vec2 xyv3(vec3 vec);
vec2 xzv3(vec3 vec);
vec2 yzv3(vec3 vec);

vec3 absv3(vec3 v);
vec3 negv3(vec3 v);
float dotv3(vec3 left, vec3 right);
vec3 crossv3(vec3 left, vec3 right);
vec3 addv3(vec3 left, vec3 right);
vec3 subv3(vec3 left, vec3 right);
vec3 subv3f(vec3 left, float right);
vec3 mulv3(vec3 left, vec3 right);
vec3 mulv3f(vec3 left, float right);
vec3 divv3f(vec3 left, float right);
vec3 divv3(vec3 left, vec3 right);
vec3 powv3(vec3 left, vec3 right);
vec3 mixv3(vec3 left, vec3 right, float proportion);
vec3 maxv3(vec3 left, vec3 right);
vec3 minv3(vec3 left, vec3 right);

float lenv3(vec3 v);
vec3 sqrtv3(vec3 v);
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
vec3 v4tov3(vec4 v);
vec4 v4zero();
vec4 v4one();

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

// region ------------------- SDFS -------------------

float sdXZPlane(vec3 p);
float sdSphere(vec3 p, float r);
float sdBox(vec3 p, vec3 b);
float sdCappedCylinder(vec3 p, vec3 a, vec3 b, float r);
float sdSimplifiedCyl(vec3 p, float cylLen, float cylRad);
float sdCone(vec3 p, vec2 c, float h);
float sdTriPrism(vec3 p, vec2 h);

float opUnion(float d1, float d2);
float opSubtraction(float d1, float d2);
float opIntersection(float d1, float d2);

// endregion ------------------- SDFS -------------------

// region ------------------- RAY -------------------

ray rayBack();
vec3 rayPoint(ray ray, float t);

// endregion ------------------- RAY -------------------

// region ------------------- CAMERA -------------------

vec2 centerUV(vec2 uv, float aspect);
Camera cameraLookAt(vec3 eye, vec3 center, vec3 up, float fovy, float aspect, float aperture, float focusDist);
ray rayFromCamera(Camera camera, vec2 uv);

// endregion ------------------- CAMERA -------------------

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

// region ------------------- SAMPLER -------------------

vec4 sampler(sampler2D sampler, vec2 texCoords);
vec4 texel(samplerBuffer sampler, int index);
vec4 samplerq(samplerCube sampler, vec3 texCoords);

// endregion ------------------- SAMPLER -------------------
