cmake_minimum_required(VERSION 3.19)
project(shadergen C)

set(CMAKE_C_STANDARD 11)

# add_compile_options(-Wall -Wextra -pedantic)
add_compile_options(-Wall -Wextra -pedantic)

include_directories(cglm/include)

add_executable(shadergen main.c shaderlang.h math.c vec2.c vec3.c vec4.c ivec2.c mat3.c mat4.c float.c raytracer.c shading.c random.c)
target_link_libraries(shadergen m)