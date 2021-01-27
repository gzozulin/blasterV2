package com.gzozulin.proj

class ProjApp {
    private val exampleFlag = true

    fun exampleFun() {
        if (exampleFlag) {
            println("Hello!")
        }
    }
}

private val scenario = """
    /home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjApp.kt
        ProjApp
            exampleFlag
            exampleFun
    /home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/alch/AlchApp.kt
        injector
""".trimIndent()

private data class ScenarioNode(val address: List<String>)

private fun parseScenario(scenario: String): List<ScenarioNode> {
    val lines = scenario.split("\n")
    val result = mutableListOf<ScenarioNode>()
    var currentAddress = mutableListOf<String>()
    var currentDepth = 0
    for (line in lines) {
        val (depth, noDepth) = removeDepth(line)
        if (depth > currentDepth) {
            currentAddress.add(noDepth)
        } else {
            result.add(ScenarioNode(currentAddress))
            currentAddress = mutableListOf()
        }
        currentDepth = depth
    }
    return result
}

private fun removeDepth(line: String): Pair<Int, String> {
    var result = line
    var depth = 0
    while (result[0] == '\t' || result[0] == ' ') {
        depth++
        result = result.substring(1 until result.length)
    }
    return depth to result
}

fun main() {
    val nodes = parseScenario(scenario)
    ProjApp().exampleFun()
}