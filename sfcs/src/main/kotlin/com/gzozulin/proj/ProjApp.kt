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

private const val FRAMES_PER_PAGE = 10

private typealias DeclCtx = KotlinParser.DeclarationContext

private data class ProjectorNode(val identifier: String, var isKnown: Boolean = false,
                                 val children: List<ProjectorNode>? = null)

private data class RenderStep(val tokens: List<Token>)

private val chars by lazy { CharStreams.fromFileName(
    "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/UtilityClass.kt") }
private val lexer by lazy { KotlinLexer(chars) }
private val tokens by lazy { CommonTokenStream(lexer) }
private val parser by lazy { KotlinParser(tokens) }

private val scenario = listOf(
    ProjectorNode("MainClass", children = listOf(
        ProjectorNode("originFunction"),
        ProjectorNode("internalFlag"),
        ProjectorNode("internalValue")
    )))

private val renderedSteps = mutableListOf<RenderStep>()
private val renderedPages = mutableListOf<TextPage>()

private val capturer = GlCapturer()

private val simpleTextTechnique = SimpleTextTechnique(capturer.width, capturer.height)

private var currentPage = 0
private var currentFrame = 0

fun main() {
    stepThrough(scenario)
    preparePages()
    capturer.create {
        glUse(simpleTextTechnique) {
            capturer.show(::onFrame, ::onBuffer)
        }
    }
}

private fun stepThrough(nodes: List<ProjectorNode>) {
    nodes.forEach { node ->
        node.isKnown = true
        renderAll()
        if (node.children != null) {
            stepThrough(node.children)
        }
    }
}

private fun renderAll() {
    parser.reset()
    val tokens = mutableListOf<Token>()
    val visitor = visit(scenario) { increment ->
        tokens += increment
    }
    visitor.visitKotlinFile(parser.kotlinFile())
    renderedSteps.add(RenderStep(tokens))
}

private fun visit(nodes: List<ProjectorNode>, result: (increment: List<Token>) -> Unit) =
    object : KotlinParserBaseVisitor<Unit>() {
        override fun visitDeclaration(decl: DeclCtx) {
            val identifier = decl.identifier()
            val node = nodes.firstOrNull { it.identifier == identifier } ?: return
            if (node.isKnown) {
                if (node.children != null) {
                    result.invoke(decl.predeclare())
                    decl.visitNext(node.children, result)
                    result.invoke(decl.postdeclare())
                } else {
                    result.invoke(decl.define())
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

private fun DeclCtx.visitNext(nodes: List<ProjectorNode>, result: (increment: List<Token>) -> Unit) {
    val visitor = visit(nodes, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

private fun Int.leftWS(): Int {
    var result = this - 1
    while (result >= 0 && tokens.get(result).type == KotlinLexer.WS) {
        result --
    }
    return result
}

private fun preparePages() {
    for (renderedStep in renderedSteps) {
        val currentTokens = mutableListOf<Token>()
        for (token in renderedStep.tokens) {
            currentTokens.add(token)
            val spans = mutableListOf<TextSpan>()
            for (currentToken in currentTokens) {
                spans.add(TextSpan(currentToken.text, col3().cyan()))
            }
            renderedPages.add(TextPage(spans))
        }
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