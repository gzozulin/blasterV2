package com.gzozulin.proj

import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.assets.meshLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSimpleTechnique
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique

private val window = GlWindow()

private val matrixStack = MatrixStack()
private val camera = Camera()
private val controller = Controller(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/miramar")
private val simpleTechnique = StaticSimpleTechnique()

private val fontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.3f, fontScaleV = 0.3f,
    fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

private val simpleTextTechnique = SimpleTextTechnique(fontDescription, window.width, window.height)

private val text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
        "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when\n" +
        "an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
        "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
        "remaining essentially unchanged. It was popularised in the 1960s with the release of\n" +
        "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing\n" +
        "software like Aldus PageMaker including versions of Lorem Ipsum."

private val spans = text.run {
    val words = split(' ')
    val spans = mutableListOf<SimpleSpan>()
    words.forEach { spans.add(SimpleSpan("$it ", col3().green(), SpanVisibility.INVISIBLE)) }
    return@run spans
}

private val examplePage = TextPage(spans)

private val framebuffer = GlFrameBuffer()
private var textTex = GlTexture(
    width = 1920, height = 1080, internalFormat = backend.GL_RGBA,
    pixelFormat = backend.GL_RGBA, pixelType = backend.GL_UNSIGNED_BYTE)
private val textRect = GlMesh.rect(.35f, .5f)
private val textMatrix = mat4().identity().translate(.065f, .11f, .151f)

private val pcjrtex = texturesLib.loadTexture("models/pcjr/pcjr.jpeg")
private val pcjr = meshLib.loadMesh("models/pcjr/pcjr.obj") { println(it) }

private var mouseLook = false

private var frame = 0

private fun updateScene() {
    frame++
    controller.apply { position, direction ->
        camera.setPosition(position)
        camera.lookAlong(direction)
    }
    if (frame % 3 == 0) {
        val found = spans.firstOrNull { it.visibility == SpanVisibility.INVISIBLE }
        if (found == null) {
            spans.forEach { it.visibility = SpanVisibility.INVISIBLE }
        } else {
            found.visibility = SpanVisibility.VISIBLE
        }
    }
}

private fun renderText() {
    glBind(framebuffer, textTex) {
        framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, textTex)
        framebuffer.setOutputs(intArrayOf(backend.GL_COLOR_ATTACHMENT0))
        framebuffer.checkIsComplete()
        glClear()
        simpleTextTechnique.page(examplePage)
    }
}

private fun renderScene() {
    glClear()
    skyboxTechnique.skybox(camera)
    glDepthTest {
        glCulling {
            simpleTechnique.draw(camera.calculateViewM(), camera.projectionM) {
                simpleTechnique.instance(pcjr.mesh, pcjrtex, matrixStack.peekMatrix())
                matrixStack.pushMatrix(textMatrix) {
                    simpleTechnique.instance(textRect, textTex, matrixStack.peekMatrix())
                }
            }
        }
    }
}

fun main() {
    window.create(isHoldingCursor = false, isFullscreen = true) {
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
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        glUse(simpleTechnique, skyboxTechnique, simpleTextTechnique,
            framebuffer, pcjr.mesh, pcjrtex, textRect, textTex) {
            window.show {
                updateScene()
                renderText()
                renderScene()
            }
        }
    }
}