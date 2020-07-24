#version 300 es

precision highp float;

const float PI = 3.14159265359;

const float lightConstantAtt    = 0.9;
const float lightLinearAtt      = 0.7;
const float lightQuadraticAtt   = 0.3;

in vec3 vWorldPos;
in vec2 vTexCoord;
in vec3 vNormal;

uniform vec3 uEye;

struct Light {
    vec3 vector;
    vec3 intensity;
};

uniform int uLightsPointCnt;
uniform int uLightsDirCnt;
uniform Light uLights[128];

uniform sampler2D uTexAlbedo;
uniform sampler2D uTexNormal;
uniform sampler2D uTexMetallic;
uniform sampler2D uTexRoughness;
uniform sampler2D uTexAo;

layout (location = 0) out vec4 oFragColor;

float attenuation(float distance) {
    return 1.0 / (lightConstantAtt + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
}

vec3 getNormalFromMap() {
    vec3 tangentNormal = texture(uTexNormal, vTexCoord).xyz * 2.0 - 1.0;

    vec3 Q1  = dFdx(vWorldPos);
    vec3 Q2  = dFdy(vWorldPos);
    vec2 st1 = dFdx(vTexCoord);
    vec2 st2 = dFdy(vTexCoord);

    vec3 N   = normalize(vNormal);
    vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
    vec3 B  = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);

    return normalize(TBN * tangentNormal);
}

float distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return nom / denom;
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return nom / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = geometrySchlickGGX(NdotV, roughness);
    float ggx1 = geometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

void main() {
    vec3 albedo     = pow(texture(uTexAlbedo, vTexCoord).rgb, vec3(2.2));
    float metallic  = texture(uTexMetallic, vTexCoord).r;
    float roughness = texture(uTexRoughness, vTexCoord).r;
    float ao        = texture(uTexAo, vTexCoord).r;

    vec3 N = getNormalFromMap();
    vec3 V = normalize(uEye - vWorldPos);

    // calculate reflectance at normal incidence; if dia-electric (like plastic) use F0
    // of 0.04 and if it's a metal, use the albedo color as F0 (metallic workflow)
    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    // reflectance equation
    vec3 Lo = vec3(0.0);
    for(int i = 0; i < uLightsPointCnt; ++i)
    {
        // calculate per-light radiance
        vec3 L = normalize(uLights[i].vector - vWorldPos);
        vec3 H = normalize(V + L);
        float distance = length(uLights[i].vector - vWorldPos);
        float attenuation = attenuation(distance);
        vec3 radiance = uLights[i].intensity * attenuation;

        // Cook-Torrance BRDF
        float NDF = distributionGGX(N, H, roughness);
        float G   = geometrySmith(N, V, L, roughness);
        vec3 F    = fresnelSchlick(max(dot(H, V), 0.0), F0);

        vec3 nominator    = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001; // 0.001 to prevent divide by zero.
        vec3 specular = nominator / denominator;

        // kS is equal to Fresnel
        vec3 kS = F;
        // for energy conservation, the diffuse and specular light can't
        // be above 1.0 (unless the surface emits light); to preserve this
        // relationship the diffuse component (kD) should equal 1.0 - kS.
        vec3 kD = vec3(1.0) - kS;
        // multiply kD by the inverse metalness such that only non-metals
        // have diffuse lighting, or a linear blend if partly metal (pure metals
        // have no diffuse light).
        kD *= (1.0 - metallic);

        // scale light by NdotL
        float NdotL = max(dot(N, L), 0.0);

        // add to outgoing radiance Lo
        Lo += (kD * albedo / PI + specular) * radiance * NdotL;  // note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
    }

    // ambient lighting (note that the next IBL tutorial will replace
    // this ambient lighting with environment lighting).
    vec3 ambient = vec3(0.1) * albedo * ao;

    vec3 color = ambient + Lo;

    // HDR tonemapping
    color = color / (color + vec3(1.0));
    // gamma correct
    color = pow(color, vec3(1.0/2.2));

    oFragColor = vec4(color, 1.0);
}