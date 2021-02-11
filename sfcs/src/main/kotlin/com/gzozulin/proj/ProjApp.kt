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

private const val FRAMES_PER_PAGE = 60

private typealias DeclCtx = KotlinParser.DeclarationContext

private data class ProjectorNode(val order: Int, val identifier: String, val children: List<ProjectorNode>? = null)

private data class RenderedToken(val order: Int, val token: Token)

private val chars by lazy { CharStreams.fromFileName(
    "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/UtilityClass.kt") }
private val lexer by lazy { KotlinLexer(chars) }
private val tokens by lazy { CommonTokenStream(lexer) }
private val parser by lazy { KotlinParser(tokens) }

// todo: add the aility to "split" the node: i.e. MainClass first, then UtilityClass, then MainClass again
private var nodeOrder = 0
private val scenario = listOf(
    ProjectorNode(
        nodeOrder++,"MainClass", children = listOf(
            ProjectorNode(nodeOrder++, "originFunction"),
            ProjectorNode(nodeOrder++, "internalValue"),
            ProjectorNode(nodeOrder++, "internalFlag"),
    )),
    ProjectorNode(nodeOrder++, "UtilityClass", children = listOf(
        ProjectorNode(nodeOrder++, "internalFunction")
    )),
    ProjectorNode(nodeOrder++, "highlevelFunction")
)

private val renderedTokens = mutableListOf<RenderedToken>()
private val renderedPages = mutableListOf<TextPage>()

private val capturer = GlCapturer()

private val simpleTextTechnique = SimpleTextTechnique(capturer.width, capturer.height)

private var currentPage = 0
private var currentFrame = 0

fun main() {
    renderScenario()
    preparePages()
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
            if (!renderedTokens.contains(renderedToken)) {
                renderedTokens += renderedToken
            }
        }
    }
    visitor.visitKotlinFile(parser.kotlinFile())
}

private fun visit(nodes: List<ProjectorNode>, result: (increment: List<RenderedToken>) -> Unit) =
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

private fun DeclCtx.visitNext(nodes: List<ProjectorNode>, result: (increment: List<RenderedToken>) -> Unit) {
    val visitor = visit(nodes, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun List<Token>.withOrder(step: Int) = stream().map { RenderedToken(step, it) }.toList()

private fun Int.leftWS(): Int {
    var result = this - 1
    while (result >= 0 && tokens.get(result).type == KotlinLexer.WS) {
        result --
    }
    return result
}

private fun preparePages() {
    for (current in 0 until nodeOrder) {
        val spans = mutableListOf<TextSpan>()
        for (renderedToken in renderedTokens) {
            if (renderedToken.order <= current) {
                spans.add(TextSpan(renderedToken.token.text, col3().cyan()))
            }
        }
        renderedPages.add(TextPage(spans))
    }
}

private fun nextFrame() {
    currentFrame++
    if (currentFrame == FRAMES_PER_PAGE) {
        currentPage++
        currentFrame = 0
        if (currentPage == renderedPages.size) {
            currentPage = 0
        }
    }
}

private fun currentPage() = renderedPages[currentPage]

private fun onFrame() {
    glClear(col3().ltGrey())
    simpleTextTechnique.page(currentPage())
    nextFrame()
}

private fun onBuffer(buffer: ByteBuffer) {
    // todo: store the buffer
}