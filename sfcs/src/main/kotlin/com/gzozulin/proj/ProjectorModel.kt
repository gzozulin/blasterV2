package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.api.vec3
import com.gzozulin.minigl.assembly.SpanVisibility
import com.gzozulin.minigl.assembly.TextPage
import com.gzozulin.minigl.assembly.TextSpan
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.*
import java.io.File
import kotlin.math.abs
import kotlin.streams.toList

const val LINES_TO_SHOW = 22
const val FRAMES_PER_SPAN = 2

typealias DeclCtx = KotlinParser.DeclarationContext

data class KotlinFile(val charStream: CharStream, val lexer: KotlinLexer,
                      val tokens: CommonTokenStream, val parser: KotlinParser)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(val order: Int, override val text: String, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

class ProjectorModel {
    private val projectScenario by lazy {
        ProjectorScenario(File("/home/greg/ep0_scenario/scenario").readText())
    }

    private val kotlinFiles = mutableMapOf<File, KotlinFile>()

    private val renderedPages = mutableListOf<TextPage<OrderedSpan>>()

    lateinit var currentPage: TextPage<OrderedSpan>
    var currentPageCenter = 0

    private var isAdvancingSpans = true // spans or timeout

    private var currentFrame = 0
    private var currentOrder = 0
    private var nextKeyFrame = 0

    fun renderScenario() {
        val nodesToFiles = splitPerFile()
        renderConcurrently(nodesToFiles)
        prepareOrder()
    }

    fun advanceScenario() {
        currentFrame++
        if (isAdvancingSpans) {
            advanceSpans()
        } else {
            waitForFrame()
        }
    }

    private fun splitPerFile(): Map<File, List<ScenarioNode>> {
        val result = mutableMapOf<File, MutableList<ScenarioNode>>()
        for (scenarioNode in projectScenario.scenario) {
            if (!result.containsKey(scenarioNode.file)) {
                result[scenarioNode.file] = mutableListOf()
            }
            result[scenarioNode.file]!!.add(scenarioNode)
        }
        return result
    }

    private fun renderConcurrently(nodesToFiles: Map<File, List<ScenarioNode>>) {
        runBlocking {
            val deferred = mutableListOf<Deferred<TextPage<OrderedSpan>>>()
            for (pairs in nodesToFiles) {
                deferred.add(async { renderFile(pairs.key, pairs.value) })
            }
            renderedPages.addAll(deferred.awaitAll())
        }
    }

    private fun renderFile(file: File, nodes: List<ScenarioNode>): TextPage<OrderedSpan> {
        val kotlinFile = parseKotlinFile(file)
        val orderedTokens = mutableListOf<OrderedToken>()
        val claimedNodes = mutableListOf<ScenarioNode>()
        val visitor = Visitor(0, nodes, claimedNodes, kotlinFile.tokens) { increment ->
            if (increment.last().token.type != KotlinLexer.NL) {
                orderedTokens += addTrailingNl(increment)
            } else {
                orderedTokens += increment
            }
        }
        visitor.visitKotlinFile(kotlinFile.parser.kotlinFile())
        if (claimedNodes.size != projectScenario.scenario.size) {
            val unclaimed = mutableListOf<ScenarioNode>()
            unclaimed.addAll(projectScenario.scenario)
            unclaimed.removeAll(claimedNodes)
            error("Unclaimed nodes left! $unclaimed")
        }
        return preparePage(orderedTokens)
    }

    private fun parseKotlinFile(file: File) = kotlinFiles.computeIfAbsent(file) {
        val chars = CharStreams.fromFileName(file.absolutePath)
        val lexer = KotlinLexer(chars)
        val tokens = CommonTokenStream(lexer)
        val parser = KotlinParser(tokens).apply { reset() }
        return@computeIfAbsent KotlinFile(chars, lexer, tokens, parser)
    }

    private fun preparePage(orderedTokens: MutableList<OrderedToken>) =
        TextPage(orderedTokens.stream().map { it.toOrderedSpan() }.toList())

    private fun prepareOrder() {
        findCurrentPage()
        makeOrderInvisible()
        findOrderFrame(projectScenario.scenario)
    }

    private fun findCurrentPage() {
        for (renderedPage in renderedPages) {
            for (span in renderedPage.spans) {
                if (span.order == currentOrder) {
                    currentPage = renderedPage
                    return
                }
            }
        }
        error("Next page not found!")
    }

    private fun makeOrderInvisible() {
        currentPage.spans
            .filter { it.order == currentOrder }
            .forEach { it.visibility = SpanVisibility.INVISIBLE }
    }

    private fun findOrderFrame(scenario: List<ScenarioNode>) {
        for (scenarioNode in scenario) {
            if (scenarioNode.order == currentOrder) {
                nextKeyFrame = scenarioNode.frame
                return
            }
        }
        error("Key frame not found!")
    }

    private fun advanceSpans() {
        if (currentFrame % FRAMES_PER_SPAN == 0) {
            val found = findNextInvisibleSpan()
            if (found != null) {
                found.visibility = SpanVisibility.VISIBLE
                updatePageCenter(found)
            } else {
                isAdvancingSpans = false
            }
        }
    }

    private fun findNextInvisibleSpan() =
        currentPage.spans.firstOrNull {
            it.order == currentOrder &&
                    it.visibility == SpanVisibility.INVISIBLE &&
                    it.text.isNotBlank()
        }

    private fun updatePageCenter(span: OrderedSpan) {
        val newCenter = currentPage.findLineNo(span)
        val delta = newCenter - currentPageCenter
        if (abs(delta) >= LINES_TO_SHOW) {
            currentPageCenter += delta - (LINES_TO_SHOW - 1)
        }
    }

    private fun waitForFrame() {
        if (currentFrame >= nextKeyFrame) {
            isAdvancingSpans = true
            nextOrder()
            prepareOrder()
        }
    }

    private fun nextOrder() {
        currentOrder++
        if (currentOrder == projectScenario.scenario.size) {
            currentOrder = 0
            renderedPages.forEach { textPage ->
                textPage.spans.forEach { span ->
                    span.visibility = SpanVisibility.GONE
                }
            }
        }
    }
}

private class Visitor(private val depth: Int,
                      private val nodes: List<ScenarioNode>,
                      private val claimed: MutableList<ScenarioNode>,
                      private val tokens: CommonTokenStream,
                      private val result: (increment: List<OrderedToken>) -> Unit)
    : KotlinParserBaseVisitor<Unit>() {

    override fun visitDeclaration(decl: DeclCtx) {
        val matching = findMatching(decl)
        when {
            matching.isEmpty()  -> return
            matching.size == 1  -> define(decl, matching)
            matching.size > 1   -> declare(decl, matching)
        }
    }

    private fun findMatching(decl: DeclCtx) =
        nodes.filter { it.path[depth] == decl.identifier() }

    private fun define(decl: DeclCtx, matching: List<ScenarioNode>) {
        result.invoke(decl.define(tokens).withOrder(matching.first().order))
        claimed.add(matching.first())
    }

    private fun declare(decl: DeclCtx, matching: List<ScenarioNode>) {
        val nextDepth = depth + 1
        val parents = mutableListOf<ScenarioNode>()
        val children = mutableListOf<ScenarioNode>()
        matching.forEach { node ->
            when {
                node.path.size <  nextDepth -> error("wtf?!")
                node.path.size == nextDepth -> parents.add(node)
                node.path.size >  nextDepth -> children.add(node)
            }
        }
        check(parents.size == 1) { "Only one parent declaration allowed!" }
        result.invoke(decl.predeclare(tokens).withOrder(parents.first().order))
        decl.visitNext(depth + 1, children, claimed, tokens, result)
        result.invoke(decl.postdeclare(tokens).withOrder(parents.first().order))
        claimed.add(parents.first())
    }
}

private fun addTrailingNl(increment: List<OrderedToken>) =
    listOf(*increment.toTypedArray(), OrderedToken(increment.first().order, CommonToken(KotlinParser.NL, "\n")))

private fun DeclCtx.identifier() = when {
    classDeclaration() != null -> classDeclaration().simpleIdentifier().text
    functionDeclaration() != null -> functionDeclaration().simpleIdentifier().text
    propertyDeclaration() != null -> propertyDeclaration().variableDeclaration().simpleIdentifier().text
    objectDeclaration() != null -> objectDeclaration().simpleIdentifier().text
    typeAlias() != null -> typeAlias().simpleIdentifier().text
    else -> error("Unknown declaration!")
}

// FIXME: 2021-02-12 objects and functions, ctors
private fun DeclCtx.predeclare(tokens: CommonTokenStream): List<Token> {
    val start = start.tokenIndex.leftPadding(tokens)
    val classDecl = classDeclaration()!!
    val stop = classDecl.classBody().start.tokenIndex
    return tokens.get(start, stop)
}

private fun DeclCtx.define(tokens: CommonTokenStream): List<Token> {
    val start = start.tokenIndex.leftPadding(tokens)
    return tokens.get(start, stop.tokenIndex)
}

private fun DeclCtx.postdeclare(tokens: CommonTokenStream): List<Token> {
    val start = stop.tokenIndex.leftPadding(tokens)
    return tokens.get(start, stop.tokenIndex)
}

private fun Int.leftPadding(tokens: CommonTokenStream): Int {
    var result = this - 1
    // FIXME: 2021-02-12 other WS types
    while (result >= 0 && tokens.get(result).type == KotlinLexer.WS) {
        result --
    }
    return result
}

private fun DeclCtx.visitNext(depth: Int, nodes: List<ScenarioNode>, claimed: MutableList<ScenarioNode>,
                              tokens: CommonTokenStream, result: (increment: List<OrderedToken>) -> Unit) {
    val visitor = Visitor(depth, nodes, claimed, tokens, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun List<Token>.withOrder(order: Int) = stream().map { OrderedToken(order, it) }.toList()

private fun OrderedToken.toOrderedSpan() =
    OrderedSpan(order, token.text, token.color(), visibility = SpanVisibility.GONE)

private val kotlin_white = vec3(0.659f, 0.718f, 0.776f)
private val kotlin_orange = vec3(0.922f, 0.537f, 0.239f)
private val kotlin_blue = vec3(0.314f, 0.553f, 0.631f)
private val kotlin_green = vec3(0.282f, 0.451f, 0.337f)

fun Token.color(): col3 = when (type) {
    KotlinLexer.CLASS, KotlinLexer.FUN, KotlinLexer.VAL, KotlinLexer.WHEN, KotlinLexer.IF,
    KotlinLexer.ELSE, KotlinLexer.NullLiteral, KotlinLexer.PRIVATE, KotlinLexer.PROTECTED,
    KotlinLexer.RETURN, KotlinLexer.FOR, KotlinLexer.WHILE -> kotlin_orange
    KotlinLexer.LongLiteral, KotlinLexer.IntegerLiteral, KotlinLexer.DoubleLiteral, KotlinLexer.FloatLiteral,
    KotlinLexer.RealLiteral, KotlinLexer.HexLiteral, KotlinLexer.BinLiteral -> kotlin_blue
    KotlinLexer.LineStrText -> kotlin_green
    else -> kotlin_white
}