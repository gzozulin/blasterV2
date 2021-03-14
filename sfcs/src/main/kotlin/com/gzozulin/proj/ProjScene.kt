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

private val backgroundModelM = constm4(mat4().identity().translate(capturer.width.toFloat()/2f, 0f, 0f))
private val backgroundViewM = constm4(mat4().identity())
private val backgroundProjM = constm4(mat4().ortho(0f, capturer.width.toFloat(), 0f, capturer.height.toFloat(), -1f, 1f))

private val backgroundMesh = GlMesh.rect(1600f, capturer.height.toFloat() * 2)
private val backgroundTech = SimpleTechnique(backgroundModelM, backgroundViewM, backgroundProjM, constv4(vec4(0f, 0f, 0f, 0.8f)))

class ProjScene : GlResource() {
    private val model: ProjModel by ProjApp.injector.instance()
    private val mechanicPlayback: MechanicPlayback by ProjApp.injector.instance()

    private val camera = Camera()

    init {
        addChildren(simpleTextTechnique, skyboxTechnique, backgroundTech, backgroundMesh)
    }

    fun onFrame() {
        glClear(col3().ltGrey())
        mechanicPlayback.updateSpans()
        camera.tick()
        skyboxTechnique.skybox(camera)
        glBlend {
            backgroundTech.draw {
                backgroundTech.instance(backgroundMesh)
            }
        }
        simpleTextTechnique.pageCentered(model.page, model.center, LINES_TO_SHOW)
    }
}