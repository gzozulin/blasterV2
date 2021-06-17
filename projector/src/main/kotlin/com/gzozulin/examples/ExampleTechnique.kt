package com.gzozulin.examples

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.tech.ShadingFlat
import com.gzozulin.minigl.tech.glShadingFlatDraw
import com.gzozulin.minigl.tech.glShadingFlatInstance
import com.gzozulin.minigl.tech.glShadingFlatUse

private val window = GlWindow(isFullscreen = true)
private val capturer = Capturer(window)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson()
private val wasdInput = WasdInput(controller)

private val group = libWavefrontCreate("models/cube/cube")
private val mesh = group.objects.first().mesh

private val textureFont = libTextureCreate("textures/font.png")
    .copy(minFilter = backend.GL_NEAREST, magFilter = backend.GL_NEAREST)
private val textureGrass = libTextureCreate("textures/grass.jpg")
    .copy(minFilter = backend.GL_NEAREST, magFilter = backend.GL_NEAREST)

private val uniformMatrix = unifm4 { camera.calculateFullM() }

private val tiledFontUVUniform = unifv2i(15, 15)
private val tiledFontTexCoords = tile(namedTexCoordsV2(), tiledFontUVUniform, constv2i(16, 16))
private val tiledFontSampler = sampler(unifs(textureFont), tiledFontTexCoords)

private var tintedGrassTick = 0f
private val tintedGrassSampler = sampler(unifs(textureGrass))
private val tintedGrassRUniform = uniff(1f)
private val tintedGrassColor = setrv4(tintedGrassSampler, tintedGrassRUniform)

private val resultingColor = mulv4(tiledFontSampler, tintedGrassColor)

private val shadingFlat = ShadingFlat(uniformMatrix, resultingColor)

fun use(callback: Callback) {
    glShadingFlatUse(shadingFlat) {
        glTextureUse(textureGrass) {
            glTextureUse(textureFont) {
                glMeshUse(mesh) {
                    callback.invoke()
                }
            }
        }
    }
}

fun bind(callback: Callback) {
    glTextureBind(textureFont) {
        glTextureBind(textureGrass) {
            glShadingFlatDraw(shadingFlat) {
                callback.invoke()
            }
        }
    }
}

fun update() {
    tintedGrassTick += 0.01f
    tintedGrassRUniform.value = sinf(tintedGrassTick)
}

fun main() {
    window.create {
        capturer.capture {
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
            glDepthTest {
                use {
                    window.show {
                        controller.apply { position, direction ->
                            camera.setPosition(position)
                            camera.lookAlong(direction)
                        }
                        bind {
                            update()
                            glClear(col3().azure().mul(0.5f))
                            glShadingFlatInstance(shadingFlat, mesh)
                        }
                        //capturer.addFrame()
                    }
                }
            }
        }
    }
}
