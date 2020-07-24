#version 300 es

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 uModelM;
uniform mat4 uViewM;
uniform mat4 uProjectionM;

uniform vec3 uMatAmbient;
uniform vec3 uMatDiffuse;
uniform vec3 uMatSpecular;
uniform float uMatShine;
uniform float uMatTransp;

out vec4 vFragPosition;
out vec2 vTexCoord;
out vec3 vNormal;

out vec3 vMatAmbient;
out vec3 vMatDiffuse;
out vec3 vMatSpecular;
out float vMatShine;
out float vMatTransp;

void main()
{
    vec4 worldPos = uModelM * vec4(aPosition, 1.0);
    vFragPosition = worldPos;

    vTexCoord = aTexCoord;

    mat3 normalMatrix = transpose(inverse(mat3(uModelM)));
    vNormal = normalMatrix * aNormal;

    gl_Position = uProjectionM * uViewM * worldPos;

    vMatAmbient = uMatAmbient;
    vMatDiffuse = uMatDiffuse;
    vMatSpecular = uMatSpecular;
    vMatShine = uMatShine;
    vMatTransp = uMatTransp;
}
