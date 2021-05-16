package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreateCubeMap
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.WasdInput

private const val vertexSrc = """
$VERT_SHADER_HEADER

layout (location = 0) in vec3 aPosition;

out vec3 vTexCoord;

void main() {
    vTexCoord = aPosition;
    vec4 pos = %PROJ% * %VIEW% * vec4(aPosition, 1.0);

    // Setting the vertex z == w, "The depth of a fragment in the Z-buffer is computed as z/w.
    // If z==w, then you get a depth of 1.0, or 100%."
    // [explanation; https://www.gamedev.net/forums/topic/577973-skybox-and-depth-buffer/4683037/]
    gl_Position = pos.xyzz;
}
"""

private const val fragmentSrc = """
$FRAG_SHADER_HEADER

in vec3 vTexCoord;

layout (location = 0) out vec4 oFragColor;

void main() {
    oFragColor = %COLOR%;
}
"""

/* private val onlyRotationM = mat3()
    private val noTranslationM = mat4()

    fun skybox(camera: Camera) {
        checkReady()
        onlyRotationM.set(camera.calculateViewM())
        noTranslationM.set(onlyRotationM)
        glBind(program, cube.mesh, diffuse) {
            program.setUniform(GlUniform.UNIFORM_PROJ_M.label, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_VIEW_M.label, noTranslationM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE.label, diffuse)
            program.draw(indicesCount = cube.mesh.indicesCount)
        }
    }
}*/

data class TechniqueSkybox(val camera: Camera, val color: Expression<vec4>) {

    private val onlyRotationM = mat3()
    private val noTranslationM = mat4()

    internal val unifProjM = unifm4 { camera.projectionM }
    internal val unifViewM = unifm4 { noTranslationM.set(onlyRotationM.set(camera.calculateViewM())) }

    private val vertShader = GlShader(
        backend.GL_VERTEX_SHADER,
        glExprSubstitute(vertexSrc, mapOf(
            "VIEW" to unifProjM,
            "PROJ" to unifViewM))
    )

    private val fragShader = GlShader(
        backend.GL_FRAGMENT_SHADER,
        glExprSubstitute(fragmentSrc, mapOf("COLOR" to color))
    )

    internal val program = GlProgram(vertShader, fragShader)

    internal val cube = libWavefrontCreate("models/cube/cube")
        .objects.first().mesh
}

fun glTechSkyboxUse(techniqueSkybox: TechniqueSkybox, callback: Callback) {
    glProgramUse(techniqueSkybox.program) {
        glMeshUse(techniqueSkybox.cube) {
            callback.invoke()
        }
    }
}

fun glTechSkyboxDraw(techniqueSkybox: TechniqueSkybox) {
    glProgramBind(techniqueSkybox.program) {
        techniqueSkybox.unifProjM.submit(techniqueSkybox.program)
        techniqueSkybox.unifViewM.submit(techniqueSkybox.program)
        techniqueSkybox.color.submit(techniqueSkybox.program)
        glMeshBind(techniqueSkybox.cube) {
            glDrawTriangles(techniqueSkybox.program, techniqueSkybox.cube)
        }
    }
}

private val window = GlWindow()

private var mouseLook = false
private val camera = Camera()
private val controller = ControllerFirstPerson()
private val wasdInput = WasdInput(controller)

private val cubeMap = libTextureCreateCubeMap("textures/snowy")
private val texCoords = namedTexCoordsV3()
private val cubeSampler = unifsq(cubeMap)
private val color = texq(texCoords, cubeSampler)

private val techniqueSkybox = TechniqueSkybox(camera, color)

fun main() {
    window.create(isHoldingCursor = false, isFullscreen = true) {
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
        glTechSkyboxUse(techniqueSkybox) {
            glTextureUse(cubeMap) {
                window.show {
                    glClear()
                    controller.apply { position, direction ->
                        camera.setPosition(position)
                        camera.lookAlong(direction)
                    }
                    glTextureBind(cubeMap) {
                        glTechSkyboxDraw(techniqueSkybox)
                    }
                }
            }
        }
    }
}