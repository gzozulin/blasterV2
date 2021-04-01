package com.gzozulin.proj

import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.PointLight
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import org.kodein.di.instance

class SceneCozyRoom : GlResource() {
    private val modelCozyRoom: ModelCozyRoom by ProjectorApp.injector.instance()
    private val capturer: GlCapturer by ProjectorApp.injector.instance()

    private val fontDescription = FontDescription(
        textureFilename = "textures/font_hires.png",
        glyphSidePxU = 64, glyphSidePxV = 64,
        fontScaleU = 0.4f, fontScaleV = 0.5f,
        fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

    private val crossFadeTechnique = CrossFadeTechnique(timeout = 2000)

    // ----------------- Text ------------------

    private val simpleTextTechnique = SimpleTextTechnique(fontDescription, capturer.width, capturer.height)
    private val rttTechnique = RttTechnique(1600, 1080)

    private val backgroundModelM = constm4(mat4().identity().translate(capturer.width.toFloat()/2f, capturer.height.toFloat()/2f, 0f))
    private val backgroundViewM = constm4(mat4().identity())
    private val backgroundProjM = constm4(mat4().ortho(0f, capturer.width.toFloat(), 0f, capturer.height.toFloat(), -1f, 1f))

    private val backgroundTexCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
    private val backgroundSampler = unifsampler()
    private val backgroundTech = FlatTechnique(backgroundModelM, backgroundViewM, backgroundProjM, tex(backgroundTexCoords, backgroundSampler))
    private val backgroundColor = vec4(0f, 0f, 0f, 0.8f)
    private val backgroundMesh = GlMesh.rect(1600f, 1080f)

    // ----------------- Scene ------------------

    private val model = modelLib.load("models/bedroom/bedroom", join = false)
    private val empty = texturesLib.loadTexture("textures/snow.png")

    private val camera = Camera()
    private val controller = Controller(position = model.aabb.center(), velocity = 1f)
    private val wasdInput = WasdInput(controller)

    private val cameraLight = PointLight(model.aabb.center(), vec3(1f), 350f)
    private val lights = listOf(cameraLight,
        PointLight(vec3(178.7f, 91.33f, -107.6f), vec3().yellow(), 500f),
        PointLight(vec3(-113.8f, 104.5f, -91.5f), vec3().azure(), 1000f),
        PointLight(vec3(28.2f, 112.2f, 193.7f), vec3().cyan(), 300f),
    )

    private val modelM = constm4(mat4().identity())
    private val viewM = unifm4 { camera.calculateViewM() }
    private val eye = unifv3 { camera.position }
    private val projM = unifm4 { camera.projectionM }

    private val texCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
    private val sampler = unifsampler()
    private val diffuseMap = tex(texCoords, sampler)

    private val matAmbient = unifv3()
    private val matDiffuse = unifv3()
    private val matSpecular = unifv3()
    private val matShine = uniff()
    private val matTransparency = uniff()

    private val deferredTextureTechnique = DeferredTechnique(
        modelM, viewM, projM, eye, diffuseMap, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)

    init {
        addChildren(simpleTextTechnique, rttTechnique, crossFadeTechnique, backgroundTech, backgroundMesh,
            deferredTextureTechnique, model, empty)
    }

    fun resize(width: Int, height: Int) {
        deferredTextureTechnique.resize(width, height)
    }

    fun fadeIn() {
        crossFadeTechnique.fadeIn()
    }

    fun tickCamera() {
        camera.tick()
    }

    fun prepareCode() {
        glMultiSample {
            rttTechnique.render {
                glClear(backgroundColor)
                simpleTextTechnique.pageCentered(modelCozyRoom.page, modelCozyRoom.center, LINES_TO_SHOW)
            }
        }
    }

    fun renderScene() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        cameraLight.position.set(camera.position)
        glDepthTest {
            glBind(empty) {
                deferredTextureTechnique.draw(lights) {
                    model.objects.forEach { obj ->
                        val material = obj.phong()
                        sampler.value = material.mapDiffuse ?: empty
                        matAmbient.value = vec3(0f)
                        matDiffuse.value = material.diffuse
                        matSpecular.value = material.specular
                        matShine.value = material.shine
                        matTransparency.value = material.transparency
                        deferredTextureTechnique.instance(obj)
                    }
                }
            }
        }
    }

    fun renderCode() {
        glBlend {
            backgroundTech.draw {
                glBind(rttTechnique.colorAttachment0) {
                    backgroundSampler.value = rttTechnique.colorAttachment0
                    backgroundTech.instance(backgroundMesh)
                }
            }
        }
    }

    fun renderCrossFade() {
        crossFadeTechnique.draw()
    }
}