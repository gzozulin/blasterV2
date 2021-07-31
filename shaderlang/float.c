//
// Created by greg on 2021-07-30.
//

#include "lang.h"

custom
float itof(const int i) {
    return (float) i;
}

custom
int ftoi(const float f) {
    return (int) f;
}

custom
float dtof(const double d) {
    return (float) d;
}

public
float addf(const float left, const float right) {
    return left + right;
}

public
float subf(const float left, const float right) {
    return left - right;
}

public
float mulf(const float left, const float right) {
    return left * right;
}

public
float divf(const float left, const float right) {
    return left / right;
}
