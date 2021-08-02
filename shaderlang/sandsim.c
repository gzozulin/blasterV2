//
// Created by greg on 2021-08-02.
//

#include "lang.h"

public
vec4 sandPhysics(sampler2D orig, vec2 uv, ivec2 wh) {
    // 1. spread if compressed (find the nearest empty point via siblings)
    // 2. solve the equation for each one
    return sampler(orig, v2zero());
}

public
vec4 sandSolver(sampler2D orig, sampler2D deltas, vec2 uv, ivec2 wh) {
    // select all around me and check who is pointing on me, accumulate
    return addv4(sampler(orig, v2zero()), sampler(deltas, v2zero()));
}