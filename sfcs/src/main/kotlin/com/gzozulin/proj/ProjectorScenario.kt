package com.gzozulin.proj

import java.io.File
import java.util.*

private val whitespaceRegex = "\\s+".toRegex()
private val equalsRegex = "=".toRegex()

class ProjectorScenario(private val scenarioText: String) {

    var nodesCnt = 0
    lateinit var scenario: List<ScenarioNode>

    private val aliases = mutableMapOf<String, String>()
    private var currentFile = File("unknown")

    private val childrenStack = Stack<MutableList<ScenarioNode>>().apply { add(mutableListOf()) }

    init {
        parseScenario()
    }

    private fun parseScenario() {
        check(scenarioText.indexOf("\t") == -1) { "Tabulation is not allowed!" }
        val lines = scenarioText.lines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .toMutableList()
        parseAliases(lines)
        parseNodes(lines)
    }

    private fun parseAliases(lines: MutableList<String>) {
        while (lines.first().startsWith("alias")) {
            val split = lines.removeAt(0).split(whitespaceRegex)
            check(split.size == 2) { "Too many items in alias!" }
            val (name, substitute) = split[1].split(equalsRegex)
            aliases[name] = substitute
        }
    }

    private fun parseNodes(lines: List<String>) {
        var currentDepth = -1
        for (line in lines) {
            if (line.startsWith("----")) {
                parseFileStatement(line)
                continue
            }
            val (nextDepth, node) = parseScenarioNode(line)
            if (currentDepth == -1) {
                currentDepth = nextDepth
            }
            when {
                nextDepth == currentDepth -> addSibling(node)
                nextDepth > currentDepth -> addChildren(node)
                nextDepth < currentDepth -> addParent(node)
            }
            currentDepth = nextDepth
        }
        scenario = childrenStack.first()
    }

    private fun parseFileStatement(line: String) {
        val filename = line.removePrefix("----").trim()
        currentFile = File(aliases[filename] ?: filename)
        check(currentFile.exists()) { "File do not exists! ${currentFile.absolutePath}" }
    }

    private fun parseScenarioNode(line: String): Pair<Int, ScenarioNode> {
        val depth = line.count { it == ' ' }
        val firstDelimiter = line.indexOfFirst { it == '\t' || it == ' ' }
        val timeout = line.substring(0, firstDelimiter).toLong()
        val identifier = line.substring(firstDelimiter, line.length).trim()
        return depth to ScenarioNode(
            order = nodesCnt++,
            file = currentFile,
            identifier = aliases[identifier] ?: identifier,
            timeout = timeout)
    }

    private fun addSibling(node: ScenarioNode) {
        childrenStack.peek().add(node)
    }

    private fun addChildren(node: ScenarioNode) {
        val last = childrenStack.peek().removeLast()
        val all = mutableListOf<ScenarioNode>()
        if (last.children != null) {
            all.addAll(last.children)
        }
        all.add(node)
        childrenStack.peek().add(last.copy(children = all))
        childrenStack.push(all)
    }

    private fun addParent(node: ScenarioNode) {
        childrenStack.pop()
        childrenStack.peek().add(node)
    }
}

fun main() {
    val scenarioText = """
        alias ModelFile=/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjectorModel.kt
        
        ----    ModelFile
        2000        ProjectorModel
        2000            renderScenario
        2000            preparePage
        2000            renderFile
        3000        predeclare
        4000        define
        5000        postdeclare
        5000        Visitor
    """.trimIndent()
    val projScenario = ProjectorScenario(scenarioText)
    println(projScenario.nodesCnt)
    println(projScenario.scenario)
}