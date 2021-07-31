//
// Created by greg on 2021-07-30.
//

#include "shaderlang.h"

const int uLightsPointCnt = 1;
const int uLightsDirCnt = 0;
const Light uLights[MAX_LIGHTS] = {
        { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 1.0f }, 1.0f, 1.0f, 1.0f }
};

// region ------------------- SHADING ---------------

// todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
// https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/

public
float luminosity(const float distance, const Light light) {
    return 1.0f / (light.attenConstant + light.attenLinear * distance + light.attenQuadratic * distance * distance);
}

public
vec3 diffuseContrib(const vec3 lightDir, const vec3 fragNormal, const PhongMaterial material) {
    float diffuseTerm = dotv3(fragNormal, lightDir);
    return diffuseTerm > 0.0f ? mulv3f(material.diffuse, diffuseTerm) : v3zero();
}

public vec3 halfVector(const vec3 left, const vec3 right) {
    return normv3(addv3(left, right));
}

public
vec3 specularContrib(const vec3 viewDir, const vec3 lightDir, const vec3 fragNormal,
                     const PhongMaterial material) {
    vec3 hv = halfVector(viewDir, lightDir);
    float specularTerm = dotv3(hv, fragNormal);
    return specularTerm > 0.0f ? mulv3f(material.specular, pow(specularTerm, material.shine)) : v3zero();
}

public
vec3 lightContrib(const vec3 viewDir, const vec3 lightDir, const vec3 fragNormal,
                  const float attenuation, const Light light, const PhongMaterial material) {
    vec3 lighting = v3zero();
    lighting = addv3(lighting, diffuseContrib(lightDir, fragNormal, material));
    lighting = addv3(lighting, specularContrib(viewDir, lightDir, fragNormal, material));
    return mulv3(mulv3f(light.color, attenuation), lighting);
}

public
vec3 pointLightContrib(const vec3 viewDir, const vec3 fragPosition, const vec3 fragNormal,
                       const Light light, const PhongMaterial material) {
    vec3 direction = subv3(light.vector, fragPosition);
    vec3 lightDir = normv3(direction);
    if (dotv3(lightDir, fragNormal) < 0.0f) {
        return v3zero();
    }
    float distance = lenv3(direction);
    float lum = luminosity(distance, light);
    return lightContrib(viewDir, lightDir, fragNormal, lum, light, material);
}

public
vec3 dirLightContrib(const vec3 viewDir, const vec3 fragNormal, const Light light, const PhongMaterial material) {
    vec3 lightDir = negv3(normv3(light.vector));
    return lightContrib(viewDir, lightDir, fragNormal, 1.0f, light, material);
}

public
vec4 shadingFlat(vec4 color) {
    return color;
}

public
vec4 shadingPhong(const vec3 fragPosition, const vec3 eye, const vec3 fragNormal, const vec3 fragAlbedo,
                  const PhongMaterial material) {
    vec3 viewDir = normv3(subv3(eye, fragPosition));
    vec3 color = material.ambient;
    for (int i = 0; i < uLightsPointCnt; ++i) {
        color = addv3(color, pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i], material));
    }
    for (int i = uLightsPointCnt; i < uLightsPointCnt + uLightsDirCnt; ++i) {
        color = addv3(color, dirLightContrib(viewDir, fragNormal, uLights[i], material));
    }
    color = mulv3(color, fragAlbedo);
    return v3tov4(color, material.transparency);
}

custom
vec3 getNormalFromMap(const vec3 normal, const vec3 worldPos, const vec2 texCoord, const vec3 vnormal) {

    const vec3 result = addv3(addv3(addv3(normal, worldPos), vnormal), v2tov3(texCoord, 1));
    assert(dotv3(result, worldPos) && "Some chicken shit happens here..");
    /*vec3 tangentNormal = fromMap * 2.0 - 1.0;
    vec3 Q1  = dFdx(vWorldPos);
    vec3 Q2  = dFdy(vWorldPos);
    vec2 st1 = dFdx(vTexCoord);
    vec2 st2 = dFdy(vTexCoord);
    vec3 N   = normalize(vNormal);
    vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
    vec3 B  = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);
    return normalize(TBN * tangentNormal);*/
    return result;
}

public
float distributionGGX(const vec3 N, const vec3 H, const float a) {
    float a2        = a * a;
    float NdotH     = max(dotv3(N, H), 0.0f);
    float NdotH2    = NdotH*NdotH;
    float nom       = a2;
    float denom     = (NdotH2 * (a2 - 1.0f) + 1.0f);
    denom           = PI * denom * denom;
    return nom / denom;
}

public
float geometrySchlickGGX(const float NdotV, const float roughness) {
    float r         = (roughness + 1.0f);
    float k         = (r*r) / 8.0f;
    float nom       = NdotV;
    float denom     = NdotV * (1.0f - k) + k;
    return nom / denom;
}

public
float geometrySmith(const vec3 N, const vec3 V, const vec3 L, const float roughness) {
    float NdotV     = max(dotv3(N, V), 0.0f);
    float NdotL     = max(dotv3(N, L), 0.0f);
    float ggx2      = geometrySchlickGGX(NdotV, roughness);
    float ggx1      = geometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

public
vec3 fresnelSchlick(const float cosTheta, const vec3 F0) {
    return addv3(F0, mulv3(subv3(ftov3(1.0f), F0), ftov3(pow(1.0f - cosTheta, 5.0f))));
}

public
vec4 shadingPbr(const vec3 eye, const vec3 worldPos, const vec3 albedo, const vec3 N,
                const float metallic, const float roughness, const float ao) {

    const vec3 alb = powv3(albedo, ftov3(2.2f));
    const vec3 V   = normv3(subv3(eye, worldPos));

    vec3 F0  = ftov3(0.04f);
    F0 = mixv3(F0, alb, metallic);

    vec3 Lo = v3zero();

    for(int i = 0; i < uLightsPointCnt; ++i) {
        const vec3 toLight = subv3(uLights[i].vector, worldPos);
        const vec3 L = normv3(toLight);
        const vec3 H = normv3(addv3(V, L));

        const float distance          = lenv3(toLight);
        const float lum               = luminosity(distance, uLights[i]);
        const vec3 radiance    = mulv3(uLights[i].color, ftov3(lum));

        const float NDF = distributionGGX(N, H, roughness);
        const float G   = geometrySmith(N, V, L, roughness);
        const vec3 F    = fresnelSchlick(max(dotv3(H, V), 0.0f), F0);

        const vec3 nominator = mulv3(F, ftov3(NDF * G));
        const float denominator = 4.0f * max(dotv3(N, V), 0.0f) * max(dotv3(N, L), 0.0f) + 0.001f;

        const vec3 specular = divv3f(nominator, denominator);

        vec3 kD = subv3(ftov3(1.0f), F);
        kD = mulv3(kD, ftov3(1.0f - metallic));
        const float NdotL = max(dotv3(N, L), 0.0f);
        Lo = addv3(Lo, mulv3(mulv3(addv3(divv3(mulv3(kD, alb), ftov3(PI)), specular), radiance), ftov3(NdotL)));
    }

    const vec3 ambient = mulv3(ftov3(0.1f * ao), alb);
    vec3 color = addv3(ambient, Lo);
    color = divv3(color, addv3(color, ftov3(1.0f)));
    color = powv3(color, ftov3(1.0f/2.2f));
    return v3tov4(color, 1.0f);
}

// endregion ------------------- SHADING ---------------
