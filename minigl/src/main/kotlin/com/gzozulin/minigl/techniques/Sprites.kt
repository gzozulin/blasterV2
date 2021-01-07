package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.ValueCache
import com.gzozulin.minigl.scene.WasdInput
import java.util.*

@Deprecated("Use assembly instead!")
data class Sprite(
    val frames: List<Frame>,
    val animations: List<Animation>
)

@Deprecated("Use assembly instead!")
data class Animation(
    val frameIndices: List<Int>
)

@Deprecated("Use assembly instead!")
data class Frame(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {

    companion object {
        fun fromImage(offsetX: Int, offsetY: Int, frameX: Int, frameY: Int,
                      frameWidth: Int, frameHeight: Int, imageWidth: Int, imageHeight: Int): Frame {
            val width = frameWidth.toFloat() / imageWidth.toFloat()
            val height = frameHeight.toFloat() / imageHeight.toFloat()
            val left = offsetX + width * frameX
            val top = offsetY + height * frameY
            return Frame(left, top, width, height)
        }
    }
}

@Deprecated("Use assembly instead!")
class StaticSpritesTechnique : GlResource() {
    private val program = shadersLib.loadProgram(
        "shaders/sprites/sprites.vert", "shaders/sprites/sprites.frag")

    private val rect = GlMesh.rect()

    private val cachedFrameLeft = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_LEFT.label, it) }
    private val cachedFrameTop = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_TOP.label, it) }
    private val cachedFrameWidth = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_WIDTH.label, it) }
    private val cachedFrameHeight = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_HEIGHT.label, it) }

    private val cachedWidth = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_WIDTH.label, it) }
    private val cachedHeight = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_HEIGHT.label, it) }

    init {
        addChildren(program, rect)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(program, rect) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M.label, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M.label, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE.label, camera.position)
            draw.invoke()
        }
    }

    fun instance(modelM: mat4, diffuse: GlTexture, frameLeft: Float, frameTop: Float,
                 frameWidth: Float, frameHeight: Float, width: Float, height: Float) {
        glBind(diffuse) {
            cachedFrameLeft.set(frameLeft)
            cachedFrameTop.set(frameTop)
            cachedFrameWidth.set(frameWidth)
            cachedFrameHeight.set(frameHeight)
            cachedWidth.set(width)
            cachedHeight.set(height)
            program.setUniform(GlUniform.UNIFORM_MODEL_M.label, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE.label, diffuse)
            program.draw(mesh = rect)
        }
    }

    fun instance(modelM: mat4, diffuse: GlTexture, sprite: Sprite, animationIndex: Int, frameIndex: Int,
                 width: Float, height: Float) {
        val frame = sprite.frames[sprite.animations[animationIndex].frameIndices[frameIndex]]
        instance(modelM, diffuse, frame.left, frame.top, frame.width, frame.height, width, height)
    }
}

private val window = GlWindow()

private val diffuse = texturesLib.loadTexture("textures/woman.png")

private val simpleTechnique = StaticSimpleTechnique()
private val spritesTechnique = StaticSpritesTechnique()
private val skyboxTechnique = StaticSkyboxTechnique("textures/darkskies")

private val camera = Camera()
private val controller = Controller(position = vec3(50f, 5f, 70f), pitch = radf(-30f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val field = GlMesh.rect(0f, 100f, 100f, 0f)
private val fieldM = mat4().identity().rotate(radf(90f), vec3().right())
private val fieldDiffuse = texturesLib.loadTexture("textures/floor.jpg")

private const val MODELS = 10000
private const val FRAMES = 9
private const val ANIMS = 8
private var a = 0;

private val sprite = Sprite(
    frames = listOf(
        Frame.fromImage(0, 0, 0, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 0, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 1, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 2, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 3, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 4, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 5, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 6, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 0, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 1, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 2, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 3, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 4, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 5, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 6, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 7, 7, 48, 92, 432, 736),
        Frame.fromImage(0, 0, 8, 7, 48, 92, 432, 736)
    ),
    animations = listOf(
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++)),
        Animation(listOf(a++, a++, a++, a++, a++, a++, a++, a++, a++))
    )
)

private data class Model(val startFrame: Int, val animation: Int, val modelM: mat4)

private fun createModels() {
    val origin = mat4().identity()
    repeat(MODELS) {
        origin.setTranslation(randf(0f, 100f), 1f, randf(0f, 100f))
        models.add(Model(random.nextInt(FRAMES), random.nextInt(ANIMS), mat4().set(origin)))
    }
}

private val models = mutableListOf<Model>()

private val random = Random()

private var currentFrame = 0

private var mouseLook = false

fun main() {
    createModels()
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
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
        glUse(simpleTechnique, spritesTechnique, skyboxTechnique, diffuse, field, fieldDiffuse) {
            window.show {
                currentFrame++
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                glDepthTest {
                    skyboxTechnique.skybox(camera)
                    simpleTechnique.draw(camera) {
                        simpleTechnique.instance(field, fieldDiffuse, fieldM)
                    }
                    spritesTechnique.draw(camera) {
                        for (model in models) {
                            val frame = (currentFrame + model.startFrame) % FRAMES
                            spritesTechnique.instance(model.modelM, diffuse, sprite, model.animation, frame, 1f, 2f)
                        }
                    }
                }
            }
        }
    }
}