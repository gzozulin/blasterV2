@file:Suppress("UNCHECKED_CAST")

package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse
import java.io.File

private val window = GlWindow()

private val rect = glMeshCreateRect()

private class EdReloadRequest : Exception()

private val input = mapOf(
    "time"              to timef(),
    "ortho"             to constm4(mat4().orthoBox()),
    "aspect"            to uniff(window.width.toFloat()/ window.height.toFloat()),
)

private data class EdRecipe(val file: File, val onReload: (recipe: EdRecipe, callback: Callback) -> Unit) {
    constructor(filename: String, onReload: (recipe: EdRecipe, callback: Callback) -> Unit) : this(File(filename), onReload)

    internal var lastModified = file.lastModified()
    internal var isError = false
}

private fun edRecipeUse(recipe: EdRecipe, callback: Callback) {
    recipe.onReload.invoke(recipe, callback)
}

private var shadingFlat = ShadingFlat()
private val recipe = EdRecipe("/home/greg/blaster/assets/recipes/colors") { recipe, callback ->
    while (true) {
        try {
            if (!recipe.isError) {
                val heap = edParseRecipe(recipe.file.readText(), input)
                shadingFlat = ShadingFlat(heap["matrix"] as Expression<mat4>, heap["color"] as Expression<col4>)
            }
            glShadingFlatUse(shadingFlat, callback)
        } catch (reload: EdReloadRequest) {
            println("Recipe reloaded:  ${recipe.file}")
        } catch (program: GlProgramException) {
            recipe.isError = true
            println("Error reloading shader: ${program.message}")
        } catch (parsing: EdParsingException) {
            recipe.isError = true
            println("Error parsing recipe: ${parsing.message}")
        }
    }
}

private fun edRecipeCheck(recipe: EdRecipe) {
    if (recipe.lastModified != recipe.file.lastModified()) {
        recipe.lastModified = recipe.file.lastModified()
        recipe.isError = false
        throw EdReloadRequest()
    }
}

fun main() = window.create {
    glMeshUse(rect) {
        edRecipeUse(recipe) {
            window.show {
                edRecipeCheck(recipe)
                glShadingFlatDraw(shadingFlat) {
                    glShadingFlatInstance(shadingFlat, rect)
                }
            }
        }
    }
}