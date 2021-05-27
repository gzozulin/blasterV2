package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.techniques.ShadingFlat
import com.gzozulin.minigl.techniques.glShadingFlatDraw
import com.gzozulin.minigl.techniques.glShadingFlatInstance
import com.gzozulin.minigl.techniques.glShadingFlatUse

private val window = GlWindow(isFullscreen = true)
private val capturer = Capturer(window)

private val texture = libTextureCreate("textures/utah.jpg")
private val mesh = glMeshCreateRect(15f, 15f)

private var rotation = 0f
private val uniformMatrix = unifm4 {
    rotation += 0.01f
    mat4().identity().orthoBox(15f).rotate(rotation, vec3().front())
}
private val uniformColor = sampler(unifs(texture))

private val shadingFlat = ShadingFlat(uniformMatrix, uniformColor)

fun main() {
    window.create {
        capturer.capture {
            glShadingFlatUse(shadingFlat) {
                glTextureUse(texture) {
                    glMeshUse(mesh) {
                        window.show {
                            glClear(col3().rose())
                            glTextureBind(texture) {
                                glShadingFlatDraw(shadingFlat) {
                                    glShadingFlatInstance(shadingFlat, mesh)
                                }
                            }
                            capturer.addFrame()
                        }
                    }
                }
            }
        }
    }
}