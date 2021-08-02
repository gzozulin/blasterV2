//
// Created by greg on 2021-08-02.
//

#include "lang.h"

public
const float PI = 3.1415f;

public
const float BOUNCE_ERR = 0.001f;

public
const HitRecord NO_HIT = { -1, { 0, 0, 0 }, { 1, 0, 0 }, 0, 0 };

public
const ScatterResult NO_SCATTER = { { -1, -1, -1 }, { { 0, 0, 0 }, { 0, 0, 0 } } };

public
const RefractResult NO_REFRACT = { false, { 0, 0, 0 } };

const int uLightsPointCnt = 1;
const int uLightsDirCnt = 0;
const Light uLights[MAX_LIGHTS] = {
        { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 1.0f }, 1.0f, 1.0f, 1.0f }
};

const BvhNode uBvhNodes[MAX_BVH] = {
        { { { -100, -100,  -100 }, { 100, 100, 100 } }, HITABLE_BVH,     1, HITABLE_BVH,   2 },
        { { { -100, -100,  -100 }, {  0,   0,    0 } }, HITABLE_SPHERE,  0,   -1,         -1 },
        { { {    0,    0,     0 }, { 100, 100, 100 } }, HITABLE_SPHERE,  1,   -1,         -1 }
};

int bvhStack[MAX_BVH];
int bvhTop = 0;

const Sphere uSpheres[MAX_SPHERES] = {
        { { -50, -50, -50 }, 50, 0, 0 },
        { {  50,  50,  50 }, 50, 0, 1 }
};

const LambertianMaterial uLambertianMaterials[MAX_LAMBERTIANS] = {
        { { 1, 0, 0 } },
        { { 0, 1, 0 } }
};
const MetallicMaterial   uMetallicMaterials  [MAX_METALS] = { { { 0, 1, 0 } } };
const DielectricMaterial uDielectricMaterials[MAX_DIELECTRICS] = { { 2 } };
