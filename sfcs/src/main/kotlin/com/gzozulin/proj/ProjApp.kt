package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.gl.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import java.nio.ByteBuffer
import kotlin.streams.toList

private const val FRAMES_PER_SPAN = 1

private typealias DeclCtx = KotlinParser.DeclarationContext

private data class ProjectorNode(val order: Int, val identifier: String, val children: List<ProjectorNode>? = null)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(val order: Int, override val text: String, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

private val chars by lazy { CharStreams.fromFileName(
    "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjApp.kt") }
private val lexer by lazy { KotlinLexer(chars) }
private val tokens by lazy { CommonTokenStream(lexer) }
private val parser by lazy { KotlinParser(tokens) }

// todo: add the aility to "split" the node: i.e. MainClass first, then UtilityClass, then MainClass again
private var orderCnt = 0
private val scenario = listOf(
    ProjectorNode(orderCnt++, "main"),
    ProjectorNode(orderCnt++, "renderScenario"),
    ProjectorNode(orderCnt++, "preparePage"),
    ProjectorNode(orderCnt++, "capturer"),
    ProjectorNode(orderCnt++, "visit"),
    ProjectorNode(orderCnt++, "updateCursor"),
)

private val orderedTokens = mutableListOf<OrderedToken>()
private lateinit var renderedPage: TextPage<OrderedSpan>

private val capturer = GlCapturer()

private val simpleTextTechnique = SimpleTextTechnique(capturer.width, capturer.height)

private var currentFrame = 0
private var currentOrder = 0

fun main() {
    renderScenario()
    preparePage()
    capturer.create {
        glUse(simpleTextTechnique) {
            capturer.show(::onFrame, ::onBuffer)
        }
    }
}

private fun renderScenario() {
    parser.reset()
    val visitor = visit(scenario) { increment ->
        for (renderedToken in increment) {
            if (!orderedTokens.contains(renderedToken)) {
                orderedTokens += renderedToken
            }
        }
    }
    visitor.visitKotlinFile(parser.kotlinFile())
}

private fun visit(nodes: List<ProjectorNode>, result: (increment: List<OrderedToken>) -> Unit) =
    object : KotlinParserBaseVisitor<Unit>() {
        override fun visitDeclaration(decl: DeclCtx) {
            val identifier = decl.identifier()
            for (node in nodes) {
                if (node.identifier == identifier) {
                    if (node.children != null) {
                        result.invoke(decl.predeclare().withOrder(node.order))
                        decl.visitNext(node.children, result)
                        result.invoke(decl.postdeclare().withOrder(node.order))
                    } else {
                        result.invoke(decl.define().withOrder(node.order))
                    }
                }
            }
        }
    }

private fun DeclCtx.identifier() = when {
    classDeclaration() != null -> classDeclaration().simpleIdentifier().text
    functionDeclaration() != null -> functionDeclaration().simpleIdentifier().text
    propertyDeclaration() != null -> propertyDeclaration().variableDeclaration().simpleIdentifier().text
    objectDeclaration() != null -> objectDeclaration().simpleIdentifier().text
    typeAlias() != null -> typeAlias().simpleIdentifier().text
    else -> error("Unknown declaration!")
}

private fun DeclCtx.predeclare(): List<Token> {
    val start = start.tokenIndex.leftWS()
    val classDecl = classDeclaration()!!
    val stop = classDecl.classBody().start.tokenIndex
    return tokens.get(start, stop)
}

private fun DeclCtx.define(): List<Token> {
    val start = start.tokenIndex.leftWS()
    return tokens.get(start, stop.tokenIndex)
}

private fun DeclCtx.postdeclare(): List<Token> {
    val start = stop.tokenIndex.leftWS()
    return tokens.get(start, stop.tokenIndex)
}

private fun DeclCtx.visitNext(nodes: List<ProjectorNode>, result: (increment: List<OrderedToken>) -> Unit) {
    val visitor = visit(nodes, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun List<Token>.withOrder(step: Int) = stream().map { OrderedToken(step, it) }.toList()

private fun Int.leftWS(): Int {
    var result = this - 1
    while (result >= 0 && tokens.get(result).type == KotlinLexer.WS) {
        result --
    }
    return result
}

private fun preparePage() {
    val spans = mutableListOf<OrderedSpan>()
    for (renderedToken in orderedTokens) {
        spans.add(renderedToken.toOrderedSpan())
    }
    renderedPage = TextPage(spans)
}

fun OrderedToken.toOrderedSpan() = OrderedSpan(order, token.text, token.color(), visibility = SpanVisibility.GONE)

private fun updateSpans() {
    currentFrame++
    if (currentFrame == FRAMES_PER_SPAN) {
        currentFrame = 0
        val cnt = orderedTokens.size
        var found = false
        for (i in 0 until cnt) {
            val orderedSpan = renderedPage.spans[i]
            if (orderedSpan.order == currentOrder) {
                if (orderedSpan.visibility == SpanVisibility.GONE) {
                    orderedSpan.visibility = SpanVisibility.VISIBLE
                    found = true
                    break
                }
            }
        }
        if (!found) {
            nextOrder()
        }
    }
}

private fun nextOrder() {
    currentOrder++
    if (currentOrder == orderCnt) {
        currentOrder = 0
        renderedPage.spans.forEach { it.visibility = SpanVisibility.GONE }
    }
}

private fun onFrame() {
    glClear(col3().ltGrey())
    updateSpans()
    simpleTextTechnique.page(renderedPage)
}

private fun onBuffer(buffer: ByteBuffer) {
    // todo: store the buffer
}