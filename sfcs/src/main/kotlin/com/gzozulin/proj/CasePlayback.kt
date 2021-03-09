package com.gzozulin.proj

import com.gzozulin.minigl.assembly.SpanVisibility
import org.kodein.di.instance
import kotlin.math.abs

private const val FRAMES_PER_SPAN = 3

class CasePlayback {
    private val repo: Repository by ProjApp.injector.instance()

    private var isRequestedToProceed = false
    private var isAdvancingSpans = true // spans or timeout

    private var currentFrame = 0
    private var currentOrder = 0

    fun proceed() {
        isRequestedToProceed = true
    }

    fun prepareNextOrder() {
        findCurrentPage()
        updateOrderVisibility()
    }

    private fun findCurrentPage() {
        for (renderedPage in repo.renderedPages) {
            for (span in renderedPage.spans) {
                if (span.order == currentOrder) {
                    repo.currentPage = renderedPage
                    return
                }
            }
        }
        error("Did not found next page!")
    }

    private fun updateOrderVisibility() {
        repo.currentPage.spans
            .filter { it.order == currentOrder }
            .forEach { it.visibility = SpanVisibility.INVISIBLE }
    }

    fun updateSpans() {
        if (isAdvancingSpans) {
            advanceSpans()
        } else {
            advanceTimeout()
        }
    }

    private fun advanceSpans() {
        currentFrame++
        if (currentFrame == FRAMES_PER_SPAN) {
            currentFrame = 0
            val found = findNextInvisibleSpan()
            if (found != null) {
                found.visibility = SpanVisibility.VISIBLE
                updateCenter(found)
            } else {
                isAdvancingSpans = false
            }
        }
    }

    private fun findNextInvisibleSpan() =
        repo.currentPage.spans.firstOrNull {
            it.order == currentOrder &&
                    it.visibility == SpanVisibility.INVISIBLE &&
                    it.text.isNotBlank()
        }

    private fun updateCenter(span: OrderedSpan) {
        val newCenter = repo.currentPage.findLineNo(span)
        val delta = newCenter - repo.currentCenter
        if (abs(delta) >= LINES_TO_SHOW) {
            repo.currentCenter += delta - (LINES_TO_SHOW - 1)
        }
    }

    private fun advanceTimeout() {
        if (isRequestedToProceed) {
            isRequestedToProceed = false
            isAdvancingSpans = true
            nextOrder()
            prepareNextOrder()
        }
    }

    private fun nextOrder() {
        currentOrder++
        if (currentOrder == repo.scenarioNodeCnt) {
            currentOrder = 0
            repo.renderedPages.forEach {
                it.spans.forEach { span -> span.visibility = SpanVisibility.GONE }
            }
        }
    }
}