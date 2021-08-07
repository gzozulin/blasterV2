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
const vec3 spheres[3] = { { 0, 1, 6 }, { 1, 1, 6 }, { -1, 1, 6 } };

protected
float getDist(vec3 p) {
    float sphereDist0 = lenv3(subv3(p, spheres[0])) - 1.0f;
    float sphereDist1 = lenv3(subv3(p, spheres[1])) - 1.0f;
    float sphereDist2 = lenv3(subv3(p, spheres[2])) - 1.0f;
    float planeDist = p.y;

    float d = minf(minf(minf(sphereDist0, sphereDist1), sphereDist2), planeDist);
    return d;
}

protected
float rayMarch(vec3 ro, vec3 rd) {
    float dO = 0.0f;

    for(int i=0; i<MAX_STEPS; i++) {
        vec3 p = addv3(ro, mulv3f(rd, dO));
        float dS = getDist(p);
        dO += dS;
        if(dO>MAX_DIST || dS<SURF_DIST) break;
    }

    return dO;
}

protected
vec3 getNormal(vec3 p) {
    float d = getDist(p);

    vec3 n = subv3(ftov3(d), v3(
            getDist(subv3(p, v3(0.01f, 0.0f, 0.0f))),
            getDist(subv3(p, v3(0.0f, 0.01f, 0.0f))),
            getDist(subv3(p, v3(0.0f, 0.0f, 0.01f)))));

    return normv3(n);
}

protected
float getLight(vec3 p, float time) {
    vec3 lightPos = v3(0, 5, 6);
    lightPos = v3(lightPos.x + sinf(time) * 2.0f, lightPos.y, lightPos.z + cosf(time) * 2.0f);

    vec3 l = normv3(subv3(lightPos, p));
    vec3 n = getNormal(p);

    float dif = clampf(dotv3(n, l), 0.0f, 1.0f);
    float d = rayMarch(addv3(p, mulv3f(n, SURF_DIST * 2.0f)), l);
    if(d<lenv3(subv3(lightPos, p))) dif *= 0.1f;

    return dif;
}

protected
vec2 centerUV(vec2 uv, float aspect) {
    const vec2 center = subv2f(uv, 0.5f);
    return v2(center.x * aspect, center.y);
}

public
vec4 raymarcher(vec2 uv, float time, float aspect) {
    uv = centerUV(uv, aspect);

    vec3 ro = v3(0, 1, 0);
    vec3 rd = normv3(v3(uv.x, uv.y, 1));

    float d = rayMarch(ro, rd);
    vec3 p = addv3(ro, mulv3f(rd, d));

    float dif = getLight(p, time);
    vec3 col = ftov3(dif);
    col = powv3(col, ftov3(0.4545f));	// gamma correction

    return v3tov4(col, 1.0f);
}
