package com.gzozulin.gen

import com.gzozulin.c.CBaseVisitor
import com.gzozulin.c.CLexer
import com.gzozulin.c.CParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.File

private const val DEFINITIONS = "/home/greg/blaster/shaderlang/main.c"
private const val ASSEMBLY = "/home/greg/blaster/minigl/src/main/kotlin/com/gzozulin/minigl/api/GlGenerated.kt"

private const val ACCESS_PUBLIC = "public"
private const val ACCESS_CUSTOM = "custom"

private typealias FunctionCtx = CParser.FunctionDefinitionContext

private data class CFile(val chars: CharStream, val lexer: CLexer, val tokens: CommonTokenStream, val parser: CParser)
private data class CParam(val type: String, val name: String)
private enum class COperationAccess { PRIVATE, CUSTOM, PUBLIC }
private data class COperation(val type: String, val name: String, val params: List<CParam>, val def: String,
                              val access: COperationAccess)

fun main(): Unit = doCreateOutput(
    renderAssembly(visitCFile(parseCFile(File(DEFINITIONS)))), File(ASSEMBLY)
)

private fun doCreateOutput(content: String, file: File) {
    file.writeText(content)
}

private fun renderAssembly(operations: List<COperation>): String {
    var result = "package com.gzozulin.minigl.api\n\n" +
            "import com.gzozulin.minigl.scene.Light\n" +
            "import com.gzozulin.minigl.scene.Hitable\n" +
            "import com.gzozulin.minigl.scene.HitRecord\n" +
            "import com.gzozulin.minigl.scene.ScatterResult\n" +
            "import com.gzozulin.minigl.scene.Sphere\n" +
            "import com.gzozulin.minigl.scene.PhongMaterial\n\n" +
            "import com.gzozulin.minigl.scene.LambertianMaterial\n\n"
    operations.forEach { operation ->
        if (operation.access == COperationAccess.PUBLIC) {
            result += renderDefinition(operation) + "\n"
        }
    }
    result += "\n"
    result += "const val PUBLIC_DEFINITIONS = ${operations.filter { it.access == COperationAccess.PUBLIC }
        .joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"
    operations.forEach { operation ->
        result += renderOperation(operation) + "\n\n"
    }
    return result
}

private fun renderDefinition(operation: COperation) =
    "private const val DEF_${operation.name.toUpperCase()} = \"${operation.def}\\n\\n\""

private fun renderOperation(operation: COperation) = """
    fun ${operation.name}(${renderParams(operation.params)}) = object : Expression<${convertType(operation.type)}>() {
        override fun expr() = "${operation.name}(${renderExpressions(operation.params)})"
        ${renderRoots(operation.params)}
    }
""".trimIndent()

private fun renderParams(params: List<CParam>) =
    params.joinToString { "${it.name}: Expression<${convertType(it.type)}>" }

private fun renderExpressions(params: List<CParam>) =
    params.joinToString { "\${${it.name}.expr()}" }

private fun renderRoots(params: List<CParam>): String {
    return if (params.isNotEmpty()) "override fun roots() = listOf(${params.joinToString { it.name }})"
    else "override fun roots() = listOf<Expression<*>>()"
}

private fun convertType(ctype: String) = when (ctype) {
    "bool"          -> "Boolean"
    "int"           -> "Int"
    "float"         -> "Float"
    "double"        -> "Double"
    "vec2"          -> "vec2"
    "ivec2"         -> "vec2i"
    "vec3"          -> "vec3"
    "vec4"          -> "vec4"
    "mat4"          -> "mat4"
    "mat3"          -> "mat3"
    "Light"         -> "Light"
    "Ray"           -> "ray"
    "Sphere"        -> "Sphere"
    "Hitable"     -> "Hitable"
    "HitRecord"     -> "HitRecord"
    "HitableList"   -> "HitableList"
    "PhongMaterial" -> "PhongMaterial"
    "LambertianMaterial" -> "LambertianMaterial"
    "ScatterResult" -> "ScatterResult"
    else            -> error("Unknown type! $ctype")
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
    var access = COperationAccess.PRIVATE
    for (i in 0 until declSpecifiers.childCount) {
        val specifier = declSpecifiers.getChild(i)
        if (specifier.text == ACCESS_CUSTOM) {
            access = COperationAccess.CUSTOM
        } else if (specifier.text == ACCESS_PUBLIC) {
            access = COperationAccess.PUBLIC
        }
    }
    if (access == COperationAccess.PRIVATE) {
        return null
    }
    val definition = tokens.filterAndExtract(ctx)
    val type = tokens.filterAndExtract(declSpecifiers)
    val params = mutableListOf<CParam>()
    if (ctx.declarator().directDeclarator().parameterTypeList() != null) {
        val paramList = ctx.declarator().directDeclarator().parameterTypeList().parameterList()
        for (i in 0 until paramList.childCount) {
            val child = paramList.getChild(i)
            if (child is CParser.ParameterDeclarationContext) {
                check(child.childCount == 2) { "Too many children for a parameter declaration! ${child.text}" }
                params.add(CParam(tokens.filterAndExtract(child.declarationSpecifiers()), child.getChild(1).text))
            }
        }
    }
    return COperation(type, name, params, definition, access)
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
        .filterNot { it.text == "const" }
        .filterNot { it.text == "struct" }
        .filterNot { it.text == ACCESS_PUBLIC }
        .filterNot { it.text == ACCESS_CUSTOM }
        .joinToString(separator) { it.text }
        .trim()