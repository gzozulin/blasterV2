package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.minigl.assembly.SpanVisibility
import com.gzozulin.minigl.assembly.TextSpan
import com.gzozulin.minigl.gl.col3
import com.gzozulin.minigl.gl.cyan
import com.gzozulin.minigl.gl.green
import com.gzozulin.minigl.gl.red

private val red = col3().red()
private val cyan = col3().cyan()
private val green = col3().green()

fun OrderedToken.toSpan(): TextSpan {
    val color = when (token.type) {
        KotlinLexer.IF -> red
        KotlinLexer.CLASS -> green
        else -> cyan
    }
    return TextSpan(token.text, color, visibility = SpanVisibility.GONE)
}