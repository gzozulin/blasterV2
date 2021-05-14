package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.techniques.SpanVisibility
import com.gzozulin.minigl.techniques.TextPage
import com.gzozulin.minigl.techniques.TextSpan
import java.io.File
import kotlin.math.abs

const val LINES_TO_SHOW = 20

const val FRAMES_PER_SPAN = 2
const val FRAMES_PER_LINE = 2

typealias DeclCtx = KotlinParser.DeclarationContext

data class OrderedSpan(override var text: String, val order: Int, override var color: col3,
                       override var visibility: SpanVisibility
) : TextSpan

private enum class AnimationState { ADVANCING, KEY_FRAME, SCROLLING }

class ProjectorModel {
    private val projectScenario by lazy { ScenarioFile(text = File("/home/greg/ep1_model/scenario").readText()) }
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
        findOrderKeyFrame(projectScenario.scenario)
        makeOrderInvisible()
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

    private fun findOrderKeyFrame(scenario: List<ScenarioNode>) {
        for (scenarioNode in scenario) {
            if (scenarioNode.order == currentOrder) {
                nextKeyFrame = scenarioNode.frame
                return
            }
        }
        error("Key frame not found!")
    }

    private fun makeOrderInvisible() {
        currentPage.spans
            .filter { it.order == currentOrder }
            .forEach { it.visibility = SpanVisibility.INVISIBLE }
    }

    private fun waitForKeyFrame() {
        if (currentFrame >= nextKeyFrame) {
            animationState = AnimationState.ADVANCING
        }
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
}

private val exampleSpans = TextPage(listOf(
    OrderedSpan(text = "What is Lorem Ipsum?",
        order = 0, color = col3().red(), visibility = SpanVisibility.VISIBLE),
    OrderedSpan(text = "Lorem Ipsum is",
        order = 1, color = col3().green(), visibility = SpanVisibility.INVISIBLE),
    OrderedSpan(text = "simply dummy text",
        order = 2, color = col3().blue(), visibility = SpanVisibility.GONE),
    OrderedSpan(text = "of the printing and",
        order = 3, color = col3().orange(), visibility = SpanVisibility.VISIBLE),
    OrderedSpan(text = "typesetting industry",
        order = 4, color = col3().cyan(), visibility = SpanVisibility.INVISIBLE),
))