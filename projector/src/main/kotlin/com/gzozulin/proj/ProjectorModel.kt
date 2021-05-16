package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.techniques.SpanVisibility
import com.gzozulin.minigl.techniques.TextPage
import com.gzozulin.minigl.techniques.TextSpan
import java.io.File
import kotlin.math.abs

const val LINES_TO_SHOW = 20

const val FRAMES_PER_UPDATE = 2

typealias DeclCtx = KotlinParser.DeclarationContext

data class OrderedSpan(override var text: String, val order: Int, override var color: col3,
                       override var visibility: SpanVisibility) : TextSpan

private enum class AnimationState { PREPARE, KEY_FRAME, SCROLLING, MAKE_SPACE, NEXT_ORDER, ADVANCING, FINISHED }

class ProjectorModel {
    private val projectScenario by lazy { ScenarioFile(text = File("/home/greg/ep1_model/scenario_copy").readText()) }
    private val scenarioRenderer by lazy { ScenarioRenderer(scenarioFile = projectScenario) }

    private lateinit var pages: List<ProjectorTextPage<OrderedSpan>>
    lateinit var currentPage: ProjectorTextPage<OrderedSpan>

    private var animationState = AnimationState.PREPARE

    private var currentFrame = 0
    private var currentOrder = 0
    private var nextKeyFrame = 0

    var currentPageCenter = 0
    private var nextPageCenter = 0

    fun isPageReady() = ::currentPage.isInitialized

    fun renderScenario() {
        pages = scenarioRenderer.renderScenario()
    }

    fun advanceScenario() {
        currentFrame++
        when (animationState) {
            AnimationState.PREPARE    -> findKeyFrame()
            AnimationState.KEY_FRAME  -> waitForKeyFrame()
            AnimationState.MAKE_SPACE -> makeSpace()
            AnimationState.ADVANCING  -> advanceSpans()
            AnimationState.SCROLLING  -> scrollToPageCenter()
            AnimationState.NEXT_ORDER -> nextOrder()
            AnimationState.FINISHED   -> {}
        }
    }

    private fun isNextTick() = currentFrame % FRAMES_PER_UPDATE == 0

    private fun findKeyFrame() {
        findOrderKeyFrame()
        animationState = AnimationState.KEY_FRAME
    }

    private fun waitForKeyFrame() {
        if (currentFrame >= nextKeyFrame) {
            animationState = AnimationState.MAKE_SPACE
        }
    }

    private fun makeSpace() {
        findCurrentPage()
        currentPage.spans
            .filter { it.order == currentOrder }
            .forEach { it.visibility = SpanVisibility.INVISIBLE }
        animationState = AnimationState.ADVANCING
    }

    private fun advanceSpans() {
        if (isNextTick()) {
            val found = findNextInvisibleSpan()
            if (found != null) {
                showNextInvisibleSpan(found)
            } else {
                animationState = AnimationState.NEXT_ORDER
            }
        }
    }

    private fun scrollToPageCenter() {
        if (isNextTick()) {
            when {
                nextPageCenter > currentPageCenter -> currentPageCenter++
                nextPageCenter < currentPageCenter -> currentPageCenter--
                else -> animationState = AnimationState.ADVANCING
            }
        }
    }

    private fun nextOrder() {
        currentOrder++
        if (currentOrder != projectScenario.scenario.size) {
            animationState = AnimationState.PREPARE
        } else {
            animationState = AnimationState.FINISHED
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

    private fun findOrderKeyFrame() {
        for (scenarioNode in projectScenario.scenario) {
            if (scenarioNode.order == currentOrder) {
                nextKeyFrame = scenarioNode.frame
                return
            }
        }
        error("Key frame not found!")
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