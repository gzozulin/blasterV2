package com.gzozulin.proj

import java.io.File
import kotlin.streams.toList

private val whitespaceRegex = "\\s+".toRegex()
private val equalsRegex = "=".toRegex()
private val slashRegex = "/".toRegex()

class ProjectorScenario(private val scenarioText: String) {

    var nodesCnt = 0
    val scenario = mutableListOf<ScenarioNode>()

    private val aliases = mutableMapOf<String, String>()

    init {
        parseScenario()
    }

    private fun parseScenario() {
        val lines = scenarioText.lines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
        for (line in lines) {
            if (line.startsWith("alias")) {
                parseAlias(line)
            } else {
                parseStep(line)
            }
        }
    }

    private fun parseAlias(line: String) {
        val split = line.split(whitespaceRegex)
        check(split.size == 2) { "Too many items in alias!" }
        val (name, substitute) = split[1].split(equalsRegex)
        aliases[name] = substitute
    }

    private fun parseStep(line: String) {
        val split = line.split(whitespaceRegex)
        val timeout = split[0].toLong()
        val path = split[1].split(slashRegex).toMutableList()
            .stream().map { aliases[it] ?: it }
            .toList().toMutableList()
        val file = File(path.removeAt(0))
        check(file.exists()) { "File should exist and be reachable!" }
        scenario.add(ScenarioNode(nodesCnt++, file, path, timeout))
    }
}

fun main() {
    val scenarioText = """
        alias file=/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjectorModel.kt
        alias class=ProjectorModel
        
        2000 file/class/renderScenario
        2000 file/class/preparePage
        2000 file/class/renderFile
        5000 file/Visitor
    """.trimIndent()
    val projScenario = ProjectorScenario(scenarioText)
    println(projScenario.nodesCnt)
    println(projScenario.scenario)
}