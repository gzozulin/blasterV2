package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.MeshLib
import com.gzozulin.minigl.assets.ShadersLib
import com.gzozulin.minigl.assets.TexturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.*
import org.joml.Matrix4f

class PbrTechnique(shadersLib: ShadersLib): GlResource() {
    private val program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")

    init {
        addChildren(program)
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, lights: () -> Unit, meshes: () -> Unit) {
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M,    camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M,    camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE,       camera.position)
            lights.invoke()
            program.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT, pointLightCnt)
            //program.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT, dirLightCnt)
            meshes.invoke()
        }
        pointLightCnt = 0
        dirLightCnt = 0
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        if (light.point) {
            modelM.getColumn(3, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, pointLightCnt, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, pointLightCnt, light.intensity)
            pointLightCnt++
        } else {
            TODO()
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {
        glBind(mesh, material.albedo, material.normal, material.metallic, material.roughness, material.ao) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M,           modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ALBEDO,    material.albedo)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL,    material.normal)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_METALLIC,  material.metallic)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ROUGHNESS, material.roughness)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_AO,        material.ao)
            program.draw(mesh)
        }
    }
}

private val window = GlWindow()

private val shadersLib = ShadersLib()
private val texturesLib = TexturesLib()
private val meshLib = MeshLib()

private val camera = Camera()
private val controller = Controller(position = vec3(0f, 2.5f, 4f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = SkyboxTechnique(shadersLib, texturesLib, meshLib, "textures/miramar")
private val pbrTechnique = PbrTechnique(shadersLib)

private val meshData = meshLib.loadMesh("models/mandalorian/mandalorian.obj") { println("loading $it") }
private val material = texturesLib.loadPbr("models/mandalorian")

private val light = Light(vec3(25f), true)

private val objMatrix = mat4().identity().scale(meshData.aabb.scaleTo(5f))
private val lightMatrix = mat4().identity().translate(vec3(3f))

fun main() {
    window.create(isFullscreen = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
        }
        window.deltaCallback = { delta ->
            wasdInput.onCursorDelta(delta)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        glUse(skyboxTechnique, pbrTechnique, meshData.mesh, material) {
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
                            pbrTechnique.instance(meshData.mesh, objMatrix, material)
                        })
                    }
                }
            }
        }
    }
}