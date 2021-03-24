package com.gzozulin.minigl.api

// todo: reentry bindable?

abstract class GlBindable : GlResource() {
    private var bound = 0

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

    override fun addChildren(children: Collection<GlResource>) {
        super.addChildren(children)
        children.forEach {
            if (it is GlBindable) {
                childBindable.add(it)
            }
        }
    }

    fun bind() {
        super.checkReady()
        childBindable.forEach { it.bind() }
        bound++
        if (bound == 1) {
            onBound()
        }
    }

    fun unbind() {
        bound--
        check(bound >= 0) { "Unbound more times than bound!" }
        if (bound == 0) {
            onUnbound()
        }
        childBindable.reversed().forEach { it.unbind() }
    }

    open fun onBound() { }
    open fun onUnbound() { }

    override fun checkReady() {
        super.checkReady()
        if (STRICT_MODE) {
            check(bound > 0) { "Is not bound!" }
        }
    }
}