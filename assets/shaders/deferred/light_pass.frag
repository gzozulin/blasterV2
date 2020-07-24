#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexPosition;
uniform sampler2D uTexNormal;
uniform sampler2D uTexDiffuse;

uniform sampler2D uTexMatAmbientShine;
uniform sampler2D uTexMatDiffTransp;
uniform sampler2D uTexMatSpecular;

uniform vec3 uEye;

struct Light {
    vec3 vector;
    vec3 intensity;
};

uniform int uLightsPointCnt;
uniform int uLightsDirCnt;
uniform Light uLights[128];

const float lightConstantAtt    = 0.9;
const float lightLinearAtt      = 0.7;
const float lightQuadraticAtt   = 0.3;

out vec4 oFragColor;

// todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
// https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/

float attenuation(float distance) {
    return 1.0 / (lightConstantAtt + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
}

vec3 lightContrib(vec3 viewDir, vec3 lightDir, vec3 fragNormal, vec3 lightIntensity, float attenuation,
        vec3 matDiffuse, vec3 matSpecular, float shine) {
    vec3 contribution = vec3(0.0);
    if (attenuation < 0.01){
        return contribution;
    }
    vec3 attenuatedLight = lightIntensity * attenuation;
    // diffuse
    float diffuseTerm = dot(fragNormal, lightDir);
    if (diffuseTerm > 0.0) {
        contribution += diffuseTerm * attenuatedLight * matDiffuse;
    }
    // specular
    vec3 reflectDir = reflect(-lightDir, fragNormal);
    float specularTerm = dot(viewDir, reflectDir);
    if (specularTerm > 0.0) {
        contribution += pow(specularTerm, shine) * attenuatedLight * matSpecular;
    }
    return contribution;
}

vec3 pointLightContrib(vec3 viewDir, vec3 fragPosition, vec3 fragNormal, vec3 lightVector, vec3 lightIntensity,
        vec3 matDiffuse, vec3 matSpecular, float shine) {
    vec3 direction = lightVector - fragPosition;
    float attenuation = attenuation(length(direction));
    vec3 lightDir = normalize(direction);
    return lightContrib(viewDir, lightDir, fragNormal, lightIntensity, attenuation, matDiffuse, matSpecular, shine);
}

vec3 dirLightContrib(vec3 viewDir, vec3 fragNormal, vec3 lightVector, vec3 lightIntensity,
        vec3 matDiffuse, vec3 matSpecular, float shine) {
    float attenuation = 1.0; // no attenuation
    vec3 lightDir = -normalize(lightVector);
    return lightContrib(viewDir, lightDir, fragNormal, lightIntensity, attenuation, matDiffuse, matSpecular, shine);
}

void main()
{
    vec4 positionLookup = texture(uTexPosition, vTexCoord);
    if (positionLookup.a != 1.0) {
        discard;
    }

    vec3 fragPosition = positionLookup.rgb;
    vec3 fragNormal = texture(uTexNormal, vTexCoord).rgb;
    vec3 fragDiffuse = texture(uTexDiffuse, vTexCoord).rgb;

    vec4 matAmbientShine = texture(uTexMatAmbientShine, vTexCoord);
    vec4 matDiffuseTransp = texture(uTexMatDiffTransp, vTexCoord);
    vec3 matSpecular = texture(uTexMatSpecular, vTexCoord).rgb;

    vec3 viewDir  = normalize(uEye - fragPosition);
    vec3 lighting  = matAmbientShine.rgb;

    for (int i = 0; i < uLightsPointCnt; ++i) {
        lighting += pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i].vector, uLights[i].intensity,
        matDiffuseTransp.rgb, matSpecular, matAmbientShine.a);
    }

    for (int i = 0; i < uLightsDirCnt; ++i) {
        lighting += dirLightContrib(viewDir, fragNormal, uLights[i].vector, uLights[i].intensity,
        matDiffuseTransp.rgb, matSpecular, matAmbientShine.a);
    }

    lighting *= fragDiffuse;
    // todo: oFragColor = vec4(lighting, matDiffuseTransp.a);
    oFragColor = vec4(lighting, 1.0);
}
