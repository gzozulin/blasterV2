//
// Created by greg on 2021-08-02.
//

#include "lang.h"

custom
vec4 sampler(const sampler2D sampler, const vec2 texCoords) {
    return v4zero();
}

custom
vec4 texel(const samplerBuffer sampler, const int index) {
    return v4zero();
}

custom
vec4 samplerq(const samplerCube sampler, const vec3 texCoords) {
    return v4zero();
}
