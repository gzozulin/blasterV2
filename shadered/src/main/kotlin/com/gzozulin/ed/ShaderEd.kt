package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse
import java.io.File

private val FILE_RECIPE = File("/home/greg/blaster/shadered/recipe")
private val PATTERN_WHITESPACE = "\\s+".toPattern()

private val window = GlWindow()
private val started = System.currentTimeMillis()

private val rect = glMeshCreateRect()
private var shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), constv4(vec4(vec3().azure(), 1f)))
private var lastModified = FILE_RECIPE.lastModified()

private val logoTexture = libTextureCreate("textures/logo.png")
    .copy(minFilter = backend.GL_LINEAR, magFilter = backend.GL_LINEAR)

private val input = mapOf(
    "time"      to uniff { (System.currentTimeMillis() - started).toFloat() / 1000f },
    "texture"   to sampler(unifs(logoTexture))
)

private fun <T> edParseRecipe(recipe: String, input: Map<String, Expression<*>>): Expression<T> {
    val heap = mutableMapOf<String, Expression<*>>()
    input.forEach { entry -> heap[entry.key] = entry.value }
    val lines = recipe.lines().filter { it.isNotBlank() }.filter { !it.startsWith("//") }
    for (line in lines) {
        val (label, expression) = edParseLine(line, heap)
        heap[label] = expression
    }
    @Suppress("UNCHECKED_CAST")
    return heap["out"]!! as Expression<T>
}

internal fun <T> edParseParam(param: String, heap: Map<String, Expression<*>>): Expression<T> {
    @Suppress("UNCHECKED_CAST")
    when {
        heap.containsKey(param) -> {
            return heap[param]!! as Expression<T>
        }
        param.contains(',') -> {
            val split = param.split(',').toMutableList()
            return when (split.size) {
                2 -> constv2(vec2(split.removeFirst().toFloat(), split.removeFirst().toFloat()))  as Expression<T>
                3 -> constv3(vec3(split.removeFirst().toFloat(), split.removeFirst().toFloat(), split.removeFirst().toFloat()))  as Expression<T>
                4 -> constv4(vec4(split.removeFirst().toFloat(), split.removeFirst().toFloat(), split.removeFirst().toFloat(), split.removeFirst().toFloat()))  as Expression<T>
                else -> error("Unknown type of vector! $param")
            }
        }
        else -> {
            return constf(param.toFloat()) as Expression<T>
        }
    }
}

private fun edParseLine(line: String, heap: Map<String, Expression<*>>): Pair<String, Expression<*>> {
    val separatorIndex = line.indexOf(':')
    val label = line.substring(0, separatorIndex)
    val body = line.substring(separatorIndex + 1, line.length)
    val split = body.split(PATTERN_WHITESPACE).filter { it.isNotBlank() }.toMutableList()

    val reference = split.removeFirst()
    if (heap.containsKey(reference)) {
        check(split.size == 0) { "Reference cannot has any parameters!" }
        return label to heap[reference]!!
    }

    val expression = edParseReference(reference, split, heap)
    return label to expression
}

private fun edReloadTechnique() {
    val previous = shadingFlat
    try {
        shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), edParseRecipe(FILE_RECIPE.readText(), input))
    } catch (th: Throwable) {
        println("Error reloading shader: ${th.message}")
        shadingFlat = previous
    }
}

private fun edCheckNeedReload() {
    if (lastModified != FILE_RECIPE.lastModified()) {
        window.isLooping = false
        lastModified = FILE_RECIPE.lastModified()
    }
}

fun main() = window.create {
    glMeshUse(rect) {
        glTextureUse(logoTexture) {
            while (!glWindowShouldClose(window)) {
                edReloadTechnique()
                window.isLooping = true
                glShadingFlatUse(shadingFlat) {
                    window.show {
                        glClear()
                        glShadingFlatDraw(shadingFlat) {
                            glShadingFlatInstance(shadingFlat, rect)
                        }
                        edCheckNeedReload()
                    }
                }
            }
        }
    }
}