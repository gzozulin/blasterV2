package com.gzozulin.sim.simulation

import com.gzozulin.sim.entity.Repository
import com.gzozulin.sim.system.PhysicsSystem
import com.gzozulin.sim.system.PresentationSystem
import com.gzozulin.sim.system.ProcreationSystem
import com.gzozulin.minigl.api.GlWindow
import com.gzozulin.minigl.api.MouseButton
import com.gzozulin.minigl.api.glUse

// name: Endless Adventure, Mars, Station

// design 0: garbage-free
// design 1: one system == one mechanic, small independent systems
// design 2: no broadcasts/messages/etc, only reactive observers
// design 3: execution sequential with multithreading by forking
// design 4: generating rules known, actual actors is not generated for unknown regions
// design 5: systems can have any caches, but the only source of truth is repo
// design 6: systems can actually hold a ref to an interesting actor, not the other way
// design 7: all actors in a single bucket: simple storage/access, rules are equal
// design 7: all systems/components must be impl. generally, reusable between the scenes

// todo: access to repo should be synchronized
// todo: create nav graph with nodes, when the partition is created/generated
// todo: create visibility graph - same as nav, but for visibility
// todo: real intersection of frustum and zero plane

const val WORLD_LEFT = -160f
const val WORLD_RIGHT = 160f
const val WORLD_FRONT = 160f
const val WORLD_BACK = -160f

private val profiler = Profiler()

private val window = GlWindow()

private val procreationSystem = ProcreationSystem()
private val presentationSystem = PresentationSystem()
private val physicsSystem = PhysicsSystem()
private val gcSystem = Repository.GcSystem()

private var mouseLook = false

fun main() {
    window.create(isFullscreen = false, isHoldingCursor = false) {
        window.buttonCallback = { button, pressed ->
            if (button == MouseButton.LEFT) {
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