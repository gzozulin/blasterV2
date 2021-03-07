package com.gzozulin.proj

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

// todo: scenario to nodes
// todo: basic scene arrangement
// todo: example project + video

class ProjApp {
    companion object {
        val injector = DI {
            bind<Repository>() with singleton { Repository() }
            bind<CaseScenario>() with singleton { CaseScenario() }
            bind<CasePlayback>() with singleton { CasePlayback() }
            bind<ProjScene>() with singleton { ProjScene() }
            bind<ProjModel>() with singleton { ProjModel() }
        }
    }
}

private val caseScenario: CaseScenario by ProjApp.injector.instance()
private val casePlayback: CasePlayback by ProjApp.injector.instance()
private val scene: ProjScene by ProjApp.injector.instance()
private val model: ProjModel by ProjApp.injector.instance()

fun main() {
    caseScenario.renderScenario()
    casePlayback.prepareOrder()
    scene.loop()
}