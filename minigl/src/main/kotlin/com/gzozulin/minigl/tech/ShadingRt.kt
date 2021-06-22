package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.*

private val window = GlWindow()

private val matrix = constm4(mat4().orthoBox())
private val color = fragmentColorRt(namedTexCoordsV2())
private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val hitables = listOf(
    Sphere(vec3(0f, 0f, -1f), 0.5f, LambertianMaterial(col3().aquamarine())),
    Sphere(vec3(0f, -100.5f, -1f), 100f, LambertianMaterial(col3().azure())))

private fun glShadingRtMaterialType(material: RtMaterial) = when (material) {
    is LambertianMaterial -> MaterialType.LAMBERTIAN.ordinal
    else -> error("Unknown material!")
}

private data class MaterialsCollection(val lambertians: List<LambertianMaterial>, val lookup: Map<RtMaterial, Int>)
private fun glShadingRtCollectMaterials(hitables: List<Any>): MaterialsCollection {
    val lambertians = mutableListOf<LambertianMaterial>()
    hitables.forEach { hitable ->
        when (hitable) {
            is Sphere -> {
                when (hitable.material) {
                    is LambertianMaterial -> lambertians.add(hitable.material)
                    else -> error("Unknown material!")
                }
            }
            else -> error("Unknown hitable!")
        }
    }
    val distinctLambertians = lambertians.distinct()
    check(distinctLambertians.size <= MAX_LAMBERTIANS) { "Too many Lambertian materials" }
    val lookup = mutableMapOf<RtMaterial, Int>()
    distinctLambertians.forEachIndexed { index, rtMaterial ->
        lookup[rtMaterial] = index
    }
    return MaterialsCollection(distinctLambertians, lookup)
}

internal fun glShadingRtSubmitHitables(program: GlProgram, hitables: List<Any>) {
    check(hitables.size <= MAX_HITABLES) { "More hitables than defined in shader!" }
    glProgramCheckBound(program)
    val materialsCollection = glShadingRtCollectMaterials(hitables)
    var spheresCnt = 0
    var hitablesCnt = 0
    hitables.forEach { hitable ->
        when (hitable) {
            is Sphere -> {
                check(spheresCnt + 1 <= MAX_SPHERES) { "More spheres than defined in shader!" }
                glProgramArrayUniform(program, "uSpheres[%d].center", spheresCnt, hitable.center)
                glProgramArrayUniform(program, "uSpheres[%d].radius", spheresCnt, hitable.radius)
                glProgramArrayUniform(program, "uSpheres[%d].materialType", spheresCnt, glShadingRtMaterialType(hitable.material))
                glProgramArrayUniform(program, "uSpheres[%d].materialIndex", spheresCnt, materialsCollection.lookup[hitable.material]!!)
                glProgramArrayUniform(program, "uHitables[%d].type",  hitablesCnt, HitableType.SPHERE.ordinal)
                glProgramArrayUniform(program, "uHitables[%d].index", hitablesCnt, spheresCnt)
                spheresCnt++
                hitablesCnt++
            }
            else -> error("Unknown Hitable!")
        }
    }
    glProgramUniform(program, "uHitablesCnt", hitablesCnt)
    materialsCollection.lambertians.forEachIndexed { index, lambertian ->
        glProgramArrayUniform(program, "uLambertianMaterials[%d].albedo", index, lambertian.albedo)
    }
}

fun main() {
    window.create {
        glViewportBindPrev {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    glShadingFlatDraw(shadingFlat) {
                        glShadingRtSubmitHitables(shadingFlat.program, hitables)
                        var buffers = 0
                        window.show {
                            if (buffers < 2) {
                                glShadingFlatInstance(shadingFlat, rect)
                                println("Buffer: $buffers")
                                buffers++
                            }
                        }
                    }
                }
            }
        }
    }
}