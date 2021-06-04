#pragma clang diagnostic push
#pragma ide diagnostic ignored "readability-non-const-parameter"

#include <assert.h>
#include <malloc.h>

#define generate
#define generate_frag
#define generate_vert

typedef float* vec3;

vec3 v3(float x, float y, float z) {
    float *result = malloc(3 * sizeof(float));
    result[0] = x;
    result[1] = y;
    result[2] = z;
    return result;
}

generate float dotv3(vec3 left, vec3 right) {
    return left[0] * right[0] + left[1] * right[1] + left[2] * right[2];
}

vec3 crossv3(vec3 left, vec3 right) {

}

int main() {
    assert(dotv3(v3(1, 0, 0), v3(0, 1, 0)) == 0.0);
    assert(dotv3(v3(1, 0, 0), v3(1, 0, 0)) == 1.0);
    return 0;
}

#pragma clang diagnostic pop