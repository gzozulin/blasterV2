package com.gzozulin.minigl.scene

import com.gzozulin.minigl.gl.GlResource
import com.gzozulin.minigl.gl.GlTexture

data class PbrMaterial (
    val albedo: GlTexture,
    val normal: GlTexture,
    val metallic: GlTexture,
    val roughness: GlTexture,
    val ao: GlTexture
) : GlResource() {
    init {
        addChildren(albedo, normal, metallic, roughness, ao)
    }
}