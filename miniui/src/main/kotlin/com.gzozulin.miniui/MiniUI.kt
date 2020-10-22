package com.gzozulin.miniui

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.techniques.SimpleTechnique
import com.gzozulin.minigl.techniques.TextTechnique
import org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap

private interface Component
private data class CompBackground(val diffuse: GlTexture) : Component
private data class CompText(val text: String) : Component

private class Widget(
    val pos: vec2 = vec2(0f),
    val size: vec2 = vec2(1f),
    val compBackground: CompBackground? = null,
    val compText: CompText? = null,
    val children: MutableList<Widget> = mutableListOf()
) {
    operator fun get(index: Int): Component =
        when(index) {
            0 -> compBackground!!
            1 -> compText!!
            else -> error("Unknown component!")
        }
}

private val utahTeapot = texturesLib.loadTexture("textures/utah.jpg")

private class RenderingSystem : GlResource() {
    private val simpleTechnique = SimpleTechnique()
    private val textTechnique = TextTechnique()

    private val rectangle = GlMesh.rect()

    private val projM = mat4().ortho(-1f, 1f, -1f, 1f, 0f, 10000f)
    private val viewM = mat4().identity().lookAt(vec3(0f, 0f, 10000f), vec3(0f), vec3().up())

    private val matrixStack = MatrixStack()

    init {
        addChildren(simpleTechnique, rectangle)
    }

    fun render(root: Widget) {
        glDepthTest {
            simpleTechnique.draw(viewM, projM) {
                renderInternal(root)
            }
        }
    }

    fun renderInternal(node: Widget) {
        val nodeMatrix = mat4().identity()
            .translate(node.pos.x, node.pos.y, 1f)
            .scale(node.size.x, node.size.y, 1f)
        matrixStack.pushMatrix(nodeMatrix) {
            for (child in node.children) {
                renderInternal(child)
            }
            if (node.compBackground != null) {
                simpleTechnique.instance(rectangle, node.compBackground.diffuse, matrixStack.peekMatrix())
            }
        }
    }
}

private val window = GlWindow()

private val renderingSystem = RenderingSystem()

private val teapot3 = Widget(pos = vec2(-.5f), size = vec2(-.5f), compBackground = CompBackground(utahTeapot))
private val teapot2 = Widget(pos = vec2(.5f), size = vec2(.5f), compBackground = CompBackground(utahTeapot))
private val guiRoot = Widget(size = vec2(.5f), compBackground = CompBackground(utahTeapot), children = mutableListOf(teapot2, teapot3))

fun main() {
    window.create(isHoldingCursor = false) {
        glUse(renderingSystem, utahTeapot) {
            window.show {
                glClear()
                renderingSystem.render(guiRoot)
            }
        }
    }
}