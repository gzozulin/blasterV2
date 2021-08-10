//
// Created by greg on 2021-08-05.
//

#include "lang.h"

public
const int MAX_STEPS = 100;

public
const float MAX_DIST = 100.0f;

public
const float SURF_DIST = 0.01f;

public
const int RAYMARCH_AA = 3;

protected
float sceneDist(vec3 p) {
    const vec3 sphere = v3(0, 1, -3);
    float sphereDist = sdSphere(subv3(p, sphere), 1.0f);

    const vec3 cylStart = v3(1, 2, 0);
    const vec3 cylStop = v3(-10, 2, 0);

    float cylDist = sdCappedCylinder(p, cylStart, cylStop, 1.0f);

    const vec3 box = v3(5, 1, 0);
    const float boxDist = sdBox(subv3(p, box), v3(1, 3, 4));

    const vec3 cone = v3(-4, 4, 1);
    float coneDist = sdCone(subv3(p, cone), v2(5, 5), 3.0f);

    float planeDist = sdXZPlane(p);

    float d = opUnion(opUnion(opUnion(opUnion(sphereDist, planeDist), cylDist), boxDist), coneDist);
    return d;
}

protected
float rayMarch(vec3 ro, vec3 rd) {
    float dO = 0.0f;

    for(int i=0; i<MAX_STEPS; i++) {
        vec3 p = addv3(ro, mulv3f(rd, dO));
        float dS = sceneDist(p);
        dO += dS;
        if(dO>MAX_DIST || dS<SURF_DIST) break;
    }

    return dO;
}

protected
vec3 getNormal(vec3 p) {
    float d = sceneDist(p);

    vec3 n = subv3(ftov3(d), v3(
            sceneDist(subv3(p, v3(0.01f, 0.0f, 0.0f))),
            sceneDist(subv3(p, v3(0.0f, 0.01f, 0.0f))),
            sceneDist(subv3(p, v3(0.0f, 0.0f, 0.01f)))));

    return normv3(n);
}

protected
float getLight(vec3 p) {
    vec3 lightPos = v3(0, 5, 6);

    vec3 l = normv3(subv3(lightPos, p));
    vec3 n = getNormal(p);

    float dif = clampf(dotv3(n, l), 0.0f, 1.0f);
    float d = rayMarch(addv3(p, mulv3f(n, SURF_DIST * 2.0f)), l);
    if(d<lenv3(subv3(lightPos, p))) dif *= 0.1f;

    return dif;
}

public
vec4 raymarcher(const vec3 eye, const vec3 center, vec2 uv, float fovy, float aspect, ivec2 wh) {
    Camera camera = cameraLookAt(eye, center, v3up(), fovy, aspect, 0.0f, 1.0f);

    const float DU = 1.0f / itof(wh.x);
    const float DV = 1.0f / itof(wh.y);

    vec3 col = v3zero();
    for (int i = 0; i < RAYMARCH_AA; i++) {
        const float shift = rndv3(v2tov3(uv, itof(i)));
        const float du = remapf(0.0f, 1.0f, -DU/2, DU/2, shift);
        const float dv = remapf(0.0f, 1.0f, -DV/2, DV/2, shift);

        ray r = rayFromCamera(camera, uv.x + du, uv.y + dv);

        float d = rayMarch(r.origin, r.direction);
        vec3 p = addv3(r.origin, mulv3f(r.direction, d));

        float dif = getLight(p);
        col = addv3(col, ftov3(dif));
    }

    col = divv3f(col, itof(RAYMARCH_AA));
    col = powv3(col, ftov3(0.4545f));	// gamma correction

    return v3tov4(col, 1.0f);
}
