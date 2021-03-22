package com.gzozulin.minigl.api

abstract class GlBindable : GlResource() {
    private var isBound = false

    private val childBindable = mutableListOf<GlBindable>()

    fun addChild(vararg children: GlBindable) {
        super.addChildren(*children)
        childBindable.addAll(children)
    }

    open fun bind() {
        super.checkReady()
        childBindable.forEach { it.bind() }
        if (STRICT_MODE) {
            check(!isBound) { "Already bound!" }
        }
        isBound = true
    }

    open fun unbind() {
        isBound = false
        childBindable.reversed().forEach { it.unbind() }
    }

    override fun checkReady() {
        super.checkReady()
        if (STRICT_MODE) {
            check(isBound) { "Is not bound!" }
        }
    }
}