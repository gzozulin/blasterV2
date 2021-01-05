package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.assets.shadersLib
import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.util.*

private val colorFailure = col3().parseColor("ffabab")
private val colorInfo = col3().parseColor("6eb5ff")
private val colorSuccess = col3().parseColor("9ee09e")

private const val textScale = 0.025f

private const val startX = -1 + textScale / 2f
private const val startY = 1 - textScale

private val position = Vector2f()

class StaticTextTechnique(font: String = "textures/font.png") : GlResource() {

    private val program = shadersLib.loadProgram("shaders/text/text.vert", "shaders/text/text.frag")
    private val diffuse = texturesLib.loadTexture(font)
    private val rect = GlMesh.rect()

    private val startBuf = Vector2f()

    init {
        addChildren(program, diffuse, rect)
    }

    fun draw(call: () -> Unit) {
        checkReady()
        glBind(program, diffuse, rect) {
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            call.invoke()
        }
    }

    fun text(start: Vector2f, text: String, color: Vector3f, scale: Float) {
        checkReady()
        text.forEachIndexed { index, ch ->
            startBuf.set(start.x + index * scale, start.y)
            character(ch, startBuf, scale, color)
        }
    }

    private fun character(ch: Char, start: Vector2f, scale: Float, color: Vector3f) {
        program.setUniform(GlUniform.UNIFORM_CHAR_INDEX, ch.toInt())
        program.setUniform(GlUniform.UNIFORM_CHAR_START, start)
        program.setUniform(GlUniform.UNIFORM_CHAR_SCALE, scale)
        program.setUniform(GlUniform.UNIFORM_COLOR, color)
        program.draw(indicesCount = rect.indicesCount)
    }
}

class Console(private val timeout: Long = 1000L) {
    enum class Level { FAILURE, INFO, SUCCESS }
    private data class Line(val text: String, val timestamp: Long, val level: Level)
    private val lines = mutableListOf<Line>()

    private fun line(text: String, level: Level) {
        lines.add(Line(text, System.currentTimeMillis(), level))
    }

    fun failure(text: String) {
        line(text, Level.FAILURE)
    }

    fun info(text: String) {
        line(text, Level.INFO)
    }

    fun success(text: String) {
        line(text, Level.SUCCESS)
    }

    fun tick() {
        val current = System.currentTimeMillis()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            if (current - line.timestamp > timeout) {
                iterator.remove()
            }
        }
    }

    fun render(callback: (position: Vector2f, text: String, color: Vector3f, scale: Float) -> Unit) {
        lines.forEachIndexed { index, line ->
            val col = when (line.level) {
                Level.FAILURE -> colorFailure
                Level.INFO -> colorInfo
                Level.SUCCESS -> colorSuccess
            }
            position.set(startX, startY - textScale * index * 2f)
            callback.invoke(position, line.text, col, textScale)
        }
    }
}

private val random = Random()

private val console = Console()

private val technique = StaticTextTechnique()

fun main() {
    val window = GlWindow()
    window.create {
        window.keyCallback = { key, _ ->
            if (key == GLFW.GLFW_KEY_SPACE) {
                when (random.nextInt(3)) {
                    0 -> console.failure(System.currentTimeMillis().toString())
                    1 -> console.info(System.currentTimeMillis().toString())
                    2 -> console.success(System.currentTimeMillis().toString())
                    else -> throw IllegalArgumentException()
                }
            }
        }
        console.info("ready")
        glUse(technique) {
            window.show {
                glClear(color = col3().blue())
                console.tick()
                technique.draw {
                    console.render { position, text, color, scale ->
                        technique.text(position, text, color, scale)
                    }
                }
            }
        }
    }
}