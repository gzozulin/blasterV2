package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.minigl.assembly.SpanVisibility
import com.gzozulin.minigl.assembly.TextSpan
import com.gzozulin.minigl.gl.col3
import org.antlr.v4.runtime.Token
import java.io.File
import java.util.concurrent.TimeUnit

typealias DeclCtx = KotlinParser.DeclarationContext

data class ScenarioNode(val order: Int, val file: File, val identifier: String,
                                val timeout: Long = TimeUnit.SECONDS.toMillis(1),
                                val children: List<ScenarioNode>? = null)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(val order: Int, override val text: String, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan