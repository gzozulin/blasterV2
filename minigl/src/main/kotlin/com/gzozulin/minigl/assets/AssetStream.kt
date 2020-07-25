package com.gzozulin.minigl.assets

import java.io.File

private val siblingDir = File("assets")
private val parentDir = File("../assets")
private val assets = if (siblingDir.exists()) siblingDir else parentDir

val assetStream = AssetStream()

class AssetStream internal constructor() {
    fun openAsset(filename: String) = File(assets, filename).inputStream()
}