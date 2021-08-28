package com.gzozulin.proj

import com.gzozulin.minigl.api.col3
import com.gzozulin.minigl.api.cyan
import com.gzozulin.minigl.tech.SpanVisibility
import com.gzozulin.minigl.tech.TextPage
import com.gzozulin.minigl.tech.TextSpan
import java.io.File
import kotlin.math.max

const val LINES_TO_SHOW = 21
const val FRAMES_TO_FINALIZE = 20000 / 16

data class OrderedSpan(override var text: String, val order: Int, override var color: col3,
                       override var visibility: SpanVisibility) : TextSpan

enum class AnimationState {
    FIND_KEY, SCROLLING, PREPARE, SYNC_UP, NEXT_ORDER, ADVANCING, FINALIZING, FINISHED }

class ProjectorModel(scenario: File) {
    private val projectScenario by lazy { ScenarioFile(text = scenario.readText()) }
    private val scenarioRenderer by lazy { ScenarioRenderer(scenarioFile = projectScenario) }

    private lateinit var pages: List<ProjectorTextPage<OrderedSpan>>
    lateinit var currentPage: ProjectorTextPage<OrderedSpan>
    lateinit var minimapPage: TextPage<OrderedSpan>

    var animationState = AnimationState.FIND_KEY

    private var currentFrame = 0
    private var currentOrder = 0
    private var currentKeyFrame = 0
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
            AnimationState.PREPARE    -> prepareForOrder()
            AnimationState.SCROLLING  -> scrollToPageCenter()
            AnimationState.ADVANCING  -> advanceSpans()
            AnimationState.NEXT_ORDER -> nextOrder()
            AnimationState.FINALIZING -> finalizingCapture()
            AnimationState.FINISHED   -> {}
        }
    }

    private fun findKeyFrame() {
        currentKeyFrame = findOrderKeyFrame(currentOrder)
        animationState = AnimationState.SYNC_UP
    }

    private fun prepareForOrder() {
        findCurrentPage()
        currentPage.spans
            .filter { it.order == currentOrder }
            .forEach { it.visibility = SpanVisibility.INVISIBLE }

        // todo: doesn't account for scrolling
        val nextOrder = getNextOrder()
        val haveFrames: Int
        if (nextOrder != null) {
            val nextKeyFrame = findOrderKeyFrame(nextOrder)
            haveFrames = max(nextKeyFrame - currentKeyFrame, 1)
        } else {
            haveFrames = FRAMES_TO_FINALIZE
        }
        val tokenCount = findInvisibleSpans().count()
        wordsPerTick = max((tokenCount.toFloat() / haveFrames.toFloat()).toInt(), 1)

        findNextPageCenter()
        animationState = AnimationState.SCROLLING
    }

    private fun findNextPageCenter() {
        val spans = findInvisibleSpans()
        val firstLine = currentPage.findLineNo(spans.first())
        val lastLine = currentPage.findLineNo(spans.last())
        nextPageCenter = firstLine + (lastLine - firstLine) / 2
    }

    private fun syncWithKeyFrame() {
        if (currentFrame >= currentKeyFrame) {
            animationState = AnimationState.PREPARE
        }
    }

    private fun advanceSpans() {
        for(i in 0 until wordsPerTick) {
            val found = findNextInvisibleSpan()
            if (found == null) {
                animationState = AnimationState.NEXT_ORDER
                break
            }
            updateMinimapCenter(found)
            found.visibility = SpanVisibility.VISIBLE
        }
    }

    private fun scrollToPageCenter() {
        when {
            nextPageCenter > currentPageCenter -> currentPageCenter++
            nextPageCenter < currentPageCenter -> currentPageCenter--
            else -> animationState = AnimationState.ADVANCING
        }
    }

    private fun getNextOrder(): Int? {
        val nextOrder = currentOrder + 1
        if (nextOrder != projectScenario.scenario.size) {
            return nextOrder
        } else {
            return null
        }
    }

    private fun nextOrder() {
        val nextOrder = getNextOrder()
        if (nextOrder != null) {
            animationState = AnimationState.FIND_KEY
            currentOrder = nextOrder
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

    private fun findInvisibleSpans() =
        currentPage.spans.filter {
            it.order == currentOrder &&
            it.visibility == SpanVisibility.INVISIBLE &&
            it.text.isNotBlank()
        }

    private fun findNextInvisibleSpan() =
        findInvisibleSpans().firstOrNull()

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

    private fun findOrderKeyFrame(order: Int): Int {
        for (scenarioNode in projectScenario.scenario) {
            if (scenarioNode.order == order) {
                return scenarioNode.frame
            }
        }
        error("Key frame not found!")
    }
}