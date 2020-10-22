package com.gzozulin.minigl.techniques

import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.stbtt_InitFont
import java.io.File
import java.nio.ByteBuffer

private class FontsLib {
    fun loadFont(font: File) {
        val buffer = ByteBuffer.wrap(font.readBytes())
        val info = STBTTFontinfo.create()
        check(stbtt_InitFont(info, buffer)) { "Font initialization failed!" }
    }
}

private val fontsLib = FontsLib()

fun main() {
    fontsLib.loadFont(File("assets/fonts/Roboto-Regular.ttf"))
}