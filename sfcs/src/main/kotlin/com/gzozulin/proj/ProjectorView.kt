package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.scene.*
import java.lang.Float.max
import java.lang.Float.min

private const val SCREEN_WIDTH = 1920 // 1080p
private const val SCREEN_HEIGHT = 1080

private const val CODE_PANEL_WIDTH = 1080
private const val CODE_PANEL_HEIGHT = 1000
private const val CODE_PANEL_POS_X = SCREEN_WIDTH / 2f - 200
private const val CODE_PANEL_POS_Y = SCREEN_HEIGHT / 2f

private val codeModelM = mat4().identity()
    .translate(CODE_PANEL_POS_X, CODE_PANEL_POS_Y, 0f)
    .scale(CODE_PANEL_WIDTH.toFloat(), CODE_PANEL_HEIGHT.toFloat(), 1f)

private const val MINIMAP_PANEL_WIDTH = 300
private const val MINIMAP_PANEL_HEIGHT = 1000
private const val MINIMAP_CURSOR_HEIGHT = 300
private const val MINIMAP_PANEL_POS_X = SCREEN_WIDTH / 2f + 490
private const val MINIMAP_PANEL_POS_Y = SCREEN_HEIGHT / 2f

private const val MAX_MINIMAP_LINES = 140

private val minimapModelM = mat4().identity()
    .translate(MINIMAP_PANEL_POS_X, MINIMAP_PANEL_POS_Y, 0f)
    .scale(MINIMAP_PANEL_WIDTH.toFloat(), MINIMAP_PANEL_HEIGHT.toFloat(), 1f)

private val minimapCursorModelM = mat4().identity()
    .translate(MINIMAP_PANEL_POS_X, MINIMAP_PANEL_POS_Y, 0f)
    .scale(MINIMAP_PANEL_WIDTH.toFloat(), MINIMAP_CURSOR_HEIGHT.toFloat(), 1f)

private val codeFontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.5f, fontScaleV = 0.5f,
    fontStepScaleU = 0.5f, fontStepScaleV = 0.8f)

private val minimapFontDescription = codeFontDescription.copy(
    fontScaleU = 0.6f, fontScaleV = 0.15f
)

class ProjectorView(private val model: ProjectorModel) : GlResource(), GlResizable {

    private val crossFadeTechnique = CrossFadeTechnique(timeout = 2000)

    // ----------------- Text ------------------

    private val codeTextTechnique = SimpleTextTechnique(codeFontDescription, SCREEN_WIDTH, SCREEN_HEIGHT)
    private val codeRttTechnique = RttTechnique(CODE_PANEL_WIDTH, CODE_PANEL_HEIGHT)

    private val minimapTextTechnique = SimpleTextTechnique(minimapFontDescription, SCREEN_WIDTH, SCREEN_HEIGHT)
    private val minimapRttTechnique = RttTechnique(MINIMAP_PANEL_WIDTH, MINIMAP_PANEL_HEIGHT)
    private val minimapCursorTechnique = RttTechnique(MINIMAP_PANEL_WIDTH, MINIMAP_CURSOR_HEIGHT)
    private val minimapCursorColor = vec4(0.25f)

    private val panelModelM = unifm4()
    private val panelViewM = constm4(mat4().identity())
    private val panelProjM = constm4(mat4().ortho(0f, SCREEN_WIDTH.toFloat(), 0f, SCREEN_HEIGHT.toFloat(), -1f, 1f))

    private val panelTexCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
    private val panelSampler = unifsampler()
    private val panelColor = vec4(0f, 0f, 0f, 0.8f)
    private val panelMesh = GlMesh.rect(1f, 1f)
    private val panelTechnique = FlatTechnique(panelModelM, panelViewM, panelProjM, tex(panelTexCoords, panelSampler))

    // ----------------- Scene ------------------

    private val bedroomModel = modelLib.load("models/bedroom/bedroom", join = false)
    private val noiseTexture = texturesLib.loadTexture("textures/snow.png")

