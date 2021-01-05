package com.gzozulin.minigl.scene

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