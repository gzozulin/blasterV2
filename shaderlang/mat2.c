//
// Created by greg on 2021-07-30.
//

#include "lang.h"

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
