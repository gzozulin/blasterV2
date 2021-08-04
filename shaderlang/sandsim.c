//
// Created by greg on 2021-08-02.
//

#include "lang.h"

public
const int TYPE_EMPTY    = 0;

public
const int TYPE_SAND     = 1;

public
const int TYPE_WATER    = 2;

public
vec4 sandConvert(const vec4 pixel) {
    if (pixel.x > 0.9f && pixel.y > 0.9f) {
        return v4(itof(TYPE_SAND), 0.0f, 0.0f, pixel.x);
    } if (pixel.z > 0.9f) {
        return v4(itof(TYPE_WATER), 0.0f, 0.0f, pixel.x);
    } else {
        return v4zero();
    }
}

protected
vec2 nearbyCellCoords(const vec2 uv, const float cellW, const float cellH, const int x, const int y) {
    return v2(uv.x + itof(x) * cellW, uv.y + itof(y) * cellH);
}

protected
ivec2 tryDepositParticle(const sampler2D orig, const vec2 uv,
                         const float cellW, const float cellH,
                         const int x, const int y) {
    const vec2 coords = nearbyCellCoords(uv, cellW, cellH, x, y);
    if (coords.x < 0.0f || coords.y < 0.0f || coords.x > 1.0f || coords.y > 1.0f) {
        return iv2zero();
    }
    const vec4 cell = sampler(orig, coords);
    if (ftoi(cell.x) == TYPE_EMPTY) {
        return iv2(x, y);
    }
    return iv2zero();
}

protected
ivec2 simTypeSand(const sampler2D orig, const vec2 uv, const float cellW, const float cellH) {
    ivec2 deposit = tryDepositParticle(orig, uv, cellW, cellH, 0, -1);
    if (!eqiv2(deposit, iv2zero())) {
        return deposit;
    }

    const bool left = rndv2(uv) > 0.5f;
    const int first =  left ? -1 :  1;
    const int second = left ?  1 : -1;

    deposit = tryDepositParticle(orig, uv, cellW, cellH, first, -1);
    if (!eqiv2(deposit, iv2zero())) {
        return deposit;
    }

    deposit = tryDepositParticle(orig, uv, cellW, cellH, second, -1);
    if (!eqiv2(deposit, iv2zero())) {
        return deposit;
    }

    return iv2zero();
}

protected
ivec2 simTypeWater(const sampler2D orig, const vec2 uv, const float cellW, const float cellH) {
    ivec2 deposit = simTypeSand(orig, uv, cellW, cellH);
    if (!eqiv2(deposit, iv2zero())) {
        return deposit;
    }

    const bool left = rndv3(v2tov3(uv, itof(TYPE_WATER))) > 0.5f;
    const int first =  left ? -1 :  1;
    const int second = left ?  1 : -1;

    deposit = tryDepositParticle(orig, uv, cellW, cellH, first, 0);
    if (!eqiv2(deposit, iv2zero())) {
        return deposit;
    }

    deposit = tryDepositParticle(orig, uv, cellW, cellH, second, 0);
    if (!eqiv2(deposit, iv2zero())) {
        return deposit;
    }

    return iv2zero();
}

public
vec4 sandPhysics(const sampler2D orig, const vec2 uv, const ivec2 wh) {
    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);

    const int type = ftoi(sampler(orig, uv).x);
    if (type == TYPE_SAND) {
        return iv2tov4(simTypeSand(orig, uv, cellW, cellH), 0.0f, 0.0f);
    } if (type == TYPE_WATER) {
        return iv2tov4(simTypeWater(orig, uv, cellW, cellH), 0.0f, 0.0f);
    } else {
        return v4zero();
    }
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
            if (ftoi(cell.x) == TYPE_EMPTY) {
                continue;
            }
            const vec4 delta = sampler(deltas, coords);
            if (delta.x == itof(-x) && delta.y == itof(-y)) {
                return cell;
            }
        }
    }
    return result;
}

public
vec4 sandDraw(const sampler2D orig, const vec2 uv, const ivec2 wh) {
    const float cellW = 1.0f / itof(wh.x);
    const float cellH = 1.0f / itof(wh.y);

    vec3 result = v3zero();

    for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
            const vec2 coords = nearbyCellCoords(uv, cellW, cellH, x, y);
            const vec4 cell = sampler(orig, coords);

            const int type = ftoi(cell.x);
            if (type == TYPE_SAND) {
                result = addv3(result, v3yellow());
            } if (type == TYPE_WATER) {
                result = addv3(result, v3blue());
            } else {
                result = addv3(result, mulv3f(v3cyan(), coords.y));
            }
        }
    }

    result = divv3f(result, 9.0f);
    return v3tov4(result, 1.0f);
}

