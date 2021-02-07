package com.gzozulin.proj

//val node = nodes.firstOrNull { it.identifier == identifier } ?: return

// declared and defined
// if branch - add to declared
// if leaf - add to defined
// after each step - render the whole file in "snapshots"

/*
val out = (parser.tokenStream as CommonTokenStream)
                .get(ctx.start.startIndex, ctx.stop.stopIndex)
                .joinToString(separator = "") { it.text }


            println(out)
 */

/*private class Visitor(val lookup: Label, val callback: () -> Unit) : KotlinParserBaseVisitor<Unit>() {
    override fun visitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        if (ctx!!.simpleIdentifier().text == lookup) {
            callback.invoke()
        }
    }

    override fun visitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        super.visitFunctionDeclaration(ctx)
    }

    override fun visitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        super.visitPropertyDeclaration(ctx)
    }

    override fun visitObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        super.visitObjectDeclaration(ctx)
    }
}


// render all before cursor
// if branch - declaration
// if leaf - declaration and definition
private fun render(cursor: ProjNode, current: ProjNode) {
    Visitor("looo") {

    }.visit(parser.kotlinFile())
}*/