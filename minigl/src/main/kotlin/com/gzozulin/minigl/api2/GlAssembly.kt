package com.gzozulin.minigl.api2

import java.util.concurrent.atomic.AtomicInteger

private var next = AtomicInteger()
private fun nextName() = "_v${next.incrementAndGet()}"

abstract class Expression<T> {
    open val name: String = nextName()
    abstract fun expr(): String
    open fun roots(): List<Expression<*>> = emptyList()
}

data class Varying<T>(override val name: String, val location: Int) : Expression<T>() {
    override fun expr() = name
    fun declare() = ""
}

data class Uniform<T>(override val name: String) : Expression<T>() {
    override fun expr() = name
}

data class Constant<T>(internal val value: T, override val name: String = nextName()) : Expression<T>() {
    override fun expr() = name
}

fun varf(name: String) = Varying<Float>(name, 0)
fun uniff(name: String) = Uniform<Float>(name)
fun constf(value: Float) = Constant(value)

fun <T> add(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} - ${left.expr()})"
    override fun roots() = listOf(left, right)
}

fun <T> sub(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} - ${left.expr()})"
    override fun roots() = listOf(left, right)
}

fun <T> mul(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} * ${left.expr()})"
    override fun roots() = listOf(left, right)
}
fun <T> div(left: Expression<T>, right: Expression<T>) = object : Expression<T>() {
    override fun expr() = "(${right.expr()} / ${left.expr()})"
    override fun roots() = listOf(left, right)
}

fun glExprSubstitute(source: String, expressions: Map<String, Expression<*>>) {
    var result = source
    val constants = mutableListOf<Expression<*>>()
    val uniforms = mutableListOf<Expression<*>>()
    val varying = mutableListOf<Expression<*>>()
    fun search(expression: Expression<*>) {
        expression.roots().forEach { root ->
            when (root) {
                is Constant -> constants.add(root)
                is Varying  -> varying.add(root)
                is Uniform  -> uniforms.add(root)
                else        -> search(root)
            }
        }
    }
    expressions.forEach { (name, expr) ->
        search(expr)
        result = result.replace("%$name%", expr.expr())
    }


}

fun main() {

}