package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.assets.libTextureCreatePbr
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.PointLight
import com.gzozulin.minigl.scene.WasdInput

typealias Filter = (screen: GlTexture) -> Expression<vec4>

data class TechniquePostProcessing(val width: Int, val height: Int, val filter: Filter) {
    constructor(window: GlWindow, filter: Filter): this(window.width, window.height, filter)

    internal val techniqueRtt = TechniqueRtt(width, height)

    private val matrix = constm4(mat4().orthoBox(1f))
    private val color = filter.invoke(techniqueRtt.color)
    internal val shadingFlat = ShadingFlat(matrix, color)

    internal val rect = glMeshCreateRect()
}

fun glPostProcessingUse(techniquePP: TechniquePostProcessing, callback: Callback) {
    glRttUse(techniquePP.techniqueRtt) {
        glShadingFlatUse(techniquePP.shadingFlat) {
            glMeshUse(techniquePP.rect) {
                callback.invoke()
            }
        }
    }
}

fun glPostProcessingDraw(techniquePP: TechniquePostProcessing, callback: Callback) {
    glRttDraw(techniquePP.techniqueRtt) {
        callback.invoke()
    }
    glShadingFlatDraw(techniquePP.shadingFlat) {
        glTextureBind(techniquePP.techniqueRtt.color) {
            glShadingFlatInstance(techniquePP.shadingFlat, techniquePP.rect)
        }
    }
}