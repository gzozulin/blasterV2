package com.gzozulin.miniui

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.techniques.SimpleTechnique
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT

typealias ClickCallback = () -> Unit

// todo comp animator background?

private interface Component
private data class CompBackground(val diffuse: GlTexture) : Component
private data class CompClickable(val clicked: GlTexture, val callback: ClickCallback) : Component

private class Widget(
    val pos: vec2 = vec2(0f),
    val size: vec2 = vec2(1f),
    val compBackground: CompBackground? = null,
    val compClickable: CompClickable? = null,
    val children: MutableList<Widget> = mutableListOf()
)

private class RenderingSystem : GlResource() {
    private val simpleTechnique = SimpleTechnique()
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

private class InputSystem {
    private var width = 0
    private var height = 0

    private val relativeCursorPos = vec2()

    private val matrixStack = MatrixStack()

    fun onResize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun onPosition(position: vec2) {
        relativeCursorPos.x = lerpf(-1f, 1f, position.x / width)
        relativeCursorPos.y = lerpf(1f, -1f, position.y / height)
    }

    fun onButton(button: Int, pressed: Boolean, root: Widget) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && pressed) {
            checkClicked(root)
        }
    }

    private fun checkClicked(root: Widget) {

    }
}

private val window = GlWindow()

private val utahTeapot = texturesLib.loadTexture("textures/utah.jpg")

private val renderingSystem = RenderingSystem()
private val inputSystem = InputSystem()

private val teapot3 = Widget(pos = vec2(-0.5f), size = vec2(-.5f), compBackground = CompBackground(utahTeapot))
private val teapot2 = Widget(pos = vec2(0.5f), size = vec2(.5f), compBackground = CompBackground(utahTeapot))
private val guiRoot = Widget(size = vec2(1f), compBackground = CompBackground(utahTeapot), children = mutableListOf(teapot2, teapot3))

fun main() {
    window.create(isHoldingCursor = false) {
        window.resizeCallback = { width, height ->
            inputSystem.onResize(width, height)
        }
        window.positionCallback = { position ->
            inputSystem.onPosition(position)
        }
        window.buttonCallback = { button, pressed ->
            inputSystem.onButton(button, pressed, guiRoot)
        }
        glUse(renderingSystem, utahTeapot) {
            window.show {
                glClear()
                renderingSystem.render(guiRoot)
            }
        }
    }
}