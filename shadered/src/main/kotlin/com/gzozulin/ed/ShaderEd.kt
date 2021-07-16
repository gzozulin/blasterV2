package com.gzozulin.ed

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow()

private val rect = glMeshCreateRect()

private val colorRecipe = """
    color: v3chartreuse
    out: v3tov4 color 1f
""".trimIndent()
private lateinit var shadingFlat: ShadingFlat

private fun <T> edParseScript(recipe: String): Expression<T> {
    val heap = mutableMapOf<String, Expression<*>>()
    for (line in colorRecipe.lines()) {
        val (label, expression) = edParseLine(line, heap)
        heap[label] = expression
    }
    @Suppress("UNCHECKED_CAST")
    return heap["out"]!! as Expression<T>
}

private fun edParseLine(line: String, heap: Map<String, Expression<*>>): Pair<String, Expression<*>> {
    val label = line.substring(0, line.indexOf(':'))
    TODO()
}

private fun edReloadTechnique() {
    shadingFlat = ShadingFlat(constm4(mat4().orthoBox()), constv4(vec4(vec3().rand(), 1f)))
}

fun main() = window.create {
    window.keyCallback = { _, pressed ->
        if (pressed) {
            window.isLooping = false
        }
    }
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
                }
            }
        }
    }
}