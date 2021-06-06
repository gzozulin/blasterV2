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

private val window = GlWindow(isHoldingCursor = false, isFullscreen = true)

private var mouseLook = false
private val camera = Camera(window)
private val controller = ControllerFirstPerson()
private val wasdInput = WasdInput(controller)

private val cubeMap1 = libTextureCreateCubeMap("textures/miramar")
private val cubeSampler1 = unifsq(cubeMap1)

private val cubeMap2 = libTextureCreateCubeMap("textures/snowy")
private val cubeSampler2 = unifsq(cubeMap2)

private val texCoords = namedTexCoordsV3()

private val color1 = samplerq(texCoords, cubeSampler1)
private val color2 = samplerq(texCoords, cubeSampler2)

private var direction = true
private var timer = 0
private var proportion = 0.5f
private val unifProportion1 = uniff { proportion }
private val unifProportion2 = uniff { 1f - proportion }
private val color = addv4(mulv4(color1, v4val(unifProportion1)), mulv4(color2, v4val(unifProportion2)))

private val techniqueSkybox = TechniqueSkybox(camera, color)

fun main() {
    window.create {
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
            glTextureUse(cubeMap1) {
                glTextureUse(cubeMap2) {
                    window.show {
                        timer++
                        proportion += if (direction) 0.01f else -0.01f
                        if (timer == 100) {
                            direction = !direction
                            timer = 0
                        }
                        glClear()
                        controller.apply { position, direction ->
                            camera.setPosition(position)
                            camera.lookAlong(direction)
                        }
                        glTextureBind(cubeMap1) {
                            glTextureBind(cubeMap2) {
                                glTechSkyboxDraw(techniqueSkybox)
                            }
                        }
                    }
                }
            }
        }
    }
}