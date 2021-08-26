//
// Created by greg on 2021-08-10.
//

#include "lang.h"

public
float sdXZPlane(vec3 p) {
    return p.y;
}

public
float sdSphere(vec3 p, float r) {
    return lenv3(p) - r;
}

public
float sdBox(vec3 p, vec3 b) {
    vec3 q = subv3(absv3(p), b);
    return lenv3(maxv3(q, v3zero())) + minf(maxf(q.x, maxf(q.y, q.z)), 0.0f);
}

public
float sdCappedCylinder(vec3 p, vec3 a, vec3 b, float r) {
    vec3  ba = subv3(b, a);
    vec3  pa = subv3(p, a);
    float baba = dotv3(ba, ba);
    float paba = dotv3(pa, ba);
    float x = lenv3(subv3(mulv3f(pa, baba), mulv3f(ba, paba))) - r*baba;
    float y = absf(paba - baba * 0.5f) - baba * 0.5f;
    float x2 = x*x;
    float y2 = y*y*baba;
    float d = (maxf(x,y)<0.0f)?-minf(x2,y2):(((x>0.0f)?x2:0.0f)+((y>0.0f)?y2:0.0f));
    return signf(d) * sqrtf(absf(d)) / baba;
}

public
float sdSimplifiedCyl(vec3 p, float cylLen, float cylRad) {
    return sdCappedCylinder(p, v3(0, 0, cylLen / 2.0f), v3(0, 0, -cylLen / 2.0f), cylRad);
}

public
float sdCone(vec3 p, vec2 c, float h) {
    vec2 q = mulv2f(v2(c.x / c.y, -1.0f), h);
    vec2 w = v2(lenv2(xzv3(p)), p.y);
    vec2 a = subv2(w, mulv2f(q, clampf(dotv2(w,q)/dotv2(q,q), 0.0f, 1.0f)));
    vec2 b = subv2(w, mulv2(q, v2(clampf(w.x/q.x, 0.0f, 1.0f), 1.0f)));
    float k = signf(q.y);
    float d = minf(dotv2(a, a), dotv2(b, b));
    float s = maxf(k*(w.x*q.y-w.y*q.x), k*(w.y-q.y));
    return sqrtf(d) * signf(s);
}

public
float sdTriPrism(vec3 p, vec2 h) {
    vec3 q = absv3(p);
    return maxf(q.z-h.y,maxf(q.x*0.866025f+p.y*0.5f,-p.y)-h.x*0.5f);
}

public
float opUnion(float d1, float d2) {
    return minf(d1, d2);
}

public
float opSubtraction(float d1, float d2) {
    return maxf(-d1, d2);
}

public
float opIntersection(float d1, float d2) {
    return maxf(d1, d2);
}
