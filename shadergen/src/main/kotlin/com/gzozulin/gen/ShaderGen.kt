package com.gzozulin.gen

import com.gzozulin.c.CBaseVisitor
import com.gzozulin.c.CLexer
import com.gzozulin.c.CParser
import org.antlr.v4.runtime.*
import java.io.File

private const val DEFINITIONS = "/home/greg/blaster/shaderlang/main.c"
private const val ASSEMBLY = "/home/greg/blaster/minigl/src/main/kotlin/com/gzozulin/minigl/api/GlGenerated.kt"

private const val ACCESS_PUBLIC = "public"
private const val ACCESS_CUSTOM = "custom"

private typealias DeclCtx = CParser.ExternalDeclarationContext
private typealias FunctionCtx = CParser.FunctionDefinitionContext

private data class CFile(val chars: CharStream, val lexer: CLexer, val tokens: CommonTokenStream, val parser: CParser)
private data class CParam(val type: String, val name: String)

private enum class CAccess { PRIVATE, CUSTOM, PUBLIC }
private interface CDeclaration {
    val access: CAccess
    val name: String
    val def: String
}
private data class COperation(val type: String, override val name: String, val params: List<CParam>,
                              override val def: String, override val access: CAccess): CDeclaration
private data class CTypedef(override val name: String, override val def: String,
                            override val access: CAccess): CDeclaration

fun main() = doCreateOutput(
    renderDeclarations(visitCFile(parseCFile(File(DEFINITIONS)))), File(ASSEMBLY)
)

private fun doCreateOutput(content: String, file: File) {
    file.writeText(content)
}

private fun renderDeclarations(declarations: List<CDeclaration>): String {
    var result = "package com.gzozulin.minigl.api\n\n" +
            "import com.gzozulin.minigl.scene.Light\n" +
            "import com.gzozulin.minigl.tech.Hitable\n" +
            "import com.gzozulin.minigl.tech.RtCamera\n" +
            "import com.gzozulin.minigl.tech.HitRecord\n" +
            "import com.gzozulin.minigl.tech.ScatterResult\n" +
            "import com.gzozulin.minigl.tech.RefractResult\n" +
            "import com.gzozulin.minigl.tech.Sphere\n" +
            "import com.gzozulin.minigl.scene.PhongMaterial\n" +
            "import com.gzozulin.minigl.tech.LambertianMaterial\n" +
            "import com.gzozulin.minigl.tech.MetallicMaterial\n" +
            "import com.gzozulin.minigl.tech.DielectricMaterial\n\n"
    declarations.forEach { operation ->
        if (operation.access == CAccess.PUBLIC) {
            result += renderDeclaration(operation) + "\n"
        }
    }
    result += "\n"
    result += "const val PUBLIC_TYPES = ${declarations.filter { it.access == CAccess.PUBLIC }.filterIsInstance<CTypedef>()
        .joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"
    result += "const val PUBLIC_OPS = ${declarations.filter { it.access == CAccess.PUBLIC }.filterIsInstance<COperation>()
        .joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"
    declarations.forEach { declaration ->
        if (declaration is COperation) {
            result += renderKotlinHandle(declaration) + "\n\n"
        }
    }
    return result
}

private fun renderDeclaration(declaration: CDeclaration) =
    "private const val DEF_${declaration.name.toUpperCase()} = \"${declaration.def}\\n\""

