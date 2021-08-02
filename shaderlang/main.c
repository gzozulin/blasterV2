#include "lang.h"

custom
float error() {
    assert(0 && "WTF?!");
}

void printv3(const vec3 v) {
    printf("v3 = {%f, %f, %f}\n", v.x, v.y, v.z);
}

// region ------------------- MAIN ---------------

int main() {
    assert(eqv3(v3(1, 1, 1), v3(1, 1, 1)));
    assert(!eqv3(v3(1, 1, 1), v3(1, 0, 1)));
    assert(eqv3(negv3(ftov3(1)), ftov3(-1)));
    assert(dotv3(v3(1, 0, 0), v3(0, 1, 0)) == 0.0);
    assert(dotv3(v3(1, 0, 0), v3(1, 0, 0)) == 1.0);
    assert(eqv3(crossv3(v3(1, 0, 0), v3(0, 1, 0)), v3(0, 0, 1)));
    assert(eqv3(addv3(v3(1, 1, 1), v3(2, 2, 2)), v3(3, 3, 3)));
    assert(eqv3(subv3(v3(1, 1, 1), v3(2, 2, 2)), v3(-1, -1, -1)));
    assert(eqv3(mulv3(v3(1, 1, 1), v3(2, 2, 2)), v3(2, 2, 2)));
    assert(eqv3(mulv3f(v3(1, 1, 1), 2.5f), v3(2.5f, 2.5f, 2.5f)));
    assert(eqv3(powv3(ftov3(2.0f), ftov3(2.0f)), ftov3(4.0f)));
    assert(eqv3(divv3f(v3(10, 10, 10), 5.0f), v3(2.0f, 2.0f, 2.0f)));
    assert(eqv3(divv3(ftov3(4.0f), ftov3(2.0f)), ftov3(2.0f)));
    assert(eqv3(mixv3(ftov3(1.0f), ftov3(1.0f), 0.5f), ftov3(1.0f)));
    assert(eqv4(addv4(v4(1, 1, 1, 1), v4(2, 2, 2, 2)), v4(3, 3, 3, 3)));
    assert(eqv4(subv4(v4(1, 1, 1, 1), v4(2, 2, 2, 2)), v4(-1, -1, -1, -1)));
    assert(eqv4(mulv4(v4(1, 1, 1, 1), v4(2, 2, 2, 2)), v4(2, 2, 2, 2)));
    assert(eqv4(mulv4f(v4(1, 1, 1, 1), 2.5f), v4(2.5f, 2.5f, 2.5f, 2.5f)));
    assert(eqv4(divv4(ftov4(4.0f), ftov4(2.0f)), ftov4(2.0f)));
    assert(eqv4(divv4f(v4(10, 10, 10, 10), 5.0f), v4(2.0f, 2.0f, 2.0f, 2.0f)));
    assert(itof(123) == 123.0f);
    assert(ftoi(123.5f) == 123);
    assert(eqv2(tile(v2(1.0f, 1.0f), iv2(1, 1), iv2(2, 2)), ftov2(1.0f)));
    assert(lenv3(v3(0, 0, 0)) == 0);
    assert(lenv3(v3(1, 0, 0)) == 1);
    assert(lenv3(v3(0, 1, 0)) == 1);
    assert(lenv3(v3(0, 0, 1)) == 1);
    assert(lenv3(normv3(v3(10, 10, 10))) - 1.0f < FLT_EPSILON);
    assert(eqv3(lerpv3(v3zero(), v3one(), 0.5f), ftov3(0.5f)));
    assert(eqv3(rayPoint(rayBack(), 10.0f), v3(0, 0, -10)));
    raytracer();
    return 0;
}

// endregion ------------------- MAIN ---------------
