#include "shaderlang.h"

custom
void error() {
    assert(0 && "WTF?!");
}

// region ------------------- PUBLIC CONST -------------------

public
const float BOUNCE_ERR = 0.001f;

public
const HitRecord NO_HIT = { -1, { 0, 0, 0 }, { 1, 0, 0 }, 0, 0 };

public
const ScatterResult NO_SCATTER = { { -1, -1, -1 }, { { 0, 0, 0 }, { 0, 0, 0 } } };

// endregion ------------------- PUBLIC CONST -------------------

// region ------------------- PRIVATE CONST -------------------

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

// endregion ------------------- PRIVATE CONST -------------------

// region ------------------- MAT2 -------------------

custom
mat2 scalem2(const vec2 scale) {
    const mat2 result = {{
             scale.x, 0,
             0, scale.y,
     }};
    return result;
}

custom
vec2 transformv2(const vec2 vec, const mat2 mat) {
    return v2zero();
}

// endregion ------------------- MAT2 -------------------

// region ------------------- MAT4 -------------------

custom
mat4 m4ident() {
    const mat4 result = {{
         1, 0, 0, 0,
         0, 1, 0, 0,
         0, 0, 1, 0,
         0, 0, 0, 1
     }};
    return result;
}

custom
mat4 mulm4(const mat4 left, const mat4 right) {
    return m4ident();
}

custom
vec4 transformv4(const vec4 vec, const mat4 mat) {
    return v4zero();
}

custom
mat4 translatem4(vec3 vec) {
    const mat4 result = {{
         1, 0, 0, vec.x,
         0, 1, 0, vec.y,
         0, 0, 1, vec.z,
         0, 0, 0, 1
     }};
    return result;
}

custom
mat4 rotatem4(vec3 axis, const float angle) {
    axis = normv3(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0f - c;

    const mat4 result = {{
         oc * axis.x * axis.x + c,          oc * axis.x * axis.y - axis.z * s, oc * axis.z * axis.x + axis.y * s, 0.0f,
         oc * axis.x * axis.y + axis.z * s, oc * axis.y * axis.y + c,          oc * axis.y * axis.z - axis.x * s, 0.0f,
         oc * axis.z * axis.x - axis.y * s, oc * axis.y * axis.z + axis.x * s, oc * axis.z * axis.z + c,          0.0f,
         0.0f,                              0.0f,                              0.0f,                              1.0f
     }};

    return result;
}

custom
mat4 scalem4(const vec3 scale) {
    const mat4 result = {{
         scale.x, 0, 0, 0,
         0, scale.y, 0, 0,
         0, 0, scale.z, 0,
         0, 0, 0, 1
     }};
    return result;
}

// endregion ------------------- MAT4 -------------------

// region ------------------- RAY -------------------

public
Ray rayBack() {
    const Ray result = { v3zero(), v3back() };
    return result;
}

public
vec3 rayPoint(const Ray ray, const float t) {
    return addv3(ray.origin, mulv3f(ray.direction, t));
}

// endregion ------------------- RAY -------------------

// region ------------------- BOOL -------------------

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

// endregion ------------------- BOOL -------------------

// region ------------------- RAND -------------------

custom
float rndf (float x) { return dtof(drand48()); }

custom
float rndv2(vec2  v) { return dtof(drand48()); }

custom
float rndv3(vec3  v) { return dtof(drand48()); }

custom
float rndv4(vec4  v) { return dtof(drand48()); }

custom
vec3 seedRandom(const vec3 s) {
    return s;
}

custom
float seededRndf() {
    return dtof(drand48());
}

public
vec3 randomInUnitSphere() {
    vec3 result;
    for (int i = 0; i < 10; i++) {
        result = v3(seededRndf() * 2.0f - 1.0f, seededRndf() * 2.0f - 1.0f, seededRndf() * 2.0f - 1.0f);
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
        result = subv3(mulv3f(v3(seededRndf(), seededRndf(), 0.0f), 2.0f), v3(1.0f, 1.0f, 0.0f));
        if (dotv3(result, result) >= 1.0f) {
            return result;
        }
    }
    return normv3(result); // wrong, but should not happen
}

// endregion ------------------- RAND -------------------

// region ------------------- RAYTRACING ---------------

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
        error(); // spheres only
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
    if (seededRndf() < reflectProbe) {
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
        const float du = DU * seededRndf();
        const float dv = DV * seededRndf();
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
                    seededRndf(), SAMPLES, 4,
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

// endregion ------------------- RAYTRACING ---------------

// region ------------------- LOGGING ---------------

void printv3(const vec3 v) {
    printf("v3 = {%f, %f, %f}\n", v.x, v.y, v.z);
}

// endregion ------------------- LOGGING ---------------

// region ------------------- MAIN ---------------

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

// endregion ------------------- MAIN ---------------
