package com.gzozulin.minigl.scene

import com.gzozulin.minigl.gl.vec3

data class Material(val ambient: vec3, val diffuse: vec3, val specular: vec3, val shine: Float, val transparency: Float = 1f) {
    companion object {
        val CONCRETE        = Material(vec3(0.329412f, 0.223529f, 0.027451f), vec3(0.75f, 0.75f, 0.73f), vec3(0.01f, 0.01f, 0.01f), 1f)
        val BRASS           = Material(vec3(0.329412f, 0.223529f, 0.027451f), vec3(0.780392f, 0.568627f, 0.113725f), vec3(0.992157f, 0.941176f, 0.807843f), 27.8974f)
        val BRONZE          = Material(vec3(0.2125f, 0.1275f, 0.054f), vec3(0.714f, 0.4284f, 0.18144f), vec3(0.393548f, 0.271906f, 0.166721f), 25.6f)
        val POLISHED_BRONZE = Material(vec3(0.25f, 0.148f, 0.06475f), vec3(0.4f, 0.2368f, 0.1036f), vec3(0.774597f, 0.458561f, 0.200621f), 76.8f)
        val CHROME          = Material(vec3(0.25f, 0.25f, 0.25f), vec3(0.4f, 0.4f, 0.4f), vec3(0.774597f, 0.774597f, 0.774597f), 76.8f)
        val COPPER          = Material(vec3(0.19125f, 0.0735f, 0.0225f), vec3(0.7038f, 0.27048f, 0.0828f), vec3(0.256777f, 0.137622f, 0.086014f), 12.8f)
        val POLISHED_COPPER = Material(vec3(0.2295f, 0.08825f, 0.0275f), vec3(0.5508f, 0.2118f, 0.066f), vec3(0.580594f, 0.223257f, 0.0695701f), 51.2f)
        val GOLD            = Material(vec3(0.24725f, 0.1995f, 0.0745f), vec3(0.75164f, 0.60648f, 0.22648f), vec3(0.628281f, 0.555802f, 0.366065f), 51.2f)
        val POLISHED_GOLD   = Material(vec3(0.24725f, 0.2245f, 0.0645f), vec3(0.34615f, 0.3143f, 0.0903f), vec3(0.797357f, 0.723991f, 0.208006f), 83.2f)
        val TIN             = Material(vec3(0.105882f, 0.058824f, 0.113725f), vec3(0.427451f, 0.470588f, 0.541176f), vec3(0.333333f, 0.333333f, 0.521569f), 9.84615f)
        val SILVER          = Material(vec3(0.19225f, 0.19225f, 0.19225f), vec3(0.50754f, 0.50754f, 0.50754f), vec3(0.508273f, 0.508273f, 0.508273f), 51.2f)
        val POLISHED_SILVER = Material(vec3(0.23125f, 0.23125f, 0.23125f), vec3(0.2775f, 0.2775f, 0.2775f), vec3(0.773911f, 0.773911f, 0.773911f), 89.6f)
        val EMERALD         = Material(vec3(0.0215f, 0.1745f, 0.0215f), vec3(0.07568f, 0.61424f, 0.07568f), vec3(0.633f, 0.727811f, 0.633f), 76.8f, 0.55f)
        val JADE            = Material(vec3(0.135f, 0.2225f, 0.1575f), vec3(0.54f, 0.89f, 0.63f), vec3(0.316228f, 0.316228f, 0.316228f), 12.8f, 0.95f)
        val OBSIDIAN        = Material(vec3(0.05375f, 0.05f, 0.06625f), vec3(0.18275f, 0.17f, 0.22525f), vec3(0.332741f, 0.328634f, 0.346435f), 38.4f, 0.82f)
        val PERL            = Material(vec3(0.25f, 0.20725f, 0.20725f), vec3(1.0f, 0.829f, 0.829f), vec3(0.296648f, 0.296648f, 0.296648f), 11.264f, 0.922f)
        val RUBY            = Material(vec3(0.1745f, 0.01175f, 0.01175f), vec3(0.61424f, 0.04136f, 0.04136f), vec3(0.727811f, 0.626959f, 0.626959f), 76.8f, 0.55f)
        val TURQUOISE       = Material(vec3(0.1f, 0.18725f, 0.1745f), vec3(0.396f, 0.74151f, 0.69102f), vec3(0.297254f, 0.30829f, 0.306678f), 12.8f, 0.8f)
        val BLACK_PLASTIC   = Material(vec3(0.0f, 0.0f, 0.0f), vec3(0.01f, 0.01f, 0.01f), vec3(0.5f, 0.50f, 0.50f), 32.0f)
        val CIAN_PLASTIC    = Material(vec3(0.0f, 0.1f, 0.06f), vec3(0.0f, 0.509803f, 0.509803f), vec3(0.5f, 0.50f, 0.50f), 32.0f)
        val GREEN_PLASTIC   = Material(vec3(0.0f, 0.0f, 0.0f), vec3(0.1f, 0.35f, 0.1f), vec3(0.45f, 0.55f, 0.45f), 32.0f)
        val RED_PLASTIC     = Material(vec3(0.0f, 0.0f, 0.0f), vec3(0.5f, 0.0f, 0.0f), vec3(0.7f, 0.6f, 0.6f), 32.0f)
        val WHITE_PLASTIC   = Material(vec3(0.0f, 0.0f, 0.0f), vec3(0.55f, 0.55f, 0.55f), vec3(0.70f, 0.70f, 0.70f), 32.0f)
        val YELLOW_PLASTIC  = Material(vec3(0.0f, 0.0f, 0.0f), vec3(0.5f, 0.5f, 0.0f), vec3(0.60f, 0.60f, 0.50f), 32.0f)
        val BLACK_RUBBER    = Material(vec3(0.02f, 0.02f, 0.02f), vec3(0.01f, 0.01f, 0.01f), vec3(0.4f, 0.4f, 0.4f), 10.0f)
        val CIAN_RUBBER     = Material(vec3(0.0f, 0.05f, 0.05f), vec3(0.4f, 0.5f, 0.5f), vec3(0.04f, 0.7f, 0.7f), 10.0f)
        val GREEN_RUBBER    = Material(vec3(0.0f, 0.05f, 0.0f), vec3(0.4f, 0.5f, 0.4f), vec3(0.04f, 0.7f, 0.04f), 10.0f)
        val RED_RUBBER      = Material(vec3(0.05f, 0.0f, 0.0f), vec3(0.5f, 0.4f, 0.4f), vec3(0.7f, 0.04f, 0.04f), 10.0f)
        val WHITE_RUBBER    = Material(vec3(0.05f, 0.05f, 0.05f), vec3(0.5f, 0.5f, 0.5f), vec3(0.7f, 0.7f, 0.7f), 10.0f)
        val YELLOW_RUBBER   = Material(vec3(0.05f, 0.05f, 0.0f), vec3(0.5f, 0.5f, 0.4f), vec3(0.7f, 0.7f, 0.04f), 10.0f)

        val MATERIALS = mapOf(
            "concrete"        to CONCRETE,
            "brass"           to BRASS,
            "bronze"          to BRONZE,
            "polished bronze" to POLISHED_BRONZE,
            "chrome"          to CHROME,
            "copper"          to COPPER,
            "polished copper" to POLISHED_COPPER,
            "gold"            to GOLD,
            "polished gold"   to POLISHED_GOLD,
            "tin"             to TIN,
            "silver"          to SILVER,
            "polished silver" to POLISHED_SILVER,
            "emerald"         to EMERALD,
            "jade"            to JADE,
            "obsidian"        to OBSIDIAN,
            "perl"            to PERL,
            "ruby"            to RUBY,
            "turquoise"       to TURQUOISE,
            "black plastic"   to BLACK_PLASTIC,
            "cian plastic"    to CIAN_PLASTIC,
            "green plastic"   to GREEN_PLASTIC,
            "red plastic"     to RED_PLASTIC,
            "white plastic"   to WHITE_PLASTIC,
            "yellow plastic"  to YELLOW_PLASTIC,
            "black rubber"    to BLACK_RUBBER,
            "cian rubber"     to CIAN_RUBBER,
            "green rubber"    to GREEN_RUBBER,
            "red rubber"      to RED_RUBBER,
            "white rubber"    to WHITE_RUBBER,
            "yellow rubber"   to YELLOW_RUBBER
        )
    }
}