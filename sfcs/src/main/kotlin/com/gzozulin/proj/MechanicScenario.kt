package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.assembly.SpanVisibility
import com.gzozulin.minigl.assembly.TextPage
import com.gzozulin.minigl.assembly.TextSpan
import com.gzozulin.minigl.api.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.kodein.di.instance
import java.io.File
import kotlin.streams.toList

typealias DeclCtx = KotlinParser.DeclarationContext

data class ScenarioNode(val order: Int, val file: File, val identifier: String,
                        val children: List<ScenarioNode>? = null)

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(val order: Int, override val text: String, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

class MechanicScenario {
    private val repo: RepoProjector by ProjectorApp.injector.instance()

    fun renderScenario() {
        val nodesToFiles = mutableMapOf<File, MutableList<ScenarioNode>>()
        for (scenarioNode in repo.scenario) {
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
            repo.renderedPages.addAll(deferred.awaitAll())
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

    private fun preparePage(orderedTokens: MutableList<OrderedToken>): TextPage<OrderedSpan> {
        val spans = mutableListOf<OrderedSpan>()
        for (orderedToken in orderedTokens) {
            spans.add(orderedToken.toOrderedSpan())
        }
        return TextPage(spans)
    }
}

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