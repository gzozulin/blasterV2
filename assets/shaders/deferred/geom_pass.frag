#version 300 es

precision highp float;

in vec4 vFragPosition;
in vec2 vTexCoord;
in vec3 vNormal;

in vec3 vMatAmbient;
in vec3 vMatDiffuse;
in vec3 vMatSpecular;
in float vMatShine;
in float vMatTransp;

uniform sampler2D uTexDiffuse;

layout (location = 0) out vec4 oPosition;
layout (location = 1) out vec3 oNormal;
layout (location = 2) out vec4 oDiffuse;

layout (location = 3) out vec4 oMatAmbientShine;
layout (location = 4) out vec4 oMatDiffTransp;
layout (location = 5) out vec3 oMatSpecular;

void main()
{
    vec4 diffuse = texture(uTexDiffuse, vTexCoord);
    if (diffuse.a < 0.1) {
        discard;
    }
    oDiffuse = diffuse;
    oPosition = vFragPosition;
    oNormal = normalize(vNormal);
    oMatAmbientShine.a = vMatShine;
    oMatAmbientShine.rgb = vMatAmbient;
    oMatDiffTransp.a = vMatTransp;
    oMatDiffTransp.rgb = vMatDiffuse;
    oMatSpecular = vMatSpecular;
}