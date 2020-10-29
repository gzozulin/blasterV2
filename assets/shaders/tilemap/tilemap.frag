#version 400

precision highp float;
precision highp sampler2D;
precision highp usampler2D;

in vec2 vTexCoord;

const int DIFFUSE_CNT = 16;
const int SIDE_CNT = 8;

const float STEP = 1.0 / float(SIDE_CNT);

uniform usampler2D uTileMap;
uniform sampler2D uDiffuseArray[DIFFUSE_CNT];

layout (location = 0) out vec4 oFragColor;

void main() {
    uint tile = texture(uTileMap, vTexCoord).r;
    vec2 uv = vec2(mod(vTexCoord.x, STEP) / STEP, mod(vTexCoord.y, STEP) / STEP);
    oFragColor = texture(uDiffuseArray[tile], uv);
}