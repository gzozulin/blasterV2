//
// Created by greg on 2021-07-30.
//

#include "lang.h"

// region ------------------- BOOL -------------------

public
bool eqv2(const vec2 left, const vec2 right) {
    return left.x == right.x && left.y == right.y;
}

public
bool eqv3(const vec3 left, const vec3 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z;
}

public
bool eqv4(const vec4 left, const vec4 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z && left.w == right.w;
}

// endregion ------------------- BOOL -------------------

