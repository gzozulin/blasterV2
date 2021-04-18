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
    private val controller = ControllerScenic(
        positions = listOf(
            vec3(-8.511e1f, 1.148e2f,  1.536e2f),
            vec3( 1.735e1f, 1.208e2f,  1.576e2f),
            vec3( 1.231e2f, 1.141e2f,  1.750e2f),
            vec3( 1.214e2f, 1.052e2f,  9.576e1f),
            vec3( 1.236e2f, 1.099e2f, -8.687e1f),
            vec3( 4.717e1f, 1.052e2f, -9.311e1f),
            vec3(-9.781e1f, 1.104e2f,  1.249e1f)),
        points = listOf(
            vec3( 1.712e2f, 8.569e1f, -1.056e2f),
            vec3(-1.051e2f, 1.019e2f, -7.971e1f),
            vec3( 2.484e1f, 1.106e2f,  1.942e2f),
            vec3( 8.932e1f, 1.102e2f,  1.880e2f),
            vec3( 1.621e2f, 1.665e2f,  7.581e1f),
            vec3( 1.677e2f, 1.040e2f,  5.499e1f)))

    private val cameraLight = PointLight(bedroom.aabb.center(), vec3(1f), 350f)
    private val lights = listOf(
        cameraLight,
        PointLight(vec3(2.732e1f, 150.3f, 5.401e1f),    vec3().white(),  800f),
        PointLight(vec3(178.7f,   91.33f,  -107.6f),    vec3().yellow(), 500f),
        PointLight(vec3(-113.8f,  104.5f,  -91.5f),     vec3().azure(),  1000f),
        PointLight(vec3(28.2f,    112.2f,   193.7f),    vec3().cyan(),   300f),
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