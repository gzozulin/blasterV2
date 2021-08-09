package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

typealias Heap = Map<String, Expression<*>>;

private val window = GlWindow()

private val rect = glMeshCreateRect()

private val params = mapOf(
    "time"              to timef(),
    "ortho"             to constm4(mat4().orthoBox()),
    "aspect"            to uniff(window.width.toFloat()/ window.height.toFloat()),
)

private var shadingFlat = ShadingFlat()
private val recipe = EdRecipe("/home/greg/blaster/assets/recipes/colors", params) { isReloaded, heap, callback ->
    if (isReloaded) {
        @Suppress("UNCHECKED_CAST")
        shadingFlat = ShadingFlat(heap["matrix"] as Expression<mat4>, heap["color"] as Expression<col4>)
    }
    glShadingFlatUse(shadingFlat, callback)
}

fun main() = window.create {
    glMeshUse(rect) {
        edRecipeUse(window, recipe) {
            window.show {
                edRecipeCheck(recipe)
                glShadingFlatDraw(shadingFlat) {
                    glShadingFlatInstance(shadingFlat, rect)
                }
            }
        }
    }
}