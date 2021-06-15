package com.gzozulin.proj

import com.gzozulin.kotlin.KotlinParser
import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.api.cyan
import com.gzozulin.minigl.techniques.SpanVisibility
import com.gzozulin.minigl.techniques.TextPage
import com.gzozulin.minigl.techniques.TextSpan
import java.io.File
import kotlin.math.abs

const val LINES_TO_SHOW = 20
const val FRAMES_PER_UPDATE = 2
const val FRAMES_TO_FINALIZE = 20000 / 16

typealias DeclCtx = KotlinParser.DeclarationContext

data class OrderedSpan(override var text: String, val order: Int, override var color: col3,
                       override var visibility: SpanVisibility) : TextSpan

enum class AnimationState {
    FIND_KEY, SYNC_UP, SCROLLING, MAKE_SPACE, NEXT_ORDER, ADVANCING, FINALIZING, FINISHED }

class ProjectorModel {
    private val projectScenario by lazy { ScenarioFile(text = File("/home/greg/ep3-shadergen/scenario").readText()) }
    private val scenarioRenderer by lazy { ScenarioRenderer(scenarioFile = projectScenario) }

    private lateinit var pages: List<ProjectorTextPage<OrderedSpan>>
    lateinit var currentPage: ProjectorTextPage<OrderedSpan>
    lateinit var minimapPage: TextPage<OrderedSpan>

    var animationState = AnimationState.FIND_KEY

    private var currentFrame = 0
    private var currentOrder = 0
    private var nextKeyFrame = 0
    private var lastFrame    = 0

    var currentPageCenter = 0
    var currentMinimapCenter = 0
    private var nextPageCenter = 0

    fun isPageReady() = ::currentPage.isInitialized

    fun renderScenario() {
        pages = scenarioRenderer.renderScenario()
        createMinimap()
    }

    private fun createMinimap() {
        val allSpans = mutableListOf<OrderedSpan>()
        pages.forEach { page ->
            allSpans.add(OrderedSpan(
                "\n-------------------------------------------------- " +
                        page.file.name +
                " --------------------------------------------------\n\n",
                0, col3().cyan(), SpanVisibility.VISIBLE))
            allSpans.addAll(page.spans)
        }
        minimapPage = TextPage(allSpans)
    }

    fun advanceScenario() {
        currentFrame++
        when (animationState) {
            AnimationState.FIND_KEY   -> findKeyFrame()
            AnimationState.SYNC_UP    -> syncWithKeyFrame()
            AnimationState.MAKE_SPACE -> makeSpace()
            AnimationState.ADVANCING  -> advanceSpans()
            AnimationState.SCROLLING  -> scrollToPageCenter()
            AnimationState.NEXT_ORDER -> nextOrder()
            AnimationState.FINALIZING -> finalizingCapture()
            AnimationState.FINISHED   -> {}
        }
    }

    private fun isNextTick() = currentFrame % FRAMES_PER_UPDATE == 0

    private fun findKeyFrame() {
        findOrderKeyFrame()
        animationState = AnimationState.SYNC_UP
    }

    private fun syncWithKeyFrame() {
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
                updateMinimapCenter(found)
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
            animationState = AnimationState.FIND_KEY
        } else {
            animationState = AnimationState.FINALIZING
        }
    }

    private fun finalizingCapture() {
        if (lastFrame == 0) {
            lastFrame = currentFrame
        }
        if (currentFrame - lastFrame >= FRAMES_TO_FINALIZE) {
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

    private fun updateMinimapCenter(span: OrderedSpan) {
        currentMinimapCenter = minimapPage.findLineNo(span)
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