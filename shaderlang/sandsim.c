//
// Created by greg on 2021-08-02.
//

#include "lang.h"

public
vec4 sandPhysics(sampler2D orig) {
    return sampler(orig, v2zero());
}

public
vec4 sandSolver(sampler2D orig, sampler2D deltas) {
    return addv4(sampler(orig, v2zero()), sampler(deltas, v2zero()));
}