    private val camera = Camera()
    private val controller = ControllerScenic(
        positions = listOf(
            vec3(-8.511e1f, 1.148e2f,  1.536e2f),
            vec3( 1.735e1f, 1.208e2f,  1.576e2f),
            vec3( 1.231e2f, 1.141e2f,  1.750e2f),
            vec3( 1.214e2f, 1.052e2f,  9.576e1f),
            vec3( 1.236e2f, 1.099e2f, -8.687e1f),
            vec3( 4.717e1f, 1.052e2f, -9.311e1f),
            vec3(-9.781e1f, 1.104e2f,  1.249e1f)).reversed(), // CW
        points = listOf(vec3(2.732e1f, 100f, 5.401e1f)))

    private val cameraLight = PointLight(bedroomModel.aabb.center(), vec3(1f), 350f)
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

    private val deferredTechnique = DeferredTechnique(
        modelM, viewM, projM, eye, diffuseMap, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)

    init {
        addChildren(codeTextTechnique, codeRttTechnique, minimapTextTechnique, minimapRttTechnique, minimapCursorTechnique,
            crossFadeTechnique, panelTechnique, panelMesh, deferredTechnique, bedroomModel, noiseTexture)
    }

    // 1080p only
    override fun resize(width: Int, height: Int) {
        deferredTechnique.resize(SCREEN_WIDTH, SCREEN_HEIGHT)
    }

    fun fadeIn() {
        crossFadeTechnique.fadeIn()
    }

    fun renderCrossFade() {
        crossFadeTechnique.draw()
    }

    fun tickCamera() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
    }

    fun renderScene() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        cameraLight.position.set(camera.position)
        glDepthTest {
            glBind(noiseTexture) {
                deferredTechnique.draw(lights) {
                    bedroomModel.objects.forEach { obj ->
                        val material = obj.phong()
                        sampler.value = material.mapDiffuse ?: noiseTexture
                        matDiffuse.value = material.diffuse
                        deferredTechnique.instance(obj)
                    }
                }
            }
        }
    }

    fun renderCode() {
        glMultiSample {
            codeRttTechnique.render {
                glClear(panelColor)
                codeTextTechnique.pageCentered(model.currentPage, model.currentPageCenter, LINES_TO_SHOW)
            }
            minimapRttTechnique.render {
                glClear(panelColor)
                minimapTextTechnique.page(model.currentPage)
            }
            minimapCursorTechnique.render {
                glClear(minimapCursorColor)
            }
            glBlend {
                panelTechnique.draw {
                    glBind(codeRttTechnique.colorAttachment0, minimapRttTechnique.colorAttachment0,
                        minimapCursorTechnique.colorAttachment0) {
                        panelSampler.value = codeRttTechnique.colorAttachment0
                        panelModelM.value = codeModelM
                        panelTechnique.instance(panelMesh)
                        panelSampler.value = minimapRttTechnique.colorAttachment0
                        panelModelM.value = minimapModelM
                        panelTechnique.instance(panelMesh)
                        panelSampler.value = minimapCursorTechnique.colorAttachment0
                        updateMinimapCursor()
                        panelModelM.value = minimapCursorModelM
                        panelTechnique.instance(panelMesh)
                    }
                }
            }
        }
    }

    private fun updateMinimapCursor() {
        val progress = 1f - model.currentPageCenter.toFloat() / MAX_MINIMAP_LINES.toFloat()
        val minimapStart = MINIMAP_PANEL_POS_Y - MINIMAP_PANEL_HEIGHT/2
        val minimapEnd = MINIMAP_PANEL_POS_Y + MINIMAP_PANEL_HEIGHT/2
        val minimapSpan = minimapEnd - minimapStart
        val position = minimapStart + progress * minimapSpan
        val bounded = min(max(position, minimapStart + MINIMAP_CURSOR_HEIGHT/2f), minimapEnd - MINIMAP_CURSOR_HEIGHT/2f)
        minimapCursorModelM.identity()
            .translate(MINIMAP_PANEL_POS_X, bounded, 0f)
            .scale(MINIMAP_PANEL_WIDTH.toFloat(), MINIMAP_CURSOR_HEIGHT.toFloat(), 1f)
    }
}
