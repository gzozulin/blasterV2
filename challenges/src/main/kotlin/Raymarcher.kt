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

private var mouseLook = false
private val controller = ControllerFirstPerson(velocity = 0.5f, position = vec3(0f, 5f, 5f))
private val wasdInput = WasdInput(controller)

/*private val controller = ControllerScenic(
    positions = listOf(
        vec3(-4f, 6f, -4f),
        vec3( 4f, 6f, -4f),
        vec3( 4f, 6f,  4f),
        vec3(-4f, 6f,  4f),
    ),
    points = listOf(vec3(0f, 0f, 0f)))*/

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
                heap["cylALen"] as Expression<Float>,
                heap["cylARad"] as Expression<Float>,
                heap["cylAMat"] as Expression<mat4>,
                heap["coneBShape"] as Expression<vec2>,
                heap["coneBHeight"] as Expression<Float>,
                heap["coneBMat"] as Expression<mat4>,
                heap["cylCLen"] as Expression<Float>,
                heap["cylCRad"] as Expression<Float>,
                heap["cylCMat"] as Expression<mat4>,
                heap["boxDShape"] as Expression<vec3>,
                heap["boxDMat"] as Expression<mat4>,
                heap["boxEShape"] as Expression<vec3>,
                heap["boxEMat"] as Expression<mat4>,
                heap["prismFShape"] as Expression<vec2>,
                heap["prismFMat"] as Expression<mat4>,
                heap["cylGLen"] as Expression<Float>,
                heap["cylGRad"] as Expression<Float>,
                heap["cylGMat"] as Expression<mat4>,
                heap["boxHShape"] as Expression<vec3>,
                heap["boxHMat"] as Expression<mat4>
            )
        )
    }
    glShadingFlatUse(shadingFlat, callback)
}

private val rect = glMeshCreateRect()

fun main() = window.create {
    window.buttonCallback = { button, pressed ->
        if (button == MouseButton.LEFT) {
            mouseLook = pressed
        }
    }
    window.deltaCallback = { delta ->
        if (mouseLook) {
            wasdInput.onCursorDelta(delta)
        }
    }
    window.keyCallback = { key, pressed ->
        wasdInput.onKeyPressed(key, pressed)
    }
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