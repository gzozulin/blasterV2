package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

// todo: multiple outs: for each tech param
// todo: good random with sampler
// todo: wrap into technique
// todo: like & subscribe demo screen
// todo: split current C code into files
// todo: named constants: uv coords and so on

private val FILE_RECIPE = File("/home/greg/blaster/shadered/recipe")
private val PATTERN_WHITESPACE = "\\s+".toPattern()

private val window = GlWindow(winWidth = 1280, winHeight = 720)

private var showNextError = true
private var showNextSuccess = true

private val rect = glMeshCreateRect()
private var shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), constv4(vec4(vec3().azure(), 1f)))
private var lastModified = FILE_RECIPE.lastModified()

private val logoTexture = libTextureCreate("textures/logo.png")
private val foggyTexture = libTextureCreate("textures/foggy.jpg")

private val intermediateVal = AtomicInteger(0)

private val mouseVec = vec2()

private val input = mapOf(
    "time"      to timef(),
    "logo"      to sampler(unifs(logoTexture)),
    "foggy"     to sampler(unifs(foggyTexture)),
    "mouse"     to unifv2 { mouseVec }
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
        val end = beg + line.substring(beg, line.length).indexOfFirst { it == ')' }
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

private fun edCheckNeedReload() {
    if (lastModified != FILE_RECIPE.lastModified()) {
        window.isLooping = false
        lastModified = FILE_RECIPE.lastModified()
    }
}

private fun edReportError(throwable: Throwable) {
    if (showNextError) {
        println("Error reloading shader: ${throwable.message}")
        showNextError = false
        showNextSuccess = true
    }
}

private fun edShaderIsGood() {
    if (showNextSuccess) {
        println("Shader compiled successfully!")
        showNextSuccess = false
        showNextError = true
    }
}

private fun edShowWindow() {
    while (!glWindowShouldClose(window)) {
        val previous = shadingFlat
        try {
            shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), edParseRecipe(FILE_RECIPE.readText(), input))
            window.isLooping = true
            glShadingFlatUse(shadingFlat) {
                window.show {
                    glClear()
                    glTextureBind(logoTexture) {
                        glTextureBind(foggyTexture) {
                            glShadingFlatDraw(shadingFlat) {
                                glShadingFlatInstance(shadingFlat, rect)
                            }
                        }
                    }
                    edShaderIsGood()
                    edCheckNeedReload()
                }
            }
        } catch (th: Throwable) {
            edReportError(th)
            shadingFlat = previous
        }
    }
}

fun main() = window.create {
    window.positionCallback = { mouseVec.set(it) }
    glMeshUse(rect) {
        glTextureUse(logoTexture) {
            glTextureUse(foggyTexture) {
                edShowWindow()
            }
        }
    }
}