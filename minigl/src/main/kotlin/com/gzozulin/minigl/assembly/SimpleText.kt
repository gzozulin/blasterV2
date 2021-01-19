package com.gzozulin.minigl.assembly

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*

private val propIdentityM = propm4(mat4().identity())
private val propProjM = propm4(mat4().ortho(-1f, 1f, -1f, 1f, -1f, 1f))
private val propUVCnt = propi(16)

private val unifTileU = unifi(0)
private val unifTileV = unifi(0)

class SimpleTextTechnique : SimpleTechnique(
    propIdentityM, propIdentityM, propProjM,
    tileU = unifTileU,
    tileV = unifTileV,
    cntU = propUVCnt,
    cntV = propUVCnt
) {
    private val rect = GlMesh.rect(-1f, 1f, 1f, -1f)
    private val font = texturesLib.loadTexture("textures/font.png")

    init {
        addChildren(rect, font)
    }

    fun page(page: TextPage) {
        draw {
            for (line in page.lines) {
                for (fragment in line.fragments) {
                    instance(rect)
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