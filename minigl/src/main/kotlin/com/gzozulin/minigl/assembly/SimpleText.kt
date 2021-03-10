package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
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
    val text: String
    val color: col3
    var visibility: SpanVisibility
}

data class TextPage<T : TextSpan>(val spans: List<T>) {
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

class SimpleTextTechnique(
    private val fontDescription: FontDescription = FontDescription(),
    private var windowWidth: Int, private var windowHeight: Int) : GlResource() {

    private val font = texturesLib.loadTexture(fontDescription.textureFilename)
    private val rect = GlMesh.rect(0f, fontDescription.letterSizeU, 0f, fontDescription.letterSizeV)

    private val texCoord = varying<vec2>(SimpleVarrying.vTexCoord.name)

    private val cursor = mat4().identity()

    private val modelM = unifm4(cursor)

    private val unifProj = unifm4(mat4().identity())
    private val unifCenter = unifm4(mat4().identity())

    init {
        resize(windowWidth, windowHeight)
    }

    private val tileUV = vec2i(0)
    private val unifTileUV = unifv2i(tileUV)
    private val texCoordTiled = tile(texCoord, unifTileUV, constv2i(vec2i(fontDescription.fontCntU, fontDescription.fontCntV)))

    private val uniformColor = unifv4()
    private val fontCheck = near(tex(texCoordTiled, unifsampler(font)), constv4(vec4(1f)))
    private val result = ifexp(fontCheck, uniformColor, discard())

    private val simpleTechnique = SimpleTechnique(modelM, unifCenter, unifProj, result)

    init {
        addChildren(simpleTechnique, rect, font)
    }

    fun resize(width: Int, height: Int) {
        windowWidth = width
        windowHeight = height
        unifProj.value!!.set(mat4().ortho(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat(), -1f, 1f))
        unifCenter.value!!.set(mat4().identity().translate(vec3(windowWidth / 2f, windowHeight / 2f, 0f)))
    }

    private fun updateCursor(line: Int, letter: Int) {
        cursor.identity().setTranslation(
            letter * fontDescription.letterSizeU * fontDescription.fontStepScaleU - windowWidth/2f,
            windowHeight/2f - fontDescription.letterSizeV * (line + 1) * fontDescription.fontStepScaleV,
            0f)
        modelM.value = cursor
    }

    private fun updateGlyph(character: Char) {
        tileUV.x = character.toInt() % fontDescription.fontCntU
        tileUV.y = fontDescription.fontCntV - character.toInt() / fontDescription.fontCntV - 1
        unifTileUV.value = tileUV
    }

    private fun updateSpan(span: TextSpan) {
        uniformColor.value = vec4(span.color, 1f)
    }

    fun <T : TextSpan> pageCentered(page: TextPage<T>, centerLine: Int, linesCnt: Int) {
        val fromLine = max(centerLine - linesCnt, 0)
        val toLine = fromLine + linesCnt * 2
        pageRange(page, fromLine, toLine)
    }

    private fun <T : TextSpan> pageRange(page: TextPage<T>, fromLine: Int = 0, toLine: Int = Int.MAX_VALUE) {
        check(fromLine in 0 until toLine) { "wtf?!" }
        var currentLetter = 0
        var currentLine = 0
        glBind(font) {
            simpleTechnique.draw {
                for (span in page.spans) {
                    if (span.visibility == SpanVisibility.GONE) {
                        continue
                    }
                    updateSpan(span)
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
                            return@draw
                        }
                        if (span.visibility == SpanVisibility.VISIBLE) {
                            updateCursor(currentLine - fromLine, currentLetter)
                            updateGlyph(character)
                            simpleTechnique.instance(rect)
                        }
                        currentLetter ++
                    }
                }
            }
        }
    }
}

private val window = GlWindow()

private data class SimpleSpan(
    override val text: String,
    override val color: col3,
    override var visibility: SpanVisibility = SpanVisibility.VISIBLE) : TextSpan

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
    fontScaleU = 0.296875f, fontScaleV = 0.40625f,
    fontStepScaleU = 0.45f, fontStepScaleV = 0.75f)

private val simpleTextTechnique = SimpleTextTechnique(fontDescription, window.width, window.height)

fun main() {
    window.create(isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            simpleTextTechnique.resize(width, height)
        }
        glUse(simpleTextTechnique) {
            window.show {
                glClear(col3().ltGrey())
                val centerLine = examplePage.findLineNo(occasionSpan)
                simpleTextTechnique.pageCentered(examplePage, centerLine, 1000)
            }
        }
    }
}