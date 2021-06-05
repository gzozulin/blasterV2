cmake_minimum_required(VERSION 3.19)
project(shaderlang C)

set(CMAKE_C_STANDARD 11)

# add_compile_options(-Wall -Wextra -pedantic)
# add_compile_options(-Wall -Wextra -pedantic -Werror)

add_executable(shaderlang main.c)