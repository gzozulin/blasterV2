package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.api.constf
import com.gzozulin.minigl.api.constv2i
import com.gzozulin.minigl.api.discard
import com.gzozulin.minigl.api.unifm4
import com.gzozulin.minigl.api.unifv2i
import com.gzozulin.minigl.api.unifv4
import com.gzozulin.minigl.assets.libTextureCreate
import java.lang.Integer.max

data class FontDescription(
    val textureFilename: String = "textures/font.png",
    val fontCntU: Int = 16, val fontCntV: Int = 16,
    val glyphSidePxU: Int = 12, val glyphSidePxV: Int = 12,
    val fontScaleU: Float = 1f, val fontScaleV: Float = 1.5f,
    val fontStepScaleU: Float = 0.6f, val fontStepScaleV: Float = 1f
) {
    val letterSizeU = glyphSidePxU * fontScaleU
    val letterSizeV = glyphSidePxV * fontScaleV
}

enum class SpanVisibility { VISIBLE, INVISIBLE, GONE }

interface TextSpan {
    var text: String
    var color: col3
    var visibility: SpanVisibility
}

data class SimpleSpan(
    override var text: String,
    override var color: col3,
    override var visibility: SpanVisibility = SpanVisibility.VISIBLE
) : TextSpan

open class TextPage<T : TextSpan>(val spans: List<T>) {
    fun findLineNo(lookup: T) = run {
        var currentLine = 0
        for (span in spans) {
            if (span === lookup) {
                return@run currentLine
            }
            if (span.visibility == SpanVisibility.GONE) {
                continue
            }
            for (character in span.text) {
                if (character == '\n') {
                    currentLine++
                }
            }
        }
        error("Span $lookup not found in page")
    }
}

class TechniqueText(
    internal val width: Int, internal val height: Int, val fontDescription: FontDescription = FontDescription()) {

    internal val rect = glMeshCreateRect(0f, fontDescription.letterSizeU, 0f, fontDescription.letterSizeV)
    internal val font = libTextureCreate(fontDescription.textureFilename)
        .copy(minFilter = backend.GL_LINEAR, magFilter = backend.GL_LINEAR)

    internal val cursor = mat4().identity()
    private val viewM = mat4().identity().translate(width / 2f, height / 2f, 0f)
    private val projM = mat4().identity().ortho(0f, width.toFloat(), 0f, height.toFloat(), -1f, 1f)
    private val fullM = mat4().identity()

    private val texCoord = namedTexCoordsV2()
    private val uniformMatrix = unifm4 { fullM.set(projM).mul(viewM).mul(cursor) }

    internal val tileUV = vec2i(0)
    private val unifTileUV = unifv2i { tileUV }
    private val texCoordTiled = tile(texCoord, unifTileUV,
        constv2i(vec2i(fontDescription.fontCntU, fontDescription.fontCntV))
    )

    internal val uniformColor = unifv4()
    private val fontColor = cachev4(sampler(unifs(font), texCoordTiled))
    private val fontCheck = moreexp(getav4(fontColor), constf(0f))
    private val resultColor = setav4(uniformColor, getrv4(fontColor))
    private val result = ifexp(fontCheck, resultColor, discard())

    internal val shadingFlat = ShadingFlat(uniformMatrix, result)
}

fun glTechTextUse(techniqueText: TechniqueText, callback: Callback) {
    glShadingFlatUse(techniqueText.shadingFlat) {
        glMeshUse(techniqueText.rect) {
            glTextureUse(techniqueText.font) {
                callback.invoke()
            }
        }
    }
}

private fun glTechTextUpdateCursor(techniqueText: TechniqueText, line: Int, letter: Int) {
    techniqueText.cursor.identity().setTranslation(
        letter * techniqueText.fontDescription.letterSizeU * techniqueText.fontDescription.fontStepScaleU - techniqueText.width/2f,
        techniqueText.height/2f - techniqueText.fontDescription.letterSizeV * (line + 1.5f) * techniqueText.fontDescription.fontStepScaleV,
        0f)
}

private fun glTechTextUpdateGlyph(techniqueText: TechniqueText, character: Char) {
    techniqueText.tileUV.x = character.toInt() % techniqueText.fontDescription.fontCntU
    techniqueText.tileUV.y = techniqueText.fontDescription.fontCntV - character.toInt() / techniqueText.fontDescription.fontCntV - 1
}

