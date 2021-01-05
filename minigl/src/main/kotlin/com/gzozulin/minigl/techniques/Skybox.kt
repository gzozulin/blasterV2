package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.*
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput

class StaticSkyboxTechnique(skybox: String) : GlResource() {

    private val program = shadersLib.loadProgram(
        "shaders/skybox/skybox.vert", "shaders/skybox/skybox.frag")
    private val diffuse = texturesLib.loadSkybox(skybox)
    private val cube = meshLib.loadMesh("models/cube/cube.obj").mesh

    init {
        addChildren(program, diffuse, cube)
    }

    private val onlyRotationM = mat3()
    private val noTranslationM = mat4()

    fun skybox(camera: Camera) {
        checkReady()
        onlyRotationM.set(camera.calculateViewM())
        noTranslationM.set(onlyRotationM)
        glBind(program, cube, diffuse) {
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_VIEW_M, noTranslationM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            program.draw(indicesCount = cube.indicesCount)
        }
    }
}

private val camera = Camera()
private val controller = Controller()
private val wasdInput = WasdInput(controller)

private val technique = StaticSkyboxTechnique("textures/snowy")

fun main() {
    val window = GlWindow()
    window.create {
        window.deltaCallback = { delta ->
            wasdInput.onCursorDelta(delta)
        }
        glUse(technique) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                technique.skybox(camera)
            }
        }
    }
}