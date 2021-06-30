package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.capture.Capturer
import com.gzozulin.minigl.scene.*
import kotlin.system.exitProcess

private const val BATCH_CNT = 20

private const val FRAMES_TO_CAPTURE = 5
private val sampleCnt = consti(32)
private val rayBounces = consti(3)

private val window = GlWindow(isFullscreen = true, isHeadless = true)
private val capturer = Capturer(window)

data class ShadingRt(val sampleCnt: Expression<Int>, val rayBounces: Expression<Int>,
                     val eye: Expression<vec3>, val center: Expression<vec3>, val up: Expression<vec3>,
                     val fovy: Expression<Float>, val aspect: Expression<Float>,
                     val aperture: Expression<Float>, val focus: Expression<Float>) {
    internal val rects = glShadingRtCreateRects()

    private val matrix = constm4(mat4().orthoBox())
    private val color = fragmentColorRt(
        sampleCnt, rayBounces,
        eye, center, up,
        fovy, aspect, aperture, focus,
        namedTexCoordsV2())

    internal val shadingFlat = ShadingFlat(matrix, color)
}

private data class MaterialsCollection(val lambertians: List<LambertianMaterial>,
                                       val metallics: List<MetallicMaterial>,
                                       val dielectrics: List<DielectricMaterial>,
                                       val lookup: Map<RtMaterial, Int>)

private fun glShadingRtCreateRects(): List<GlMesh> {
    val result = mutableListOf<GlMesh>()

    val dx = 2f / BATCH_CNT
    val dy = 2f / BATCH_CNT
    val du = 1f / BATCH_CNT
    val dv = 1f / BATCH_CNT

    for (x in 0 until BATCH_CNT) {
        for (y in 0 until BATCH_CNT) {

            val left    = -1f + dx * x
            val bottom  = -1f + dy * y
            val right   = left + dx
            val top     = bottom + dy
            val uStart  = du * x
            val vStart  = dv * y
            val uEnd    = uStart + du
            val vEnd    = vStart + dv

            val vertices = floatArrayOf(
                left,    top,     0f,
                left,    bottom,  0f,
                right,   top,     0f,
                right,   bottom,  0f)
            val texCoords = floatArrayOf(
                uStart,  vEnd,
                uStart,  vStart,
                uEnd,    vEnd,
                uEnd,    vStart)
            val normals = floatArrayOf(
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f)
            val indices = intArrayOf(0, 1, 2, 1, 3, 2)
            result.add(glMeshCreateRect(vertices, texCoords, normals, indices))
        }
    }
    return result
}

private fun glShadingRtMaterialType(material: RtMaterial) = when (material) {
    is LambertianMaterial -> MaterialType.LAMBERTIAN.ordinal
    is MetallicMaterial -> MaterialType.METALLIC.ordinal
    is DielectricMaterial -> MaterialType.DIELECTRIC.ordinal
    else -> error("Unknown material!")
}

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
    materialsCollection.lambertians.forEachIndexed { index, lambertian ->
        glProgramArrayUniform(program, "uLambertianMaterials[%d].albedo", index, lambertian.albedo)
    }
    materialsCollection.metallics.forEachIndexed { index, metallic ->
        glProgramArrayUniform(program, "uMetallicMaterials[%d].albedo", index, metallic.albedo)
    }
    materialsCollection.dielectrics.forEachIndexed { index, metallic ->
        glProgramArrayUniform(program, "uDielectricMaterials[%d].reflectiveIndex", index, metallic.reflectiveIdx)
    }

    var spheresCnt = 0
    var hitablesCnt = 0
    hitables.forEach { hitable ->
        when (hitable) {
            is Sphere -> {
                check(spheresCnt + 1 <= MAX_SPHERES) { "More spheres than defined in shader!" }
                glProgramArrayUniform(program, "uSpheres[%d].center", spheresCnt, hitable.center)
                glProgramArrayUniform(program, "uSpheres[%d].radius", spheresCnt, hitable.radius)
                glProgramArrayUniform(program, "uSpheres[%d].materialType", spheresCnt,
                    glShadingRtMaterialType(hitable.material))
                glProgramArrayUniform(program, "uSpheres[%d].materialIndex", spheresCnt,
                    materialsCollection.lookup[hitable.material]!!)
                glProgramArrayUniform(program, "uHitables[%d].type",  hitablesCnt, HitableType.SPHERE.ordinal)
                glProgramArrayUniform(program, "uHitables[%d].index", hitablesCnt, spheresCnt)
                spheresCnt++
                hitablesCnt++
            }
            else -> error("Unknown Hitable!")
        }
    }
    glProgramUniform(program, "uHitablesCnt", hitablesCnt)
}

fun glShadingRtUse(shadingRt: ShadingRt, callback: Callback) {
    glShadingFlatUse(shadingRt.shadingFlat) {
        glMeshUse(shadingRt.rects) {
            callback.invoke()
        }
    }
}

fun glShadingRtDraw(shadingRt: ShadingRt, hitables: List<Any>, callback: Callback) {
    glShadingFlatDraw(shadingRt.shadingFlat) {
        glShadingRtSubmitHitables(shadingRt.shadingFlat.program, hitables)
        callback.invoke()
    }
}

fun glShadingRtInstance(shadingRt: ShadingRt) {
    for (rect in shadingRt.rects) {
        glShadingFlatInstance(shadingRt.shadingFlat, rect)
    }
}

/*private var mouseLook = false
private val controller = ControllerFirstPerson(position = vec3(2f))
private val wasdInput = WasdInput(controller)*/

private val controller = ControllerScenic(
    positions = listOf(
        vec3(-5f, 0.7f, -5f),
        vec3( 5f, 0.7f, -5f),
        vec3( 5f, 0.7f,  5f),
        vec3(-5f, 0.7f,  5f),
    ),
    points = listOf(vec3()))

private val eye = unifv3()
private val center = unifv3()
private val up = constv3(vec3().up())

private val fovy = constf(radf(90.0f))
private val aspect = constf(window.width.toFloat() / window.height.toFloat())
private val aperture = constf(0f)
private val focus = constf(1f)

private val shadingRt = ShadingRt(sampleCnt, rayBounces, eye, center, up, fovy, aspect, aperture, focus)

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
        val millisPerFrame = millisTotal / FRAMES_TO_CAPTURE
        println(String.format("Job took: %.2f sec, per frame: %.2f sec, 10 sec video will take: ~%.2f min",
            millisTotal.toFloat() / 1000f,
            millisPerFrame.toFloat() / 1000f,
            10f * 60f * millisPerFrame.toFloat() / 1000f / 60f))
    }
}

fun main() {
    window.create {
        /*window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        window.buttonCallback = { button, pressed ->
            if (button == MouseButton.LEFT) {
                mouseLook = pressed
            }
        }
        window.deltaCallback = { delta ->
            if (mouseLook) {
                wasdInput.onCursorDelta(delta)
            }
        }*/
        glShadingRtUse(shadingRt) {
            glShadingRtDraw(shadingRt, hitables) {
                var frame = 0
                //capturer.capture {
                    val start = System.currentTimeMillis()
                    window.show {
                        controller.apply { position, direction ->
                            eye.value = position
                            center.value = vec3().set(position).add(direction)
                        }
                        if (frame < FRAMES_TO_CAPTURE) {
                            glShadingRtInstance(shadingRt)
                            //capturer.addFrame()
                            frame++
                        } else {
                            val stop = System.currentTimeMillis()
                            glShadingRtDumpStats(start, stop)
                            glClear(col3().green())
                            exitProcess(0)
                        }
                    }
                //}
            }
        }
    }
}