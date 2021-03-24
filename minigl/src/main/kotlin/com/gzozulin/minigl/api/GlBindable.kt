package com.gzozulin.minigl.api

// todo: reentry bindable?

abstract class GlBindable : GlResource() {
    private var isBound = false

    private val childBindable = mutableListOf<GlBindable>()

    override fun addChildren(child: GlResource) {
        super.addChildren(child)
        if (child is GlBindable) {
            childBindable.add(child)
        }
    }

    override fun addChildren(vararg children: GlResource) {
        super.addChildren(*children)
        children.forEach {
            if (it is GlBindable) {
                childBindable.add(it)
            }
        }
    }

    override fun addChildren(children: List<GlResource>) {
        super.addChildren(children)
        children.forEach {
            if (it is GlBindable) {
                childBindable.add(it)
            }
        }
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