package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.minigl.gl.*
import org.antlr.v4.runtime.Token

private val white = col3().white()
private val red = col3().red()
private val cyan = col3().cyan()
private val green = col3().green()

fun Token.color(): col3 = when (type) {
    KotlinLexer.IF -> red
    KotlinLexer.CLASS -> green
    KotlinLexer.FUN -> cyan
    else -> white
}