package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token

private typealias DeclCtx = KotlinParser.DeclarationContext

private data class ProjectorNode(val identifier: String, var isKnown: Boolean = false,
                                 val children: List<ProjectorNode>? = null)

private val scenario = listOf(
    ProjectorNode("MainClass",
        children = listOf(
            ProjectorNode("originFunction"),
            ProjectorNode("internalFlag"),
            ProjectorNode("internalValue")
        )))

private val chars by lazy { CharStreams.fromFileName(
    "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/UtilityClass.kt") }
private val lexer by lazy { KotlinLexer(chars) }
private val tokens by lazy { CommonTokenStream(lexer) }
private val parser by lazy { KotlinParser(tokens) }

private fun List<Token>.toSrc() = joinToString("") { it.text }

private fun inclLeftWs(start: Int): Int {
    var result = start - 1
    while (result >= 0 && tokens.get(result).type == KotlinLexer.WS) {
        result --
    }
    return result
}

private fun DeclCtx.identifier() = when {
    classDeclaration() != null -> classDeclaration().simpleIdentifier().text
    functionDeclaration() != null -> functionDeclaration().simpleIdentifier().text
    propertyDeclaration() != null -> propertyDeclaration().variableDeclaration().simpleIdentifier().text
    objectDeclaration() != null -> objectDeclaration().simpleIdentifier().text
    else -> error("Unknown declaration!")
}

private fun DeclCtx.predeclare(): List<Token> {
    val start = inclLeftWs(start.tokenIndex)
    val classDecl = classDeclaration()!!
    val stop = classDecl.classBody().start.tokenIndex
    return tokens.get(start, stop)
}

private fun DeclCtx.define(): List<Token> {
    val start = inclLeftWs(start.tokenIndex)
    return tokens.get(start, stop.tokenIndex)
}

private fun DeclCtx.postdeclare(): List<Token> {
    val start = inclLeftWs(stop.tokenIndex)
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

private fun visit(nodes: List<ProjectorNode>,
                  result: (increment: List<Token>) -> Unit) = object : KotlinParserBaseVisitor<Unit>() {

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

fun main() {
    step(scenario)
}

private fun step(nodes: List<ProjectorNode>) {
    nodes.forEach { node ->
        node.isKnown = true
        renderAll()
        if (node.children != null) {
            step(node.children)
        }
    }
}

private fun renderAll() {
    parser.reset()
    println("--------------------------------------------------")
    var result = ""
    val visitor = visit(scenario) { increment ->
        result += increment.toSrc()
    }
    visitor.visitKotlinFile(parser.kotlinFile())
    println(result)
}