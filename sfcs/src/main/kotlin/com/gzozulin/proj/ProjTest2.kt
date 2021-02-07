package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

private val chars by lazy { CharStreams.fromFileName(
    "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/UtilityClass.kt") }
private val lexer by lazy { KotlinLexer(chars) }
private val tokens by lazy { CommonTokenStream(lexer) }
private val parser by lazy { KotlinParser(tokens) }

private typealias DeclCtx = KotlinParser.DeclarationContext

private fun source(start: Int, end: Int) = tokens.get(start, end).joinToString("") { it.text }

private fun DeclCtx.identifier() = when {
    classDeclaration() != null -> classDeclaration().simpleIdentifier().text
    functionDeclaration() != null -> functionDeclaration().simpleIdentifier().text
    propertyDeclaration() != null -> propertyDeclaration().variableDeclaration().simpleIdentifier().text
    objectDeclaration() != null -> objectDeclaration().simpleIdentifier().text
    else -> error("Unknown declaration!")
}

private fun DeclCtx.declare(): String {
    val start = start.tokenIndex
    val classDecl = classDeclaration()!!
    val end = classDecl.classBody().start.tokenIndex - 1
    return source(start, end)  + "\n"
}

private fun DeclCtx.define() = when {
    classDeclaration() != null -> source(start.tokenIndex, classDeclaration().classBody().stop.tokenIndex)
    functionDeclaration() != null -> source(start.tokenIndex, functionDeclaration().functionBody().stop.tokenIndex)
    propertyDeclaration() != null -> source(start.tokenIndex, propertyDeclaration().variableDeclaration().stop.tokenIndex)
    objectDeclaration() != null -> source(start.tokenIndex, objectDeclaration().classBody().stop.tokenIndex)
    else -> error("Unknown declaration!")
} + "\n"

private data class ProjectorNode(val identifier: String,
                                 var isKnown: Boolean = false,
                                 val children: List<ProjectorNode>? = null)

private val scenario = listOf(
    ProjectorNode("MainClass",
        children = listOf(
            ProjectorNode("originFunction"),
            ProjectorNode("internalFlag"),
            ProjectorNode("internalValue")
        )))

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
    val visitor = DeclVisitor(scenario) { increment ->
        result += increment
    }
    visitor.visitKotlinFile(parser.kotlinFile())
    println(result)
}

private class DeclVisitor(val nodes: List<ProjectorNode>,
                          val result: (increment: String) -> Unit) : KotlinParserBaseVisitor<Unit>() {

    override fun visitDeclaration(decl: DeclCtx) {
        val identifier = decl.identifier()
        val node = nodes.firstOrNull { it.identifier == identifier } ?: return
        if (node.isKnown) {
            if (node.children != null) {
                result.invoke(decl.declare())
                visitNext(node.children, decl)
            } else {
                result.invoke(decl.define())
            }
        }
    }

    private fun visitNext(nodes: List<ProjectorNode>, decl: DeclCtx) {
        val visitor = DeclVisitor(nodes, result)
        when {
            decl.classDeclaration() != null -> visitor.visitClassDeclaration(decl.classDeclaration())
            decl.functionDeclaration() != null -> visitor.visitFunctionDeclaration(decl.functionDeclaration())
            decl.propertyDeclaration() != null -> visitor.visitPropertyDeclaration(decl.propertyDeclaration())
            decl.objectDeclaration() != null -> visitor.visitObjectDeclaration(decl.objectDeclaration())
            else -> error("Unknown declaration!")
        }
    }
}