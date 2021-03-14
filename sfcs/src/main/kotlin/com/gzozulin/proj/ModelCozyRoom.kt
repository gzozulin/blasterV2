package com.gzozulin.proj

import com.gzozulin.minigl.assembly.TextPage
import org.kodein.di.instance

class ModelCozyRoom {
    private val repo: RepoProjector by ProjectorApp.injector.instance()

    val page: TextPage<OrderedSpan>
        get() = repo.currentPage

    val center: Int
        get() = repo.currentCenter
}