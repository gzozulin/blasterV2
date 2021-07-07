package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.scene.*
import kotlin.system.exitProcess

private const val FRAMES_CNT = 50
private const val SAMPLES_PER_BATCH = 8
private const val SAMPLES_CNT = 32
private const val BOUNCES_CNT = 3

enum class HitableType { BVH, SPHERE }
enum class MaterialType { LAMBERTIAN, METALLIC, DIELECTRIC }

object HitRecord // placeholder
object ScatterResult // placeholder
object RefractResult // placeholder
object RtCamera // placeholder

interface Hitable
data class BvhNode(val aabb: aabb, val left: Hitable?, val right: Hitable?): Hitable
data class Sphere(val center: vec3, val radius: Float, val material: RtMaterial): Hitable

interface RtMaterial
data class LambertianMaterial(val albedo: vec3) : RtMaterial
data class MetallicMaterial(val albedo: vec3) : RtMaterial
data class DielectricMaterial(val reflectiveIdx: Float) : RtMaterial

data class ShadingRt(val window: GlWindow,
                     val sampleCnt: Expression<Int>, val rayBounces: Expression<Int>,
                     val eye: Expression<vec3>, val center: Expression<vec3>, val up: Expression<vec3>,
                     val fovy: Expression<Float>, val aspect: Expression<Float>,
                     val aperture: Expression<Float>, val focus: Expression<Float>) {
    internal val rect = glMeshCreateRect()
    private val matrix = constm4(mat4().orthoBox())

    internal val unifRandom = uniff()
    private val colorSampled = errorHandler(fragmentColorRt(
        unifRandom, sampleCnt, rayBounces,
        eye, center, up,
        fovy, aspect, aperture, focus,
        namedTexCoordsV2()))

    internal var currentBuffer = 0
    internal val buffer0 = TechniqueRtt(window, internalFormat = backend.GL_RGBA32F)
    internal val buffer1 = TechniqueRtt(window, internalFormat= backend.GL_RGBA32F)

    internal val prevBuffer = unifs()
    private val colorAdded = addv4(sampler(prevBuffer), colorSampled)
    internal val shadingSamples = ShadingFlat(matrix, colorAdded)

    internal val unifPresent = unifs()
    internal val unifSamples = unifi()
    private val colorAveraged = gammaSqrt(divv4f(sampler(unifPresent), itof(unifSamples)))
    internal val shadingAveraged = ShadingFlat(matrix, colorAveraged)
}

private data class MaterialsCollection(val lambertians: List<LambertianMaterial>,
                                       val metallics: List<MetallicMaterial>,
                                       val dielectrics: List<DielectricMaterial>,
                                       val lookup: Map<RtMaterial, Int>)

private fun glShadingRtMaterialType(material: RtMaterial) = when (material) {
    is LambertianMaterial -> MaterialType.LAMBERTIAN.ordinal
    is MetallicMaterial -> MaterialType.METALLIC.ordinal
    is DielectricMaterial -> MaterialType.DIELECTRIC.ordinal
    else -> error("Unknown material!")
}

