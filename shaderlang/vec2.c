//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- VEC2 -------------------

custom
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

public
vec2 addv2(const vec2 l, const vec2 r) {
    return v2(l.x + r.x, l.y + r.y);
}

custom
vec2 subv2(const vec2 left, const vec2 right) {
    return v2(left.x - right.x, left.y - right.y);
}

custom
vec2 mulv2(const vec2 left, const vec2 right) {
    return v2(left.x * right.x, left.y * right.y);
}

public
vec2 divv2(const vec2 left, const vec2 right) {
    return v2(left.x/right.x, left.y/right.y);
}

custom
vec2 mulv2f(const vec2 vec, const float v) {
    return v2(vec.x * v, vec.y * v);
}

public
vec2 divv2f(const vec2 v, const float f) {
    return v2(v.x / f, v.y / f);
}

custom
vec2 addv2f(const vec2 left, const float right) {
    return v2(left.x + right, left.y + right);
}

custom
vec2 subv2f(const vec2 left, const float right) {
    return v2(left.x - right, left.y - right);
}

custom
float dotv2(const vec2 left, const vec2 right) {
    return left.x * right.x + left.y * right.y;
}

public
float getxv2(const vec2 v) {
    return v.x;
}

public
float getyv2(const vec2 v) {
    return v.y;
}

public
float lenv2(const vec2 v) {
    return sqrtf(v.x*v.x + v.y*v.y);
}

// endregion ------------------- VEC2 -------------------
