package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext

typealias RuleCtx     = ParserRuleContext
typealias ClassCtx    = KotlinParser.ClassDeclarationContext
typealias FunctionCtx = KotlinParser.FunctionDeclarationContext
typealias PropertyCtx = KotlinParser.PropertyDeclarationContext
typealias ObjectCtx   = KotlinParser.ObjectDeclarationContext

// todo: nodes are better representation
// todo subScenario
// todo instead of adding, mark tokens in stream as "visible", overlapping ranges
// todo: maybe tests?

private val parser by lazy {
    KotlinParser(CommonTokenStream(KotlinLexer(CharStreams.fromFileName(
        "/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/UtilityClass.kt"))))
}

private val scenario = listOf(
    listOf("MainClass", "originFunction"),
    listOf("UtilityClass", "internalFlag"),
    listOf("UtilityClass", "internalValue"),
    listOf("FLAG"),
    listOf("highlevelFunction")
)

fun main() {
    renderStep(0)
}

private fun RuleCtx.getIdentifier() = when (this) {
    is ClassCtx -> simpleIdentifier().text
    is FunctionCtx -> identifier().text
    is PropertyCtx -> variableDeclaration().simpleIdentifier().text
    is ObjectCtx -> simpleIdentifier().text
    else -> wtf()
}

private fun RuleCtx.getDeclaration() = when (this) {
    is ClassCtx -> simpleIdentifier().text
    is FunctionCtx -> identifier().text
    is PropertyCtx -> variableDeclaration().simpleIdentifier().text
    is ObjectCtx -> simpleIdentifier().text
    else -> wtf()
}


private fun RuleCtx.getDefinition(): String {
    return (parser.tokenStream as CommonTokenStream)
        .get(start.startIndex, stop.stopIndex)
        .joinToString(separator = "") { it.text }
}

private fun wtf(): Nothing = error("wtf?!")

private class Visitor(
    private val address: List<List<String>>,
    private val resutCallback: (ctx: String) -> Unit) : KotlinParserBaseVisitor<Unit>() {

    override fun visitClassDeclaration(ctx: ClassCtx) {
        handleRuleContext(ctx)
    }

    override fun visitFunctionDeclaration(ctx: FunctionCtx) {
        handleRuleContext(ctx)
    }

    override fun visitPropertyDeclaration(ctx: PropertyCtx) {
        handleRuleContext(ctx)
    }

    override fun visitObjectDeclaration(ctx: ObjectCtx) {
        handleRuleContext(ctx)
    }

    private fun handleRuleContext(ctx: RuleCtx) {
        for (addr in address) {
            val current = addr[0]
            val lookup = ctx.getIdentifier()
            if (current == lookup) {
                if (addr.size > 1) {
                    resutCallback.invoke(ctx.getDeclaration())
                    val newAddress = addr.subList(1, addr.size)
                    val newVisitor = Visitor(listOf(newAddress), resutCallback)
                    visitNext(newVisitor, ctx)
                } else {
                    resutCallback(ctx.getDefinition())
                }
            }
        }
    }

    private fun visitNext(visitor: Visitor, ctx: RuleCtx) = when (ctx) {
        is ClassCtx -> visitor.visitClassDeclaration(ctx)
        is FunctionCtx -> visitor.visitFunctionDeclaration(ctx)
        is PropertyCtx -> visitor.visitPropertyDeclaration(ctx)
        is ObjectCtx -> visitor.visitObjectDeclaration(ctx)
        else -> wtf()
    }
}

private fun subScenario(step: Int) {
    var current = 0
}

private fun renderStep(step: Int) {

    var result = ""

    val visitor = Visitor(scenario) { increment ->
        result += increment
        println(result)
        println("----------------------------------------------------------")
    }

    visitor.visitKotlinFile(parser.kotlinFile())
}


