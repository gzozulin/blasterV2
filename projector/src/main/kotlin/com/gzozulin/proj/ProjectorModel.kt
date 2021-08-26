package com.gzozulin.proj

import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.api.cyan
import com.gzozulin.minigl.tech.SpanVisibility
import com.gzozulin.minigl.tech.TextPage
import com.gzozulin.minigl.tech.TextSpan
import java.io.File
import kotlin.math.abs

const val LINES_TO_SHOW = 21
const val FRAMES_TO_FINALIZE = 20000 / 16

data class OrderedSpan(override var text: String, val order: Int, override var color: col3,
                       override var visibility: SpanVisibility) : TextSpan

enum class AnimationState {
    FIND_KEY, SYNC_UP, SCROLLING, MAKE_SPACE, NEXT_ORDER, ADVANCING, FINALIZING, FINISHED }

class ProjectorModel(scenario: File) {
    private val projectScenario by lazy { ScenarioFile(text = scenario.readText()) }
    private val scenarioRenderer by lazy { ScenarioRenderer(scenarioFile = projectScenario) }

    private lateinit var pages: List<ProjectorTextPage<OrderedSpan>>
    lateinit var currentPage: ProjectorTextPage<OrderedSpan>
    lateinit var minimapPage: TextPage<OrderedSpan>

    var animationState = AnimationState.FIND_KEY

    private var currentFrame = 0
    private var currentOrder = 0
    private var nextKeyFrame = 0
    private var wordsPerTick = 100
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
        for(i in 0 until wordsPerTick) {
            val found = findNextInvisibleSpan()
            if (found == null) {
                animationState = AnimationState.NEXT_ORDER
                break
            }
            updateMinimapCenter(found)
            if (checkNeedToScroll(found)) {
                animationState = AnimationState.SCROLLING
                return
            } else {
                found.visibility = SpanVisibility.VISIBLE
            }
        }
    }

    private fun scrollToPageCenter() {
        when {
            nextPageCenter > currentPageCenter -> currentPageCenter++
            nextPageCenter < currentPageCenter -> currentPageCenter--
            else -> animationState = AnimationState.ADVANCING
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

    private fun checkNeedToScroll(span: OrderedSpan): Boolean {
        val newCenter = currentPage.findLineNo(span)
        val delta = newCenter - currentPageCenter
        if (abs(delta) >= LINES_TO_SHOW) {
            nextPageCenter += delta - (LINES_TO_SHOW - 1)
            if (currentPageCenter != nextPageCenter) {
                return true
            }
        }
        return false
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