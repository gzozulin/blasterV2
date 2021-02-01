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
    val currentAddress = mutableListOf<String>()
    var currentDepth = -1
    fun createNode() { result.add(ScenarioNode(currentAddress.toList())) }
    fun addAddress(new: String) { currentAddress.add(new) }
    for (line in lines) {
        val (newDepth, noDepth) = removeDepth(line)
        when {
            newDepth == currentDepth -> {
                createNode()
            }
            newDepth > currentDepth -> {
                addAddress(noDepth)
            }
            newDepth < currentDepth -> {
                check(newDepth == 0) { "Only new address is expected here" }
                createNode()
                currentAddress.clear()
                addAddress(noDepth)
            }
        }
        currentDepth = newDepth
    }
    createNode()
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