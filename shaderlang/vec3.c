//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- VEC3 -------------------

public
float indexv3(const vec3 v, const int index) {
    switch (index) {
        case 0: return v.x;
        case 1: return v.y;
        case 2: return v.z;
        default:
            error(); return v.x;
    }
}

custom
vec3 v3(const float x, const float y, const float z) {
    return (vec3) { x, y, z };
}

public
vec3 v2tov3(vec2 v, const float f) {
    return v3(v.x, v.y, f);
}

public
vec3 ftov3(const float v) {
    return v3(v, v, v);
}

public
vec3 v3zero() {
    return ftov3(0.0f);
}

public
vec3 v3one() {
    return ftov3(1.0f);
}

public
vec3 v3front() {
    return v3(0, 0, 1);
}

public
vec3 v3back() {
    return v3(0, 0, -1);
}

public
vec3 v3left() {
    return v3(-1, 0, 0);
}

public
vec3 v3right() {
    return v3(1, 0, 0);
}

public
vec3 v3up() {
    return v3(0, 1, 0);
}

public
vec3 v3down() {
    return v3(0, -1, 0);
}

public
vec3 v3white() {
    return v3(1.0f, 1.0f, 1.0f);
}

public
vec3 v3black() {
    return v3(0.0f, 0.0f, 0.0f);
}
public
vec3 v3ltGrey() {
    return ftov3(0.3f);
}

public
vec3 v3grey() {
    return ftov3(0.5f);
}

public
vec3 v3dkGrey() {
    return ftov3(0.7f);
}

public
vec3 v3red() {
    return v3(1.0f, 0.0f, 0.0f);
}

public
vec3 v3green() {
    return v3(0.0f, 1.0f, 0.0f);
}

public
vec3 v3blue() {
    return v3(0.0f, 0.0f, 1.0f);
}

public
vec3 v3yellow() {
    return v3(1.0f, 1.0f, 0.0f);
}

public
vec3 v3magenta() {
    return v3(1.0f, 0.0f, 1.0f);
}

public
vec3 v3cyan() {
    return v3(0.0f, 1.0f, 1.0f);
}

public
vec3 v3orange() {
    return v3(1.0f, 0.5f, 0.0f);
}
public
vec3 v3rose() {
    return v3(1.0f, 0.0f, 0.5f);
}

public
vec3 v3violet() {
    return v3(0.5f, 0.0f, 1.0f);
}

public
vec3 v3azure() {
    return v3(0.0f, 0.5f, 1.0f);
}

public
vec3 v3aquamarine() {
    return v3(0.0f, 1.0f, 0.5f);
}

public
vec3 v3chartreuse() {
    return v3(0.5f, 1.0f, 0.0f);
}

public
vec2 v3xy(vec3 vec) {
    return v2(vec.x, vec.y);
}

public
vec2 v3xz(vec3 vec) {
    return v2(vec.x, vec.z);
}

public
vec2 v3yz(vec3 vec){
    return v2(vec.y, vec.z);
}

public
vec3 negv3(const vec3 v) {
    return v3(-v.x, -v.y, -v.z);
}

custom
float dotv3(const vec3 left, const vec3 right) {
    return left.x * right.x + left.y * right.y + left.z * right.z;
}

custom
vec3 crossv3(const vec3 left, const vec3 right) {
    return v3(
            left.y * right.z - left.z * right.y,
            left.z * right.x - left.x * right.z,
            left.x * right.y - left.y * right.x);
}

custom
vec3 addv3(const vec3 left, const vec3 right) {
    return v3(left.x + right.x, left.y + right.y, left.z + right.z);
}

custom
vec3 subv3(const vec3 left, const vec3 right) {
    return v3(left.x - right.x, left.y - right.y, left.z - right.z);
}

custom
vec3 mulv3(const vec3 left, const vec3 right) {
    return v3(left.x * right.x, left.y * right.y, left.z * right.z);
}

custom
vec3 mulv3f(const vec3 left, const float right) {
    return v3(left.x * right, left.y * right, left.z * right);
}

custom
vec3 divv3f(const vec3 left, const float right) {
    return v3(left.x / right, left.y / right, left.z / right);
}

custom
vec3 divv3(const vec3 left, const vec3 right) {
    return v3(left.x / right.x, left.y / right.y, left.z / right.z);
}

public
vec3 powv3(const vec3 left, const vec3 right) {
    return v3(powf(left.x, right.x), powf(left.y, right.y), powf(left.z, right.z));
}

public
vec3 mixv3(const vec3 left, const vec3 right, const float proportion) {
    return addv3(mulv3(left, ftov3(1.0f - proportion)), mulv3(right, ftov3(proportion)));
}

public
float lenv3(const vec3 v) {
    return sqrtf(v.x*v.x + v.y*v.y + v.z*v.z);
}

public
float lensqv3(const vec3 v) {
    return (v.x*v.x + v.y*v.y + v.z*v.z);
}

public
vec3 normv3(const vec3 v) {
    return divv3f(v, lenv3(v));
}

public
vec3 lerpv3(const vec3 from, const vec3 to, const float t) {
    return addv3(mulv3f(from, 1.0f - t), mulv3f(to, t));
}

public
vec3 reflectv3(const vec3 v, const vec3 n) {
    return subv3(v, mulv3f(n, 2.0f * dotv3(v, n)));
}

protected
RefractResult refractv3(const vec3 v, const vec3 n, const float niOverNt) {
    const vec3 unitV = normv3(v);
    const float dt = dotv3(unitV, n);
    const float D = 1.0f - niOverNt*niOverNt*(1.0f - dt*dt);
    if (D > 0) {
        const vec3 left = mulv3f(subv3(unitV, mulv3f(n, dt)), niOverNt);
        const vec3 right = mulv3f(n, sqrtf(D));
        const RefractResult result = { true, subv3(left, right) };
        return result;
    } else {
        return NO_REFRACT;
    }
}

// endregion ------------------- VEC3 -------------------

