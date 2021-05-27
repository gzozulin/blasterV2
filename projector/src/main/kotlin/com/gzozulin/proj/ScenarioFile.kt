package com.gzozulin.proj

import java.io.File
import kotlin.streams.toList

private const val FRAMES_PER_SECOND = 60

private val scenarioExample = """
    # Aliases are declared here
    alias file=/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjectorModel.kt
    alias class=ProjectorModel
    
    # Here we start parsing the scenario
    offset 10
    10  file/class/renderScenario
    110 file/class/preparePage
    120 file/class/renderFile
    130 file/Visitor
""".trimIndent()

private val whitespaceRegex = "\\s+".toRegex()
private val equalsRegex = "=".toRegex()
private val slashRegex = "/".toRegex()

data class ScenarioNode(val file: File, val path: List<String>, val frame: Int, val order: Int)

class ScenarioFile(private val text: String) {

    var offset = 0
    val aliases = mutableMapOf<String, String>()
    val scenario = mutableListOf<ScenarioNode>()

    init {
        parseScenario()
    }

    private fun parseScenario() {
        val lines = text.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        for (line in lines) {
            when {
                line.startsWith("offset") -> parseOffset(line)
                line.startsWith("alias")  -> parseAlias(line)
                else                      -> parseNode(line)
            }
        }
        return
    }

    private fun parseOffset(line: String) {
        val split = line.split(whitespaceRegex)
        offset = split[1].toInt()
    }

    private fun parseAlias(line: String) {
        val split = line.split(whitespaceRegex)
        check(split.size == 2) { "Too many items in alias! $line" }
        val (name, substitute) = split[1].split(equalsRegex)
        aliases[name] = substitute
    }

    private fun parseNode(line: String) {
        val split = line.split(whitespaceRegex)
        val timestamp = split[0]
        val frame: Int
        if (timestamp.contains("s")) {
            frame = (timestamp.substring(0, timestamp.length - 1).toFloat() * FRAMES_PER_SECOND).toInt() - offset
        } else {
            frame = timestamp.toInt() - offset
        }
        check(frame >= 0) { "Frame should be positive of 0!" }
        if (scenario.isNotEmpty()) {
            check(frame >= scenario.last().frame) { "Key frames are not in order! $line" }
        }
        val path = split[1].split(slashRegex).toMutableList()
            .stream().map { aliases[it] ?: it }
            .toList().toMutableList()
        val file = File(path.removeAt(0))
        check(file.exists()) { "File should exist and be reachable! $file" }
        scenario.add(ScenarioNode(file, path, frame, scenario.size))
    }
}

fun main() {
    val projScenario = ScenarioFile(scenarioExample)
    println(projScenario.scenario)
}