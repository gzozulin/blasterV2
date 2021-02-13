package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*

private const val FONT_CNT_UV = 16
private const val FONT_GLYPH_SIDE = 12
private const val FONT_SCALE_U = 1f
private const val FONT_SCALE_V = 1.5f
private const val FONT_STEP_U = .6f
private const val FONT_STEP_V = 1f

private const val LETTER_SIZE_U = FONT_GLYPH_SIDE * FONT_SCALE_U
private const val LETTER_SIZE_V = FONT_GLYPH_SIDE * FONT_SCALE_V

enum class SpanVisibility { VISIBLE, INVISIBLE, GONE }

interface TextSpan {
    val text: String
    val color: col3
    var visibility: SpanVisibility
}

data class TextPage<T : TextSpan>(val spans: List<T>)

class SimpleTextTechnique(
    private var windowWidth: Int, private var windowHeight: Int) : GlResource() {

    private val rect = GlMesh.rect(0f, LETTER_SIZE_U, 0f, LETTER_SIZE_V)
    private val font = texturesLib.loadTexture("textures/font.png")

    private val texCoord = varying<vec2>(SimpleVarrying.vTexCoord.name)

    private var currentLetter = 0
    private var currentLine = 0
    private val cursor = mat4().identity()

    private val modelM = unifm4(cursor)

    private val unifProj = unifm4(mat4().identity())
    private val unifCenter = unifm4(mat4().identity())

    init {
        resize(windowWidth, windowHeight)
    }

    private val tileUV = vec2i(0)
    private val unifTileUV = unifv2i(tileUV)
    private val texCoordTiled = tile(texCoord, unifTileUV, constv2i(vec2i(FONT_CNT_UV)))

    private val uniformColor = unifv4()
    private val fontCheck = eq(tex(texCoordTiled, unifsampler(font)), constv4(vec4(1f)))
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

    private fun updateCursor() {
        cursor.identity().setTranslation(
            currentLetter * LETTER_SIZE_U * FONT_STEP_U - windowWidth/2f,
            windowHeight/2f - LETTER_SIZE_V * (currentLine + 1) * FONT_STEP_V,
            0f)
        modelM.value = cursor
    }

    private fun updateGlyph(character: Char) {
        tileUV.x = character.toInt() % FONT_CNT_UV
        tileUV.y = FONT_CNT_UV - character.toInt() / FONT_CNT_UV - 1
        unifTileUV.value = tileUV
    }

    private fun updateSpan(span: TextSpan) {
        uniformColor.value = vec4(span.color, 0f)
    }

    fun <T : TextSpan> page(page: TextPage<T>) {
        currentLetter = 0
        currentLine = 0
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
                        if (span.visibility == SpanVisibility.VISIBLE) {
                            updateCursor()
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

private val examplePage = TextPage(listOf(
    SimpleSpan("Heeeeeelloooo Greg!!\n", color = col3().red()),
    SimpleSpan("What an occasion, we met again!!\n", color = col3().blue()),
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

private val simpleTextTechnique = SimpleTextTechnique(window.width, window.height)

fun main() {
    window.create(isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            simpleTextTechnique.resize(width, height)
        }
        glUse(simpleTextTechnique) {
            window.show {
                glClear(col3().ltGrey())
                simpleTextTechnique.page(examplePage)
            }
        }
    }
}