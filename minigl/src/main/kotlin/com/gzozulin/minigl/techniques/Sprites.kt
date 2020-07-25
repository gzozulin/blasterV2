package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput

private class ValueCache<T>(private val setter: (T) -> Unit) {

    private var cached: T? = null;

    fun set(value: T) {
        if (value != cached) {
            setter.invoke(value)
            cached = value
        }
    }
}

class SpritesTechnique : GlResource() {
    private val program = shadersLib.loadProgram(
        "shaders/sprites/sprites.vert", "shaders/sprites/sprites.frag")

    private val rect = GlMesh.rect()

    private val cachedLeft = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_LEFT, it) }
    private val cachedTop = ValueCache<Float> { program.setUniform(GlUniform.UNIFORM_TOP, it) }
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

    fun instance(modelM: mat4, diffuse: GlTexture, left: Float, top: Float, width: Float, height: Float) {
        glBind(diffuse) {
            //cachedLeft.set(left)
            //cachedTop.set(top)
            //cachedWidth.set(width)
            //cachedHeight.set(height)
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            program.draw(mesh = rect)
        }
    }
}

private val window = GlWindow()

private val diffuse = texturesLib.loadTexture("textures/ss/reaver.png")

private val spritesTechnique = SpritesTechnique()

private val camera = Camera()
private val controller = Controller(position = vec3().front(), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val identityM = mat4().identity()

fun main() {
    window.create(isFullscreen = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        window.deltaCallback = { delta ->
            wasdInput.onCursorDelta(delta)
        }
        glUse(spritesTechnique, diffuse) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                spritesTechnique.draw(camera) {
                    spritesTechnique.instance(identityM, diffuse, 0f, 0f, 1f, 1f)
                }
            }
        }
    }
}