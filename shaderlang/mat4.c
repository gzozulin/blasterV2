//
// Created by greg on 2021-07-30.
//

#include "lang.h"

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
    float s = sinf(angle);
    float c = cosf(angle);
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
