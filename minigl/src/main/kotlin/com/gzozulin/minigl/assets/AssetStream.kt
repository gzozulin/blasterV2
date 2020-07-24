package com.gzozulin.minigl.assets

import java.io.File

private val assets = File("assets")

class AssetStream {
    fun openAsset(filename: String) = File(assets, filename).inputStream()
}