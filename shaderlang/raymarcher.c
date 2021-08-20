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
typedef struct RaymarcherScene {
    float cylALen;
    float cylARad;
    mat4 cylAMat;

    vec2 coneBShape;
    float coneBHeight;
    mat4 coneBMat;

    float cylCLen;
    float cylCRad;
    mat4 cylCMat;

    vec3 boxDShape;
    mat4 boxDMat;

    vec3 boxEShape;
    mat4 boxEMat;

    vec2 prismFShape;
    mat4 prismFMat;

    float cylGLen;
    float cylGRad;
    mat4 cylGMat;
    
    vec3 boxHShape;
    mat4 boxHMat;
} RaymarcherScene;

protected
float simplidfiedCyl(vec3 p, float cylLen, float cylRad) {
    return sdCappedCylinder(p, v3(0, 0, cylLen / 2.0f), v3(0, 0, -cylLen / 2.0f), cylRad);
}

protected
float sceneDist(vec3 p, RaymarcherScene scene) {
    const vec3 cylAP = v4tov3(transformv4(v3tov4(p, 1.0f), scene.cylAMat));
    const float cylA = simplidfiedCyl(cylAP, scene.cylALen, scene.cylARad);

    const vec3 coneBP = v4tov3(transformv4(v3tov4(p, 1.0f), scene.coneBMat));
    const float coneB = sdCone(coneBP, scene.coneBShape, scene.coneBHeight);

    return opSubtraction(coneB, cylA);
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
float getLight(vec3 p, vec3 eye, RaymarcherScene scene) {
    vec3 l = normv3(subv3(eye, p));
    vec3 n = getNormal(p, scene);

    float dif = clampf(dotv3(n, l), 0.0f, 1.0f);
    float d = rayMarch(addv3(p, mulv3f(n, SURF_DIST * 2.0f)), l, scene);
    if(d < lenv3(subv3(eye, p))) dif *= 0.1f;

    return dif;
}

public
vec4 raymarcher(
        const vec3 eye, const vec3 center, vec2 uv, float fovy, float aspect, ivec2 wh,
        const int samplesAA,
        
        float cylALen, float cylARad, mat4 cylAMat,
        vec2 coneBShape, float coneBHeight, mat4 coneBMat,
        float cylCLen, float cylCRad, mat4 cylCMat,
        vec3 boxDShape, mat4 boxDMat,
        vec3 boxEShape, mat4 boxEMat,
        vec2 prismFShape, mat4 prismFMat,
        float cylGLen, float cylGRad, mat4 cylGMat,
        vec3 boxHShape, mat4 boxHMat) {

    const RaymarcherScene scene = { cylALen, cylARad, cylAMat,
                                    coneBShape, coneBHeight, coneBMat,
                                    cylCLen, cylCRad, cylCMat,
                                    boxDShape, boxDMat,
                                    boxEShape, boxEMat,
                                    prismFShape, prismFMat,
                                    cylGLen, cylGRad, cylGMat,
                                    boxHShape, boxHMat };

    Camera camera = cameraLookAt(eye, center, v3up(), fovy, aspect, 0.0f, 1.0f);

    vec3 col = v3zero();
    for( int m = 0; m < samplesAA; m++ ) {
        for( int n = 0; n < samplesAA; n++ ) {

            const vec2 duv = divv2(subv2f(divv2f(v2(itof(m), itof(n)), itof(samplesAA)), 0.5f), iv2tov2(wh));
            const ray r = rayFromCamera(camera, addv2(uv, duv));

            const float d = rayMarch(r.origin, r.direction, scene);
            const vec3 p = addv3(r.origin, mulv3f(r.direction, d));

            const vec3 addition = ftov3(getLight(p, eye, scene));
            col = addv3(col, sqrtv3(addition));
        }
    }

    col = divv3f(col, itof(samplesAA * samplesAA));
    return v3tov4(col, 1.0f);
}
