package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*

class SimpleTextTechnique : GlResource() {

    private val rect = GlMesh.rect(-1f, 1f, -1f, 1f)
    private val font = texturesLib.loadTexture("textures/font.png")
    private val floor = texturesLib.loadTexture("textures/floor.jpg")

    private val propIdentityM = propm4(mat4().identity())
    private val propProjM = propm4(mat4().ortho(-1f, 1f, -1f, 1f, -1f, 1f))

    private val unifTileUV = unifv2i(vec2i(0))
    private val texCoord = constv2(SimpleVarrying.vTexCoord.name)
    private val texCoordTiled = tile(texCoord, unifTileUV, propv2i(vec2i(16)))

    private val unifFont = unifsampler(font)
    private val unifFont2 = unifsampler(floor)

    private val color = texv4(texCoordTiled, unifFont)
    private val color2 = texv4(texCoord, unifFont2)

    private val propVec1 = propv4(vec4(1f))

    private val colorResult = ifv4(eq(color, propVec1), color, color2)
    private val filteredResult = filterv4(not(eq(colorResult, propVec1)), colorResult)

    private val simpleTechnique = SimpleTechnique(propIdentityM, propIdentityM, propProjM, filteredResult)

    init {
        addChildren(floor, simpleTechnique, rect, font)
    }

    fun page(page: TextPage) {
        glBind(floor, font) {
            simpleTechnique.draw {
                for (line in page.lines) {
                    for (fragment in line.fragments) {
                        simpleTechnique.instance(rect)
                    }
                }
            }
        }
    }
}

data class TextFragment(val text: String,
                        val background: col3 = col3().black(),
                        val color: col3 = col3().white())

data class TextLine(val fragments: List<TextFragment>)
data class TextPage(val lines: List<TextLine>)

private val examplePage = TextPage(listOf(TextLine(listOf(TextFragment("h")))))

private val simpleTextTechnique = SimpleTextTechnique()

private val window = GlWindow()

fun main() {
    window.create(isHoldingCursor = false) {
        glUse(simpleTextTechnique) {
            window.show {
                glClear()
                simpleTextTechnique.page(examplePage)
            }
        }
    }
}