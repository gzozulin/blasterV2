package com.gzozulin.minigl.assets2

import java.io.File

private val siblingDir = File("assets")
private val parentDir = File("../assets")
private val assets = if (siblingDir.exists()) siblingDir else parentDir

internal fun libAssetCreate(asset: String) = File(assets, asset)