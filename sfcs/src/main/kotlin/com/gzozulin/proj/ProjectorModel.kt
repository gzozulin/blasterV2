package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.assembly.SpanVisibility
import com.gzozulin.minigl.assembly.TextPage
import com.gzozulin.minigl.assembly.TextSpan
import org.antlr.v4.runtime.*
import kotlin.math.abs

const val LINES_TO_SHOW = 20

const val FRAMES_PER_SPAN = 2
const val FRAMES_PER_LINE = 2

typealias DeclCtx = KotlinParser.DeclarationContext

private val exampleScenario = """
    # Pilot scenario

    alias file=/home/greg/blaster/sfcs/src/main/kotlin/com/gzozulin/proj/ProjectorModel.kt
    alias class=ProjectorModel

    0   file/class
    1   file/class/projectScenario
    2   file/class/scenarioRenderer
    3   file/class/renderScenario
    4   file/class/advanceSpans
    5   file/class/advanceScenario
    6   file/class/findCurrentPage
    7   file/class/makeOrderInvisible
    8   file/class/findOrderFrame
    9   file/class/showNextInvisibleSpan
    10  file/LINES_TO_SHOW
    11  file/class/prepareOrder
    12  file/DeclCtx
    13  file/exampleScenario
""".trimIndent()

data class OrderedToken(val order: Int, val token: Token)
data class OrderedSpan(override val text: String, val order: Int, override val color: col3,
                       override var visibility: SpanVisibility) : TextSpan

private enum class AnimationState { WAITING_KEY_FRAME, SCROLLING, ADVANCING_SPANS }

class ProjectorModel {
    private val projectScenario by lazy { ScenarioFile(text = exampleScenario) }
    private val scenarioRenderer by lazy { ScenarioRenderer(scenarioFile = projectScenario) }

    private lateinit var pages: List<TextPage<OrderedSpan>>
    lateinit var currentPage: TextPage<OrderedSpan>
    var currentPageCenter = 0
    private var expectedPageCenter = 0

    private var animationState = AnimationState.WAITING_KEY_FRAME

    private var currentFrame = 0
    private var currentOrder = 0
    private var nextKeyFrame = 0

    fun renderScenario() {
        pages = scenarioRenderer.renderScenario()
        prepareOrder()
    }

    fun advanceScenario() {
        currentFrame++
        when (animationState) {
            AnimationState.WAITING_KEY_FRAME -> waitForKeyFrame()
            AnimationState.SCROLLING -> scrollToPageCenter()
            AnimationState.ADVANCING_SPANS -> advanceSpans()
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

    private fun advanceSpans() {
        if (currentFrame % FRAMES_PER_SPAN == 0) {
            val found = findNextInvisibleSpan()
            if (found != null) {
                showNextInvisibleSpan(found)
            } else {
                val haveNext = nextOrder()
                if (haveNext) {
                    prepareOrder()
                } else {
                    nextKeyFrame = Int.MAX_VALUE
                }
                animationState = AnimationState.WAITING_KEY_FRAME
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
            expectedPageCenter += delta - (LINES_TO_SHOW - 1)
            if (currentPageCenter != expectedPageCenter) {
                animationState = AnimationState.SCROLLING
                return // need to scroll first
            }
        }
        span.visibility = SpanVisibility.VISIBLE
    }

    private fun scrollToPageCenter() {
        if (currentFrame % FRAMES_PER_LINE == 0) {
            when {
                expectedPageCenter > currentPageCenter -> currentPageCenter++
                expectedPageCenter < currentPageCenter -> currentPageCenter--
                else -> animationState = AnimationState.ADVANCING_SPANS
            }
        }
    }

    private fun waitForKeyFrame() {
        if (currentFrame >= nextKeyFrame) {
            animationState = AnimationState.ADVANCING_SPANS
        }
    }

    private fun nextOrder(): Boolean {
        currentOrder++
        return currentOrder != projectScenario.scenario.size
    }
}