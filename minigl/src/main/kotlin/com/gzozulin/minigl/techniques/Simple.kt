package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import org.joml.Matrix4f

class StaticSimpleTechnique : GlResource() {
    private val program = shadersLib.loadProgram(
        "shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")

    init {
        addChildren(program)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        draw(camera.calculateViewM(), camera.projectionM, draw)
    }

    fun draw(viewM: mat4, projectionM: mat4, draw: () -> Unit) {
        checkReady()
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, viewM)
            program.setUniform(GlUniform.UNIFORM_PROJ_M, projectionM)
            draw.invoke()
        }
    }

    fun instance(mesh: GlMesh, diffuse: GlTexture, modelM: Matrix4f, color: col3 = col3(1f)) {
        checkReady()
        glBind(mesh, diffuse) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            program.setUniform(GlUniform.UNIFORM_COLOR, color)
            program.draw(indicesCount = mesh.indicesCount)
        }
    }
}

private val window = GlWindow()

private val matrixStack = MatrixStack()
private val camera = Camera()
private val controller = Controller(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")
private val simpleTechnique = StaticSimpleTechnique()
private val diffuse = texturesLib.loadTexture("textures/utah.jpg")
private val rectangle = GlMesh.rect()

private var mouseLook = false

fun main() {
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
        glUse(simpleTechnique, skyboxTechnique, diffuse, rectangle) {
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                simpleTechnique.draw(camera.calculateViewM(), camera.projectionM) {
                    matrixStack.pushMatrix(mat4().identity().translate(vec3().left())) {
                        simpleTechnique.instance(rectangle, diffuse, matrixStack.peekMatrix())
                    }
                    matrixStack.pushMatrix(mat4().identity().translate(vec3().right())) {
                        simpleTechnique.instance(rectangle, diffuse, matrixStack.peekMatrix())
                        matrixStack.pushMatrix(mat4().identity().translate(vec3().up())) {
                            simpleTechnique.instance(rectangle, diffuse, matrixStack.peekMatrix())
                        }
                        matrixStack.pushMatrix(mat4().identity().translate(vec3().down())) {
                            simpleTechnique.instance(rectangle, diffuse, matrixStack.peekMatrix())
                        }
                    }
                }
            }
        }
    }
}