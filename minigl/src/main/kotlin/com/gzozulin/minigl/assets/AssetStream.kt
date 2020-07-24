package com.gzozulin.minigl.assets

import java.io.File

private val siblingDir = File("assets")
private val parentDir = File("../assets")
private val assets = if (siblingDir.exists()) siblingDir else parentDir

class AssetStream {
    fun openAsset(filename: String) = File(assets, filename).inputStream()
}