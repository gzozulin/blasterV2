package com.gzozulin.proj

import com.gzozulin.minigl.assembly.TextPage
import java.io.File

// todo: assert that children file is same as parent
private val thisFile = File("/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/CaseScenario.kt")

class Repository {
    var scenarioNodeCnt = 0

    val scenario = listOf(
        ScenarioNode(
            scenarioNodeCnt++, thisFile, "CaseScenario", children = listOf(
                ScenarioNode(scenarioNodeCnt++, thisFile, "renderScenario"),
                ScenarioNode(scenarioNodeCnt++, thisFile, "preparePage"),
                ScenarioNode(scenarioNodeCnt++, thisFile, "renderFile")
            )
        ),
        ScenarioNode(scenarioNodeCnt++, thisFile, "predeclare"),
        ScenarioNode(scenarioNodeCnt++, thisFile, "define"),
        ScenarioNode(scenarioNodeCnt++, thisFile, "postdeclare"),
        ScenarioNode(scenarioNodeCnt++, thisFile, "Visitor"),
    )

    val renderedPages = mutableListOf<TextPage<OrderedSpan>>()

    lateinit var currentPage: TextPage<OrderedSpan>
    var currentCenter = 0
}