private fun glShadingRtCollectMaterials(hitables: List<Hitable>): MaterialsCollection {
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

private fun glShadingRtSubmitMaterials(program: GlProgram, materialsCollection: MaterialsCollection) {
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

private data class HitablesCollection(val spheres: List<Sphere>, val lookup: Map<Hitable, Int>)

private fun glShadingRtCollectHitables(hitables: List<Hitable>): HitablesCollection {
    val spheres = mutableListOf<Sphere>()
    val lookup = mutableMapOf<Hitable, Int>()
    hitables.forEach { 
        spheres.add(it as Sphere) // only spheres currently
    }
    check(spheres.size <= MAX_SPHERES) { "More spheres than defined in shader!" }
    spheres.forEachIndexed { index, sphere ->
        lookup[sphere] = index
    }
    return HitablesCollection(spheres, lookup)
}

private fun glShadingRtSubmitHitables(program: GlProgram, hitablesCollection: HitablesCollection, 
                                      materialsCollection: MaterialsCollection) {
    hitablesCollection.spheres.forEachIndexed { index, sphere ->
        glProgramArrayUniform(program, "uSpheres[%d].center", index, sphere.center)
        glProgramArrayUniform(program, "uSpheres[%d].radius", index, sphere.radius)
        glProgramArrayUniform(program, "uSpheres[%d].materialType", index, glShadingRtMaterialType(sphere.material))
        glProgramArrayUniform(program, "uSpheres[%d].materialIndex", index, materialsCollection.lookup[sphere.material]!!)
    }
}

private fun glShadingRtCreateAabb(hitable: Hitable): aabb {
    when (hitable) {
        is Sphere -> return aabb(
            vec3(hitable.center).sub(vec3(hitable.radius)),
            vec3(hitable.center).add(vec3(hitable.radius)))
        else -> error("Unknown type!")
    }
}

private fun glShadingRtCreateAabb(hitables: List<Hitable>): aabb {
    val result = aabb()
    hitables.forEach { result.union(glShadingRtCreateAabb(it)) }
    return result
}

private fun glShadingRtCreateBvh(hitables: List<Hitable>): BvhNode {
    val aabb = glShadingRtCreateAabb(hitables)

    if (hitables.size == 1) {
        return BvhNode(aabb, hitables.first(), null)
    }

    val selectedAxis = randi(3)
    val sorted = hitables.sortedWith { left, right ->
        val leftAabb = glShadingRtCreateAabb(left)
        val rightAabb = glShadingRtCreateAabb(right)
        val diff = when (selectedAxis) {
            0 -> leftAabb.minX - rightAabb.minX
            1 -> leftAabb.minY - rightAabb.minY
            2 -> leftAabb.minZ - rightAabb.minZ
            else -> error("wtf?!")
        }
        return@sortedWith if (diff < 0f) -1 else 1
    }

    val threshold = sorted.size / 2
    val left = sorted.subList(0, threshold)
    val right = sorted.subList(threshold, sorted.size)
    return BvhNode(aabb, glShadingRtCreateBvh(left), glShadingRtCreateBvh(right))
}

private var bvhIndex = 0
private fun glShadingRtSubmitBvh(program: GlProgram, node: BvhNode, hitablesCollection: HitablesCollection): Int {
    val index = bvhIndex
    bvhIndex++

    check(index < MAX_BVH) { "More bvh nodes than defined in shader!" }
    glProgramArrayUniform(program, "uBvhNodes[%d].aabb.pointMin", index,
        vec3(node.aabb.minX, node.aabb.minY, node.aabb.minZ))
    glProgramArrayUniform(program, "uBvhNodes[%d].aabb.pointMax", index,
        vec3(node.aabb.maxX, node.aabb.maxY, node.aabb.maxZ))

    val (leftType, leftIndex) = if (node.left != null) {
        when (node.left) {
            is BvhNode -> {
                HitableType.BVH.ordinal to glShadingRtSubmitBvh(program, node.left, hitablesCollection)
            }
            is Sphere -> {
                HitableType.SPHERE.ordinal to hitablesCollection.lookup[node.left]!!
            }
            else -> error("Unknown hitable!")
        }
    } else {
        -1 to -1
    }
    
    glProgramArrayUniform(program, "uBvhNodes[%d].leftType", index, leftType)
    glProgramArrayUniform(program, "uBvhNodes[%d].leftIndex", index, leftIndex)

    val (rightType, rightIndex) = if (node.right != null) {
        when (node.right) {
            is BvhNode -> {
                HitableType.BVH.ordinal to glShadingRtSubmitBvh(program, node.right, hitablesCollection)
            }
            is Sphere -> {
                HitableType.SPHERE.ordinal to hitablesCollection.lookup[node.right]!!
            }
            else -> error("Unknown hitable!")
        }
    } else {
        -1 to -1
    }

    glProgramArrayUniform(program, "uBvhNodes[%d].rightType", index, rightType)
    glProgramArrayUniform(program, "uBvhNodes[%d].rightIndex", index, rightIndex)

    return index
}

private fun glShadingRtSubmitAll(program: GlProgram, hitables: List<Hitable>) {
    glProgramCheckBound(program)

    val materialsCollection = glShadingRtCollectMaterials(hitables)
    glShadingRtSubmitMaterials(program, materialsCollection)

    val hitablesCollection = glShadingRtCollectHitables(hitables)
    glShadingRtSubmitHitables(program, hitablesCollection, materialsCollection)

    bvhIndex = 0
    val root = glShadingRtCreateBvh(hitables)
    glShadingRtSubmitBvh(program, root, hitablesCollection)
}

fun glShadingRtUse(shadingRt: ShadingRt, callback: Callback) {
    glShadingFlatUse(shadingRt.shadingSamples) {
        glShadingFlatUse(shadingRt.shadingAveraged) {
            glRttUse(shadingRt.buffer0) {
                glRttUse(shadingRt.buffer1) {
                    glMeshUse(shadingRt.rect) {
                        callback.invoke()
                    }
                }
            }
        }
    }
}

fun glShadingRtDraw(shadingRt: ShadingRt, hitables: List<Hitable>, callback: Callback) {
    glShadingFlatDraw(shadingRt.shadingSamples) {
        glShadingRtSubmitAll(shadingRt.shadingSamples.program, hitables)
        callback.invoke()
    }
}

fun glShadingRtInstance(shadingRt: ShadingRt) {
    glRttDraw(shadingRt.buffer0) {
        glClear(col3().black())
    }
    glRttDraw(shadingRt.buffer1) {
        glClear(col3().black())
    }
    val iterations = SAMPLES_CNT / SAMPLES_PER_BATCH
    for (i in 0 until iterations) {
        val to: TechniqueRtt
        val from: TechniqueRtt
        when (shadingRt.currentBuffer) {
            0 -> {
                to = shadingRt.buffer0
                from = shadingRt.buffer1
                shadingRt.currentBuffer = 1
            }
            1 -> {
                to = shadingRt.buffer1
                from = shadingRt.buffer0
                shadingRt.currentBuffer = 0
            }
            else -> error("Wtf!?")
        }
        glRttDraw(to) {
            glTextureBind(from.color) {
                shadingRt.prevBuffer.value = from.color
                shadingRt.unifRandom.value = Math.random().toFloat()
                glShadingFlatInstance(shadingRt.shadingSamples, shadingRt.rect)
            }
        }
        glShadingFlatDraw(shadingRt.shadingAveraged) {
            glTextureBind(to.color) {
                shadingRt.unifPresent.value = to.color
                shadingRt.unifSamples.value = (i + 1) * SAMPLES_PER_BATCH
                glShadingFlatInstance(shadingRt.shadingAveraged, shadingRt.rect)
            }
        }
        if (window.throttle()) {
            return
        }
    }
}

private val window = GlWindow(isFullscreen = false)
private val capturer = Capturer(window)

private val controller = ControllerScenic(
    positions = listOf(
        vec3(-5f, 0.7f, -5f),
        vec3( 5f, 0.7f, -5f),
        vec3( 5f, 0.7f,  5f),
        vec3(-5f, 0.7f,  5f),
    ),
    points = listOf(vec3()))

private val sampleCnt = consti(SAMPLES_PER_BATCH)
private val rayBounces = consti(BOUNCES_CNT)

private val eye = unifv3()
private val center = unifv3()
private val up = constv3(vec3().up())

private val fovy = constf(radf(90.0f))
private val aspect = constf(window.width.toFloat() / window.height.toFloat())
private val aperture = constf(0f)
private val focus = constf(1f)

private val shadingRt = ShadingRt(window, sampleCnt, rayBounces, eye, center, up, fovy, aspect, aperture, focus)

private val lambertians = (0 until 15).map { LambertianMaterial(vec3().rand())  }.toList()
private val metallics =   (0 until 16).map { MetallicMaterial(vec3().rand())    }.toList()
private val dielectrics = (0 until 16).map { DielectricMaterial(randf(1f, 10f)) }.toList()

private fun sphereRandom() = Sphere(
    vec3().rand(vec3(-5f, 0.2f, -5f), vec3(5f, 0.2f, 5f)), 0.2f,
    when(randi(3)) {
        0 -> lambertians.random()
        1 -> metallics.random()
        2 -> dielectrics.random()
        else -> error("wtf?!")
    })

private val hitables = listOf(
    Sphere(vec3(0f, -1000f, 0f), 1000f, LambertianMaterial(vec3().dkGrey())),
    Sphere(vec3(0f, 1f, 0f), 1f, dielectrics.random()),
    Sphere(vec3(-4f, 1f, 0f), 1f, lambertians.random()),
    Sphere(vec3(4f, 1f, 0f), 1f, metallics.random()),
    *(1..80).map { sphereRandom() }.toTypedArray()
)

private var statsDumped = false
private fun glShadingRtDumpStats(start: Long, stop: Long) {
    if (!statsDumped) {
        statsDumped = true
        val millisTotal = stop - start
        val millisPerFrame = millisTotal / FRAMES_CNT
        println(String.format("Job took: %.2f sec, per frame: %.2f sec, 10 sec video will take: ~%.2f min",
            millisTotal.toFloat() / 1000f,
            millisPerFrame.toFloat() / 1000f,
            10f * 60f * millisPerFrame.toFloat() / 1000f / 60f))
    }
}

fun main() {
    window.create {
        glShadingRtUse(shadingRt) {
            glShadingRtDraw(shadingRt, hitables) {
                var frame = 0
                capturer.capture {
                    val start = System.currentTimeMillis()
                    window.show {
                        controller.apply { position, direction ->
                            eye.value = position
                            center.value = vec3().set(position).add(direction)
                        }
                        if (frame < FRAMES_CNT) {
                            glShadingRtInstance(shadingRt)
                            capturer.addFrame()
                            frame++
                        } else {
                            val stop = System.currentTimeMillis()
                            glShadingRtDumpStats(start, stop)
                            exitProcess(0)
                        }
                    }
                }
            }
        }
    }
}