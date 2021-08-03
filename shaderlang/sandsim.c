//
// Created by greg on 2021-08-02.
//

#include "lang.h"

protected
vec4 tryDepositParticle(sampler2D orig, vec2 cell, vec2 uv, int x) {
    const vec2 coords = v2(uv.x + itof(x) * cell.x, uv.y - cell.y);
    if (coords.x < 0.0f || coords.y < 0.0f || coords.x > 1.0f || coords.y > 1.0f) {
        return v4zero();
    }
    const vec4 below = sampler(orig, coords);
    if (below.x == 0.0f) {
        return v4(itof(x), -1.0f, 0.0f, 1.0f);
    }
    return v4zero();
}

public
vec4 sandPhysics(sampler2D orig, vec2 uv, ivec2 wh) {
    const vec2 cell = v2(1.0f / itof(wh.x), 1.0f / itof(wh.y));

    const vec4 own = sampler(orig, uv);
    if (own.x == 0.0f) {
        return v4zero();
    }

    vec4 deposit = tryDepositParticle(orig, cell, uv, 0); // center
    if (!eqv4(deposit, v4zero())) {
        return deposit;
    }

    deposit = tryDepositParticle(orig, cell, uv, -1); // left
    if (!eqv4(deposit, v4zero())) {
        return deposit;
    }

    deposit = tryDepositParticle(orig, cell, uv, 1); // right
    if (!eqv4(deposit, v4zero())) {
        return deposit;
    }

    return v4zero();
}

public
vec4 sandSolver(sampler2D orig, sampler2D deltas, vec2 uv, ivec2 wh) {
    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);
    vec4 result = v4(0.0f, 0.0f, 0.0f, 1.0f);
    for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
            const vec2 coords = v2(uv.x + itof(x) * cellW, uv.y + itof(y) * cellH);
            if (coords.x < 0.0f || coords.y < 0.0f || coords.x > 1.0f || coords.y > 1.0f) {
                continue;
            }
            const vec4 cell = sampler(orig, coords);
            if (cell.x == 0.0f) {
                continue;
            }
            const vec4 delta = sampler(deltas, coords);
            if (delta.x == itof(-x) && delta.y == itof(-y)) {
                result.x += cell.x;
            }
        }
    }


    // if still empty - try spreading
    return result;
}
