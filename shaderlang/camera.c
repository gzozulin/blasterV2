//
// Created by greg on 2021-08-10.
//

#include "lang.h"

protected
vec2 centerUV(vec2 uv, float aspect) {
    const vec2 center = subv2f(uv, 0.5f);
    return v2(center.x * aspect, center.y);
}

protected
Camera cameraLookAt(const vec3 eye, const vec3 center, const vec3 up,
                    const float fovy, const float aspect,
                    const float aperture, const float focusDist) {
    const float lensRadius = aperture / 2.0f;

    const float halfHeight = tanf(fovy / 2.0f);
    const float halfWidth = aspect * halfHeight;

    const vec3 w = normv3(subv3(eye, center));
    const vec3 u = normv3(crossv3(up, w));
    const vec3 v = crossv3(w, u);

    const vec3 hwu = mulv3f(u, halfWidth * focusDist);
    const vec3 hhv = mulv3f(v, halfHeight * focusDist);
    const vec3 wf = mulv3f(w, focusDist);
    const vec3 lowerLeft = subv3(subv3(subv3(eye, hwu), hhv), wf);

    const vec3 horizontal = mulv3f(u, halfWidth * focusDist * 2.0f);
    const vec3 vertical  = mulv3f(v, halfHeight * focusDist * 2.0f);

    const Camera result = { eye, lowerLeft, horizontal, vertical, w, u, v, lensRadius };
    return result;
}

protected
ray rayFromCamera(const Camera camera, const vec2 uv) {
    const vec3 horShift = mulv3f(camera.horizontal, uv.x);
    const vec3 verShift = mulv3f(camera.vertical, uv.y);

    vec3 origin;
    vec3 direction;

    if (camera.lensRadius > 0.0f) {
        const vec3 rd = mulv3f(randomInUnitDisk(), camera.lensRadius);
        const vec3 offset = addv3(mulv3f(camera.u, rd.x), mulv3f(camera.v, rd.y));
        origin = addv3(camera.origin, offset);
        direction = normv3(subv3(subv3(addv3(camera.lowerLeft, addv3(horShift, verShift)), camera.origin), offset));
    } else {
        origin = camera.origin;
        direction = normv3(subv3(addv3(camera.lowerLeft, addv3(horShift, verShift)), camera.origin));
    }

    const ray result = { origin, direction };
    return result;
}
