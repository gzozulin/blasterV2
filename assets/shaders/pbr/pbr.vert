#version 300 es

precision highp float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 uModelM;
uniform mat4 uProjectionM;
uniform mat4 uViewM;

out vec3 vWorldPos;
out vec2 vTexCoord;
out vec3 vNormal;

void main() {
    vTexCoord = aTexCoord;
    vWorldPos = vec3(uModelM * vec4(aPosition, 1.0));
    vNormal = mat3(uModelM) * aNormal;
    gl_Position =  uProjectionM * uViewM * vec4(vWorldPos, 1.0);
}