package com.gzozulin.proj

import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.kodein.di.instance

private val capturer: GlCapturer by ProjApp.injector.instance()

private val fontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.4f, fontScaleV = 0.5f,
    fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

private val simpleTextTechnique = SimpleTextTechnique(fontDescription, capturer.width, capturer.height)
private val skyboxTechnique = StaticSkyboxTechnique("textures/darkskies")
private val rttTechnique = RttTechnique(1600, 1080)

private val camera = Camera()

private val backgroundModelM = constm4(mat4().identity().translate(capturer.width.toFloat()/2f, capturer.height.toFloat()/2f, 0f))
private val backgroundViewM = constm4(mat4().identity())
private val backgroundProjM = constm4(mat4().ortho(0f, capturer.width.toFloat(), 0f, capturer.height.toFloat(), -1f, 1f))

private val backgroundTexCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
private val backgroundSampler = unifsampler()
private val backgroundTech = SimpleTechnique(backgroundModelM, backgroundViewM, backgroundProjM, tex(backgroundTexCoords, backgroundSampler))
private val backgroundColor = vec4(0f, 0f, 0f, 0.8f)
private val backgroundMesh = GlMesh.rect(1600f, 1080f)

class ProjScene : GlResource() {
    private val model: ProjModel by ProjApp.injector.instance()
    private val mechanicPlayback: MechanicPlayback by ProjApp.injector.instance()

    init {
        addChildren(simpleTextTechnique, skyboxTechnique, rttTechnique, backgroundTech, backgroundMesh)
    }

    fun onFrame() {
        updateScene()
        renderCode()
        renderScene()
    }

    private fun updateScene() {
        mechanicPlayback.updateSpans()
        camera.tick()
    }

    private fun renderCode() {
        rttTechnique.render {
            glClear(backgroundColor)
            simpleTextTechnique.pageCentered(model.page, model.center, LINES_TO_SHOW)
        }
    }

    private fun renderScene() {
        skyboxTechnique.skybox(camera)
        glBlend {
            backgroundTech.draw {
                glBind(rttTechnique.colorAttachment0) {
                    backgroundSampler.value = rttTechnique.colorAttachment0
                    backgroundTech.instance(backgroundMesh)
                }
            }
        }
    }
}