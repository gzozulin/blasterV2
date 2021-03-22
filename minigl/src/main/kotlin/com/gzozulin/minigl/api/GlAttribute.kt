package com.gzozulin.minigl.api

// divisor - how many attributes per draw call for instancing (0 - default == 1 item per vertex)
enum class GlAttribute(val size: Int, val location: Int, val divisor: Int = 0) {
    ATTRIBUTE_POSITION  (3, 0),
    ATTRIBUTE_TEXCOORD  (2, 1),
    ATTRIBUTE_NORMAL    (3, 2),
    ATTRIBUTE_COLOR     (3, 3),

    ATTRIBUTE_BILLBOARD_POSITION    (3, 4, 1),
    ATTRIBUTE_BILLBOARD_SCALE       (1, 5, 1),
    ATTRIBUTE_BILLBOARD_TRANSPARENCY(1, 6, 1);
}