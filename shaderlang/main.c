#include <assert.h>

#define generate
#define generate_frag
#define generate_vert

typedef unsigned int vec3;

generate int mul(int left, int right) {
    return left * right;
}

int main() {
    assert(mul(5, 5) == 25);
    return 0;
}
