package com.gzozulin

import com.gzozulin.c.CBaseVisitor
import com.gzozulin.c.CLexer
import com.gzozulin.c.CParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.File

private const val DEFINITIONS = "shaderlang/main.c"
private const val ASSEMBLY = "minigl/src/main/kotlin/com/gzozulin/minigl/api/GlGenerated.kt"

private const val PUBLIC_DEF = "public"

private typealias FunctionCtx = CParser.FunctionDefinitionContext

private data class CFile(val chars: CharStream, val lexer: CLexer, val tokens: CommonTokenStream, val parser: CParser)
private data class CParam(val type: String, val name: String)
private data class COperation(val type: String, val name: String, val params: List<CParam>, val def: String)

fun main(): Unit = doCreateOutput(
    renderAssembly(visitCFile(parseCFile(File(DEFINITIONS)))), File(ASSEMBLY)
)

private fun doCreateOutput(content: String, file: File) {
    file.writeText(content)
}

private fun renderAssembly(operations: List<COperation>): String {
    var result = "package com.gzozulin.minigl.api\n\n"
    operations.forEach { operation ->
        result += renderDefinition(operation) + "\n"
    }
    result += "const val PUBLIC_DEFINITIONS = ${operations.joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"
    operations.forEach { operation ->
        result += renderOperation(operation) + "\n\n"
    }
    return result
}

private fun renderDefinition(operation: COperation) =
    "private const val DEF_${operation.name.toUpperCase()} = \"${operation.def}\""

private fun renderOperation(operation: COperation) = """
    fun ${operation.name}(${renderParams(operation.params)}) = object : Expression<${convertType(operation.type)}>() {
        override fun expr() = "${operation.name}(${renderRoots(operation.params)})"
        override fun roots() = listOf(${renderRoots(operation.params)})
    }
""".trimIndent()

private fun renderParams(params: List<CParam>) =
    params.joinToString { "${it.name}: Expression<${convertType(it.type)}>" }

private fun renderRoots(params: List<CParam>): String {
    return params.joinToString { it.name }
}

private fun convertType(ctype: String) = when (ctype) {
    "int"       -> "Int"
    "float"     -> "Float"
    "vec3"      -> "vec3"
    "vec2"      -> "vec2"
    "ivec2"     -> "vec2i"
    "mat4"      -> "mat4"
    else        -> error("Unknown type! $ctype")
}

private fun visitCFile(cfile: CFile): List<COperation> {
    val result = mutableListOf<COperation>()
    val visitor = FunctionVisitor { ctx ->
        parseCFunction(ctx, cfile.tokens)?.let {
            result.add(it)
        }
    }
    visitor.visit(cfile.parser.compilationUnit())
    return result
}

private fun parseCFunction(ctx: FunctionCtx, tokens: CommonTokenStream): COperation? {
    val name = ctx.declarator().directDeclarator().directDeclarator().text
    val declSpecifiers = ctx.declarationSpecifiers()
    var shouldExport = false
    for (i in 0 until declSpecifiers.childCount) {
        val specifier = declSpecifiers.getChild(i)
        if (specifier.text == PUBLIC_DEF) {
            shouldExport = true
        }
    }
    val definition = tokens.filterAndExtract(ctx)
    if (!shouldExport) {
        return null
    }
    val type = declSpecifiers.declarationSpecifier()[declSpecifiers.childCount - 1].text
    val params = mutableListOf<CParam>()
    if (ctx.declarator().directDeclarator().parameterTypeList() != null) {
        val paramList = ctx.declarator().directDeclarator().parameterTypeList().parameterList()
        for (i in 0 until paramList.childCount) {
            val child = paramList.getChild(i)
            if (child is CParser.ParameterDeclarationContext) {
                check(child.childCount == 2) { "Too many children for a parameter declaration! ${child.text}" }
                params.add(CParam(child.getChild(0).text, child.getChild(1).text))
            }
        }
    }
    return COperation(type, name, params, definition)
}

private fun parseCFile(file: File): CFile {
    val chars = CharStreams.fromFileName(file.absolutePath)
    val lexer = CLexer(chars)
    val tokens = CommonTokenStream(lexer)
    val parser = CParser(tokens).apply { reset() }
    return CFile(chars, lexer, tokens, parser)
}

private class FunctionVisitor(val callback: (ctx: FunctionCtx) -> Unit) : CBaseVisitor<Unit>() {
    override fun visitFunctionDefinition(ctx: FunctionCtx) {
        super.visitFunctionDefinition(ctx)
        callback.invoke(ctx)
    }
}

private fun CommonTokenStream.filterAndExtract(ctx: ParserRuleContext, separator: String = " ") =
    get(ctx.start.tokenIndex, ctx.stop.tokenIndex)
        .filter { it.text == "const" }
        .filter { it.text == "struct" }
        .filter { it.text == "public" }
        .joinToString(separator) { it.text }
        .trim()