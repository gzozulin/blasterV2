package com.gzozulin.minigl.techniques

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

private val billboardsFile = File("frames/billboards")

class BillboardsTest {
    @Test
    fun shouldHaveSameFirstFrame() {
        val billboardsApp = BillboardsApp()
        billboardsApp.launch(oneFrame = true)
        val array = ByteArray(billboardsApp.firstFrame.remaining())
        billboardsApp.firstFrame.get(array)
        if (!billboardsFile.exists()) {
            billboardsFile.writeBytes(array)
        }
        assertThat(billboardsFile.readBytes().contentEquals(array), `is`(true))
    }
}