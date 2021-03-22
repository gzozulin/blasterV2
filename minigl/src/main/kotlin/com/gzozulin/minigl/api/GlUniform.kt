package com.gzozulin.minigl.api

@Deprecated("Use assembly instead!")
enum class GlUniform(val label: String) {
    UNIFORM_MODEL_M             ("uModelM"),
    UNIFORM_PROJ_M              ("uProjectionM"),
    UNIFORM_VIEW_M              ("uViewM"),
    UNIFORM_EYE                 ("uEye"),

    UNIFORM_TEXTURE_POSITION    ("uTexPosition"),
    UNIFORM_TEXTURE_NORMAL      ("uTexNormal"),
    UNIFORM_TEXTURE_DIFFUSE     ("uTexDiffuse"),

    UNIFORM_TEXTURE_ALBEDO      ("uTexAlbedo"),
    UNIFORM_TEXTURE_METALLIC    ("uTexMetallic"),
    UNIFORM_TEXTURE_ROUGHNESS   ("uTexRoughness"),
    UNIFORM_TEXTURE_AO          ("uTexAo"),

    UNIFORM_TEXTURE_MAT_AMB_SHINE("uTexMatAmbientShine"),
    UNIFORM_TEXTURE_MAT_DIFF_TRANSP ("uTexMatDiffTransp"),
    UNIFORM_TEXTURE_MAT_SPECULAR("uTexMatSpecular"),

    UNIFORM_LIGHTS_POINT_CNT    ("uLightsPointCnt"),
    UNIFORM_LIGHTS_DIR_CNT      ("uLightsDirCnt"),
    UNIFORM_LIGHT_VECTOR        ("uLights[%d].vector"),
    UNIFORM_LIGHT_INTENSITY     ("uLights[%d].intensity"),

    UNIFORM_MAT_AMBIENT         ("uMatAmbient"),
    UNIFORM_MAT_DIFFUSE         ("uMatDiffuse"),
    UNIFORM_MAT_SPECULAR        ("uMatSpecular"),
    UNIFORM_MAT_SHINE           ("uMatShine"),
    UNIFORM_MAT_TRANSP          ("uMatTransp"),

    UNIFORM_COLOR               ("uColor"),

    UNIFORM_CHAR_INDEX          ("uCharIndex"),
    UNIFORM_CHAR_START          ("uCharStart"),
    UNIFORM_CHAR_SCALE          ("uCharScale"),

    UNIFORM_WIDTH               ("uWidth"),
    UNIFORM_HEIGHT              ("uHeight"),

    UNIFORM_FRAME_LEFT         ("uFrameLeft"),
    UNIFORM_FRAME_TOP          ("uFrameTop"),
    UNIFORM_FRAME_WIDTH        ("uFrameWidth"),
    UNIFORM_FRAME_HEIGHT       ("uFrameHeight"),

    UNIFORM_SCALE_FLAG          ("uScaleFlag"),
    UNIFORM_TRANSPARENCY_FLAG   ("uTransparencyFlag"),

    UNIFORM_DIFFUSE_ARRAY       ("uDiffuseArray[%d]"),

    UNIFORM_TILE_MAP            ("uTileMap")
}