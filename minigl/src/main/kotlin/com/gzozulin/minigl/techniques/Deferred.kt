package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.meshLib
import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.*
import org.joml.Matrix4f

const val MAX_LIGHTS = 128

class DeferredTechnique : GlResource() {
    private val programGeomPass: GlProgram = shadersLib.loadProgram(
        "shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
    private val programLightPass = shadersLib.loadProgram(
        "shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")

    private val quadMesh = GlMesh.rect()

    private val framebuffer = GlFrameBuffer()

    private lateinit var positionStorage: GlTexture
    private lateinit var normalStorage: GlTexture
    private lateinit var diffuseStorage: GlTexture

    private lateinit var matAmbShineStorage: GlTexture // ambient + shine
    private lateinit var matDiffTranspStorage: GlTexture // diffuse + transparency
    private lateinit var matSpecularStorage: GlTexture

    private lateinit var depthBuffer: GlRenderBuffer

    init {
        addChildren(programGeomPass, programLightPass, quadMesh, framebuffer)
    }

    override fun release() {
        releaseFrame()
        super.release()
    }

    private fun releaseFrame() {
        positionStorage.release()
        normalStorage.release()
        diffuseStorage.release()
        matAmbShineStorage.release()
        matDiffTranspStorage.release()
        matSpecularStorage.release()
        depthBuffer.release()
    }

    fun resize(width: Int, height: Int) {
        checkReady()
        if (::positionStorage.isInitialized) {
            releaseFrame()
        }
        positionStorage = GlTexture(
            unit = 0,
            width = width, height = height, internalFormat = backend.GL_RGBA16F,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        positionStorage.use()
        normalStorage = GlTexture(
            unit = 1,
            width = width, height = height, internalFormat = backend.GL_RGB16F,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        normalStorage.use()
        diffuseStorage = GlTexture(
            unit = 2,
            width = width, height = height, internalFormat = backend.GL_RGBA,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_UNSIGNED_BYTE)
        diffuseStorage.use()
        matAmbShineStorage = GlTexture(
            unit = 3,
            width = width, height = height, internalFormat = backend.GL_RGBA16F,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        matAmbShineStorage.use()
        matDiffTranspStorage = GlTexture(
            unit = 4,
            width = width, height = height, internalFormat = backend.GL_RGBA16F,
            pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        matDiffTranspStorage.use()
        matSpecularStorage = GlTexture(
            unit = 5,
            width = width, height = height, internalFormat = backend.GL_RGB16F,
            pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        matSpecularStorage.use()
        depthBuffer = GlRenderBuffer(width = width, height = height)
        depthBuffer.use()
        glBind(framebuffer, positionStorage, normalStorage, diffuseStorage,
            matAmbShineStorage, matDiffTranspStorage, matSpecularStorage, depthBuffer) {
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT3, matAmbShineStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT4, matDiffTranspStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT5, matSpecularStorage)
            framebuffer.setOutputs(intArrayOf(
                backend.GL_COLOR_ATTACHMENT0, backend.GL_COLOR_ATTACHMENT1, backend.GL_COLOR_ATTACHMENT2,
                backend.GL_COLOR_ATTACHMENT3, backend.GL_COLOR_ATTACHMENT4, backend.GL_COLOR_ATTACHMENT5
            ))
            framebuffer.setRenderBuffer(backend.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
            glBind(programLightPass) {
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_POSITION, positionStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL, normalStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuseStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_AMB_SHINE, matAmbShineStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_DIFF_TRANSP, matDiffTranspStorage)
                programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_SPECULAR, matSpecularStorage)
            }
        }
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, instances: () -> Unit, lights: () -> Unit) {
        checkReady()
        pointLightCnt = 0
        dirLightCnt = 0
        glBind(framebuffer) {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            glBind(programGeomPass) {
                programGeomPass.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
                programGeomPass.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
                instances.invoke()
            }
        }
        glBind(programLightPass, quadMesh, depthBuffer,
            positionStorage, normalStorage, diffuseStorage,
            matAmbShineStorage, matDiffTranspStorage, matSpecularStorage) {
            lights.invoke()
            programLightPass.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            programLightPass.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT, pointLightCnt)
            programLightPass.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT, dirLightCnt)
            programLightPass.draw(indicesCount = quadMesh.indicesCount)
        }
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        checkReady()
        if (light.point) {
            modelM.getColumn(3, lightVectorBuf)
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, pointLightCnt, lightVectorBuf)
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, pointLightCnt, light.intensity)
            pointLightCnt++
        } else {
            modelM.getRow(2, lightVectorBuf) // transpose
            lightVectorBuf.negate() // -Z
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, dirLightCnt, lightVectorBuf)
            programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, dirLightCnt, light.intensity)
            dirLightCnt++
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, diffuse: GlTexture, material: Material) {
        checkReady()
        glBind(mesh, diffuse) {
            programGeomPass.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_AMBIENT, material.ambient)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_DIFFUSE, material.diffuse)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_SPECULAR, material.specular)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_SHINE, material.shine)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_TRANSP, material.transparency)
            programGeomPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            programGeomPass.draw(indicesCount = mesh.indicesCount)
        }
    }
}

private val console = Console()

private val camera = Camera()
private val controller = Controller(position = vec3(1f, 4f, 6f), velocity = 0.1f)
private val wasdInput = WasdInput(controller)

private val light = Light(vec3(1f), false)
private val light2 = Light(vec3(1f), false)

private val objMatrix = mat4().identity()
private val lightMatrix = mat4().identity().lookAlong(vec3(1f, -1f, -1f), vec3().up())
private val lightMatrix2 = mat4().identity().lookAlong(vec3(-1f, -1f, -1f), vec3().up())

private val deferredTechnique = DeferredTechnique()
private val skyboxTechnique = SkyboxTechnique("textures/miramar")
private val textTechnique = TextTechnique()
private val mesh = meshLib.loadMesh("models/house/low.obj") { println("progress: $it") }.mesh
private val diffuse = texturesLib.loadTexture("models/house/house_diffuse.png")

private var mouseLook = false

fun main() {
    val window = GlWindow()
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
            deferredTechnique.resize(width, height)
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
        glUse(deferredTechnique, skyboxTechnique, textTechnique, mesh, diffuse) {
            console.success("ready to show..")
            window.show {
                glClear()
                controller.apply { position, direction ->
                    camera.setPosition(position)
                    camera.lookAlong(direction)
                }
                skyboxTechnique.skybox(camera)
                glDepthTest {
                    glCulling {
                        deferredTechnique.draw(camera, instances = {
                            deferredTechnique.instance(mesh, objMatrix, diffuse, Material.CONCRETE)
                        }, lights = {
                            deferredTechnique.light(light, lightMatrix)
                            deferredTechnique.light(light2, lightMatrix2)
                        })
                    }
                }
                console.tick()
                textTechnique.draw {
                    console.render { position, text, color, scale ->
                        textTechnique.text(position, text, color, scale)
                    }
                }
            }
        }
    }
}