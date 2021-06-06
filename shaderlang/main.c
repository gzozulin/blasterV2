#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#pragma ide diagnostic ignored "readability-non-const-parameter"

#include <assert.h>
#include <math.h>
#include <stdbool.h>
#include <float.h>

#define public
#define custom

#define sqrt sqrtf
#define pow powf

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

custom
struct vec2 v2(float x, float y) {
    return (struct vec2) {x, y};
}

custom
struct ivec2 iv2(int x, int y) {
    return (struct ivec2) {x, y};
}

custom
struct vec3 v3(float x, float y, float z) {
    return (struct vec3) {x, y, z};
}

public
struct vec3 v3val(float v) {
    return v3(v, v, v);
}

public
struct vec3 v3zero() {
    return v3val(0.0f);
}

// ------------------- BOOL -------------------

public
bool eqv3(const struct vec3 left, const struct vec3 right) {
    return left.x == right.x && left.y == right.y && left.z == right.z;
}

// ------------------- MATH -------------------

public
struct vec3 negv3(const struct vec3 v) {
    return v3(-v.x, -v.y, -v.z);
}

public
float dotv3(const struct vec3 left, const struct vec3 right) {
    return left.x * right.x + left.y * right.y + left.z * right.z;
}

public
struct vec3 crossv3(const struct vec3 left, const struct vec3 right) {
    return v3(
            left.y * right.z - left.z * right.y,
            left.z * right.x - left.x * right.z,
            left.x * right.y - left.y * right.x);
}

public
struct vec3 addv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x + right.x, left.y + right.y, left.z + right.z);
}

public
struct vec3 subv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x - right.x, left.y - right.y, left.z - right.z);
}

public
struct vec3 mulv3(const struct vec3 left, const struct vec3 right) {
    return v3(left.x * right.x, left.y * right.y, left.z * right.z);
}

public
struct vec3 mulv3f(const struct vec3 left, float right) {
    return v3(left.x * right, left.y * right, left.z * right);
}

public
struct vec3 divv3f(const struct vec3 left, float right) {
    return v3(left.x / right, left.y / right, left.z / right);
}

public
float lenv3(const struct vec3 v) {
    return sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
}

public
struct vec3 normv3(const struct vec3 v) {
    return divv3f(v, lenv3(v));
}

/*
custom
struct vec4 transform(struct mat4 m, struct vec4 vec) {

}*/

// ------------------- CASTS -------------------

custom
float itof(int i) {
    return (float) i;
}

custom
float ftoi(float f) {
    return (int) f;
}

// ------------------- PUBLIC ---------------

public
struct vec2 tile(const struct vec2 texCoord, const struct ivec2 uv, const struct ivec2 cnt) {
    struct vec2 result = v2(0.0f, 0.0f);
    float tileSideX = 1.0f / itof(cnt.x);
    float tileStartX = itof(uv.x) * tileSideX;
    result.x = tileStartX + texCoord.x * tileSideX;
    float tileSideY = 1.0f / itof(cnt.y);
    float tileStartY = itof(uv.y) * tileSideY;
    result.y = tileStartY + texCoord.y * tileSideY;
    return result;
}

public
float luminosity(float distance, const struct Light light) {
    return 1.0f / (light.attenConstant + light.attenLinear * distance + light.attenQuadratic * distance * distance);
}

public
struct vec3 diffuseContrib(const struct vec3 lightDir, const struct vec3 fragNormal, const struct PhongMaterial material) {
    float diffuseTerm = dotv3(fragNormal, lightDir);
    if (diffuseTerm > 0.0) {
        return mulv3f(material.diffuse, diffuseTerm);
    }
    return v3zero();
}

public
struct vec3 specularContrib(const struct vec3 viewDir, const struct vec3 lightDir, const struct vec3 fragNormal,
        const struct PhongMaterial material) {
    struct vec3 halfVector = normv3(addv3(viewDir, lightDir));
    float specularTerm = dotv3(halfVector, fragNormal);
    if (specularTerm > 0.0) {
        return mulv3f(material.specular, pow(specularTerm, material.shine));
    }
    return v3zero();
}

public
struct vec3 lightContrib(const struct vec3 viewDir, const struct vec3 lightDir, const struct vec3 fragNormal,
        float attenuation, const struct Light light, const struct PhongMaterial material) {
    struct vec3 lighting = v3(0.0f, 0.0f, 0.0f);
    lighting = addv3(lighting, diffuseContrib(lightDir, fragNormal, material));
    lighting = addv3(lighting, specularContrib(viewDir, lightDir, fragNormal, material));
    return mulv3(mulv3f(light.color, attenuation), lighting);
}

public
struct vec3 pointLightContrib(const struct vec3 viewDir, const struct vec3 fragPosition, struct vec3 fragNormal,
        const struct Light light, const struct PhongMaterial material) {
    struct vec3 direction = subv3(light.vector, fragPosition);
    float distance = lenv3(direction);
    float lum = luminosity(distance, light);
    struct vec3 lightDir = normv3(direction);
    return lightContrib(viewDir, lightDir, fragNormal, lum, light, material);
}

public
struct vec3 dirLightContrib(const struct vec3 viewDir, const struct vec3 fragNormal, const struct Light light, const struct PhongMaterial material) {
    struct vec3 lightDir = negv3(normv3(light.vector));
    return lightContrib(viewDir, lightDir, fragNormal, 1.0f, light, material);
}

// ------------------- MAIN ---------------

int main() {
    {
        assert(eqv3(v3(1, 1, 1), v3(1, 1, 1)));
        assert(!eqv3(v3(1, 1, 1), v3(1, 0, 1)));
    }
    {
        assert(eqv3(negv3(v3val(1)), v3val(-1)));
        assert(dotv3(v3(1, 0, 0), v3(0, 1, 0)) == 0.0);
        assert(dotv3(v3(1, 0, 0), v3(1, 0, 0)) == 1.0);
        assert(eqv3(crossv3(v3(1, 0, 0), v3(0, 1, 0)), v3(0, 0, 1)));
        assert(eqv3(addv3(v3(1, 1, 1), v3(2, 2, 2)), v3(3, 3, 3)));
        assert(eqv3(subv3(v3(1, 1, 1), v3(2, 2, 2)), v3(-1, -1, -1)));
        assert(eqv3(mulv3(v3(1, 1, 1), v3(2, 2, 2)), v3(2, 2, 2)));
        assert(eqv3(mulv3f(v3(1, 1, 1), 2.5f), v3(2.5f, 2.5f, 2.5f)));
        assert(eqv3(divv3f(v3(10, 10, 10), 5.0f), v3(2.0f, 2.0f, 2.0f)));
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
    {
        assert(lenv3(v3(0, 0, 0)) == 0);
        assert(lenv3(v3(1, 0, 0)) == 1);
        assert(lenv3(v3(0, 1, 0)) == 1);
        assert(lenv3(v3(0, 0, 1)) == 1);
    }
    {
        struct vec3 norm = normv3(v3(10, 10, 10));
        assert(lenv3(norm) - 1.0f < FLT_EPSILON);
    }
    return 0;
}

#pragma clang diagnostic pop
