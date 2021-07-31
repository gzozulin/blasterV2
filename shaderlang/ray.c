//
// Created by greg on 2021-07-30.
//

#include "lang.h"

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
