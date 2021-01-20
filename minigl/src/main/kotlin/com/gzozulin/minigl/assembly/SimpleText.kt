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

    private val constTexCoordTiled = tile(constv2(SimpleVarrying.vTexCoord.name), unifTileUV, propv2i(vec2i(16)))
    private val unifFont = tex(constTexCoordTiled, unifsampler(font))
    private val unifFloor = tex(constTexCoordTiled, unifsampler(floor))

    private val propBool = propb(true)
    private val ifExp = ifexpv4(propBool, unifFont, unifFloor)

    private val simpleTechnique = SimpleTechnique(propIdentityM, propIdentityM, propProjM, color = ifExp)

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
                simpleTextTechnique.page(examplePage)
            }
        }
    }
}