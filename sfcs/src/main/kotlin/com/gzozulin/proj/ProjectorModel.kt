package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.api.col3
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

private val exampleScenario = """
    # Pilot scenario

    alias file=/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ScenarioFile.kt
    alias class=ScenarioFile

    1	file/scenarioExample
    2	file/ScenarioNode
    3	file/whitespaceRegex
    4	file/equalsRegex
    5	file/slashRegex
    6	file/class
    7	file/class/aliases
    8	file/class/scenario
    9 	file/class/parseScenario
    10 	file/class/parseAlias
    11	file/class/parseNode
    12	file/main
""".trimIndent()

data class KotlinFile(val charStream: CharStream, val lexer: KotlinLexer,
                      val tokens: CommonTokenStream, val parser: KotlinParser)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(override val text: String, val order: Int, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

class ProjectorModel {
    private val projectScenario by lazy { ScenarioFile(exampleScenario) }

    private val kotlinFiles = mutableMapOf<File, KotlinFile>()

    private val renderedPages = mutableListOf<TextPage<OrderedSpan>>()

    lateinit var currentPage: TextPage<OrderedSpan>
    var currentPageCenter = 0

    private var isWaitingKeyFrame = true

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
        if (isWaitingKeyFrame) {
            waitForKeyFrame()
        } else {
            advanceSpans()
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
        val visitor = DeclVisitor(0, nodes, claimedNodes, kotlinFile.tokens) { increment ->
            orderedTokens += increment
        }
        visitor.visitKotlinFile(kotlinFile.parser.kotlinFile())
        if (claimedNodes.size != projectScenario.scenario.size) {
            val unclaimed = mutableListOf<ScenarioNode>()
            unclaimed.addAll(projectScenario.scenario)
            unclaimed.removeAll(claimedNodes)
            error("Unclaimed nodes left! $unclaimed")
        }
        return highlightPage(orderedTokens)
    }

    private fun parseKotlinFile(file: File) = kotlinFiles.computeIfAbsent(file) {
        val chars = CharStreams.fromFileName(file.absolutePath)
        val lexer = KotlinLexer(chars)
        val tokens = CommonTokenStream(lexer)
        val parser = KotlinParser(tokens).apply { reset() }
        return@computeIfAbsent KotlinFile(chars, lexer, tokens, parser)
    }

    private fun highlightPage(orderedTokens: List<OrderedToken>): TextPage<OrderedSpan> {
        val result = mutableListOf<OrderedSpan>()
        val orderMap = mutableMapOf<Token, Int>()
        val colorMap = mutableMapOf<Token, col3>()
        val tokens = mutableListOf<Token>()
        for (orderedToken in orderedTokens) {
            orderMap[orderedToken.token] = orderedToken.order
            tokens.add(orderedToken.token)
        }
        val highlightVisitor = HighlightVisitor(tokens, colorMap)
        val parser = KotlinParser(CommonTokenStream(ListTokenSource(tokens))).apply { reset() }
        highlightVisitor.visitKotlinFile(parser.kotlinFile())
        for (token in tokens) {
            result.add(OrderedSpan(token.text, orderMap[token]!!, colorMap[token]!!, SpanVisibility.GONE))
        }
        return TextPage(result)
    }

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
                val haveNext = nextOrder()
                if (haveNext) {
                    prepareOrder()
                } else {
                    nextKeyFrame = Int.MAX_VALUE
                }
                isWaitingKeyFrame = true
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

    private fun waitForKeyFrame() {
        if (currentFrame >= nextKeyFrame) {
            isWaitingKeyFrame = false
        }
    }

    private fun nextOrder(): Boolean {
        currentOrder++
        return currentOrder != projectScenario.scenario.size
    }
}

private class DeclVisitor(private val depth: Int,
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

private fun DeclCtx.identifier() = when {
    classDeclaration() != null -> classDeclaration().simpleIdentifier().text
    functionDeclaration() != null -> functionDeclaration().simpleIdentifier().text
    propertyDeclaration() != null -> propertyDeclaration().variableDeclaration().simpleIdentifier().text
    objectDeclaration() != null -> objectDeclaration().simpleIdentifier().text
    typeAlias() != null -> typeAlias().simpleIdentifier().text
    else -> error("Unknown declaration!")
}

private fun DeclCtx.predeclare(tokens: CommonTokenStream): List<Token> {
    val start = start.tokenIndex.withLeftWS(tokens)
    val classDecl = classDeclaration()!! // TODO: others: objects, etc
    val stop = classDecl.classBody().start.tokenIndex.withRightNL(tokens)
    return tokens.get(start, stop)
}

private fun DeclCtx.postdeclare(tokens: CommonTokenStream): List<Token> {
    val start = stop.tokenIndex.withLeftWS(tokens)
    val stop = stop.tokenIndex.withRightNL(tokens)
    return tokens.get(start, stop)
}

private fun DeclCtx.define(tokens: CommonTokenStream): List<Token> {
    val start = start.tokenIndex.withLeftWS(tokens)
    val stop = stop.tokenIndex.withRightNL(tokens)
    return tokens.get(start, stop)
}

private fun Int.withLeftWS(tokens: CommonTokenStream): Int {
    var result = this
    while (result > 0 && tokens.get(result - 1).type == KotlinLexer.WS) {
        result--
    }
    return result
}

private fun Int.withRightNL(tokens: CommonTokenStream): Int {
    var result = this
    while (result <= tokens.size() && tokens.get(result + 1).type == KotlinLexer.NL) {
        result++
    }
    return result
}

private fun DeclCtx.visitNext(depth: Int, nodes: List<ScenarioNode>, claimed: MutableList<ScenarioNode>,
                              tokens: CommonTokenStream, result: (increment: List<OrderedToken>) -> Unit) {
    val visitor = DeclVisitor(depth, nodes, claimed, tokens, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun List<Token>.withOrder(order: Int) = stream().map { OrderedToken(order, it) }.toList()

private fun ParserRuleContext.select(tokens: List<Token>) = tokens[start.tokenIndex]

private val kotlin_white    = col3(0.659f, 0.718f, 0.776f)
private val kotlin_orange   = col3(0.706f, 0.427f, 0.192f)
private val kotlin_blue     = col3(0.216f, 0.416f, 0.824f)
private val kotlin_light_blue = col3(0.314f, 0.553f, 0.631f)
private val kotlin_green    = col3(0.282f, 0.451f, 0.337f)
private val kotlin_yellow   = col3(0.937f, 0.675f, 0.306f)
private val kotlin_purple   = col3(0.596f, 0.463f, 0.667f)

private class HighlightVisitor(val tokens: List<Token>, val colorMap: MutableMap<Token, col3>)
    :  KotlinParserBaseVisitor<Unit>() {

    override fun visitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        updateColor(ctx.simpleIdentifier().select(tokens), kotlin_yellow)
        super.visitFunctionDeclaration(ctx)
    }

    override fun visitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext) { // FIXME
        val variableDeclaration = ctx.variableDeclaration()
        if (variableDeclaration != null) {
            updateColor(variableDeclaration.simpleIdentifier().select(tokens), kotlin_purple)
        }
        super.visitPropertyDeclaration(ctx)
    }

    override fun visitLabel(ctx: KotlinParser.LabelContext) {
        updateColor(ctx.simpleIdentifier().select(tokens), kotlin_blue)
        super.visitLabel(ctx)
    }

    override fun visitKotlinFile(ctx: KotlinParser.KotlinFileContext) {
        tokens.forEach { updateColor(it, it.color()) }
        super.visitKotlinFile(ctx)
    }

    private fun updateColor(token: Token, color: col3) {
        val current = colorMap[token]
        if (current == null || current == kotlin_white) {
            colorMap[token] = color
        }
    }
}

fun Token.color(): col3 = when (type) {
    KotlinLexer.CLASS, KotlinLexer.FUN, KotlinLexer.VAL, KotlinLexer.WHEN, KotlinLexer.IF,
    KotlinLexer.ELSE, KotlinLexer.NullLiteral, KotlinLexer.PRIVATE, KotlinLexer.PROTECTED,
    KotlinLexer.RETURN, KotlinLexer.FOR, KotlinLexer.WHILE -> kotlin_orange
    KotlinLexer.LongLiteral, KotlinLexer.IntegerLiteral, KotlinLexer.DoubleLiteral, KotlinLexer.FloatLiteral,
    KotlinLexer.RealLiteral, KotlinLexer.HexLiteral, KotlinLexer.BinLiteral -> kotlin_light_blue
    KotlinLexer.LineStrText -> kotlin_green
    else -> kotlin_white
}