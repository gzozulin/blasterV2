package com.gzozulin.minigl.scene

class ValueCache<T>(private val setter: (T) -> Unit) {
    private var cached: T? = null;

    fun set(value: T) {
        if (value != cached) {
            setter.invoke(value)
            cached = value
        }
    }
}

data class Version(private var version: Long = 0L, private var last: Long = Long.MAX_VALUE) {
    fun increment() { version++ }

    fun check(): Boolean {
        return if (version != last) {
            last = version
            true
        } else {
            false
        }
    }
}