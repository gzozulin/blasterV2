package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse
import java.io.File

private val FILE_RECIPE = File("/home/greg/blaster/shadered/recipe")
private val PATTERN_WHITESPACE = "\\s+".toPattern()

private val window = GlWindow()

private val rect = glMeshCreateRect()
private var shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), constv4(vec4(vec3().azure(), 1f)))
private var lastModified = FILE_RECIPE.lastModified()

private fun <T> edParseReciepe(recipe: String): Expression<T> {
    val heap = mutableMapOf<String, Expression<*>>()
    val lines = recipe.lines().filter { it.isNotBlank() }.filter { !it.startsWith("//") }
    for (line in lines) {
        val (label, expression) = edParseLine(line, heap)
        heap[label] = expression
    }
    @Suppress("UNCHECKED_CAST")
    return heap["out"]!! as Expression<T>
}

private fun <T> edParseParam(param: String, heap: Map<String, Expression<*>>): Expression<T> {
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

    val expression = when (reference) {
        "v3"            -> v3(edParseParam(split.removeFirst(), heap), edParseParam(split.removeFirst(), heap), edParseParam(split.removeFirst(), heap))
        "addv3"         -> addv3(edParseParam(split.removeFirst(), heap), edParseParam(split.removeFirst(), heap))
        "subv3"         -> subv3(edParseParam(split.removeFirst(), heap), edParseParam(split.removeFirst(), heap))
        "mulv3"         -> mulv3(edParseParam(split.removeFirst(), heap), edParseParam(split.removeFirst(), heap))
        "divv3"         -> divv3(edParseParam(split.removeFirst(), heap), edParseParam(split.removeFirst(), heap))
        "v3chartreuse"  -> v3chartreuse()
        "v3aquamarine"  -> v3aquamarine()
        else -> error("Unknown operation! $reference")
    }
    return label to expression
}

private fun edReloadTechnique() {
    val previous = shadingFlat
    try {
        shadingFlat = ShadingFlat(
            constm4(mat4().orthoBox()),
            v3tov4(edParseReciepe(FILE_RECIPE.readText()), constf(1f)))
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
    while (!glWindowShouldClose(window)) {
        edReloadTechnique()
        window.isLooping = true
        glShadingFlatUse(shadingFlat) {
            glMeshUse(rect) {
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