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
const int RAYMARCH_AA = 1;

public
typedef struct RaymarcherScene {
    vec3 sphereO;
    float sphereR;

    vec3 cylStart;
    vec3 cylStop;
    float cylR;

    vec3 boxO;
    vec3 boxR;

    vec3 coneO;
    vec2 coneS;
    float coneH;

    vec3 prismO;
    vec2 prismS;
} RaymarcherScene;

protected
float sceneDist(vec3 p, RaymarcherScene scene) {
    const float sphereDist = sdSphere(subv3(p, scene.sphereO), scene.sphereR);
    const float cylDist = sdCappedCylinder(p, scene.cylStart, scene.cylStop, scene.cylR);
    const float boxDist = sdBox(subv3(p, scene.boxO), scene.boxR);
    const float coneDist = sdCone(subv3(p, scene.coneO), scene.coneS, scene.coneH);
    const float prismDist = sdTriPrism(subv3(p, scene.prismO), scene.prismS);
    const float planeDist = sdXZPlane(p);
    return opUnion(opUnion(opUnion(opUnion(opUnion(
            sphereDist, planeDist), cylDist), boxDist), coneDist), prismDist);
}

protected
float rayMarch(vec3 ro, vec3 rd, RaymarcherScene scene) {
    float dO = 0.0f;
    for(int i = 0; i < MAX_STEPS; i++) {
        vec3 p = addv3(ro, mulv3f(rd, dO));
        float dS = sceneDist(p, scene);
        dO += dS;
        if(dO > MAX_DIST || dS < SURF_DIST) break;
    }
    return dO;
}

protected
vec3 getNormal(vec3 p, RaymarcherScene scene) {
    float d = sceneDist(p, scene);
    vec3 n = subv3(ftov3(d), v3(
            sceneDist(subv3(p, v3(0.01f, 0.0f, 0.0f)), scene),
            sceneDist(subv3(p, v3(0.0f, 0.01f, 0.0f)), scene),
            sceneDist(subv3(p, v3(0.0f, 0.0f, 0.01f)), scene)));
    return normv3(n);
}

protected
float getLight(vec3 p, RaymarcherScene scene) {
    vec3 lightPos = v3(0, 5, 6);

    vec3 l = normv3(subv3(lightPos, p));
    vec3 n = getNormal(p, scene);

    float dif = clampf(dotv3(n, l), 0.0f, 1.0f);
    float d = rayMarch(addv3(p, mulv3f(n, SURF_DIST * 2.0f)), l, scene);
    if(d < lenv3(subv3(lightPos, p))) dif *= 0.1f;

    return dif;
}

public
vec4 raymarcher(
        const vec3 eye, const vec3 center, vec2 uv, float fovy, float aspect, ivec2 wh,
        vec3 sphereO, float sphereR,
        vec3 cylStart, vec3 cylStop, float cylR,
        vec3 boxO, vec3 boxR,
        vec3 coneO, vec2 coneS, float coneH,
        vec3 prismO, vec2 prismS ) {

    const RaymarcherScene scene = { sphereO, sphereR, cylStart, cylStop, cylR, boxO, boxR, coneO, coneS, coneH, prismO, prismS };
    Camera camera = cameraLookAt(eye, center, v3up(), fovy, aspect, 0.0f, 1.0f);

    vec3 col = v3zero();
    for( int m = 0; m < RAYMARCH_AA; m++ ) {
        for( int n = 0; n < RAYMARCH_AA; n++ ) {

            const vec2 duv = divv2(subv2f(divv2f(v2(itof(m), itof(n)), itof(RAYMARCH_AA)), 0.5f), iv2tov2(wh));
            const ray r = rayFromCamera(camera, addv2(uv, duv));

            const float d = rayMarch(r.origin, r.direction, scene);
            const vec3 p = addv3(r.origin, mulv3f(r.direction, d));

            vec3 addition = ftov3(getLight(p, scene));
            addition = sqrtv3(addition);
            col = addv3(col, addition);
        }
    }

    col = divv3f(col, itof(RAYMARCH_AA * RAYMARCH_AA));
    return v3tov4(col, 1.0f);
}
