package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

// todo: all custom math GLSL, C
// todo: good random with sampler
// todo: like & subscribe demo screen

// todo: same recursive method for operation/param

// todo: split current C code into files
// todo: wrap into technique

private val FILE_RECIPE = File("/home/greg/blaster/shadered/recipe")
private val PATTERN_WHITESPACE = "\\s+".toPattern()

private val window = GlWindow(winWidth = 1400, winHeight = 800)

private enum class ShaderState { MODIFIED, RELOADED, ERROR, SUCCESS }
private var shaderState = ShaderState.MODIFIED

private val rect = glMeshCreateRect()
private var shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), constv4(vec4(vec3().azure(), 1f)))
private var lastModified = FILE_RECIPE.lastModified()

private val logoTexture = libTextureCreate("textures/logo.png")
private val foggyTexture = libTextureCreate("textures/foggy.jpg")

private val intermediateVal = AtomicInteger(0)

private val mouseVec = vec2()

private val input = mapOf(
    "time"      to timef(),
    "ortho"     to constm4(mat4().orthoBox()),
    "logo"      to sampler(unifs(logoTexture)),
    "foggy"     to sampler(unifs(foggyTexture)),
    "mouse"     to unifv2 { mouseVec }
)

private fun edParseRecipe(recipe: String, input: Map<String, Expression<*>>): Map<String, Expression<*>> {
    val heap = mutableMapOf<String, Expression<*>>()
    input.forEach { entry -> heap[entry.key] = entry.value }
    val lines = recipe.lines().filter { it.isNotBlank() }.filter { !it.startsWith("//") }
    for (line in lines) {
        val (label, expression) = edParseLine(line, heap)
        heap[label] = expression
    }
    @Suppress("UNCHECKED_CAST")
    return heap
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
        shaderState = ShaderState.MODIFIED
        lastModified = FILE_RECIPE.lastModified()
    }
}

private fun edReloadShader() {
    if (shaderState == ShaderState.MODIFIED) {
        val heap = edParseRecipe(FILE_RECIPE.readText(), input)
        @Suppress("UNCHECKED_CAST")
        shadingFlat = ShadingFlat(heap["matrix"] as Expression<mat4>, heap["color"] as Expression<col4>)
        shaderState = ShaderState.RELOADED
    }
}

private fun edShaderCompilSuccess() {
    if (shaderState == ShaderState.RELOADED) {
        println("Shader compiled successfully!")
        shaderState = ShaderState.SUCCESS
    }
}

private fun edShaderCompilFailure(th: Throwable, previous: ShadingFlat) {
    if (shaderState != ShaderState.ERROR) {
        println("Error reloading shader: ${th.message}")
        shaderState = ShaderState.ERROR
        shadingFlat = previous
    }
}

private fun edShowWindow() {
    while (!glWindowShouldClose(window)) {
        window.isLooping = true
        val previous = shadingFlat
        try {
            edReloadShader()
            glShadingFlatUse(shadingFlat) {
                window.show {
                    edCheckNeedReload()
                    edShowFrame()
                    edShaderCompilSuccess()
                }
            }
        } catch (th: Throwable) {
            edShaderCompilFailure(th, previous)
        }
    }
}

private fun edShowFrame() {
    glClear(col3().black())
    glTextureBind(logoTexture) {
        glTextureBind(foggyTexture) {
            glShadingFlatDraw(shadingFlat) {
                glShadingFlatInstance(shadingFlat, rect)
            }
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