package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BillboardsProvider {
    abstract fun flushPositions(position: FloatBuffer)
    open fun flushScale(scale: FloatBuffer) {}
    open fun flushTransparency(transparency: FloatBuffer) {}
    abstract fun size(): Int
}

class BillboardsTechnique(max: Int) : GlResource() {
    private val program = shadersLib.loadProgram(
        "shaders/billboards/billboards.vert", "shaders/billboards/billboards.frag")

    private val positions = GlBuffer(backend.GL_ARRAY_BUFFER, ByteBuffer.allocateDirect(max * 3 * 4)
        .order(ByteOrder.nativeOrder()), backend.GL_STREAM_DRAW)
    private val scale = GlBuffer(backend.GL_ARRAY_BUFFER, ByteBuffer.allocateDirect(max * 4)
        .order(ByteOrder.nativeOrder()), backend.GL_STREAM_DRAW)
    private val transparency = GlBuffer(backend.GL_ARRAY_BUFFER, ByteBuffer.allocateDirect(max * 4)
        .order(ByteOrder.nativeOrder()), backend.GL_STREAM_DRAW)

    private val rect = GlMesh.rect(additionalAttributes = listOf(
        GlAttribute.ATTRIBUTE_BILLBOARD_POSITION to positions,
        GlAttribute.ATTRIBUTE_BILLBOARD_SCALE to scale,
        GlAttribute.ATTRIBUTE_BILLBOARD_TRANSPARENCY to transparency))

    init {
        addChildren(program, positions, scale, transparency, rect)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(program, rect) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            draw.invoke()
        }
    }

    private fun updatePositions(provider: BillboardsProvider) {
        glBind(positions) {
            positions.updateBuffer {
                provider.flushPositions(it.asFloatBuffer())
            }
        }
    }

    private fun updateScales(provider: BillboardsProvider) {
        glBind(scale) {
            scale.updateBuffer {
                provider.flushScale(it.asFloatBuffer())
            }
        }
    }

    private fun updateTransparency(provider: BillboardsProvider) {
        glBind(transparency) {
            transparency.updateBuffer {
                provider.flushTransparency(it.asFloatBuffer())
            }
        }
    }

    fun instance(provider: BillboardsProvider, modelM: mat4, diffuse: GlTexture, width: Float, height: Float,
                 updateScale: Boolean = true, updateTransparency: Boolean = true) {
        updatePositions(provider)
        if (updateScale) {
            updateScales(provider)
        }
        if (updateTransparency) {
            updateTransparency(provider)
        }
        glBind(diffuse, positions, scale, transparency) {
            program.setUniform(GlUniform.UNIFORM_SCALE_FLAG, if (updateScale) 1 else 0)
            program.setUniform(GlUniform.UNIFORM_TRANSPARENCY_FLAG, if (updateTransparency) 1 else 0)
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setUniform(GlUniform.UNIFORM_WIDTH, width)
            program.setUniform(GlUniform.UNIFORM_HEIGHT, height)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            program.drawInstanced(indicesCount = rect.indicesCount, instances = provider.size())
        }
    }
}

private val window = GlWindow()

private val diffuse = texturesLib.loadTexture("textures/smoke.png")

private val billboardsTechnique = BillboardsTechnique(1000)

private val camera = Camera()
private val controller = Controller(position = vec3().front(), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val identityM = mat4().identity()

private var mouseLook = false

private val billboardsProvider = object : BillboardsProvider() {
    override fun flushPositions(position: FloatBuffer) {
        for(i in 1..1000) {
            position.put(randf(-10f, 10f))
            position.put(randf(-10f, 10f))
            position.put(0f)
        }
    }

    override fun size() = 1000
}

fun main() {
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        window.buttonCallback = { key, pressed ->
            if (key == 0) {
                mouseLook = pressed
            }
        }
        window.deltaCallback = { delta ->
            if (mouseLook) {
                wasdInput.onCursorDelta(delta)
            }
        }
        glUse(billboardsTechnique, diffuse) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                billboardsTechnique.draw(camera) {
                    billboardsTechnique.instance(billboardsProvider, identityM, diffuse, 1f, 1f,
                        updateScale = false, updateTransparency = false)
                }
            }
        }
    }
}