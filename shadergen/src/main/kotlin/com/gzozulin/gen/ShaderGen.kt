package com.gzozulin.gen

import com.gzozulin.c.CBaseVisitor
import com.gzozulin.c.CLexer
import com.gzozulin.c.CParser
import org.antlr.v4.runtime.*
import java.io.File

private val DEFINITIONS = listOf(
    "/home/greg/blaster/shaderlang/lang.h",
    "/home/greg/blaster/shaderlang/main.c",
    "/home/greg/blaster/shaderlang/float.c",
    "/home/greg/blaster/shaderlang/bool.c",
    "/home/greg/blaster/shaderlang/math.c",
    "/home/greg/blaster/shaderlang/vec2.c",
    "/home/greg/blaster/shaderlang/vec3.c",
    "/home/greg/blaster/shaderlang/vec4.c",
    "/home/greg/blaster/shaderlang/ivec2.c",
    "/home/greg/blaster/shaderlang/mat2.c",
    "/home/greg/blaster/shaderlang/mat3.c",
    "/home/greg/blaster/shaderlang/mat4.c",
    "/home/greg/blaster/shaderlang/ray.c",
    "/home/greg/blaster/shaderlang/random.c",
    "/home/greg/blaster/shaderlang/const.c",
    "/home/greg/blaster/shaderlang/raytracer.c",
    "/home/greg/blaster/shaderlang/shading.c",
    "/home/greg/blaster/shaderlang/sandsim.c",
)

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
private interface CDefinition {
    val access: CAccess
    val name: String
    val def: String
}
private data class COperation(val type: String, override val name: String, val params: List<CParam>,
                              override val def: String, override val access: CAccess): CDefinition
private data class CTypedef(override val name: String, override val def: String,
                            override val access: CAccess): CDefinition
private data class CConstant(override val name: String, override val access: CAccess,
                             override val def: String): CDefinition

fun main() {
    val definitions = mutableListOf<CDefinition>()
    DEFINITIONS.forEach { definition ->
        definitions += visitCFile(parseCFile(File(definition)))
    }
    val (glOutput, edOutput) = renderDefinitions(definitions)
    doCreateOutput(glOutput, edOutput, File(GL_GENERATED), File(ED_GENERATED))
}

private fun doCreateOutput(glContent: String, edContent: String, glGenerated: File, edGenerated: File) {
    glGenerated.writeText(glContent)
    edGenerated.writeText(edContent)
}

private fun renderDefinitions(definitions: List<CDefinition>): Pair<String, String> {
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
        val params = declaration.params.joinToString(",") { "edParseExpression(lineNo, split.removeFirst(), heap)" }
        edHandles += "        \"${declaration.name}\" -> ${declaration.name}($params)\n"
    }

    val edResult = """
        package com.gzozulin.ed

        import com.gzozulin.minigl.api.*

        private val PATTERN_WHITESPACE = "\\s+".toPattern()
        
        @Suppress("UNCHECKED_CAST")
        internal fun edParseOperation(lineNo: Int, operation: String, heap: MutableMap<String, Expression<*>>): Expression<*> {
            val split = operation.split(PATTERN_WHITESPACE).toMutableList()
            return when (split.removeFirst()) {
                $edHandles
                "namedTexCoordsV2" -> namedTexCoordsV2()
                "namedTexCoordsV3" -> namedTexCoordsV3()
                "namedGlFragCoordV2" -> namedGlFragCoordV2()
                "cachev4" -> cachev4(edParseExpression(lineNo, split.removeFirst(), heap))
                "texel" -> texel(edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap))
                "sampler" -> sampler(edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap))
                "samplerq" -> samplerq(edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap))
                "discard" -> discard<Any>()
                "ifexp" -> ifexp<Any>(edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap))
                "eqexp" -> eqexp<Any>(edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap))
                "more" -> more<Any>(edParseExpression(lineNo, split.removeFirst(), heap), edParseExpression(lineNo, split.removeFirst(), heap))
                "not" -> not(edParseExpression(lineNo, split.removeFirst(), heap))
                else -> error("Unknown operation!")
            }
        }
    """.trimIndent()

    return glResult to edResult
}

private fun renderDefinition(declaration: CDefinition) =
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

private fun visitCFile(cfile: CFile): List<CDefinition> {
    val result = mutableListOf<CDefinition>()
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
