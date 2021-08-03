//
// Created by greg on 2021-07-30.
//

#include "lang.h"

#include <math.h>

// region ------------------- MATH -------------------

custom
float sqrtf(const float value) {
    return (float) sqrt((double) value);
}

custom
float sinf(const float rad) {
    return (float) sin((double) rad);
}

custom
float cosf(const float rad) {
    return (float) cos((double) rad);
}

custom
float tanf(const float rad) {
    return (float) tan((double) rad);
}

custom
float powf(const float base, const float power) {
    return (float) pow((double) base, (double) power);
}

custom
float minf(const float left, const float right) {
    return left < right ? left : right;
}

custom
float maxf(const float left, const float right) {
    return left > right ? left : right;
}

custom
float clampf(float x, float lowerlimit, float upperlimit) {
    if (x < lowerlimit)
        x = lowerlimit;
    if (x > upperlimit)
        x = upperlimit;
    return x;
}

custom
float smoothf(float edge0, float edge1, float x) {
    // Scale, bias and saturate x to 0..1 range
    x = clampf((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
    // Evaluate polynomial
    return x * x * (3 - 2 * x);
}

custom
float floorf(float value) {
    return floor((double) value);
}

custom
float fractf(float value) {
    return value - floorf(value);
}

public
float schlickf(float cosine, float ri) {
    float r0 = (1 - ri) / (1 + ri);
    r0 = r0*r0;
    return r0 + (1 - r0) * powf((1 - cosine), 5);
}

public
float remapf(const float a, const float b, const float c, const float d, const float t) {
    return ((t-a)/(b-a)) * (d-c) + c;
}

// endregion ------------------- MATH -------------------
