package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.scene.*

private val fontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.4f, fontScaleV = 0.5f,
    fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

class ProjectorView(private val model: ProjectorModel, width: Int, height: Int) : GlResource(), GlResizable {

    private val crossFadeTechnique = CrossFadeTechnique(timeout = 2000)

    // ----------------- Text ------------------

    private val simpleTextTechnique = SimpleTextTechnique(fontDescription, width, height)
    private val rttTechnique = RttTechnique(1600, 1080)

    private val backgroundModelM = unifm4()
    private val backgroundViewM = constm4(mat4().identity())
    private val backgroundProjM = unifm4()

    private val backgroundTexCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
    private val backgroundSampler = unifsampler()
    private val backgroundTech = FlatTechnique(backgroundModelM, backgroundViewM, backgroundProjM, tex(backgroundTexCoords, backgroundSampler))
    private val backgroundColor = vec4(0f, 0f, 0f, 0.8f)
    private val backgroundMesh = GlMesh.rect(1600f, 1080f)

    // ----------------- Scene ------------------

    private val bedroom = modelLib.load("models/bedroom/bedroom", join = false)
    private val empty = texturesLib.loadTexture("textures/snow.png")

    private val camera = Camera()
    private val controller = ControllerScenic(points = listOf(
        PointOfInterest(vec3(1.138e2f, 1.388e2f, -5.025e1f), vec3(4.844e-1f, -3.756e-1f, -7.901e-1f)),
        PointOfInterest(vec3(-8.189e1f, 1.131e2f, -3.284e1f), vec3(-2.556e-1f, -3.802e-1f, -8.889e-1f)),
        PointOfInterest(vec3(8.531e1f, 1.210e2f, 1.509e2f), vec3(-2.779e-1f, -3.098e-1f, 9.093e-1f)),
    ))

    private val cameraLight = PointLight(bedroom.aabb.center(), vec3(1f), 350f)
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

    private val matAmbient = constv3(vec3(0.05f))
    private val matDiffuse = unifv3()
    private val matSpecular = constv3(vec3(0.33f))
    private val matShine = constf(1f)
    private val matTransparency = constf(1f)

    private val deferredTextureTechnique = DeferredTechnique(
        modelM, viewM, projM, eye, diffuseMap, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)

    init {
        addChildren(simpleTextTechnique, rttTechnique, crossFadeTechnique, backgroundTech, backgroundMesh,
            deferredTextureTechnique, bedroom, empty)
    }

    override fun resize(width: Int, height: Int) {
        deferredTextureTechnique.resize(width, height)
        simpleTextTechnique.resize(width, height)
        backgroundModelM.value = mat4().identity().translate(width.toFloat()/2f, height.toFloat()/2f, 0f)
        backgroundProjM.value = mat4().ortho(0f, width.toFloat(), 0f, height.toFloat(), -1f, 1f)
    }

    fun fadeIn() {
        crossFadeTechnique.fadeIn()
    }

    fun tickCamera() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
    }

    fun prepareCode() {
        glMultiSample {
            rttTechnique.render {
                glClear(backgroundColor)
                simpleTextTechnique.pageCentered(model.currentPage, model.currentPageCenter, LINES_TO_SHOW)
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
                    bedroom.objects.forEach { obj ->
                        val material = obj.phong()
                        sampler.value = material.mapDiffuse ?: empty
                        matDiffuse.value = material.diffuse
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