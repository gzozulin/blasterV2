package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.gl.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

private const val FRAMES_PER_SPAN = 5
private const val MILLIS_PER_FRAME = 16

private typealias DeclCtx = KotlinParser.DeclarationContext

private data class ProjectorNode(val order: Int, val timeout: Long,
                                 val identifier: String, val children: List<ProjectorNode>? = null)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(val order: Int, override val text: String, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

private val chars by lazy { CharStreams.fromFileName(
    "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjApp.kt") }
private val lexer by lazy { KotlinLexer(chars) }
private val tokens by lazy { CommonTokenStream(lexer) }
private val parser by lazy { KotlinParser(tokens) }

// todo: add the aility to "split" the node: i.e. MainClass first, then UtilityClass, then MainClass again
private var scenarioNodeCnt = 0
private val scenario = listOf(
    ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(7),"main"),
    ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(2),"renderScenario"),
    ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(2),"preparePage"),
    ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(2),"capturer"),
    ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(2),"Visitor",
        children = listOf(ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(2),"visitDeclaration"))),
    ProjectorNode(scenarioNodeCnt++, TimeUnit.SECONDS.toMillis(2),"updateCursor"),
)

private val orderedTokens = mutableListOf<OrderedToken>()
private lateinit var renderedPage: TextPage<OrderedSpan>

private val capturer = GlCapturer()

private val simpleTextTechnique = SimpleTextTechnique(capturer.width, capturer.height)

private var isAdvancingSpans = true // spans or timeout
private var currentFrame = 0
private var currentOrder = 0
private var currentTimeout = 0L

fun main() {
    renderScenario()
    preparePage()
    prepareOrder()
    capturer.create {
        glUse(simpleTextTechnique) {
            capturer.show(::onFrame, ::onBuffer)
        }
    }
}

private fun renderScenario() {
    parser.reset()
    val visitor = Visitor(scenario) { increment ->
        if (increment.last().token.type != KotlinLexer.NL) {
            orderedTokens += addTrailingNl(increment)
        } else {
            orderedTokens += increment
        }
    }
    visitor.visitKotlinFile(parser.kotlinFile())
}

private fun addTrailingNl(increment: List<OrderedToken>) =
    listOf(*increment.toTypedArray(), OrderedToken(increment.first().order, CommonToken(KotlinParser.NL, "\n")))

private class Visitor(val nodes: List<ProjectorNode>, val result: (increment: List<OrderedToken>) -> Unit)
    : KotlinParserBaseVisitor<Unit>() {
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

// FIXME: 2021-02-12 objects and functions
private fun DeclCtx.predeclare(): List<Token> {
    val start = start.tokenIndex.leftPadding()
    val classDecl = classDeclaration()!!
    val stop = classDecl.classBody().start.tokenIndex
    return tokens.get(start, stop)
}

private fun DeclCtx.define(): List<Token> {
    val start = start.tokenIndex.leftPadding()
    return tokens.get(start, stop.tokenIndex)
}

private fun DeclCtx.postdeclare(): List<Token> {
    val start = stop.tokenIndex.leftPadding()
    return tokens.get(start, stop.tokenIndex)
}

private fun Int.leftPadding(): Int {
    var result = this - 1
    // FIXME: 2021-02-12 other WS types
    while (result >= 0 && tokens.get(result).type == KotlinLexer.WS) {
        result --
    }
    return result
}

private fun DeclCtx.visitNext(nodes: List<ProjectorNode>, result: (increment: List<OrderedToken>) -> Unit) {
    val visitor = Visitor(nodes, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun List<Token>.withOrder(step: Int) = stream().map { OrderedToken(step, it) }.toList()

private fun preparePage() {
    val spans = mutableListOf<OrderedSpan>()
    for (orderedToken in orderedTokens) {
        spans.add(orderedToken.toOrderedSpan())
    }
    renderedPage = TextPage(spans)
}

fun OrderedToken.toOrderedSpan() = OrderedSpan(order, token.text, token.color(), visibility = SpanVisibility.GONE)

private fun prepareOrder() {
    renderedPage.spans
        .filter { it.order == currentOrder }
        .filter { it.text.isBlank() }
        .forEach { it.visibility = SpanVisibility.VISIBLE }
    currentTimeout = scenario[currentOrder].timeout
}

private fun onFrame() {
    glClear(col3().ltGrey())
    updateSpans()
    simpleTextTechnique.page(renderedPage)
}

private fun updateSpans() {
    currentTimeout -= MILLIS_PER_FRAME
    if (isAdvancingSpans) {
        advanceSpans()
    } else {
        advanceTimeout()
    }
}

private fun advanceSpans() {
    currentFrame++
    if (currentFrame == FRAMES_PER_SPAN) {
        currentFrame = 0
        val found = makeNextNonWsSpanVisible()
        if (!found) {
            isAdvancingSpans = false
        }
    }
}

private fun makeNextNonWsSpanVisible(): Boolean {
    for (orderedSpan in renderedPage.spans) {
        if (orderedSpan.order == currentOrder) {
            if (orderedSpan.visibility == SpanVisibility.GONE) {
                orderedSpan.visibility = SpanVisibility.VISIBLE
                if (orderedSpan.text.isNotBlank()) {
                    // non-WS counts
                    return true
                }
            }
        }
    }
    return false
}

private fun advanceTimeout() {
    if (currentTimeout <= 0) {
        isAdvancingSpans = true
        nextOrder()
        prepareOrder()
    }
}

private fun nextOrder() {
    currentOrder++
    if (currentOrder == scenarioNodeCnt - 1) {
        currentOrder = 0
        renderedPage.spans.forEach { it.visibility = SpanVisibility.GONE }
    }
}

private fun onBuffer(buffer: ByteBuffer) {
    // todo: store the buffer
}