package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.scene.*

private const val FRAMES_TO_CAPTURE = 3

private val window = GlWindow(isFullscreen = true)
private val capturer = Capturer(window)

private val controller = ControllerScenic(
    positions = listOf(
        vec3(-6f, 1f, -6f),
        vec3( 6f, 1f, -6f),
        vec3( 6f, 1f,  6f),
        vec3(-6f, 1f,  6f),
    ),
    points = listOf(vec3()))

private val sampleCnt = consti(32)
private val rayBounces = consti(4)

private val eye = unifv3()
private val center = unifv3()
private val up = constv3(vec3().up())

private val fovy = constf(radf(90.0f))
private val aspect = constf(window.width.toFloat() / window.height.toFloat())
private val aperture = constf(0f)
private val focusDist = constf(1f)

private val matrix = constm4(mat4().orthoBox())
private val color = fragmentColorRt(
    sampleCnt, rayBounces,
    eye, center, up,
    fovy, aspect, aperture, focusDist,
    namedTexCoordsV2())

private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val lambertians = (1..10).map { LambertianMaterial(vec3().rand()) }.toList()
private val metallics = (1..10).map { MetallicMaterial(vec3().rand()) }.toList()
private val dielectrics = (1..10).map { DielectricMaterial(randf(1f, 3f)) }.toList()

private fun sphereRandom() = Sphere(vec3().rand(vec3(-5f, 0.2f, -5f), vec3(5f, 0.2f, 5f)), 0.2f, when(randi(3)) {
    0 -> lambertians.random()
    1 -> metallics.random()
    2 -> dielectrics.random()
    else -> error("wtf?!")
})

private val hitables = listOf(
    Sphere(vec3(0f, -1000f, 0f), 1000f, LambertianMaterial(col3(0.5f))),
    Sphere(vec3(0f, 1f, 0f), 1f, DielectricMaterial(1.5f)),
    Sphere(vec3(-4f, 1f, 0f), 1f, LambertianMaterial(vec3(0.4f, 0.2f, 0.1f))),
    Sphere(vec3(4f, 1f, 0f), 1f, MetallicMaterial(vec3(0.7f, 0.6f, 0.5f))),
    *(1..100).map { sphereRandom() }.toTypedArray()
)

private fun glShadingRtMaterialType(material: RtMaterial) = when (material) {
    is LambertianMaterial -> MaterialType.LAMBERTIAN.ordinal
    is MetallicMaterial -> MaterialType.METALLIC.ordinal
    is DielectricMaterial -> MaterialType.DIELECTRIC.ordinal
    else -> error("Unknown material!")
}

private data class MaterialsCollection(val lambertians: List<LambertianMaterial>,
                                       val metallics: List<MetallicMaterial>,
                                       val dielectrics: List<DielectricMaterial>,
                                       val lookup: Map<RtMaterial, Int>)

private fun glShadingRtCollectMaterials(hitables: List<Any>): MaterialsCollection {
    val lambertians = mutableListOf<LambertianMaterial>()
    val metallics = mutableListOf<MetallicMaterial>()
    val dielectrics = mutableListOf<DielectricMaterial>()
    hitables.forEach { hitable ->
        when (hitable) {
            is Sphere -> {
                when (hitable.material) {
                    is LambertianMaterial -> lambertians.add(hitable.material)
                    is MetallicMaterial -> metallics.add(hitable.material)
                    is DielectricMaterial -> dielectrics.add(hitable.material)
                    else -> error("Unknown material!")
                }
            }
            else -> error("Unknown hitable!")
        }
    }

    val distinctLambertians = lambertians.distinct()
    check(distinctLambertians.size <= MAX_LAMBERTIANS) { "Too many Lambertian materials" }
    val distinctMetallics = metallics.distinct()
    check(distinctMetallics.size <= MAX_METALLICS) { "Too many Metallic materials" }
    val distinctDielectrics = dielectrics.distinct()
    check(distinctDielectrics.size <= MAX_DIELECTRICS) { "Too many Dielectric materials" }

    val lookup = mutableMapOf<RtMaterial, Int>()
    distinctLambertians.forEachIndexed { index, rtMaterial ->
        lookup[rtMaterial] = index
    }
    distinctMetallics.forEachIndexed { index, rtMaterial ->
        lookup[rtMaterial] = index
    }
    distinctDielectrics.forEachIndexed { index, rtMaterial ->
        lookup[rtMaterial] = index
    }
    return MaterialsCollection(distinctLambertians, distinctMetallics, distinctDielectrics, lookup)
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
    materialsCollection.metallics.forEachIndexed { index, metallic ->
        glProgramArrayUniform(program, "uMetallicMaterials[%d].albedo", index, metallic.albedo)
    }
    materialsCollection.dielectrics.forEachIndexed { index, metallic ->
        glProgramArrayUniform(program, "uDielectricMaterials[%d].reflectiveIndex", index, metallic.reflectiveIdx)
    }
}

private var statsDumped = false
fun glShadingRtDumpStats(start: Long, stop: Long) {
    if (!statsDumped) {
        statsDumped = true
        val millisTotal = stop - start
        val millisPerFrame = millisTotal / FRAMES_TO_CAPTURE
        println(String.format("Job took: %.2f sec, ~approx. per frame: %.2f sec, 10 sec video will take: %.2f sec",
            millisTotal.toFloat() / 1000f, millisPerFrame.toFloat() / 1000f, 10f * 60f * millisPerFrame.toFloat() / 1000f))
    }
}

fun main() {
    window.create {
        glViewportBindPrev {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    glShadingFlatDraw(shadingFlat) {
                        glShadingRtSubmitHitables(shadingFlat.program, hitables)
                        var frame = 0
                        capturer.capture {
                            val start = System.currentTimeMillis()
                            window.show {
                                controller.apply { position, direction ->
                                    eye.value = position
                                    center.value = vec3().set(position).add(direction)
                                }
                                if (frame < FRAMES_TO_CAPTURE) {
                                    glShadingFlatInstance(shadingFlat, rect)
                                    capturer.addFrame()
                                    frame++
                                } else {
                                    val stop = System.currentTimeMillis()
                                    glShadingRtDumpStats(start, stop)
                                    glClear(col3().green())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}