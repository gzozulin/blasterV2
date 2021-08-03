//
// Created by greg on 2021-08-02.
//

#include "lang.h"

protected
vec2 nearbyCellCoords(const vec2 uv, const float cellW, const float cellH, const int x, const int y) {
    return v2(uv.x + itof(x) * cellW, uv.y + itof(y) * cellH);
}

protected
ivec2 tryDepositParticle(const sampler2D orig, const vec2 uv, const float cellW, const float cellH, const int x) {
    const vec2 coords = nearbyCellCoords(uv, cellW, cellH, x, -1);
    if (coords.x < 0.0f || coords.y < 0.0f || coords.x > 1.0f || coords.y > 1.0f) {
        return iv2zero();
    }
    const vec4 below = sampler(orig, coords);
    if (below.x == 0.0f) {
        return iv2(x, -1);
    }
    return iv2zero();
}

public
vec4 sandPhysics(const sampler2D orig, const vec2 uv, const ivec2 wh) {
    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);

    const vec4 own = sampler(orig, uv);
    if (own.x == 0.0f) {
        return v4zero();
    }

    ivec2 deposit = tryDepositParticle(orig, uv, cellW, cellH, 0);
    if (!eqiv2(deposit, iv2zero())) {
        return iv2tov4(deposit, 0.0f, 0.0f);
    }

    const bool left = rndv2(uv) > 0.5f;
    const int first =  left ? -1 :  1;
    const int second = left ?  1 : -1;

    deposit = tryDepositParticle(orig, uv, cellW, cellH, first);
    if (!eqiv2(deposit, iv2zero())) {
        return iv2tov4(deposit, 0.0f, 0.0f);
    }

    deposit = tryDepositParticle(orig, uv, cellW, cellH, second);
    if (!eqiv2(deposit, iv2zero())) {
        return iv2tov4(deposit, 0.0f, 0.0f);
    }

    return v4zero();
}

public
vec4 sandSolver(const sampler2D orig, const sampler2D deltas, const vec2 uv, const ivec2 wh) {
    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);

    vec4 result = v4zero();
    for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
            const vec2 coords = nearbyCellCoords(uv, cellW, cellH, x, y);
            if (coords.x < 0.0f || coords.y < 0.0f || coords.x > 1.0f || coords.y > 1.0f) {
                continue;
            }
            const vec4 cell = sampler(orig, coords);
            if (eqv4(cell, v4zero())) {
                continue;
            }
            const vec4 delta = sampler(deltas, coords);
            if (delta.x == itof(-x) && delta.y == itof(-y)) {
                result.x += cell.x;
            }
        }
    }
    return result;
}
