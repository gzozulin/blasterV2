package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.assembly.SpanVisibility
import com.gzozulin.minigl.assembly.TextSpan
import org.antlr.v4.runtime.Token
import kotlin.math.abs

const val LINES_TO_SHOW = 20

const val FRAMES_PER_SPAN = 2
const val FRAMES_PER_LINE = 2

typealias DeclCtx = KotlinParser.DeclarationContext

private val exampleScenario = """
    # Pilot scenario
    alias file1=/home/greg/blaster/projector/src/main/kotlin/com/gzozulin/proj/ProjectorModel.kt
    alias file2=/home/greg/blaster/projector/src/main/kotlin/com/gzozulin/proj/ScenarioRenderer.kt
    alias class1=ProjectorModel
    alias class2=ScenarioRenderer

    0   file1/class1
    1   file1/class1/projectScenario
    2   file1/class1/scenarioRenderer
    3   file1/class1/pages
    
    4   file2/class2
    5   file2/class2/splitPerFile
    6   file2/class2/renderConcurrently
    7   file2/class2/renderFile
    8   file2/class2/enforceAllNodesClaimed
    
    9   file1/class1/currentPage
    10  file1/class1/renderScenario
    11  file1/class1/advanceScenario
    12  file1/class1/advanceSpans
    13  file1/class1/findCurrentPage
    14  file1/class1/makeOrderInvisible
    15  file1/class1/findOrderFrame
    16  file1/class1/showNextInvisibleSpan
    17  file1/class1/scrollToPageCenter
    18  file1/class1/waitForKeyFrame
    19  file1/class1/nextOrder
    20  file1/LINES_TO_SHOW
    21  file1/class1/prepareOrder
    22  file1/DeclCtx
    23  file1/exampleScenario
""".trimIndent()

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(override var text: String, val order: Int, override var color: col3,
                       override var visibility: SpanVisibility) : TextSpan

private enum class AnimationState { ADVANCING, KEY_FRAME, SCROLLING }

class ProjectorModel {
    private val projectScenario by lazy { ScenarioFile(text = exampleScenario) }
    private val scenarioRenderer by lazy { ScenarioRenderer(scenarioFile = projectScenario) }

    private lateinit var pages: List<ProjectorTextPage<OrderedSpan>>
    lateinit var currentPage: ProjectorTextPage<OrderedSpan>

    private var animationState = AnimationState.KEY_FRAME

    private var currentFrame = 0
    private var currentOrder = 0
    private var nextKeyFrame = 0

    var currentPageCenter = 0
    private var nextPageCenter = 0

    fun isPageReady() = ::currentPage.isInitialized

    fun renderScenario() {
        pages = scenarioRenderer.renderScenario()
        prepareOrder()
    }

    fun advanceScenario() {
        currentFrame++
        when (animationState) {
            AnimationState.ADVANCING -> advanceSpans()
            AnimationState.KEY_FRAME -> waitForKeyFrame()
            AnimationState.SCROLLING -> scrollToPageCenter()
        }
    }

    private fun advanceSpans() {
        if (currentFrame % FRAMES_PER_SPAN == 0) {
            val found = findNextInvisibleSpan()
            if (found != null) {
                showNextInvisibleSpan(found)
            } else {
                nextOrder()
            }
        }
    }

    private fun findNextInvisibleSpan() =
        currentPage.spans.firstOrNull {
            it.order == currentOrder &&
                    it.visibility == SpanVisibility.INVISIBLE &&
                    it.text.isNotBlank()
        }

    private fun showNextInvisibleSpan(span: OrderedSpan) {
        val newCenter = currentPage.findLineNo(span)
        val delta = newCenter - currentPageCenter
        if (abs(delta) >= LINES_TO_SHOW) {
            nextPageCenter += delta - (LINES_TO_SHOW - 1)
            if (currentPageCenter != nextPageCenter) {
                animationState = AnimationState.SCROLLING
                return // need to scroll first
            }
        }
        span.visibility = SpanVisibility.VISIBLE
    }

    private fun scrollToPageCenter() {
        if (currentFrame % FRAMES_PER_LINE == 0) {
            when {
                nextPageCenter > currentPageCenter -> currentPageCenter++
                nextPageCenter < currentPageCenter -> currentPageCenter--
                else -> animationState = AnimationState.ADVANCING
            }
        }
    }

    private fun waitForKeyFrame() {
        if (currentFrame >= nextKeyFrame) {
            animationState = AnimationState.ADVANCING
        }
    }

    private fun nextOrder() {
        currentOrder++
        animationState = AnimationState.KEY_FRAME
        if (currentOrder != projectScenario.scenario.size) {
            prepareOrder()
        } else {
            nextKeyFrame = Int.MAX_VALUE
        }
    }

    private fun prepareOrder() {
        findCurrentPage()
        makeOrderInvisible()
        findOrderFrame(projectScenario.scenario)
    }

    private fun findCurrentPage() {
        for (renderedPage in pages) {
            for (span in renderedPage.spans) {
                if (span.order == currentOrder) {
                    currentPage = renderedPage
                    return
                }
            }
        }
        error("Next page not found!")
    }

    private fun makeOrderInvisible() {
        currentPage.spans
            .filter { it.order == currentOrder }
            .forEach { it.visibility = SpanVisibility.INVISIBLE }
    }

    private fun findOrderFrame(scenario: List<ScenarioNode>) {
        for (scenarioNode in scenario) {
            if (scenarioNode.order == currentOrder) {
                nextKeyFrame = scenarioNode.frame
                return
            }
        }
        error("Key frame not found!")
    }
}