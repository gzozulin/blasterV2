package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.*
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput
import org.joml.Matrix4f
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private const val SHADER_DIFFUSE_CNT = 16
private const val SHADER_SIDE_CNT = 8

@Deprecated("Use assembly instead!")
class StaticTileMapTechnique : GlResource() {
    private val program = shadersLib.loadProgram(
        "shaders/tilemap/tilemap.vert", "shaders/tilemap/tilemap.frag")

    init {
        addChildren(program)
    }

    fun draw(viewM: mat4, projectionM: mat4, diffuseArray: Array<GlTexture>, draw: () -> Unit) {
        check(diffuseArray.size <= SHADER_DIFFUSE_CNT) { "Too many diffuse!" }
        checkReady()
        glBind(program, *diffuseArray) {
            diffuseArray.forEachIndexed { index, texture ->
                program.setArrayTexture(GlUniform.UNIFORM_DIFFUSE_ARRAY.label, index, texture)
            }
            program.setUniform(GlUniform.UNIFORM_VIEW_M.label, viewM)
            program.setUniform(GlUniform.UNIFORM_PROJ_M.label, projectionM)
            draw.invoke()
        }
    }

    fun instance(mesh: GlMesh, tileMap: GlTexture, modelM: Matrix4f) {
        checkReady()
        glBind(mesh, tileMap) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M.label, modelM)
            program.setTexture(GlUniform.UNIFORM_TILE_MAP.label, tileMap)
            program.draw(indicesCount = mesh.indicesCount)
        }
    }

    companion object {
        fun createTileMap(values: IntArray): GlTexture {
            check(values.size == SHADER_SIDE_CNT * SHADER_SIDE_CNT)
            val byteBuffer = ByteBuffer.allocateDirect(SHADER_SIDE_CNT * SHADER_SIDE_CNT * 4)
                .order(ByteOrder.nativeOrder())
            byteBuffer.asIntBuffer().put(values)
            return GlTexture(
                width = SHADER_SIDE_CNT, height = SHADER_SIDE_CNT, pixels = byteBuffer,
                internalFormat = backend.GL_R8UI, pixelFormat = backend.GL_RED_INTEGER, pixelType = backend.GL_UNSIGNED_INT)
        }
    }
}

private val camera = Camera()
private val controller = Controller(position = vec3(2.5f, 10f, 2.5f), pitch = radf(-90f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/hills")
private val tileMapTechnique = StaticTileMapTechnique()

private val rect = GlMesh.rect()

private val diffuseArray = arrayOf(
    texturesLib.loadTexture("textures/utah.jpg"),
    texturesLib.loadTexture("textures/landscape.jpg"),
    texturesLib.loadTexture("textures/grass.jpg"),
    texturesLib.loadTexture("textures/marble.jpg"),
    texturesLib.loadTexture("textures/nuke/nuke_bk.jpg"),
    texturesLib.loadTexture("textures/nuke/nuke_dn.jpg"),
    texturesLib.loadTexture("textures/nuke/nuke_ft.jpg"),
    texturesLib.loadTexture("textures/nuke/nuke_lf.jpg"),
    texturesLib.loadTexture("textures/snowy/snowy_bk.jpg"),
    texturesLib.loadTexture("textures/snowy/snowy_dn.jpg"),
    texturesLib.loadTexture("textures/snowy/snowy_ft.jpg"),
    texturesLib.loadTexture("textures/snowy/snowy_lf.jpg"),
    texturesLib.loadTexture("textures/sincity/sincity_bk.jpg"),
    texturesLib.loadTexture("textures/sincity/sincity_dn.jpg"),
    texturesLib.loadTexture("textures/sincity/sincity_ft.jpg"),
    texturesLib.loadTexture("textures/sincity/sincity_lf.jpg")
)

private var mouseLook = false

fun main() {
    val random = Random()
    val tileMaps = mutableListOf<GlTexture>()
    (0 until 25).forEach {
        val list = mutableListOf<Int>()
        (0 until 64).forEach {
            list.add(random.nextInt(diffuseArray.size))
        }
        tileMaps.add(StaticTileMapTechnique.createTileMap(list.toIntArray()))
    }
    val window = GlWindow()
    window.create(isHoldingCursor = false) {
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
        glUse(skyboxTechnique, tileMapTechnique, rect, *diffuseArray, *tileMaps.toTypedArray()) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glDepthTest {
                    tileMapTechnique.draw(camera.calculateViewM(), camera.projectionM, diffuseArray) {
                        val origin = mat4().identity().rotate(radf(90f), vec3().right())
                        var tileMapIdx = 0
                        for (x in 0 until 5) {
                            for (z in 0 until 5) {
                                origin.setTranslation(x.toFloat() * 2f, 0f, z.toFloat() * 2f)
                                tileMapTechnique.instance(rect, tileMaps[tileMapIdx], origin)
                                tileMapIdx++
                            }
                        }
                    }
                }
            }
        }
    }
}