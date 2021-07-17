package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

// todo: v3 (getr tex) (getg tex) (getb tex)
// todo: some errors are fatal
// todo: multiple outs: for each tech param
// todo: good random with sampler
// todo: wrap into technique
// todo: like & subscribe demo screen

private val FILE_RECIPE = File("/home/greg/blaster/shadered/recipe")
private val PATTERN_WHITESPACE = "\\s+".toPattern()

private val window = GlWindow()

private val rect = glMeshCreateRect()
private var shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), constv4(vec4(vec3().azure(), 1f)))
private var lastModified = FILE_RECIPE.lastModified()

private val logoTexture = libTextureCreate("textures/logo.png")
    .copy(minFilter = backend.GL_LINEAR, magFilter = backend.GL_LINEAR)

private val intermediateVal = AtomicInteger(0)

private val input = mapOf(
    "time"      to timef(),
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

private fun edSubstituteBrackets(line: String, heap: MutableMap<String, Expression<*>>): String {
    if (line.contains('(')) {
        val beg = line.indexOfLast { it == '(' } + 1
        val end = line.indexOfFirst { it == ')' }
        val body = line.substring(beg, end)
        val name = "val${intermediateVal.getAndIncrement()}"
        val split = body.split(PATTERN_WHITESPACE).filter { it.isNotBlank() }.toMutableList()
        val reference = split.removeFirst()
        val expression = edParseReference(reference, split, heap)
        heap[name] = expression
        return edSubstituteBrackets(line.substring(0, beg - 1) + name + line.substring(end + 1, line.length), heap)
    } else {
        return line
    }
}

private fun edParseLine(line: String, heap: MutableMap<String, Expression<*>>): Pair<String, Expression<*>> {
    val resolvedLine = edSubstituteBrackets(line, heap)
    val separatorIndex = resolvedLine.indexOf(':')
    val label = resolvedLine.substring(0, separatorIndex)
    val body = resolvedLine.substring(separatorIndex + 1, resolvedLine.length)
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
                        glTextureBind(logoTexture) {
                            glShadingFlatDraw(shadingFlat) {
                                glShadingFlatInstance(shadingFlat, rect)
                            }
                        }
                        edCheckNeedReload()
                    }
                }
            }
        }
    }
}