private fun glTechTextUpdateSpan(techniqueText: TechniqueText, span: TextSpan) {
    techniqueText.uniformColor.value = vec4(span.color, 1f)
}

fun <T : TextSpan> glTechTextPage(techniqueText: TechniqueText, page: TextPage<T>) {
    glTechTextPageRange(techniqueText, page, 0, Int.MAX_VALUE)
}

fun <T : TextSpan> glTechTextPageCentered(techniqueText: TechniqueText, page: TextPage<T>, centerLine: Int, linesCnt: Int) {
    val fromLine = max(centerLine - linesCnt, 0)
    val toLine = fromLine + linesCnt * 2
    glTechTextPageRange(techniqueText, page, fromLine, toLine)
}

private fun <T : TextSpan> glTechTextPageRange(techniqueText: TechniqueText, page: TextPage<T>,
                                               fromLine: Int = 0, toLine: Int = Int.MAX_VALUE) {
    check(fromLine in 0 until toLine) { "wtf?!" }
    var currentLetter = 0
    var currentLine = 0
    glBlend {
        glMeshBind(techniqueText.rect) {
            glTextureBind(techniqueText.font) {
                glShadingFlatDraw(techniqueText.shadingFlat) {
                    for (span in page.spans) {
                        if (span.visibility == SpanVisibility.GONE) {
                            continue
                        }
                        glTechTextUpdateSpan(techniqueText, span)
                        for (character in span.text) {
                            if (character == '\n') {
                                currentLetter = 0
                                currentLine++
                                continue
                            }
                            if (currentLine < fromLine) {
                                continue
                            }
                            if (currentLine >= toLine) {
                                return@glShadingFlatDraw
                            }
                            if (span.visibility == SpanVisibility.VISIBLE) {
                                glTechTextUpdateCursor(techniqueText, currentLine - fromLine, currentLetter)
                                glTechTextUpdateGlyph(techniqueText, character)
                                glShadingFlatInstance(techniqueText.shadingFlat, techniqueText.rect)
                            }
                            currentLetter ++
                        }
                    }
                }
            }
        }
    }
}

private val window = GlWindow()

private val occasionSpan = SimpleSpan("What an occasion, we met again!!\n", color = col3().blue())

private val examplePage = TextPage(listOf(
    SimpleSpan("Heeeeeelloooo Greg!!\n", color = col3().red()),
    occasionSpan,
    SimpleSpan("This span is invisible!\n", color = col3().blue(), visibility = SpanVisibility.INVISIBLE),
    SimpleSpan("This span is gone!\n", color = col3().blue(), visibility = SpanVisibility.GONE),
    SimpleSpan("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when\n" +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
            "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of\n" +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing\n" +
            "software like Aldus PageMaker including versions of Lorem Ipsum.\n\n", color = col3().green()),
    SimpleSpan("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when\n" +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
            "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of\n" +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing\n" +
            "software like Aldus PageMaker including versions of Lorem Ipsum.\n\n", color = col3().cyan()),
    SimpleSpan("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when\n" +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
            "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of\n" +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing\n" +
            "software like Aldus PageMaker including versions of Lorem Ipsum.\n\n", color = col3().azure()),
    SimpleSpan("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when\n" +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
            "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of\n" +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing\n" +
            "software like Aldus PageMaker including versions of Lorem Ipsum.\n\n", color = col3().aquamarine()),
    SimpleSpan("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when\n" +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book.\n" +
            "It has survived not only five centuries, but also the leap into electronic typesetting,\n" +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of\n" +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing\n" +
            "software like Aldus PageMaker including versions of Lorem Ipsum.\n\n", color = col3().rose())
))

private val fontDescription = FontDescription(
    textureFilename = "textures/font_hires.png",
    glyphSidePxU = 64, glyphSidePxV = 64,
    fontScaleU = 0.3f, fontScaleV = 0.4f,
    fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

private val techniqueText = TechniqueText(window.width, window.height, fontDescription)

fun main() {
    window.create {
        glTechTextUse(techniqueText) {
            window.show {
                glClear(col3().ltGrey())
                val centerLine = examplePage.findLineNo(occasionSpan)
                glTechTextPageCentered(techniqueText, examplePage, centerLine, 1000)
            }
        }
    }
}
