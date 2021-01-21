#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexDiffuse;
uniform vec3 uColor;

layout (location = 0) out vec4 oFragColor;

vec4 tex2d(sampler2D sampler, vec2 texCoord) { return texture(sampler, texCoord); }

void main() {
    // Retreiving the color directly from texture
    oFragColor = texture(uTexDiffuse, vTexCoord);

    // Discarding by alpha

    if (oFragColor.a < 0.1) {
        discard;
    }

    oFragColor.rgb *= uColor;
}