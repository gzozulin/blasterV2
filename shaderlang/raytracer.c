//
// Created by greg on 2021-07-30.
//

#include "lang.h"

#include <float.h>
#include <stdio.h>
#include <stdlib.h>

// region ------------------- RAYTRACING ---------------

protected
vec3 background(const ray ray) {
    const float t = (ray.direction.y + 1.0f) * 0.5f;
    const vec3 gradient = lerpv3(v3one(), v3(0.5f, 0.7f, 1.0f), t);
    return gradient;
}

protected
bool rayHitAabb(const ray ray, const aabb aabb, const float tMin, const float tMax) {
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
HitRecord rayHitSphereRecord(ray ray, float t, Sphere sphere) {
    const vec3 point = rayPoint(ray, t);
    const vec3 N = normv3(divv3f(subv3(point, sphere.center), sphere.radius));
    const HitRecord result = { t, point, N, sphere.materialType, sphere.materialIndex };
    return result;
}

protected
HitRecord rayHitSphere(const ray ray, const float tMin, const float tMax, const Sphere sphere) {
    const vec3 oc = subv3(ray.origin, sphere.center);
    const float a = dotv3(ray.direction, ray.direction);
    const float b = 2 * dotv3(oc, ray.direction);
    const float c = dotv3(oc, oc) - sphere.radius * sphere.radius;
    const float D = b*b - 4*a*c;

    if (D > 0) {
        float t = (-b - sqrtf(D)) / 2 * a;
        if (t < tMax && t > tMin) {
            return rayHitSphereRecord(ray, t, sphere);
        }

        t = (-b + sqrtf(D)) / 2 * a;
        if (t < tMax && t > tMin) {
            return rayHitSphereRecord(ray, t, sphere);
        }
    }
    return NO_HIT;
}

protected
HitRecord rayHitObject(const ray ray, const float tMin, const float tMax, const int type, const int index) {
    if (type != HITABLE_SPHERE) {
        error(); // raymarcherSpheres only
        return NO_HIT;
    }
    return rayHitSphere(ray, tMin, tMax, uSpheres[index]);
}

protected
HitRecord rayHitBvh(const ray ray, const float tMin, const float tMax, const int index) {
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
HitRecord rayHitWorld(const ray ray, const float tMin, const float tMax) {
    return rayHitBvh(ray, tMin, tMax, 0);
}

protected
ScatterResult scatterLambertian(HitRecord record, LambertianMaterial material) {
    const vec3 tangent = addv3(record.point, record.normal);
    const vec3 direction = addv3(tangent, randomInUnitSphere());
    const ScatterResult result = { material.albedo, { record.point, subv3(direction, record.point) } };
    return result;
}

protected
ScatterResult scatterMetallic(ray ray, HitRecord record, MetallicMaterial material) {
    const vec3 reflected = reflectv3(ray.direction, record.normal);
    if (dotv3(reflected, record.normal) > 0) {
        const ScatterResult result = { material.albedo, { record.point, reflected } };
        return result;
    } else {
        return NO_SCATTER;
    }
}

protected
ScatterResult scatterDielectric(ray ray, HitRecord record, DielectricMaterial material) {
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
        reflectProbe = schlickf(cosine, material.reflectiveIndex);
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
ScatterResult scatterMaterial(ray ray, HitRecord record) {
    switch (record.materialType) {
        case MATERIAL_LAMBERTIAN:
            return scatterLambertian(record, uLambertianMaterials[record.materialIndex]);
        case MATERIAL_METALIIC:
            return scatterMetallic(ray, record, uMetallicMaterials[record.materialIndex]);
        case MATERIAL_DIELECTRIC:
            return scatterDielectric(ray, record, uDielectricMaterials[record.materialIndex]);
        default:
            return NO_SCATTER;
    }
}

protected
vec3 sampleColor(const int rayBounces, const Camera camera, const vec2 uv) {
    ray ray = rayFromCamera(camera, uv);
    vec3 fraction = ftov3(1.0f);
    for (int i = 0; i < rayBounces; i++) {
        const HitRecord record = rayHitWorld(ray, BOUNCE_ERR, FLT_MAX);
        if (record.t < 0) {
            break;
        } else {
            const ScatterResult scatterResult = scatterMaterial(ray, record);
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

    const Camera camera = cameraLookAt(eye, center, up, fovy, aspect, aperture, focusDist);
    vec3 result = v3zero();
    for (int i = 0; i < sampleCnt; i++) {
        const float du = DU * seededRndf();
        const float dv = DV * seededRndf();
        const vec2 uv = addv2(texCoord, v2(du, dv));
        result = addv3(result, sampleColor(rayBounces, camera, uv));
    }
    return v3tov4(result, 1.0f);
}

public
vec4 gammaSqrt(const vec4 result) {
    return v4(sqrtf(result.x), sqrtf(result.y), sqrtf(result.z), 1.0f);
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
