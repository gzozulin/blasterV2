//
// Created by greg on 2021-07-30.
//

#include "shaderlang.h"

// region ------------------- IVEC2 -------------------

custom
ivec2 iv2(const int x, const int y) {
    return (ivec2) { x, y };
}

public
float getxiv2(const vec2 v) {
    return v.x;
}

public
float getyiv2(const vec2 v) {
    return v.y;
}

public
float getuiv2(const vec2 v) {
    return v.x;
}

public
float getviv2(const vec2 v) {
    return v.y;
}

public
vec2 tile(const vec2 texCoord, const ivec2 uv, const ivec2 cnt) {
    float tileSideX = 1.0f / itof(cnt.x);
    float tileStartX = itof(uv.x) * tileSideX;
    float tileSideY = 1.0f / itof(cnt.y);
    float tileStartY = itof(uv.y) * tileSideY;
    return v2(tileStartX + texCoord.x * tileSideX, tileStartY + texCoord.y * tileSideY);
}

// endregion ------------------- IVEC2 -------------------

