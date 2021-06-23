package com.gzozulin.minigl.tech

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.*

private val window = GlWindow(isFullscreen = false)

private val controller = ControllerFirstPerson(velocity = 0.1f, position = vec3(-1f, 0f, 3f))
private val wasdInput = WasdInput(controller)
private var mouseLook = false

private val sampleCnt = consti(2048)
private val rayBounces = consti(8)

private val eye = unifv3 { controller.position }
private val center = unifv3 { vec3().set(controller.position).add(controller.direction) }
private val up = constv3(vec3().up())

private val fovy = constf(radf(90.0f))
private val aspect = constf(window.width.toFloat() / window.height.toFloat())
private val aperture = constf(0f)
private val focusDist = constf(3f)

private val matrix = constm4(mat4().orthoBox())
private val color = fragmentColorRt(
    sampleCnt, rayBounces,
    eye, center, up,
    fovy, aspect, aperture, focusDist,
    namedTexCoordsV2())

private val shadingFlat = ShadingFlat(matrix, color)

private val rect = glMeshCreateRect()

private val hitables = listOf(
    Sphere(vec3(0f, 0f, 0f), 0.5f, LambertianMaterial(col3(0.8f, 0.3f, 0.3f))),
    Sphere(vec3(0f, -100.5f, 0f), 100f, LambertianMaterial(col3(0.8f, 0.8f, 0.0f))),
    Sphere(vec3(1f, 0f, 0f), 0.5f, MetallicMaterial(col3(0.8f, 0.6f, 0.2f))),
    Sphere(vec3(-1f, 0f, 0f), 0.5f, DielectricMaterial(1.5f)),
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

fun main() {
    window.create {
        window.keyCallback = { key, pressed ->
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
        }
        glViewportBindPrev {
            glShadingFlatUse(shadingFlat) {
                glMeshUse(rect) {
                    glShadingFlatDraw(shadingFlat) {
                        glShadingRtSubmitHitables(shadingFlat.program, hitables)
                        var buffers = 0
                        window.show {
                            controller.updatePosition()
                            controller.updateDirection()
                            if (buffers < 2) {
                                glShadingFlatInstance(shadingFlat, rect)
                                buffers++
                            }
                        }
                    }
                }
            }
        }
    }
}