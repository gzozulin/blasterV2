package com.gzozulin.gen

import com.gzozulin.c.CBaseVisitor
import com.gzozulin.c.CLexer
import com.gzozulin.c.CParser
import org.antlr.v4.runtime.*
import java.io.File

private const val DEFINITIONS = "/home/greg/blaster/shaderlang/main.c"
private const val GL_GENERATED = "/home/greg/blaster/minigl/src/main/kotlin/com/gzozulin/minigl/api/GlGenerated.kt"
private const val ED_GENERATED = "/home/greg/blaster/shadered/src/main/kotlin/com/gzozulin/ed/EdGenerated.kt"

private const val ACCESS_PUBLIC = "public"
private const val ACCESS_PROTECTED = "protected"
private const val ACCESS_CUSTOM = "custom"

private typealias DeclCtx = CParser.ExternalDeclarationContext
private typealias FunctionCtx = CParser.FunctionDefinitionContext

private data class CFile(val chars: CharStream, val lexer: CLexer, val tokens: CommonTokenStream, val parser: CParser)
private data class CParam(val type: String, val name: String)

private enum class CAccess { PRIVATE, CUSTOM, PROTECTED, PUBLIC }
private interface CDeclaration {
    val access: CAccess
    val name: String
    val def: String
}
private data class COperation(val type: String, override val name: String, val params: List<CParam>,
                              override val def: String, override val access: CAccess): CDeclaration
private data class CTypedef(override val name: String, override val def: String,
                            override val access: CAccess): CDeclaration
private data class CConstant(override val name: String, override val access: CAccess,
                             override val def: String): CDeclaration

fun main() {
    val (glOutput, edOutput) = renderDefinitions(visitCFile(parseCFile(File(DEFINITIONS))))
    doCreateOutput(glOutput, edOutput, File(GL_GENERATED), File(ED_GENERATED))
}

private fun doCreateOutput(glContent: String, edContent: String, glGenerated: File, edGenerated: File) {
    glGenerated.writeText(glContent)
    edGenerated.writeText(edContent)
}

private fun renderDefinitions(definitions: List<CDeclaration>): Pair<String, String> {
    var glResult = "package com.gzozulin.minigl.api\n\n" +
            "import com.gzozulin.minigl.scene.Light\n" +
            "import com.gzozulin.minigl.scene.PhongMaterial\n\n"
    definitions.forEach { operation ->
        if (operation.access == CAccess.PUBLIC || operation.access == CAccess.PROTECTED) {
            glResult += renderDefinition(operation) + "\n"
        }
    }
    val visibleDefinitions = definitions.filter { it.access == CAccess.PUBLIC || it.access == CAccess.PROTECTED}
    glResult += "\n"
    glResult += "const val TYPES_DEF = ${visibleDefinitions.filterIsInstance<CTypedef>()
        .joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"
    glResult += "const val OPS_DEF = ${visibleDefinitions.filterIsInstance<COperation>()
        .joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"
    glResult += "const val CONST_DEF = ${visibleDefinitions.filterIsInstance<CConstant>()
        .joinToString("+") { "DEF_${it.name.toUpperCase()}" }}\n\n"


    val handledDefinitions = definitions
        .filterIsInstance<COperation>().filter { it.access == CAccess.PUBLIC || it.access == CAccess.CUSTOM }
    var edHandles = ""
    handledDefinitions.forEach { declaration ->
        glResult += renderKotlinHandle(declaration) + "\n\n"
        val params = declaration.params.joinToString(",") { "edParseParam(params.removeFirst(), heap)" }
        edHandles += "        \"${declaration.name}\" -> ${declaration.name}($params)\n"
    }

    val edResult = """
        package com.gzozulin.ed

        import com.gzozulin.minigl.api.*
        
        internal fun edParseReference(reference: String, params: MutableList<String>, heap: Map<String, Expression<*>>) =
            when (reference) {
                $edHandles
                "namedTexCoordsV2" -> namedTexCoordsV2()
                "namedTexCoordsV3" -> namedTexCoordsV3()
                "namedGlFragCoordV2" -> namedGlFragCoordV2()
                "cachev4" -> cachev4(edParseParam(params.removeFirst(), heap))
                "texel" -> texel(edParseParam(params.removeFirst(), heap), edParseParam(params.removeFirst(), heap))
                "sampler" -> sampler(edParseParam(params.removeFirst(), heap), edParseParam(params.removeFirst(), heap))
                "samplerq" -> samplerq(edParseParam(params.removeFirst(), heap), edParseParam(params.removeFirst(), heap))
                "discard" -> discard()
                "ifexp" -> ifexp(edParseParam(params.removeFirst(), heap), edParseParam(params.removeFirst(), heap), edParseParam(params.removeFirst(), heap))
                "moref" -> more(edParseParam<Float>(params.removeFirst(), heap), edParseParam(params.removeFirst(), heap))
                "not" -> not(edParseParam(params.removeFirst(), heap))
                else -> error("Unknown operation! " + reference)
            }
    """.trimIndent()
    return glResult to edResult
}

private fun renderDefinition(declaration: CDeclaration) =
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
    "mat2"                  -> "mat2"
    "mat3"                  -> "mat3"
    "mat4"                  -> "mat4"
    "Light"                 -> "Light"
    "Ray"                   -> "ray"
    "PhongMaterial"         -> "PhongMaterial"
    else                    -> error("Unknown type! $ctype")
}

private fun visitCFile(cfile: CFile): List<CDeclaration> {
    val result = mutableListOf<CDeclaration>()
    val visitor = ExternalDeclarationVisitor { ctx ->
        when {
            ctx.isFunction() -> {
                parseCFunction(ctx.functionDefinition(), cfile.tokens)?.let {
                    result.add(it)
                }
            }
            ctx.isTypedef() -> {
                parseTypedef(ctx.declaration(), cfile.tokens)?.let {
                    result.add(it)
                }
            }
            ctx.isConstant() -> {
                parseCConstant(ctx.declaration(), cfile.tokens)?.let {
                    result.add(it)
                }
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

private fun DeclCtx.isConstant(): Boolean {
    for (declarationSpecifierContext in declaration().declarationSpecifiers().declarationSpecifier()) {
        if (declarationSpecifierContext.text == "const") {
            return true
        }
    }
    return false
}

private fun parseAccess(declSpecifiers: CParser.DeclarationSpecifiersContext): CAccess {
    for (i in 0 until declSpecifiers.childCount) {
        val specifier = declSpecifiers.getChild(i)
        when (specifier.text) {
            ACCESS_CUSTOM       -> return CAccess.CUSTOM
            ACCESS_PUBLIC       -> return CAccess.PUBLIC
            ACCESS_PROTECTED    -> return CAccess.PROTECTED
        }
    }
    return CAccess.PRIVATE
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

private fun parseCConstant(ctx: CParser.DeclarationContext, tokens: CommonTokenStream): CConstant? {
    val declSpecifiers = ctx.declarationSpecifiers()
    val access = parseAccess(declSpecifiers)
    if (access == CAccess.PRIVATE) {
        return null
    }
    val name = ctx.initDeclaratorList().initDeclarator()[0].declarator().text
    val def = tokens.filterAndExtract(ctx)
    return CConstant(name, access, def)
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
        .filterNot { it.text == ACCESS_PROTECTED }
        .joinToString(separator) { it.text }
        .trim()
