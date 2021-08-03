//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- VEC4 -------------------

custom
vec4 v4(const float x, const float y, const float z, const float w) {
    return (vec4) { x, y, z, w };
}

public
vec4 v3tov4(const vec3 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

public
vec4 ftov4(const float v) {
    return v4(v, v, v, v);
}

public
vec4 v4zero() {
    return ftov4(0.0f);
}

public
vec4 v4one() {
    return ftov4(1.0f);
}

public
vec4 addv4(const vec4 left, const vec4 right) {
    return v4(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
}

public
vec4 subv4(const vec4 left, const vec4 right) {
    return v4(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
}

public
vec4 mulv4(const vec4 left, const vec4 right) {
    return v4(left.x * right.x, left.y * right.y, left.z * right.z, left.w * right.w);
}

public
vec4 mulv4f(const vec4 left, const float right) {
    return v4(left.x * right, left.y * right, left.z * right, left.w * right);
}

public
vec4 divv4(const vec4 left, const vec4 right) {
    return v4(left.x / right.x, left.y / right.y, left.z / right.z, left.w / right.w);
}

public
vec4 divv4f(const vec4 left, const float right) {
    return v4(left.x / right, left.y / right, left.z / right, left.z / right);
}

public
float getxv4(const vec4 v) {
    return v.x;
}

public
float getyv4(const vec4 v) {
    return v.y;
}

public
float getzv4(const vec4 v) {
    return v.z;
}

public
float getwv4(const vec4 v) {
    return v.w;
}

public
float getrv4(const vec4 v) {
    return v.x;
}

public
float getgv4(const vec4 v) {
    return v.y;
}

public
float getbv4(const vec4 v) {
    return v.z;
}

public
float getav4(const vec4 v) {
    return v.w;
}

public
vec4 setxv4(const vec4 v, const float f) {
    return v4(f, v.y, v.z, v.w);
}

public
vec4 setyv4(const vec4 v, const float f) {
    return v4(v.x, f, v.z, v.w);
}

public
vec4 setzv4(const vec4 v, const float f) {
    return v4(v.x, v.y, f, v.w);
}

public
vec4 setwv4(const vec4 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

public
vec4 setrv4(const vec4 v, const float f) {
    return v4(f, v.y, v.z, v.w);
}

public
vec4 setgv4(const vec4 v, const float f) {
    return v4(v.x, f, v.z, v.w);
}

public
vec4 setbv4(const vec4 v, const float f) {
    return v4(v.x, v.y, f, v.w);
}

public
vec4 setav4(const vec4 v, const float f) {
    return v4(v.x, v.y, v.z, f);
}

// endregion ------------------- VEC4 -------------------
