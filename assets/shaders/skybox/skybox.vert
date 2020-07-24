#version 300 es
precision mediump float;

layout (location = 0) in vec3 aPosition;

uniform mat4 uProjectionM;
uniform mat4 uViewM;

out vec3 vTexCoord;

void main() {
    vTexCoord = aPosition;
    vec4 pos = uProjectionM * uViewM * vec4(aPosition, 1.0);

    // Setting the vertex z == w, "The depth of a fragment in the Z-buffer is computed as z/w. If z==w, then you get a depth of 1.0, or 100%." [explanation; https://www.gamedev.net/forums/topic/577973-skybox-and-depth-buffer/4683037/]
    gl_Position = pos.xyzz;
}