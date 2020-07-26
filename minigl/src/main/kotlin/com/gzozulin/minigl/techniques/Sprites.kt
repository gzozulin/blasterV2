package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput
import java.util.*

private class ValueCache<T>(private val setter: (T) -> Unit) {

    private var cached: T? = null;

    fun set(value: T) {
        if (value != cached) {
            setter.invoke(value)
            cached = value
        }
    }
}

data class Sprite(
    val frames: List<Frame>,
    val animations: List<Animation>
)

data class Animation(
    val frameIndices: List<Int>
)

data class Frame(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {
    constructor(pixelLeft: Int, pixelTop: Int, pixelWidth: Int, pixelHeight: Int):
            this(0f, 0f, 1f, 1f)
}

class SpritesTechnique : GlResource() {
    private val program = shadersLib.loadProgram(
        "shaders/sprites/sprites.vert", "shaders/sprites/sprites.frag")

    private val rect = GlMesh.rect()

    private val cachedFrameLeft = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_LEFT, it) }
    private val cachedFrameTop = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_TOP, it) }
    private val cachedFrameWidth = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_WIDTH, it) }
    private val cachedFrameHeight = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_FRAME_HEIGHT, it) }

    private val cachedWidth = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_WIDTH, it) }
    private val cachedHeight = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_HEIGHT, it) }

    init {
        addChildren(program, rect)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(program, rect) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
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
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
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

private val diffuse = texturesLib.loadTexture("textures/ss/reaver.png")

private val spritesTechnique = SpritesTechnique()
private val skyboxTechnique = SkyboxTechnique("textures/gatekeeper")

private val camera = Camera()
private val controller = Controller(position = vec3().front(), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val sprite = Sprite(
    frames = listOf(
        Frame(  0f,   0f, .25f, .25f),
        Frame(.25f, .25f, .25f, .25f),
        Frame( .5f,  .5f, .25f, .25f),
        Frame(.75f, .75f, .25f, .25f)
    ),
    animations = listOf(
        Animation(listOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)),
        Animation(listOf(
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
        Animation(listOf(
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3))
    )
)

private val random = Random()

fun main() {
    window.create(isFullscreen = true) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        window.deltaCallback = { delta ->
            wasdInput.onCursorDelta(delta)
        }
        glUse(spritesTechnique, skyboxTechnique, diffuse) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                spritesTechnique.draw(camera) {
                    val origin = mat4().identity()
                    for (x in 0..100) {
                        for (z in 0..100) {
                            origin.setTranslation(x.toFloat(), 0f, z.toFloat())
                            spritesTechnique.instance(origin, diffuse, sprite, 0, random.nextInt(60), 1f, 1f)
                        }
                    }
                }
            }
        }
    }
}