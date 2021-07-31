//
// Created by greg on 2021-07-30.
//

#include "lang.h"

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