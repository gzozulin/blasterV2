package com.gzozulin.proj

import com.gzozulin.c.CBaseVisitor
import com.gzozulin.c.CLexer
import com.gzozulin.c.CParser
import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.tech.SpanVisibility
import org.antlr.v4.runtime.*
import java.io.File

private const val KEYWORD_PUBLIC = "public"
private const val KEYWORD_PROTECTED = "protected"

// ------------------------ PARSING -----------------------

private typealias CDeclCtx = CParser.ExternalDeclarationContext

data class CFile(val charStream: CharStream, val lexer: CLexer,
                 val tokens: CommonTokenStream, val parser: CParser)

private val cfiles = mutableMapOf<File, CFile>()

fun renderCFile(file: File, nodes: List<ScenarioNode>,
                claimedNodes: MutableList<ScenarioNode>): ProjectorTextPage<OrderedSpan> {
    val cfile = parseCFile(file)
    val orderedTokens = mutableListOf<OrderedToken>()
    val visitor = CDeclVisitor(nodes, claimedNodes, cfile.tokens) { increment ->
        orderedTokens += increment
    }
    visitor.visit(cfile.parser.compilationUnit())
    return highlightCPage(file, orderedTokens)
}

private fun parseCFile(file: File) = cfiles.computeIfAbsent(file) {
    val chars = CharStreams.fromFileName(file.absolutePath)
    val lexer = CLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = CParser(tokens).apply { reset() }
    return@computeIfAbsent CFile(chars, lexer, tokens, parser)
}

private class CDeclVisitor(private val nodes: List<ScenarioNode>,
                           private val claimed: MutableList<ScenarioNode>,
                           private val tokens: CommonTokenStream,
                           private val result: (increment: List<OrderedToken>) -> Unit)
    : CBaseVisitor<Unit>() {

    override fun visitExternalDeclaration(decl: CDeclCtx) {
        val matching = findMatching(decl)
        when {
            matching.isEmpty()  -> return
            matching.size == 1  -> define(decl, matching)
            matching.size > 1   -> error("No inlays allowed!")
        }
    }

    private fun findMatching(decl: CDeclCtx) =
        nodes.filter { it.path[0] == decl.identifier() } // no inlays

    private fun define(decl: CDeclCtx, matching: List<ScenarioNode>) {
        result.invoke(decl
            .define(tokens, CParser.Whitespace, CParser.Newline)
            .filterPrivateCKeywords()
            .withOrder(matching.first().order))
        claimed.add(matching.first())
    }
}

private fun List<Token>.filterPrivateCKeywords(): List<Token> {
    val result = mutableListOf<Token>()
    var removeNextNL = false
    for (token in this) {
        if (token.text == KEYWORD_PUBLIC || token.text == KEYWORD_PROTECTED) {
            removeNextNL = true
        } else if (removeNextNL && token.text == "\n") {
            removeNextNL = false
        } else {
            result.add(token)
        }
    }
    return result
}

//TODO: messy
private fun CDeclCtx.identifier(): String {

    // constant
    try {
        var text = declaration()
            .initDeclaratorList()
            .initDeclarator()[0]
            .declarator()
            .text
        if (text.contains('[')) {
            text = text.removeRange(text.indexOf('['), text.indexOf(']') + 1)
        }
        return text
    } catch (th: Throwable) { }

    // struct
    try {
        val typedef = declaration()
            .declarationSpecifiers()
            .declarationSpecifier()[1]
            .text

        if (typedef == "typedef") {
            return declaration()
                .declarationSpecifiers()
                .declarationSpecifier()[3]
                .text
        } else {
            return declaration()
                .declarationSpecifiers()
                .declarationSpecifier()[2]
                .text
        }
    } catch (th: Throwable) { }

    // function
    try {
        return functionDefinition()
            .declarator()
            .directDeclarator()
            .directDeclarator()
            .Identifier()
            .text
    } catch (th: Throwable) { }

    error("Unknown declaration! $text")
}

// ------------------------ HIGHLIGHTING -----------------------

private fun highlightCPage(file: File, orderedTokens: List<OrderedToken>): ProjectorTextPage<OrderedSpan> {
    val result = mutableListOf<OrderedSpan>()
    val orderMap = mutableMapOf<Token, Int>()
    val colorMap = mutableMapOf<Token, col3>()
    val tokens = mutableListOf<Token>()
    for (orderedToken in orderedTokens) {
        orderMap[orderedToken.token] = orderedToken.order
        tokens.add(orderedToken.token)
    }
    val highlightVisitor = CHighlightVisitor(tokens, colorMap)
    val parser = CParser(CommonTokenStream(ListTokenSource(tokens))).apply { reset() }
    highlightVisitor.visit(parser.compilationUnit())
    for (token in tokens) {
        result.add(OrderedSpan(token.text, orderMap[token]!!, colorMap[token]!!, SpanVisibility.GONE))
    }
    return ProjectorTextPage(file, result)
}

private class CHighlightVisitor(val tokens: List<Token>, val colorMap: MutableMap<Token, col3>)
    :  CBaseVisitor<Unit>() {

    override fun visitCompilationUnit(ctx: CParser.CompilationUnitContext) {
        tokens.forEach { updateColor(it, it.color()) }
        super.visitCompilationUnit(ctx)
    }

    override fun visitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) {
        updateColor(ctx.declarator()
            .directDeclarator()
            .directDeclarator()
            .Identifier().symbol, darkula_yellow)
        super.visitFunctionDefinition(ctx)
    }

    private fun updateColor(token: Token, color: col3) {
        val current = colorMap[token]
        if (current == null || current == darkula_white) {
            colorMap[token] = color
        }
    }
}

private fun Token.color(): col3 = when (type) {
    CLexer.Int, CLexer.Float, CLexer.Struct, CLexer.Const, CLexer.Return, CLexer.For, CLexer.While, CLexer.If,
    CLexer.Else, CLexer.Switch, CLexer.Case, CLexer.Default, CLexer.Break, CLexer.Void, CLexer.Typedef -> darkula_orange
    CLexer.Constant -> darkula_light_blue
    CLexer.StringLiteral -> darkula_green
    else -> darkula_white
}