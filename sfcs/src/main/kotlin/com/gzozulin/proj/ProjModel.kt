package com.gzozulin.proj

import com.gzozulin.minigl.assembly.TextPage
import org.kodein.di.instance

const val LINES_TO_SHOW = 30

class ProjModel {
    private val repo: Repository by ProjApp.injector.instance()

    val page: TextPage<OrderedSpan>
        get() = repo.currentPage

    val center: Int
        get() = repo.currentCenter
}