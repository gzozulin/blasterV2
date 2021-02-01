package com.gzozulin.proj

private const val FLAG = false

fun highlevelFunction() {
    if (FLAG) {
        println("Doing it high-level")
    }
}

class UtilityClass {
    fun internalFunction() {
        highlevelFunction()
    }
}

class MainClass {
    private val internalFlag = true
    private val internalValue = UtilityClass()

    fun originFunction() {
        if (internalFlag) {
            internalValue.internalFunction()
        }
    }
}