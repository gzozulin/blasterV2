package com.gzozulin.minigl.scene

import com.gzozulin.minigl.assets.Material
import com.gzozulin.minigl.api.GlTexture

data class PbrMaterial (
    val albedo: GlTexture,
    val normal: GlTexture,
    val metallic: GlTexture,
    val roughness: GlTexture,
    val ao: GlTexture) : Material() {

    init {
        addChildren(albedo, normal, metallic, roughness, ao)
    }
}