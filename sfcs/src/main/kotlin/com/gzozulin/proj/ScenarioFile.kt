package com.gzozulin.proj

import java.io.File
import kotlin.streams.toList

private val scenarioExample = """
    # Aliases are declared here
    alias file=/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjectorModel.kt
    alias class=ProjectorModel
    
    # Here we start parsing the scenario
    0   file/class/renderScenario
    100 file/class/preparePage
    120 file/class/renderFile
    300 file/Visitor
""".trimIndent()

private val whitespaceRegex = "\\s+".toRegex()
private val equalsRegex = "=".toRegex()
private val slashRegex = "/".toRegex()

data class ScenarioNode(val file: File, val path: List<String>, val order: Int, val frame: Int)

class ScenarioFile(private val text: String) {

    val aliases = mutableMapOf<String, String>()
    val scenario = mutableListOf<ScenarioNode>()

    init {
        parseScenario()
    }

    private fun parseScenario() {
        val lines = text.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        for (line in lines) {
            if (line.startsWith("alias")) {
                parseAlias(line)
            } else {
                parseNode(line)
            }
        }
    }

    private fun parseAlias(line: String) {
        val split = line.split(whitespaceRegex)
        check(split.size == 2) { "Too many items in alias! $line" }
        val (name, substitute) = split[1].split(equalsRegex)
        aliases[name] = substitute
    }

    private fun parseNode(line: String) {
        val split = line.split(whitespaceRegex)
        val frame = split[0].toInt()
        if (scenario.isNotEmpty()) {
            check(frame >= scenario.last().frame) { "Key frames are not in order! $line" }
        }
        val path = split[1].split(slashRegex).toMutableList()
            .stream().map { aliases[it] ?: it }
            .toList().toMutableList()
        val file = File(path.removeAt(0))
        check(file.exists()) { "File should exist and be reachable! $file" }
        scenario.add(ScenarioNode(file, path, scenario.size, frame))
    }
}

fun main() {
    val projScenario = ScenarioFile(scenarioExample)
    println(projScenario.scenario)
}