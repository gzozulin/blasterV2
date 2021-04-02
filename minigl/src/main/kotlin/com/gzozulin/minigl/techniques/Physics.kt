package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.modelLib
import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.*
import org.joml.Matrix4f

@Deprecated("Use assembly instead!")
class StaticPbrTechnique: GlResource() {
    private val program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")

    init {
        addChildren(program)
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, lights: () -> Unit, meshes: () -> Unit) {
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M.label,    camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M.label,    camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE.label,       camera.position)
            lights.invoke()
            program.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT.label, pointLightCnt)
            //program.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT.label, dirLightCnt)
            meshes.invoke()
        }
        pointLightCnt = 0
        dirLightCnt = 0
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        if (light is PointLight) {
            modelM.getColumn(3, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR.label, pointLightCnt, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY.label, pointLightCnt, light.color)
            pointLightCnt++
        } else {
            TODO()
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {
        glBind(mesh, material.albedo, material.normal, material.metallic, material.roughness, material.ao) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M.label,           modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ALBEDO.label,    material.albedo)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL.label,    material.normal)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_METALLIC.label,  material.metallic)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ROUGHNESS.label, material.roughness)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_AO.label,        material.ao)
            program.draw(mesh)
        }
    }
}

private val window = GlWindow()

private val camera = Camera()
private val controller = ControllerFirstPerson(position = vec3(0f, 2.5f, 4f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/miramar")
private val pbrTechnique = StaticPbrTechnique()

private val obj = modelLib.load("models/mandalorian/mandalorian").first()
    .copy(material = texturesLib.loadPbr("models/mandalorian"))

private val light = PointLight(vec3(3f), vec3(25f), 100f)

private val objMatrix = mat4().identity().scale(obj.aabb.scaleTo(5f))
private val lightMatrix = mat4().identity().translate(vec3(3f))

private var mouseLook = false

fun main() {
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
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
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        glUse(skyboxTechnique, pbrTechnique, obj) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glCulling {
                    glDepthTest {
                        pbrTechnique.draw(camera, lights = {
                            pbrTechnique.light(light, lightMatrix)
                        }, meshes = {
                            pbrTechnique.instance(obj.mesh, objMatrix, obj.pbr())
                        })
                    }
                }
            }
        }
    }
}