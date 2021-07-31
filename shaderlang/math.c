//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- COMMON -------------------

public
float sqrtv(const float value) {
    return sqrt(value);
}

public
float sinv(const float rad) {
    return sin(rad);
}

public
float cosv(const float rad) {
    return cos(rad);
}

public
float tanv(const float rad) {
    return tan(rad);
}

public
float powv(const float base, const float power) {
    return pow(base, power);
}

public
float minv(const float left, const float right) {
    return min(left, right);
}

public
float maxv(const float left, const float right) {
    return max(left, right);
}

custom
float clamp(float x, float lowerlimit, float upperlimit) {
    if (x < lowerlimit)
        x = lowerlimit;
    if (x > upperlimit)
        x = upperlimit;
    return x;
}

custom
float smoothstep(float edge0, float edge1, float x) {
    // Scale, bias and saturate x to 0..1 range
    x = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
    // Evaluate polynomial
    return x * x * (3 - 2 * x);
}

custom
float floor(float value) {
    return floorf(value);
}

custom
float fract(float value) {
    return value - floorf(value);
}

public
float schlick(float cosine, float ri) {
    float r0 = (1 - ri) / (1 + ri);
    r0 = r0*r0;
    return r0 + (1 - r0) * pow((1 - cosine), 5);
}

custom
float length(const vec2 v) {
    return sqrtf(v.x*v.x + v.y*v.y);
}

public
float remap(const float a, const float b, const float c, const float d, const float t) {
    return ((t-a)/(b-a)) * (d-c) + c;
}

// endregion ------------------- COMMON -------------------
