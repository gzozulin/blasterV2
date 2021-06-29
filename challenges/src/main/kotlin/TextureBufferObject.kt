package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow()

private val buffer = glBufferCreateFloats(
    floats = floatArrayOf(
        1f, 0.5f, 0.25f, 1f
    ))

private val textureBuffer = GlTextureBuffer(
    target = backend.GL_TEXTURE_BUFFER, internalFormat = backend.GL_R32F, buffer = buffer)
private val texture = GlTexture(target = backend.GL_TEXTURE_1D, data = listOf(textureBuffer))

private val constMatrix = constm4(mat4().identity().orthoBox(2f))
private val uniformSampler = unifs1(texture)
private val color = texel(uniformSampler, consti(0))

private val shadingFlat = ShadingFlat(constMatrix, color)
private val rect = glMeshCreateRect()

private fun use(callback: Callback) {
    glShadingFlatUse(shadingFlat) {
        glMeshUse(rect) {
            glTextureUse(texture, callback)
        }
    }
}

fun main() {
    window.create {
        use {
            window.show {
                glClear(col3().green())
                glShadingFlatDraw(shadingFlat) {
                    glTextureBind(texture) {
                        glShadingFlatInstance(shadingFlat, rect)
                    }
                }
            }
        }
    }
}