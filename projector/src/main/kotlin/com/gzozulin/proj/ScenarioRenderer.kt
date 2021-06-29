package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinLexer
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.tech.TextPage
import com.gzozulin.minigl.tech.TextSpan
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import java.io.File
import kotlin.streams.toList

private enum class Language { KOTLIN, C }

internal data class OrderedToken(val order: Int, val token: Token)
class ProjectorTextPage<T : TextSpan>(val file: File, spans: List<T>) : TextPage<T>(spans)

class ScenarioRenderer(private val scenarioFile: ScenarioFile) {
    private lateinit var pages: MutableList<ProjectorTextPage<OrderedSpan>>

    fun renderScenario(): List<ProjectorTextPage<OrderedSpan>> {
        pages = mutableListOf()
        val nodesToFiles = splitPerFile()
        renderConcurrently(nodesToFiles)
        return pages
    }

    private fun splitPerFile(): Map<File, List<ScenarioNode>> {
        val result = mutableMapOf<File, MutableList<ScenarioNode>>()
        for (scenarioNode in scenarioFile.scenario) {
            if (!result.containsKey(scenarioNode.file)) {
                result[scenarioNode.file] = mutableListOf()
            }
            result[scenarioNode.file]!!.add(scenarioNode)
        }
        return result
    }

    private fun renderConcurrently(nodesToFiles: Map<File, List<ScenarioNode>>) = runBlocking {
        val claimedNodes = mutableListOf<ScenarioNode>()
        val deferred = mutableListOf<Deferred<ProjectorTextPage<OrderedSpan>>>()
        nodesToFiles.forEach { deferred.add(async { renderFile(it.key, it.value, claimedNodes)}) }
        pages.addAll(deferred.awaitAll())
        enforceAllNodesClaimed(claimedNodes)
    }

    private fun renderFile(file: File, nodes: List<ScenarioNode>,
                           claimedNodes: MutableList<ScenarioNode>): ProjectorTextPage<OrderedSpan> {
        return when (selectLanguage(file)) {
            Language.KOTLIN -> renderKotlinFile(file, nodes, claimedNodes)
            Language.C -> renderCFile(file, nodes, claimedNodes)
        }
    }

    private fun selectLanguage(file: File) = when {
        file.name.endsWith(".kt") -> Language.KOTLIN
        file.name.endsWith(".c") -> Language.C
        else -> error("Unknown language! $file")
    }

    private fun enforceAllNodesClaimed(claimedNodes: List<ScenarioNode>) {
        if (claimedNodes.size != scenarioFile.scenario.size) {
            val unclaimed = mutableListOf<ScenarioNode>()
            unclaimed.addAll(scenarioFile.scenario)
            unclaimed.removeAll(claimedNodes)
            error("Unclaimed nodes left! $unclaimed")
        }
    }
}

internal fun ParserRuleContext.define(tokens: CommonTokenStream, ws: Int, nl: Int): List<Token> {
    val start = start.tokenIndex.withLeftWS(tokens, ws)
    val stop = stop.tokenIndex.withRightNL(tokens, nl)
    return tokens.get(start, stop)
}

internal fun Int.withLeftWS(tokens: CommonTokenStream, ws: Int): Int {
    var result = this
    while (result > 0 && tokens.get(result - 1).type == ws) {
        result--
    }
    return result
}

internal fun Int.withRightNL(tokens: CommonTokenStream, nl: Int): Int {
    var result = this
    while (result <= tokens.size() && tokens.get(result + 1).type == nl) {
        result++
    }
    return result
}

internal fun List<Token>.withOrder(order: Int) = stream().map { OrderedToken(order, it) }.toList()

internal fun ParserRuleContext.select(tokens: List<Token>) = tokens[start.tokenIndex]

internal val darkula_white        = col3(0.659f, 0.718f, 0.776f)
internal val darkula_orange       = col3(0.706f, 0.427f, 0.192f)
internal val darkula_blue         = col3(0.216f, 0.416f, 0.824f)
internal val darkula_light_blue   = col3(0.314f, 0.553f, 0.631f)
internal val darkula_green        = col3(0.282f, 0.451f, 0.337f)
internal val darkula_yellow       = col3(0.937f, 0.675f, 0.306f)
internal val darkula_purple       = col3(0.596f, 0.463f, 0.667f)
internal val darkula_red          = col3(0.780f, 0.329f, 0.314f)