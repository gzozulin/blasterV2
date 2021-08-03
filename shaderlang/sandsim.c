//
// Created by greg on 2021-08-02.
//

#include "lang.h"

public
vec4 sandPhysics(sampler2D orig, vec2 uv, ivec2 wh) {
    // 1. spread if compressed (find the nearest empty point via siblings)
    // 2. solve the equation for each one

    // only one per cell at the end!!

    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);

    // 1. get own cell, if empty - return zero
    // 2. if 3 cells below not empty - return zero
    // 3. move into random cell below

    const vec4 dummy = sampler(orig, v2(cellW * uv.x, cellH * uv.y));

    return v4(0.0f, 0.0f, dummy.x, dummy.y);
}

public
vec4 sandSolver(sampler2D orig, sampler2D deltas, vec2 uv, ivec2 wh) {
    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);
    vec4 result = v4zero();
    for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
            const vec2 coords = v2(uv.x + itof(x) * cellW, uv.y + itof(y) * cellH);
            vec4 curr = sampler(orig, coords);
            if (curr.x == 0) {
                continue;
            }
            vec4 delta = sampler(deltas, coords);
            if (delta.x == itof(-x) && delta.y == itof(-y)) {
                addv4(result, v4one());
            }
        }
    }
    return result;
}