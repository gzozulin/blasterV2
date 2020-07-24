package com.gzozulin.sim.simulation

import com.gzozulin.sim.entity.Repository
import com.gzozulin.sim.system.PhysicsSystem
import com.gzozulin.sim.system.PresentationSystem
import com.gzozulin.sim.system.ProcreationSystem
import com.gzozulin.minigl.gl.GlWindow
import com.gzozulin.minigl.gl.glUse

private val profiler = Profiler()

private val window = GlWindow()

private val procreationSystem = ProcreationSystem()
private val presentationSystem = PresentationSystem()
private val physicsSystem = PhysicsSystem()
private val gcSystem = Repository.GcSystem()

private var mouseLook = false

fun main() {
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.buttonCallback = { key, pressed ->
            if (key == 0) {
                mouseLook = pressed
            }
        }
        window.deltaCallback = { delta ->
            if (mouseLook) {
                presentationSystem.onCursorDelta(delta)
            }
        }
        window.keyCallback = { key, pressed ->
            presentationSystem.onKeyPressed(key, pressed)
        }
        glUse(presentationSystem) {
            procreationSystem.createFields()
            window.show {
                profiler.frame {
                    profiler.section(ProfilerSection.PROCREA) { procreationSystem.procreateSoccerBalls() }
                    profiler.section(ProfilerSection.PHYSICS) { physicsSystem.updatePhysics() }
                    profiler.section(ProfilerSection.PRESENT) { presentationSystem.updateAndDrawFrame() }
                    profiler.section(ProfilerSection.GARBAGE) { gcSystem.performGC() }
                }
            }
        }
    }
    profiler.printAll()
}