package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.kotlin.KotlinParserBaseVisitor
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.tech.SpanVisibility
import org.antlr.v4.runtime.*
import java.io.File

// ------------------------ PARSING -----------------------

private typealias KotlinDeclCtx = KotlinParser.DeclarationContext

data class KotlinFile(val charStream: CharStream, val lexer: KotlinLexer,
                      val tokens: CommonTokenStream, val parser: KotlinParser)

private val kotlinFiles = mutableMapOf<File, KotlinFile>()

fun renderKotlinFile(file: File, nodes: List<ScenarioNode>, claimedNodes: MutableList<ScenarioNode>): ProjectorTextPage<OrderedSpan> {
    val kotlinFile = parseKotlinFile(file)
    val orderedTokens = mutableListOf<OrderedToken>()
    val visitor = KotlinDeclVisitor(0, nodes, claimedNodes, kotlinFile.tokens) { increment ->
        orderedTokens += increment
    }
    visitor.visitKotlinFile(kotlinFile.parser.kotlinFile())
    return highlightKotlinPage(file, orderedTokens)
}

private fun parseKotlinFile(file: File) = kotlinFiles.computeIfAbsent(file) {
    val chars = CharStreams.fromFileName(file.absolutePath)
    val lexer = KotlinLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = KotlinParser(tokens).apply { reset() }
    return@computeIfAbsent KotlinFile(chars, lexer, tokens, parser)
}

private class KotlinDeclVisitor(private val depth: Int,
                                private val nodes: List<ScenarioNode>,
                                private val claimed: MutableList<ScenarioNode>,
                                private val tokens: CommonTokenStream,
                                private val result: (increment: List<OrderedToken>) -> Unit)
    : KotlinParserBaseVisitor<Unit>() {

    override fun visitDeclaration(decl: KotlinDeclCtx) {
        val matching = findMatching(decl)
        when {
            matching.isEmpty()  -> return
            matching.size == 1  -> define(decl, matching)
            matching.size > 1   -> declare(decl, matching)
        }
    }

    private fun findMatching(decl: KotlinDeclCtx) =
        nodes.filter { it.path[depth] == decl.identifier() }

    private fun define(decl: KotlinDeclCtx, matching: List<ScenarioNode>) {
        result.invoke(decl.define(tokens).withOrder(matching.first().order))
        claimed.add(matching.first())
    }

    private fun declare(decl: KotlinDeclCtx, matching: List<ScenarioNode>) {
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
        // 1. Only one parent 2. Parent is required 3. Multiple declarations
        check(parents.size == 1) { "Only one parent declaration allowed, but single is required! $matching" }
        result.invoke(decl.predeclare(tokens).withOrder(parents.first().order))
        decl.visitNext(depth + 1, children, claimed, tokens, result)
        result.invoke(decl.postdeclare(tokens).withOrder(parents.first().order))
        claimed.add(parents.first())
    }
}

private fun KotlinDeclCtx.identifier() = when {
    classDeclaration()      != null -> classDeclaration().simpleIdentifier().text
    functionDeclaration()   != null -> functionDeclaration().simpleIdentifier().text
    propertyDeclaration()   != null -> propertyDeclaration().variableDeclaration().simpleIdentifier().text
    objectDeclaration()     != null -> objectDeclaration().simpleIdentifier().text
    typeAlias()             != null -> typeAlias().simpleIdentifier().text
    else -> error("Unknown declaration!")
}

private fun KotlinDeclCtx.predeclare(tokens: CommonTokenStream): List<Token> {
    val start = start.tokenIndex.withLeftWS(tokens)
    val classDecl = classDeclaration()!! // TODO: others: objects, etc
    val stop = classDecl.classBody().start.tokenIndex.withRightNL(tokens)
    return tokens.get(start, stop)
}

private fun KotlinDeclCtx.postdeclare(tokens: CommonTokenStream): List<Token> {
    val start = stop.tokenIndex.withLeftWS(tokens)
    val stop = stop.tokenIndex.withRightNL(tokens)
    return tokens.get(start, stop)
}

private fun KotlinDeclCtx.visitNext(depth: Int, nodes: List<ScenarioNode>, claimed: MutableList<ScenarioNode>,
                                    tokens: CommonTokenStream, result: (increment: List<OrderedToken>) -> Unit) {
    val visitor = KotlinDeclVisitor(depth, nodes, claimed, tokens, result)
    when {
        classDeclaration() != null -> visitor.visitClassDeclaration(classDeclaration())
        functionDeclaration() != null -> visitor.visitFunctionDeclaration(functionDeclaration())
        propertyDeclaration() != null -> visitor.visitPropertyDeclaration(propertyDeclaration())
        objectDeclaration() != null -> visitor.visitObjectDeclaration(objectDeclaration())
        else -> error("Unknown declaration!")
    }
}

// ------------------------ HIGHLIGHTING -----------------------