private fun renderKotlinHandle(operation: COperation) = """
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
    "bool"                  -> "Boolean"
    "int"                   -> "Int"
    "float"                 -> "Float"
    "double"                -> "Double"
    "vec2"                  -> "vec2"
    "ivec2"                 -> "vec2i"
    "vec3"                  -> "vec3"
    "vec4"                  -> "vec4"
    "mat4"                  -> "mat4"
    "mat3"                  -> "mat3"
    "Light"                 -> "Light"
    "Ray"                   -> "ray"
    "Sphere"                -> "Sphere"
    "Hitable"               -> "Hitable"
    "HitRecord"             -> "HitRecord"
    "HitableList"           -> "HitableList"
    "PhongMaterial"         -> "PhongMaterial"
    "LambertianMaterial"    -> "LambertianMaterial"
    "MetallicMaterial"      -> "MetallicMaterial"
    "DielectricMaterial"    -> "DielectricMaterial"
    "ScatterResult"         -> "ScatterResult"
    "RefractResult"         -> "RefractResult"
    "RtCamera"              -> "RtCamera"
    "AABB"                  -> "aabb"
    else                    -> error("Unknown type! $ctype")
}

private fun visitCFile(cfile: CFile): List<CDeclaration> {
    val result = mutableListOf<CDeclaration>()
    val visitor = ExternalDeclarationVisitor { ctx ->
        if (ctx.isFunction()) {
            parseCFunction(ctx.functionDefinition(), cfile.tokens)?.let {
                result.add(it)
            }
        } else if (ctx.isTypedef()) {
            parseTypedef(ctx.declaration(), cfile.tokens)?.let {
                result.add(it)
            }
        }
    }
    visitor.visit(cfile.parser.compilationUnit())
    return result
}

private fun DeclCtx.isTypedef(): Boolean {
    for (declarationSpecifierContext in declaration().declarationSpecifiers().declarationSpecifier()) {
        if (declarationSpecifierContext.text == "typedef") {
            return true
        }
    }
    return false
}

private fun DeclCtx.isFunction(): Boolean {
    return functionDefinition() != null
}

private fun parseAccess(declSpecifiers: CParser.DeclarationSpecifiersContext): CAccess {
    var access = CAccess.PRIVATE
    for (i in 0 until declSpecifiers.childCount) {
        val specifier = declSpecifiers.getChild(i)
        if (specifier.text == ACCESS_CUSTOM) {
            access = CAccess.CUSTOM
        } else if (specifier.text == ACCESS_PUBLIC) {
            access = CAccess.PUBLIC
        }
    }
    return access
}

private fun parseTypedef(ctx: CParser.DeclarationContext, tokens: CommonTokenStream): CTypedef? {
    val declSpecifiers = ctx.declarationSpecifiers()
    val access = parseAccess(declSpecifiers)
    if (access == CAccess.PRIVATE) {
        return null
    }

    val name = ctx.declarationSpecifiers().declarationSpecifier()[ctx.declarationSpecifiers().childCount - 1].text
    val body = tokens.filterAndExtract(ctx)
    val start = body.indexOfFirst { it == '{' } + 1
    val stop = body.indexOfFirst { it == '}' }

    val definition = "struct $name { ${body.substring(start, stop)} };"
    return CTypedef(name, definition, access)
}

private fun parseCFunction(ctx: FunctionCtx, tokens: CommonTokenStream): COperation? {
    val name = ctx.declarator().directDeclarator().directDeclarator().text
    val declSpecifiers = ctx.declarationSpecifiers()
    val access = parseAccess(declSpecifiers)
    if (access == CAccess.PRIVATE) {
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

private class ExternalDeclarationVisitor(val callback: (ctx: DeclCtx) -> Unit) : CBaseVisitor<Unit>() {
    override fun visitExternalDeclaration(ctx: DeclCtx) {
        super.visitExternalDeclaration(ctx)
        callback.invoke(ctx)
    }
}

private fun CommonTokenStream.filterAndExtract(ctx: ParserRuleContext, separator: String = " ") =
    get(ctx.start.tokenIndex, ctx.stop.tokenIndex)
        .asSequence()
        .filter { it.channel == Token.DEFAULT_CHANNEL  }
        .filterNot { it.text == "const" }
        .filterNot { it.text == "struct" }
        .filterNot { it.text == ACCESS_PUBLIC }
        .filterNot { it.text == ACCESS_CUSTOM }
        .joinToString(separator) { it.text }
        .trim()