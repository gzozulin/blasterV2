package com.gzozulin.examples

import com.gzozulin.ed.EdRecipe
import com.gzozulin.ed.edRecipeCheck
import com.gzozulin.ed.edRecipeUse
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.ControllerScenic
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow(isFullscreen = false)
private val capturer = Capturer(window)

private val controller = ControllerScenic(
    positions = listOf(
        vec3(-4f, 6f, -4f),
        vec3( 4f, 6f, -4f),
        vec3( 4f, 6f,  4f),
        vec3(-4f, 6f,  4f),
    ),
    points = listOf(vec3(0f, 0f, 0f)))

private val unifPos = unifv3()
private val unifCenter = unifv3()

private var shadingFlat = ShadingFlat()
private val recipe = EdRecipe("/home/greg/blaster/assets/recipes/raymarcher", emptyMap()) {
        isReloaded, heap, callback ->
    if (isReloaded) {
        @Suppress("UNCHECKED_CAST")
        shadingFlat = ShadingFlat(
            color = raymarcher(
                unifPos, unifCenter, namedTexCoordsV2(), fovyf(), aspectf(window), whf(window),
                heap["samplesAA"] as Expression<Int>,
                heap["sphereO"]  as Expression<vec3>, heap["sphereR"] as Expression<Float>,
                heap["cylStart"] as Expression<vec3>, heap["cylStop"] as Expression<vec3>, heap["cylR"] as Expression<Float>,
                heap["boxO"]     as Expression<vec3>, heap["boxR"]    as Expression<vec3>,
                heap["coneO"]    as Expression<vec3>, heap["coneS"]   as Expression<vec2>, heap["coneH"] as Expression<Float>,
                heap["prismO"]   as Expression<vec3>, heap["prismH"]  as Expression<vec2>
            )
        )
    }
    glShadingFlatUse(shadingFlat, callback)
}

private val rect = glMeshCreateRect()

fun main() = window.create {
    glMeshUse(rect) {
        edRecipeUse(window, recipe) {
            //capturer.capture {
                window.show {
                    edRecipeCheck(recipe)
                    controller.apply { position, direction ->
                        unifPos.value = position
                        unifCenter.value = vec3(position).add(direction)
                    }
                    glShadingFlatDraw(shadingFlat) {
                        glShadingFlatInstance(shadingFlat, rect)
                    }
                    //capturer.addFrame()
                }
            //}
        }
    }
}