private fun highlightKotlinPage(file: File, orderedTokens: List<OrderedToken>): ProjectorTextPage<OrderedSpan> {
    val result = mutableListOf<OrderedSpan>()
    val orderMap = mutableMapOf<Token, Int>()
    val colorMap = mutableMapOf<Token, col3>()
    val tokens = mutableListOf<Token>()
    for (orderedToken in orderedTokens) {
        orderMap[orderedToken.token] = orderedToken.order
        tokens.add(orderedToken.token)
    }
    val highlightVisitor = KotlinHighlightVisitor(tokens, colorMap)
    val parser = KotlinParser(CommonTokenStream(ListTokenSource(tokens))).apply { reset() }
    highlightVisitor.visitKotlinFile(parser.kotlinFile())
    for (token in tokens) {
        result.add(OrderedSpan(token.text, orderMap[token]!!, colorMap[token]!!, SpanVisibility.GONE))
    }
    return ProjectorTextPage(file, result)
}

private class KotlinHighlightVisitor(val tokens: List<Token>, val colorMap: MutableMap<Token, col3>)
    :  KotlinParserBaseVisitor<Unit>() {

    override fun visitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        updateColor(ctx.simpleIdentifier().select(tokens), darkula_yellow)
        super.visitFunctionDeclaration(ctx)
    }

    override fun visitNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext) {
        val update = tokens.subList(ctx.start.tokenIndex, ctx.stop.tokenIndex + 1)
        val color = if (tokens[ctx.stop.tokenIndex + 1].text.contains("(")) darkula_yellow else darkula_purple
        update.forEach { updateColor(it, color) }
        super.visitNavigationSuffix(ctx)
    }

    override fun visitValueArgument(ctx: KotlinParser.ValueArgumentContext) {
        if (ctx.simpleIdentifier() != null) {
            updateColor(ctx.simpleIdentifier().select(tokens), darkula_blue)
        }
        super.visitValueArgument(ctx)
    }

    override fun visitDeclaration(ctx: KotlinParser.DeclarationContext?) {
        super.visitDeclaration(ctx)
    }

    override fun visitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext) {
        val variableDeclaration = ctx.variableDeclaration()
        if (variableDeclaration != null) {
            updateColor(variableDeclaration.simpleIdentifier().select(tokens), darkula_purple)
        }
        super.visitPropertyDeclaration(ctx)
    }

    override fun visitDirectlyAssignableExpression(ctx: KotlinParser.DirectlyAssignableExpressionContext) {
        if (ctx.simpleIdentifier() != null) {
            updateColor(ctx.simpleIdentifier().select(tokens), darkula_purple)
        }
        super.visitDirectlyAssignableExpression(ctx)
    }

    override fun visitLabel(ctx: KotlinParser.LabelContext) {
        updateColor(ctx.simpleIdentifier().select(tokens), darkula_blue)
        super.visitLabel(ctx)
    }

    override fun visitStringLiteral(ctx: KotlinParser.StringLiteralContext) {
        val update = tokens.subList(ctx.start.tokenIndex, ctx.stop.tokenIndex + 1)
        update.forEach { updateColor(it, darkula_green) }
        super.visitStringLiteral(ctx)
    }

    override fun visitEnumEntry(ctx: KotlinParser.EnumEntryContext) {
        updateColor(ctx.simpleIdentifier().select(tokens), darkula_purple)
        super.visitEnumEntry(ctx)
    }

    override fun visitKotlinFile(ctx: KotlinParser.KotlinFileContext) {
        tokens.forEach { updateColor(it, it.color()) }
        super.visitKotlinFile(ctx)
    }

    private fun updateColor(token: Token, color: col3) {
        val current = colorMap[token]
        if (current == null || current == darkula_white) {
            colorMap[token] = color
        }
    }
}

private fun Token.color(): col3 = when (type) {
    KotlinLexer.CLASS, KotlinLexer.FUN, KotlinLexer.VAL, KotlinLexer.WHEN, KotlinLexer.IF,
    KotlinLexer.ELSE, KotlinLexer.NullLiteral, KotlinLexer.PRIVATE, KotlinLexer.PROTECTED,
    KotlinLexer.RETURN, KotlinLexer.FOR, KotlinLexer.WHILE, KotlinLexer.CONST, KotlinLexer.DATA,
    KotlinLexer.TYPE_ALIAS, KotlinLexer.AS, KotlinLexer.BY, KotlinLexer.VAR, KotlinLexer.OVERRIDE,
    KotlinLexer.LATEINIT, KotlinLexer.ENUM, KotlinLexer.ABSTRACT, KotlinLexer.OPEN, KotlinLexer.INTERNAL
    -> darkula_orange
    KotlinLexer.LongLiteral, KotlinLexer.IntegerLiteral, KotlinLexer.DoubleLiteral, KotlinLexer.FloatLiteral,
    KotlinLexer.RealLiteral, KotlinLexer.HexLiteral, KotlinLexer.BinLiteral
    -> darkula_light_blue
    KotlinLexer.LineStrText
    -> darkula_green
    else -> if (text != "error") darkula_white else darkula_red
}