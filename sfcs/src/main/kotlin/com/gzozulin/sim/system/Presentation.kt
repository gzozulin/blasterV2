package com.gzozulin.sim.system

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.BillboardsProvider
import com.gzozulin.minigl.techniques.StaticBillboardsTechnique
import com.gzozulin.minigl.techniques.StaticSimpleTechnique
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import com.gzozulin.sim.entity.*
import java.nio.FloatBuffer

private val camera = Camera()
private val controller = Controller(position = vec3().up().mul(3f), velocity = 1f)
private val wasdInput = WasdInput(controller)

private val identityM = mat4().identity()

class PresentationSystem : GlResource() {
    private val simpleTechnique = StaticSimpleTechnique()
    private val billboardsTechnique = StaticBillboardsTechnique(10000)
    private val skyboxTechnique = StaticSkyboxTechnique("textures/miramar")

    init {
        addChildren(simpleTechnique, billboardsTechnique, skyboxTechnique)
    }

    private val textures = mutableMapOf<BillboardType, GlTexture>()
    init {
        textures[BillboardType.SOCCER] = texturesLib.loadTexture("textures/soccer.png")
        addChildren(textures.values)
    }

    private val fieldTexture = texturesLib.loadTexture("textures/grass.jpg")
    private val fieldRect = GlMesh.rect(left = -32f, right = 32f, bottom = -32f, top = 32f)

    fun onCursorDelta(delta: vec2) {
        wasdInput.onCursorDelta(delta)
    }

    fun onKeyPressed(key: Int, pressed: Boolean) {
        wasdInput.onKeyPressed(key, pressed)
    }

    fun updateAndDrawFrame() {
        drawFrame()
    }

    private fun drawFields(fields: Collection<Actor>) {
        simpleTechnique.draw(camera.calculateViewM(), camera.projectionM) {
            val fieldM = mat4().rotate(radf(-90f), vec3().right())
            glUse(fieldTexture, fieldRect) {
                for (actor in fields) {
                    val spatial = actor.readSpatial()
                    fieldM.setTranslation(spatial.position)
                    simpleTechnique.instance(fieldRect, fieldTexture, fieldM)
                }
            }
        }
    }

    private fun drawBillboards(balls: Collection<Actor>) {
        billboardsTechnique.draw(camera) {
            val byTexture = mutableMapOf<GlTexture, MutableList<Actor>>()
            for (actor in balls) {
                val billboard = actor.readBillboard()
                val texture = textures[billboard.billboardType]!!
                val sameTexture = byTexture.getOrPut(texture) { mutableListOf() }
                sameTexture.add(actor)
            }
            byTexture.forEach { entry ->
                val texture = entry.key
                val sorted = entry.value
                glUse(texture) {
                    val provider = object : BillboardsProvider() {
                        override fun flushPositions(position: FloatBuffer) {
                            sorted.forEach { actor ->
                                val spatial = actor.readSpatial()
                                position.put(spatial.position.x)
                                position.put(0.5f)
                                position.put(spatial.position.z)
                            }
                        }

                        override fun size() = sorted.size
                    }
                    billboardsTechnique.instance(
                        provider,
                        identityM, texture, 1f, 1f,
                        updateScale = false, updateTransparency = false)
                }
            }
        }
    }

    private fun drawFrame() {
        glClear()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        val partitionIds = camera.position.partitionId().selectNeighbours()
        val actors = repository.withPartitionIds(partitionIds).toList()
        val fields = mutableListOf<Actor>()
        val balls = mutableListOf<Actor>()
        for (actor in actors) {
            if (actor.hasComponent(ComponentType.MESH)) {
                fields.add(actor)
            } else if (actor.hasComponent(ComponentType.BILLBOARD)) {
                balls.add(actor)
            }
        }
        skyboxTechnique.skybox(camera) // do not like culling :(
        glCulling {
            glDepthTest {
                drawFields(fields)
                drawBillboards(balls)
            }
        }
    }
}
