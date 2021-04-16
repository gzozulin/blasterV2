package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.Object
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique

private const val TEMPL_SIMPLE_VERT = """
$VERSION
$PRECISION_HIGH
$DECLARATIONS_VERT

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

out vec2 vTexCoord;

%DECL%

void main() {
    %VRBL%
    
    vTexCoord = aTexCoord;
    
    mat4 mvp =  %PROJ% * %VIEW% * %MODEL%;
    gl_Position = mvp * vec4(aPosition, 1.0);
}
"""

private const val TEMPL_SIMPLE_FRAG = """
$VERSION
$PRECISION_HIGH
$DECLARATIONS_FRAG

in vec2 vTexCoord;

%DECL%

layout (location = 0) out vec4 oFragColor;

void main() {
    %VRBL%
    
    oFragColor = %COLOR%;
}
"""

enum class SimpleVarrying {
    vTexCoord
}

private fun List<String>.toSrc() = distinct().joinToString("\n")

fun String.substituteDeclVrbl(vararg expression: Expression<*>): String {
    val declarations = mutableListOf<String>()
    val variables = mutableListOf<String>()
    expression.forEach {
        declarations += it.decl()
        variables += it.vrbl()
    }
    return replace("%DECL%", declarations.toSrc())
        .replace("%VRBL%", variables.toSrc())
}

class FlatTechnique(private val modelM: Expression<mat4>,
                    private val viewM: Expression<mat4>,
                    private val projM: Expression<mat4>,
                    private val color: Expression<vec4> = constv4(vec4(1f))) : GlResource() {

    private val program: GlProgram

    init {
        val vertSrc = TEMPL_SIMPLE_VERT
            .substituteDeclVrbl(modelM, viewM, projM)
            .replace("%MODEL%", modelM.expr())
            .replace("%VIEW%", viewM.expr())
            .replace("%PROJ%", projM.expr())
        val fragSrc = TEMPL_SIMPLE_FRAG
            .substituteDeclVrbl(color)
            .replace("%COLOR%", color.expr())
        program = GlProgram(
            GlShader(GlShaderType.VERTEX_SHADER, vertSrc),
            GlShader(GlShaderType.FRAGMENT_SHADER, fragSrc))
        addChildren(program)
    }

    fun draw(draw: () -> Unit) {
        glBind(program) {
            projM.submit(program)
            viewM.submit(program)
            draw.invoke()
        }
    }

    fun instance(mesh: GlMesh, vararg bindables: GlBindable) {
        glBind(mesh) {
            glBind(bindables.toList()) {
                renderInstance(mesh)
            }
        }
    }

    fun instance(obj: Object) {
        glBind(obj) {
            renderInstance(obj.mesh)
        }
    }

    private fun renderInstance(mesh: GlMesh) {
        modelM.submit(program)
        color.submit(program)
        program.draw(mesh)
    }
}

private val window = GlWindow()

private val camera = Camera()
private val controller = ControllerFirstPerson(position = vec3().front())
private val wasdInput = WasdInput(controller)

private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")
private val diffuse1 = texturesLib.loadTexture("textures/font.png")
private val diffuse2 = texturesLib.loadTexture("textures/smoke.png")
private val diffuse3 = texturesLib.loadTexture("textures/blood_moss.jpg")
private val diffuse4 = texturesLib.loadTexture("textures/floor.jpg")
private val rectangle = GlMesh.rect()

private var mouseLook = false

private var proportion = 0f
private var shift = 0f
private var delta = 0.01f

private val unifShiftUV = unifv2(vec2(0f))
private val unifProp1 = unifv4(vec4(proportion))
private val unifProp2 = unifv4(vec4(1f - proportion))

private val texCoords = varying<vec2>(SimpleVarrying.vTexCoord.name)
private val tiledCoords = cachev2(tile(texCoords, constv2i(vec2i(15, 15)), constv2i(vec2i(16, 16))))
private val tiledShiftedCoords = cachev2(add(tiledCoords, unifShiftUV))

private val identityM = constm4(mat4().identity())
private val unifViewM = unifm4 { camera.calculateViewM() }
private val unifProjM = unifm4 { camera.projectionM }

private val unifFont = unifsampler(diffuse1)
private val unifDiffuse2 = unifsampler(diffuse2)
private val unifDiffuse3 = unifsampler(diffuse3)
private val unifDiffuse4 = unifsampler(diffuse4)

private val flatTechnique = FlatTechnique(
    identityM, unifViewM, unifProjM,
    mul(
        add(
            mul(tex(tiledShiftedCoords, unifFont), unifProp1),
            mul(tex(texCoords, unifDiffuse2), unifProp2)
        ),
        add(
            mul(tex(texCoords, unifDiffuse3), unifProp1),
            mul(tex(texCoords, unifDiffuse4), unifProp2)
        )
    )
)

fun main() {
    window.create(resizables = listOf(camera), isHoldingCursor = false, isFullscreen = true) {
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
        glUse(flatTechnique, skyboxTechnique, rectangle, diffuse1, diffuse2, diffuse3, diffuse4) {
            window.show {
                glBind(rectangle, diffuse1, diffuse2, diffuse3, diffuse4) {
                    glClear()
                    controller.apply { position, direction ->
                        camera.setPosition(position)
                        camera.lookAlong(direction)
                    }
                    skyboxTechnique.skybox(camera)
                    flatTechnique.draw {
                        flatTechnique.instance(rectangle)
                    }
                    proportion += delta
                    if (proportion < 0f || proportion > 1f) {
                        delta = -delta
                    }
                    unifProp1.value = vec4(proportion)
                    unifProp2.value = vec4(1f - proportion)
                    shift += 0.001f
                    unifShiftUV.value = vec2(shift,cosf(shift * 10) * 0.1f)
                }
            }
        }
    }
}