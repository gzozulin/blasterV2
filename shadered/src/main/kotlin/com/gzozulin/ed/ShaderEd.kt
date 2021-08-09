@file:Suppress("UNCHECKED_CAST")

package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class EdParsingException(msg: String) : Exception(msg)

private class EdReloadRequest : Exception()

data class EdRecipe(val file: File, val input: Heap,
                            val onReload: (isReloaded: Boolean, heap: Heap, callback: Callback) -> Unit) {

    constructor(filename: String, input: Heap,
                onReload: (isReloaded: Boolean, heap: Heap, callback: Callback) -> Unit)
            : this(File(filename), input, onReload)

    internal var lastModified = file.lastModified()
}

fun edParseRecipe(recipe: String, input: Map<String, Expression<*>>): Map<String, Expression<*>> {
    val heap = mutableMapOf<String, Expression<*>>()
    input.forEach { entry -> heap[entry.key] = entry.value }
    val lines = recipe.lines()
    lines.forEachIndexed { index, line ->
        if (line.isNotBlank() && !line.startsWith("//")) {
            val (label, expression) = edParseLine(index + 1, line, heap)
            heap[label] = expression
        }
    }
    return heap
}

private fun edParseLine(lineNo: Int, line: String,
                        heap: MutableMap<String, Expression<*>>): Pair<String, Expression<*>> {
    val resolvedLine = edSubstituteBrackets(lineNo, line, heap)
    val separatorIndex = resolvedLine.indexOf(':')
    val label = resolvedLine.substring(0, separatorIndex)
    val body = resolvedLine.substring(separatorIndex + 1, resolvedLine.length).trim()
    return label to edParseExpression<Any>(lineNo, body, heap)
}

private val intermediateVal = AtomicInteger(0)
private fun edSubstituteBrackets(lineNo: Int, line: String, heap: MutableMap<String, Expression<*>>): String {
    if (line.contains('(')) {
        val beg = line.indexOfLast { it == '(' } + 1
        val end = beg + line.substring(beg, line.length).indexOfFirst { it == ')' }
        val body = line.substring(beg, end)
        val name = "val${intermediateVal.getAndIncrement()}"
        heap[name] = edParseExpression<Any>(lineNo, body, heap)
        return edSubstituteBrackets(lineNo,
            line.substring(0, beg - 1) + name + line.substring(end + 1, line.length), heap)
    } else {
        return line
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T> edParseExpression(lineNo: Int, expression: String,
                                   heap: MutableMap<String, Expression<*>>): Expression<T> {
    try {
        return edParseReference(expression, heap) as Expression<T>
    } catch (th: Throwable) {
        try {
            return edParseFloat(expression) as Expression<T>
        } catch (th: Throwable) {
            try {
                return edParseVector(expression) as Expression<T>
            } catch (th: Throwable) {
                try {
                    return edParseOperation(lineNo, expression, heap) as Expression<T>
                } catch (th: Throwable) {
                    throw EdParsingException("Cannot parse line #$lineNo")
                }
            }
        }
    }
}

private fun edParseReference(reference: String, heap: MutableMap<String, Expression<*>>): Expression<*> {
    return heap[reference]!!
}

private fun edParseVector(vector: String): Expression<*> {
    val split = vector.split(',').toMutableList()
    return when (split.size) {
        2 -> constv2(vec2(split.removeFirst().toFloat(), split.removeFirst().toFloat()))
        3 -> constv3(vec3(split.removeFirst().toFloat(), split.removeFirst().toFloat(), split.removeFirst().toFloat()))
        4 -> constv4(vec4(split.removeFirst().toFloat(), split.removeFirst().toFloat(), split.removeFirst().toFloat(),
            split.removeFirst().toFloat()))
        else -> error("Unknown type of vector! $vector")
    }
}

private fun edParseFloat(float: String): Expression<*> {
    return constf(float.toFloat())
}

fun edRecipeUse(window: GlWindow, recipe: EdRecipe, callback: Callback) {
    var isReloaded = true
    while (!glWindowShouldClose(window)) {
        try {
            val heap: Heap
            if (isReloaded) {
                heap = edParseRecipe(recipe.file.readText(), recipe.input)
            } else {
                heap = emptyMap()
            }
            recipe.onReload.invoke(isReloaded, heap, callback)
        } catch (reload: EdReloadRequest) {
            isReloaded = true
            println("Recipe reloaded:  ${recipe.file}")
        } catch (throwable: GlProgramException) {
            isReloaded = false
            println("Error while using recipe: ${throwable.message}")
        } catch (throwable: EdParsingException) {
            isReloaded = false
            println("Error while using recipe: ${throwable.message}")
        }
    }
}

fun edRecipeCheck(recipe: EdRecipe) {
    if (recipe.lastModified != recipe.file.lastModified()) {
        recipe.lastModified = recipe.file.lastModified()
        throw EdReloadRequest()
    }
}