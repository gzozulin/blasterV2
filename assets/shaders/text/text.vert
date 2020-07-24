#version 300 es
precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform int uCharIndex;
uniform vec2 uCharStart;
uniform float uCharScale;

out vec2 vTexCoord;

// Shader works only with 256x256 textures, but I am not using others, at least for now
const float CHAR_STEP = 16.0 / 256.0;
const int CHARS_IN_ROW  = 16;

// This method will calculate the texture coordinates based on the index in the ASCII table and font texture parameters
void calculateTexCoords() {
    float charIndexX = float(uCharIndex % CHARS_IN_ROW);
    float charIndexY = float(CHARS_IN_ROW) - float(uCharIndex / CHARS_IN_ROW) - 1.0;
    float texCoordX = (charIndexX + aTexCoord.x) * CHAR_STEP;
    float texCoordY = (charIndexY + aTexCoord.y) * CHAR_STEP;
    vTexCoord = vec2(texCoordX, texCoordY);
}

// The position of the characters on the screen is calculated based on the font scaling
void calculatePosition() {
    float positionX = uCharStart.x + aPosition.x * uCharScale;
    float positionY = uCharStart.y + aPosition.y * uCharScale;
    gl_Position = vec4(positionX, positionY, 0.0, 1.0);
}

void main() {
    calculateTexCoords();
    calculatePosition();
}
