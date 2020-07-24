#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexDiffuse;

layout (location = 0) out vec4 oFragColor;

void main() {
    // Retreiving the color directly from texture
    oFragColor = texture(uTexDiffuse, vTexCoord);

    // Discarding by alpha
    if (oFragColor.a < 0.1) {
        discard;
    }
}