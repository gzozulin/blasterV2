package com.gzozulin.minigl.api

abstract class GlResource {
    private var used = 0

    private val childResource = mutableListOf<GlResource>()

    open fun addChildren(child: GlResource) {
        childResource.add(child)
    }

    open fun addChildren(vararg children: GlResource) {
        childResource.addAll(children)
    }

    open fun addChildren(children: Collection<GlResource>) {
        childResource.addAll(children)
    }

    fun use() {
        childResource.forEach { it.use() }
        used++
        if (used == 1) {
            onUse()
        }
    }

    fun release() {
        used--
        check(used >= 0) { "Released more times than used!" }
        if (used == 0) {
            onRelease()
        }
        childResource.reversed().forEach { it.release() }
    }

    open fun onUse() { }

    open fun onRelease() { }

    open fun checkReady() {
        if (STRICT_MODE) {
            check(used > 0) { "Is not used!" }
        }
    }
}