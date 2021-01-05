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