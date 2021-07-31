//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- VEC2 -------------------

vec2 v2(const float x, const float y) {
    return (vec2) { x, y };
}

public
vec2 ftov2(const float v) {
    return v2(v, v);
}

public
vec2 v2zero() {
    return ftov2(0.0f);
}

custom
vec2 mulv2f(const vec2 vec, const float v) {
    return v2(vec.x * v, vec.y * v);
}

custom
vec2 addv2f(const vec2 left, const float right) {
    return v2(left.x + right, left.y + right);
}

custom
vec2 subv2f(const vec2 left, const float right) {
    return v2(left.x - right, left.y - right);
}

public
float getxv2(const vec2 v) {
    return v.x;
}

public
float getyv2(const vec2 v) {
    return v.y;
}

// endregion ------------------- VEC2 -------------------
