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
float sceneDist(vec3 p, RaymarcherScene scene) {
    const vec4 p4 = v3tov4(p, 1.0f);

    const vec3 cylAP = v4tov3(transformv4(p4, scene.cylAMat));
    const float cylA = sdSimplifiedCyl(cylAP, scene.cylALen, scene.cylARad);

    const vec3 coneBP = v4tov3(transformv4(p4, scene.coneBMat));
    const float coneB = sdCone(coneBP, scene.coneBShape, scene.coneBHeight);

    const vec3 cylCP = v4tov3(transformv4(p4, scene.cylCMat));
    const float cylC = sdSimplifiedCyl(cylCP, scene.cylCLen, scene.cylCRad);

    const vec3 boxDP = v4tov3(transformv4(p4, scene.boxDMat));
    const float boxD = sdBox(boxDP, scene.boxDShape);

    const vec3 boxEP = v4tov3(transformv4(p4, scene.boxEMat));
    const float boxE = sdBox(boxEP, scene.boxEShape);

    const vec3 prismFP = v4tov3(transformv4(p4, scene.prismFMat));
    const float prismF = sdTriPrism(prismFP, scene.prismFShape);

    const vec3 cylGP = v4tov3(transformv4(p4, scene.cylGMat));
    const float cylG = sdSimplifiedCyl(cylGP, scene.cylGLen, scene.cylGRad);

    const vec3 boxHP = v4tov3(transformv4(p4, scene.boxHMat));
    const float boxH = sdBox(boxHP, scene.boxHShape);

    const float AB = opSubtraction(coneB, cylA);
    const float AC = opUnion(AB, cylC);
    const float AD = opUnion(AC, boxD);
    const float AE = opUnion(AD, boxE);
    const float AF = opUnion(AE, prismF);
    const float AG = opUnion(AF, cylG);
    const float AH = opSubtraction(boxH, AG);

    return AH;
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
    for(int x = 0; x < samplesAA; x++) {
        for(int y = 0; y < samplesAA; y++) {
            const float du = (itof(x) / itof(samplesAA) - 0.5f) / itof(wh.x);
            const float dv = (itof(y) / itof(samplesAA) - 0.5f) / itof(wh.y);
            const ray r = rayFromCamera(camera, addv2(uv, v2(du, dv)));

            const float d = rayMarch(r.origin, r.direction, scene);
            const vec3 p = addv3(r.origin, mulv3f(r.direction, d));

            const vec3 addition = ftov3(getLight(p, eye, scene));
            col = addv3(col, sqrtv3(addition));
        }
    }

    col = divv3f(col, itof(samplesAA * samplesAA));
    return v3tov4(col, 1.0f);
}
