package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.assembly.*
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.techniques.StaticSkyboxTechnique
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_videoio.VideoWriter
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.streams.toList
import org.opencv.core.CvType

// todo: scenario to nodes
// todo: basic scene arrangement
// todo: scene buffer copying
// todo: video rendering
// todo: example project + video

private const val FRAMES_PER_SPAN = 3
private const val MILLIS_PER_FRAME = 16
private const val LINES_TO_SHOW = 20

private typealias DeclCtx = KotlinParser.DeclarationContext

private data class ScenarioNode(val order: Int, val file: File, val identifier: String,
                                val timeout: Long = TimeUnit.SECONDS.toMillis(1),
                                val children: List<ScenarioNode>? = null)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(val order: Int, override val text: String, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

private var scenarioNodeCnt = 0

// todo: assert that children file is same as parent
private val thisFile = File("/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjApp.kt")
private val anotherFile = File("/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/UtilityClass.kt")

private val scenario = listOf(
    ScenarioNode(scenarioNodeCnt++, thisFile, "ScenarioNode"),
    ScenarioNode(scenarioNodeCnt++, anotherFile, "highlevelFunction"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "main"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "findNextInvisibleSpan"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "updateCenter"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "advanceTimeout"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "identifier"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "predeclare"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "renderScenario"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "renderFile"),
    ScenarioNode(scenarioNodeCnt++, thisFile, "addTrailingNl"),
)

private val renderedPages = mutableListOf<TextPage<OrderedSpan>>()

private val capturer = GlCapturer()

private val camera = Camera()

private val skyboxTechnique = StaticSkyboxTechnique("textures/snowy")
private val simpleTextTechnique = SimpleTextTechnique(capturer.width, capturer.height)

private var isAdvancingSpans = true // spans or timeout
private lateinit var currentPage: TextPage<OrderedSpan>
private var currentCenter = 0
private var currentFrame = 0
private var currentOrder = 0
private var currentTimeout = 0L

private var videoWriter = VideoWriter().also {
        it.open(File("1vid.avi").absolutePath,
            VideoWriter.fourcc('M'.toByte(), 'J'.toByte(), 'P'.toByte(), 'G'.toByte()),
            60.0, Size(capturer.width, capturer.height))
        check(it.isOpened)
    }

fun main() {
    renderScenario()
    prepareOrder()
    capturer.create {
        glUse(skyboxTechnique, simpleTextTechnique) {
            capturer.show(::onFrame, ::onBuffer)
        }
    }
    videoWriter.release()
}

private fun renderScenario() {
    val nodesToFiles = mutableMapOf<File, MutableList<ScenarioNode>>()
    for (scenarioNode in scenario) {
        if (!nodesToFiles.containsKey(scenarioNode.file)) {
            nodesToFiles[scenarioNode.file] = mutableListOf()
        }
        nodesToFiles[scenarioNode.file]!!.add(scenarioNode)
    }
    runBlocking {
        val deferred = mutableListOf<Deferred<TextPage<OrderedSpan>>>()
        for (pairs in nodesToFiles) {
            deferred.add(async { renderFile(pairs.key, pairs.value) })
        }
        renderedPages.addAll(deferred.awaitAll())
    }
}

private fun renderFile(file: File, nodes: List<ScenarioNode>): TextPage<OrderedSpan> {
    val chars = CharStreams.fromFileName(file.absolutePath)
    val lexer = KotlinLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = KotlinParser(tokens).apply { reset() }
    val orderedTokens = mutableListOf<OrderedToken>()
    val visitor = Visitor(nodes, tokens) { increment ->
        if (increment.last().token.type != KotlinLexer.NL) {
            orderedTokens += addTrailingNl(increment)
        } else {
            orderedTokens += increment
        }
    }
    visitor.visitKotlinFile(parser.kotlinFile())
    return preparePage(orderedTokens)
}

private fun addTrailingNl(increment: List<OrderedToken>) =
    listOf(*increment.toTypedArray(), OrderedToken(increment.first().order, CommonToken(KotlinParser.NL, "\n")))

private class Visitor(val nodes: List<ScenarioNode>,
                      val tokens: CommonTokenStream,
                      val result: (increment: List<OrderedToken>) -> Unit)
    : KotlinParserBaseVisitor<Unit>() {

    override fun visitDeclaration(decl: DeclCtx) {
        val identifier = decl.identifier()
        val filtered = nodes.filter { it.identifier == identifier }
        val first = filtered.firstOrNull() ?: return // first or none found
        val withChildren = filtered.filter { it.children != null }
        if (withChildren.isNotEmpty()) { // need to declare then
            check(withChildren.size == filtered.size) { "All with children or none!" }
            result.invoke(decl.predeclare(tokens).withOrder(first.order))
            withChildren.forEach {
                decl.visitNext(it.children!!, tokens, result)
            }
            result.invoke(decl.postdeclare(tokens).withOrder(first.order))
        } else { // just define
            result.invoke(decl.define(tokens).withOrder(first.order))
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

private fun DeclCtx.visitNext(nodes: List<ScenarioNode>, tokens: CommonTokenStream, result: (increment: List<OrderedToken>) -> Unit) {
    val visitor = Visitor(nodes, tokens, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun List<Token>.withOrder(step: Int) = stream().map { OrderedToken(step, it) }.toList()

private fun preparePage(orderedTokens: MutableList<OrderedToken>): TextPage<OrderedSpan> {
    val spans = mutableListOf<OrderedSpan>()
    for (orderedToken in orderedTokens) {
        spans.add(orderedToken.toOrderedSpan())
    }
    return TextPage(spans)
}

fun OrderedToken.toOrderedSpan() = OrderedSpan(order, token.text, token.color(), visibility = SpanVisibility.GONE)

private fun onFrame() {
    glClear(col3().ltGrey())
    updateSpans()
    camera.tick()
    skyboxTechnique.skybox(camera)
    simpleTextTechnique.pageCentered(currentPage, currentCenter, LINES_TO_SHOW)
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
        val found = findNextInvisibleSpan()
        if (found != null) {
            found.visibility = SpanVisibility.VISIBLE
            updateCenter(found)
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

private fun updateCenter(span: OrderedSpan) {
    val newCenter = currentPage.findLineNo(span)
    val delta = newCenter - currentCenter
    if (abs(delta) >= LINES_TO_SHOW) {
        currentCenter += delta - (LINES_TO_SHOW - 1)
    }
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
    if (currentOrder == scenarioNodeCnt) {
        currentOrder = 0
        renderedPages.forEach {
            it.spans.forEach { span -> span.visibility = SpanVisibility.GONE }
        }
    }
}

private fun prepareOrder() {
    findCurrentPage()
    updateOrderVisibility()
    findOrderTimeout(scenario)
}

private fun updateOrderVisibility() {
    currentPage.spans
        .filter { it.order == currentOrder }
        .forEach { it.visibility = SpanVisibility.INVISIBLE }
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
    error("Did not found next page!")
}

private fun findOrderTimeout(scenario: List<ScenarioNode>) {
    for (scenarioNode in scenario) {
        if (scenarioNode.order == currentOrder) {
            currentTimeout = scenarioNode.timeout
            return
        } else if (scenarioNode.children != null) {
            findOrderTimeout(scenarioNode.children)
        }
    }
}

private fun onBuffer(buffer: ByteBuffer) {
    val frame = Mat(capturer.height, capturer.width, CvType.CV_8UC4)
    frame.put<BytePointer>(BytePointer(buffer))
    videoWriter.write(frame)
}