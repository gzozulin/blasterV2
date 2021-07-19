cmake_minimum_required(VERSION 3.19)
project(shadergen C)

set(CMAKE_C_STANDARD 11)

# add_compile_options(-Wall -Wextra -pedantic)
add_compile_options(-Wall -Wextra -pedantic)

include_directories(cglm/include)

add_executable(shadergen main.c)
target_link_libraries(shadergen m)