#version 300 es

precision mediump float;

in vec2 vTexCoord;

uniform sampler2D uTexDiffuse;

uniform vec3 uColor;

layout (location = 0) out vec4 oFragColor;

void main() {
    vec4 diffuse = texture(uTexDiffuse, vTexCoord);
    if (diffuse.r < 0.1) {
        discard;
    }
    oFragColor.rgb = diffuse.rgb * uColor.rgb;
    oFragColor.a = 1.0;
}