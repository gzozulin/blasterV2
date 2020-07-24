#version 300 es

precision highp float;

in vec2 vTexCoord;
in float vBillbTransparency;

uniform sampler2D uTexDiffuse;
uniform float uTransparency;

layout (location = 0) out vec4 oFragColor;

void main() {
    oFragColor = texture(uTexDiffuse, vTexCoord);
    if (oFragColor.a < 0.1) {
        discard;
    }
    oFragColor.a *= vBillbTransparency;
}