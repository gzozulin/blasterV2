#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#pragma ide diagnostic ignored "readability-non-const-parameter"

#include <assert.h>

#define public

// ------------------- TYPES -------------------

struct vec2 {
    float x;
    float y;
};

struct ivec2 {
    int x;
    int y;
};

struct vec3 {
    float x;
    float y;
    float z;
};

struct vec4 {
    float x;
    float y;
    float z;
    float w;
};

struct mat4 {
    float value;
};

// todo: export
struct Light {
    struct vec3 vector;
    struct vec3 color;
    float attenConstant;
    float attenLinear;
    float attenQuadratic;
};

// todo: export
struct PhongMaterial {
    struct vec3 ambient;
    struct vec3 diffuse;
    struct vec3 specular;
    float shine;
    float transparency;
};

// ------------------- CTORS -------------------

struct vec2 v2(float x, float y) {
    return (struct vec2) {x, y};
}

struct ivec2 iv2(int x, int y) {
    return (struct ivec2) {x, y};
}

struct vec3 v3(float x, float y, float z) {
    return (struct vec3) {x, y, z};
}

// ------------------- PRIVATE -------------------

float dot(struct vec3 left, struct vec3 right) {
    return left.x * right.x + left.y * right.y + left.z * right.z;
}

/*vec3 cross(vec3 left, vec3 right) {
    // = vec3(a[1] * b[2] - a[2] * b[1],
    // a[2] * b[0] - a[0] * b[2],
    // a[0] * b[1] - a[1] * b[0])
}

vec3 v3muli(vec3 left, int right) {

}*/

/*struct vec4 transform(struct mat4 m, struct vec4 vec) {

}*/

float itof(int i) {
    return (float)i;
}

float ftoi(float f) {
    return (int)f;
}

// ------------------- PUBLIC ---------------

public struct vec2 tile(struct vec2 texCoord, struct ivec2 uv, struct ivec2 cnt) {
    struct vec2 result = v2(0.0f, 0.0f);
    float tileSideX = 1.0f / itof(cnt.x);
    float tileStartX = itof(uv.x) * tileSideX;
    result.x = tileStartX + texCoord.x * tileSideX;
    float tileSideY = 1.0f / itof(cnt.y);
    float tileStartY = itof(uv.y) * tileSideY;
    result.y = tileStartY + texCoord.y * tileSideY;
    return result;
}

public float luminosity(float distance, struct Light light) {
    return 1.0f / (light.attenConstant + light.attenLinear * distance + light.attenQuadratic * distance * distance);
}

// ------------------- MAIN ---------------

int main() {
    {
        assert(dot(v3(1, 0, 0), v3(0, 1, 0)) == 0.0);
        assert(dot(v3(1, 0, 0), v3(1, 0, 0)) == 1.0);
    }
    {
        assert(itof(123) == 123.0f);
        assert(ftoi(123.5f) == 123);
    }
    {
        struct vec2 t = tile(v2(1.0f, 1.0f), iv2(1, 1), iv2(2, 2));
        assert(t.x == 1.0f);
        assert(t.y == 1.0f);
    }
    return 0;
}

#pragma clang diagnostic pop
