//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- RAY -------------------

public
ray rayBack() {
    const ray result = {v3zero(), v3back() };
    return result;
}

public
vec3 rayPoint(const ray ray, const float t) {
    return addv3(ray.origin, mulv3f(ray.direction, t));
}

// endregion ------------------- RAY -------------------
