package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique

private const val VERSION = "#version 300 es"
private const val PRECISION_HIGH = "precision highp float;"

private const val TEMPL_SIMPLE_VERT = """
$VERSION
$PRECISION_HIGH

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

out vec2 vTexCoord;

%DECL%

void main() {
    %EXPR%

    float tileSideX = 1.0 / float(%TILES_CNT_U%);
    float tileStartX = float(%TILE_U%) * tileSideX + %SHIFT_U%;
    vTexCoord.x = tileStartX + aTexCoord.x * tileSideX;
    
    float tileSideY = 1.0 / float(%TILES_CNT_V%);
    float tileStartY = float(%TILE_V%) * tileSideY + %SHIFT_V%;
    vTexCoord.y = tileStartY + aTexCoord.y * tileSideY;
    
    mat4 mvp =  %PROJ% * %VIEW% * %MODEL%;
    gl_Position = mvp * vec4(aPosition, 1.0);
}
"""

private const val TEMPL_SIMPLE_FRAG = """
$VERSION
$PRECISION_HIGH

in vec2 vTexCoord;

%DECL%

layout (location = 0) out vec4 oFragColor;

void main() {
    %EXPR%
    oFragColor = %COLOR%;
}
"""

private fun List<String>.toSrc() = distinct().joinToString("\n")

private class SimpleTechnique(private val modelM: Expression<mat4>,
                              private val viewM: Expression<mat4>,
                              private val projM: Expression<mat4>,
                              private val color: Expression<vec4>,
                              private val tileU: Expression<Int> = propi(0),
                              private val tileV: Expression<Int> = propi(0),
                              private val tilesCntU: Expression<Int> = propi(1),
                              private val tilesCntV: Expression<Int> = propi(1),
                              private val shiftU: Expression<Float> = propf(0f),
                              private val shiftV: Expression<Float> = propf(0f)) : GlResource() {

    private val program: GlProgram

    init {
        val vertDecl = modelM.decl() + viewM.decl() + projM.decl() +
                tileU.decl() + tileV.decl() + tilesCntU.decl() + tilesCntV.decl() +
                shiftU.decl() + shiftV.decl()
        val vertDeclSrc = vertDecl.toSrc()
        val vertExpr = modelM.expr() + modelM.expr() + modelM.expr() +
                tileU.expr() + tileV.expr() + tilesCntU.expr() + tilesCntV.expr() +
                shiftU.expr() + shiftV.expr()
        val vertExprSrc = vertExpr.toSrc()
        val vertSrc = TEMPL_SIMPLE_VERT
            .replace("%DECL%", vertDeclSrc)
            .replace("%EXPR%", vertExprSrc)
            .replace("%MODEL%", modelM.name)
            .replace("%VIEW%", viewM.name)
            .replace("%PROJ%", projM.name)
            .replace("%TILE_U%", tileU.name)
            .replace("%TILE_V%", tileV.name)
            .replace("%TILES_CNT_U%", tilesCntU.name)
            .replace("%TILES_CNT_V%", tilesCntV.name)
            .replace("%SHIFT_U%", shiftU.name)
            .replace("%SHIFT_V%", shiftV.name)
        val fragDecl = color.decl()
        val fragDeclSrc = fragDecl.toSrc()
        val fragExpr = color.expr()
        val fragExprSrc = fragExpr.toSrc()
        val fragSrc = TEMPL_SIMPLE_FRAG
            .replace("%DECL%", fragDeclSrc)
            .replace("%EXPR%", fragExprSrc)
            .replace("%COLOR%", color.name)
        program = GlProgram(
            GlShader(GlShaderType.VERTEX_SHADER, vertSrc),
            GlShader(GlShaderType.FRAGMENT_SHADER, fragSrc))
        addChildren(program)
    }

    fun draw(draw: () -> Unit) {
        glBind(program) {
            projM.submit(program)
            viewM.submit(program)
            tileU.submit(program)
            tileV.submit(program)
            tilesCntU.submit(program)
            tilesCntV.submit(program)
            shiftU.submit(program)
            shiftV.submit(program)
            draw.invoke()
        }
    }

    fun instance(mesh: GlMesh) {
        modelM.submit(program)
        color.submit(program)
        glBind(mesh) {
            program.draw(mesh)
        }
    }
}

private val window = GlWindow()

private val camera = Camera()
private val controller = Controller(position = vec3().front())
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

private val constTexCoord = constv2("vTexCoord")
private val unifModelM = unifmat4()
private val unifViewM = unifmat4(camera.calculateViewM())
private val unifProjM = unifmat4(camera.projectionM)
private val unifDiffuse1 = unifsampler(diffuse1)
private val unifDiffuse2 = unifsampler(diffuse2)
private val unifDiffuse3 = unifsampler(diffuse3)
private val unifDiffuse4 = unifsampler(diffuse4)
private val unifProp1 = unifvec4(vec4(proportion))
private val unifProp2 = unifvec4(vec4(1f - proportion))
private val unifShiftU = uniff(0f)
private val unifShiftV = uniff(0f)

private val simpleTechnique = SimpleTechnique(
    unifModelM, unifViewM, unifProjM,
    mulv4(
        addv4(
            mulv4(tex(constTexCoord, unifDiffuse1), unifProp1),
            mulv4(tex(constTexCoord, unifDiffuse2), unifProp2)
        ),
        addv4(
            mulv4(tex(constTexCoord, unifDiffuse3), unifProp1),
            mulv4(tex(constTexCoord, unifDiffuse4), unifProp2)
        )
    ),
    tileU = propi(0),
    tileV = propi(0),
    tilesCntU = propi(2),
    tilesCntV = propi(2),
    shiftU = unifShiftU,
    shiftV = unifShiftV
)

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
        glUse(simpleTechnique, skyboxTechnique, rectangle, diffuse1, diffuse2, diffuse3, diffuse4) {
            window.show {
                glBind(diffuse1, diffuse2, diffuse3, diffuse4) {
                    glClear()
                    controller.apply { position, direction ->
                        camera.setPosition(position)
                        camera.lookAlong(direction)
                    }
                    skyboxTechnique.skybox(camera)
                    simpleTechnique.draw {
                        unifModelM.value = mat4().identity()
                        simpleTechnique.instance(rectangle)
                        unifModelM.value = mat4().identity().translate(vec3(1f))
                        simpleTechnique.instance(rectangle)
                    }
                    proportion += delta
                    if (proportion < 0f || proportion > 1f) {
                        delta = -delta
                    }
                    unifProp1.value = vec4(proportion)
                    unifProp2.value = vec4(1f - proportion)
                    shift += 0.001f
                    unifShiftU.value = shift
                    unifShiftV.value = cosf(shift * 10) * 0.1f
                }
            }
        }
    }
}