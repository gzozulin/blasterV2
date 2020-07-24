package com.gzozulin.sim.simulation

import kotlin.system.measureTimeMillis

private const val MAX_PROFILER_FRAMES = 600

enum class ProfilerSection(val depth: Int) {
    SUMMARY(0),
    GARBAGE(1),
    PROCREA(1),
    PHYSICS(1),
    PRESENT(1),
}

private class Frame(val sections: Array<Long>)

class Profiler {
    private var currentFrame = 0
    private val frames = mutableListOf<Frame>()

    init {
        for (i in 1..MAX_PROFILER_FRAMES) {
            frames.add(Frame(Array(ProfilerSection.values().size) { 0L }))
        }
    }

    fun frame(frame: () -> Unit) {
        frames[currentFrame].sections[ProfilerSection.SUMMARY.ordinal] = measureTimeMillis(frame)
        currentFrame++
        currentFrame %= MAX_PROFILER_FRAMES
        if (currentFrame == 0) {
            printAll()
        }
    }

    fun section(section: ProfilerSection, work: () -> Unit) {
        frames[currentFrame].sections[section.ordinal] = measureTimeMillis(work)
    }

    fun printAll() {
        ProfilerSection.values().forEach { section ->
            var tabs = ""
            for (i in 0 until section.depth) {
                tabs += "\t"
            }
            println(tabs + printSection(section))
        }
    }

    private fun printSection(section: ProfilerSection): String {
        var min = Long.MAX_VALUE
        var max = Long.MIN_VALUE
        var avg = 0L
        frames.forEach { frame ->
            val value = frame.sections[section.ordinal]
            if (value < min) {
                min = value
            }
            if (value > max) {
                max = value
            }
            avg += value
        }
        avg /= frames.size
        return "${section.name}: avg: ${from16ms(avg)}% min: ${from16ms(min)}% max: ${from16ms(max)}%"
    }

    private fun from16ms(time: Long) = time.toFloat() / 16f * 100f
}