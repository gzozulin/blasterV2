//
// Created by greg on 2021-08-02.
//

#include "lang.h"

public
vec4 sandPhysics(vec4 orig) {
    return orig;
}

public
vec4 sandSolver(vec4 orig, vec4 deltas) {
    return addv4(orig, deltas);
}