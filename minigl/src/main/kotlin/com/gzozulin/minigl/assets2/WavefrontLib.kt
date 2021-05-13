package com.gzozulin.minigl.assets2

import com.gzozulin.minigl.api.aabb
import com.gzozulin.minigl.api.backend
import com.gzozulin.minigl.api.vec3
import com.gzozulin.minigl.api2.*
import com.gzozulin.minigl.api2.glMeshUse
import com.gzozulin.minigl.api2.glTextureUse
import com.gzozulin.minigl.assets.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

private val whitespaceRegex = "\\s+".toRegex()
private val slashRegex = "/".toRegex()

data class WavefrontObj(val mesh: GlMesh, val material: PhongMaterial, val aabb: aabb)
data class WavefrontObjGroup(val objects: List<WavefrontObj>, val aabb: aabb)

fun libWavefrontCreate(filename: String, join: Boolean = false, progress: (Float) -> Unit = { println(it.toString()) }) =
    libWavefrontCreate("$filename.obj", "$filename.mtl", join, progress)

fun libWavefrontCreate(objFilename: String, mtlFilename: String, join: Boolean = true,
                       progress: (Float) -> Unit = { println(it.toString()) }): WavefrontObjGroup {
    val materials = libWavefrontParseMtl(mtlFilename)
    val objects = libWavefrontParseObject(objFilename, materials, join, progress)
    val allAabb = aabb()
    objects.forEach { allAabb.union(it.aabb) }
    return WavefrontObjGroup(objects, allAabb)
}

fun libWavefrontObjectUse(obj: WavefrontObj, callback: Callback) {
    glMeshUse(obj.mesh) {
        glTextureUse(listOfNotNull(
            obj.material.mapAmbient,
            obj.material.mapDiffuse,
            obj.material.mapSpecular,
            obj.material.mapShine,
            obj.material.mapTransparency).iterator()) {
            callback.invoke()
        }
    }
}

fun libWavefrontGroupUse(group: WavefrontObjGroup, callback: Callback) {
    val meshes = HashSet<GlMesh>()
    val textures = HashSet<GlTexture>()
    group.objects.forEach {
        meshes.add(it.mesh)
        textures.addAll(listOfNotNull(
            it.material.mapAmbient, it.material.mapDiffuse, it.material.mapSpecular,
            it.material.mapShine, it.material.mapTransparency
        ))
    }
    glMeshUse(meshes.iterator()) {
        glTextureUse(textures.iterator()) {
            callback.invoke()
        }
    }
}

private fun libWavefrontParseAsset(filename: String) =
    BufferedReader(InputStreamReader(assetStream.openAsset(filename), Charset.defaultCharset()))

private fun libWavefrontParseObject(objFilename: String, materials: Map<String, PhongMaterial>, join: Boolean,
                                    progress: (Float) -> Unit): List<WavefrontObj> {
    val result = Intermediate()
    var linesCount = 0
    var lastProgress = 0f
    libWavefrontParseAsset(objFilename).useLines { linesCount = it.count() }
    libWavefrontParseAsset(objFilename).useLines { lines ->
        lines.forEachIndexed { index, line ->
            try {
                libWavefrontParseLine(line, join, result)
            } catch (e: Throwable) {
                error("Invalid line ${index+1}: $e")
            }
            val currentProgress = index.toFloat() / linesCount.toFloat()
            if (currentProgress - lastProgress > 0.1f) {
                progress.invoke(currentProgress)
                lastProgress = currentProgress
            }
        }
    }
    return libWavefrontCreateObjects(result, materials)
}

private fun libWavefrontCreateObjects(intermediate: Intermediate,
                                      materials: Map<String, PhongMaterial>): List<WavefrontObj> {
    val result = mutableListOf<WavefrontObj>()
    for (obj in intermediate.objects) {
        if (obj.positions.isEmpty()) {
            continue
        }
        val positionBuff = obj.positions.toByteBufferFloat()
        val texCoordBuff = obj.texCoords.toByteBufferFloat()
        val normalBuff = obj.normals.toByteBufferFloat()
        val indicesBuff = obj.indices.toByteBufferInt()
        val mesh = GlMesh(
            GlBuffer(backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW, positionBuff),
            GlBuffer(backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW, texCoordBuff),
            GlBuffer(backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW, normalBuff),
            GlBuffer(backend.GL_ELEMENT_ARRAY_BUFFER, backend.GL_STATIC_DRAW, indicesBuff), obj.indices.size
        )
        val material = materials[obj.materialTag]!!
        result.add(WavefrontObj(mesh, material, obj.aabb))
    }
    return result
}

private fun libWavefrontParseLine(line: String, join: Boolean, result: Intermediate) {
    if (line.isEmptyOrCommented()) {
        return
    }
    when (line[0]) {
        'v' -> libWavefrontParseVertexAttribute(line, result)
        'f' -> libWavefrontParsePolygon(line, result)
        'g', 'o', 'u' -> libWavefrontNextObject(line, join, result)
    }
}

private fun libWavefrontNextObject(line: String, join: Boolean, result: Intermediate) {
    if (!join) {
        val currMaterial = result.current.materialTag
        result.objects.add(IntermediateObj())
        result.current.materialTag = currMaterial
    }
    if (line[0] == 'u') {
        libWavefrontUseMaterial(line.getName(), result)
    }
}

private fun libWavefrontUseMaterial(tag: String, result: Intermediate) {
    result.current.materialTag = tag
}

private fun libWavefrontParseVertexAttribute(line: String, result: Intermediate) {
    when (line[1]) {
        ' ' -> libWavefrontParsePosition(line, result)
        't' -> libWavefrontParseTexCoordinate(line, result)
        'n' -> libWavefrontParseNormal(line, result)
        else -> error("Unknown vertex attribute! $line")
    }
}

private fun libWavefrontParsePosition(line: String, result: Intermediate) {
    val split = line.split(whitespaceRegex)
    result.positionList.add(split[1].toFloat())
    result.positionList.add(split[2].toFloat())
    result.positionList.add(split[3].toFloat())
}

private fun libWavefrontParseTexCoordinate(line: String, result: Intermediate) {
    val split = line.split(whitespaceRegex)
    result.texCoordList.add(split[1].toFloat())
    result.texCoordList.add(split[2].toFloat())
}

private fun libWavefrontParseNormal(line: String, result: Intermediate) {
    val split = line.split(whitespaceRegex)
    result.normalList.add(split[1].toFloat())
    result.normalList.add(split[2].toFloat())
    result.normalList.add(split[3].toFloat())
}

private fun libWavefrontParsePolygon(line: String, result: Intermediate) {
    val split = line.trimEnd().split(whitespaceRegex)
    val verticesCnt = split.size - 1
    val indices = mutableListOf<Int>()
    var nextIndex = result.current.positions.size / 3
    for (vertex in 0 until verticesCnt) {
        libWavefrontAddVertex(split[vertex + 1], result)
        indices.add(nextIndex)
        nextIndex++
    }
    val triangleCnt = verticesCnt - 2
    for (triangle in 0 until triangleCnt) {
        libWavefrontAddTriangle(indices[0], indices[triangle + 1], indices[triangle + 2], result.current.indices)
    }
}

private fun libWavefrontAddVertex(vertex: String, result: Intermediate) {
    val vertSplit = vertex.split(slashRegex)
    var vertIndex = vertSplit[0].toInt() - 1
    if (vertIndex < 0) {
        vertIndex += result.positionList.size / 3 + 1
    }
    val vx = result.positionList[vertIndex * 3 + 0]
    val vy = result.positionList[vertIndex * 3 + 1]
    val vz = result.positionList[vertIndex * 3 + 2]
    result.current.positions.add(vx)
    result.current.positions.add(vy)
    result.current.positions.add(vz)
    libWavefrontUpdateAabb(result.current.aabb, vx, vy, vz)
    if (result.texCoordList.isNotEmpty()) {
        var texIndex = vertSplit[1].toInt()  - 1
        if (texIndex < 0) {
            texIndex += result.texCoordList.size / 2 + 1
        }
        result.current.texCoords.add(result.texCoordList[texIndex  * 2 + 0])
        result.current.texCoords.add(result.texCoordList[texIndex  * 2 + 1])
    } else {
        result.current.texCoords.add(1f)
        result.current.texCoords.add(1f)
    }
    if (result.normalList.isNotEmpty()) {
        var normIndex = vertSplit[2].toInt() - 1
        if (normIndex < 0) {
            normIndex += result.normalList.size / 3 + 1
        }
        result.current.normals.add(result.normalList[normIndex * 3 + 0])
        result.current.normals.add(result.normalList[normIndex * 3 + 1])
        result.current.normals.add(result.normalList[normIndex * 3 + 2])
    } else {
        result.current.normals.add(0f)
        result.current.normals.add(1f)
        result.current.normals.add(0f)
    }
}

private fun libWavefrontAddTriangle(ind0: Int, ind1: Int, ind2: Int, indicesList: MutableList<Int>) {
    indicesList.add(ind0)
    indicesList.add(ind1)
    indicesList.add(ind2)
}

private fun libWavefrontUpdateAabb(aabb: aabb, vx: Float, vy: Float, vz: Float) {
    if (vx < aabb.minX) {
        aabb.minX = vx
    } else if (vx > aabb.maxX) {
        aabb.maxX = vx
    }
    if (vy < aabb.minY) {
        aabb.minY = vy
    } else if (vy > aabb.maxY) {
        aabb.maxY = vy
    }
    if (vz < aabb.minZ) {
        aabb.minZ = vz
    } else if (vz > aabb.maxZ) {
        aabb.maxZ = vz
    }
}

private fun libWavefrontParseMtl(mtlFilename: String): Map<String, PhongMaterial> {
    val parentDir = File(mtlFilename).parent
    val result = mutableListOf<Pair<String, PhongMaterial>>()
    fun updateLast(update: (PhongMaterial) -> PhongMaterial) {
        val last = result.removeLast()
        val updated = update.invoke(last.second)
        result.add(last.first to updated)
    }
    libWavefrontParseAsset(mtlFilename).useLines { lines ->
        for (line in lines) {
            if (line.isEmptyOrCommented()) {
                continue
            }
            when (line[0]) {
                'n' -> result.add(line.getName() to DEFAULT)
                'K' -> {
                    when (line[1]) {
                        'a' -> updateLast { it.copy(ambient = line.getVec3f()) }
                        'd' -> updateLast { it.copy(diffuse = line.getVec3f()) }
                        's' -> updateLast { it.copy(specular = line.getVec3f()) }
                    }
                }
                'm' -> {
                    when (line[4]) {
                        'K' -> when (line[5]) {
                            'a' -> updateLast { it.copy(mapAmbient = libTextureCreate("$parentDir/${line.getName()}")) }
                            'd' -> updateLast { it.copy(mapDiffuse = libTextureCreate("$parentDir/${line.getName()}")) }
                            's' -> updateLast { it.copy(mapSpecular = libTextureCreate("$parentDir/${line.getName()}")) }
                        }
                        'N' -> updateLast { it.copy(mapShine = libTextureCreate("$parentDir/${line.getName()}")) }
                        'd' -> updateLast { it.copy(mapTransparency = libTextureCreate("$parentDir/${line.getName()}")) }
                    }
                }
                'N' -> {
                    when (line[1]) {
                        's' -> updateLast { it.copy(shine = line.getFloat()) }
                    }
                }
                'd' -> updateLast { it.copy(transparency = line.getFloat()) }
            }
        }
    }
    val map = mutableMapOf<String, PhongMaterial>()
    result.forEach { map[it.first] = it.second }
    return map
}

private fun List<Float>.toByteBufferFloat(): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder())
    val typed = buffer.asFloatBuffer()
    forEach { typed.put(it) }
    buffer.position(0)
    return buffer
}

private fun List<Int>.toByteBufferInt(): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder())
    val typed = buffer.asIntBuffer()
    forEach { typed.put(it) }
    buffer.position(0)
    return buffer
}

private fun String.isEmptyOrCommented() = isEmpty() || isBlank() || startsWith("#")

private fun String.getVec3f(): vec3 {
    val split = split(whitespaceRegex)
    return vec3(split[1].toDouble().toFloat(), split[2].toDouble().toFloat(), split[3].toDouble().toFloat())
}

private fun String.getFloat(): Float {
    val split = split(whitespaceRegex)
    return split[1].toDouble().toFloat()
}

private fun String.getName(): String {
    val split = split(whitespaceRegex)
    return split[1]
}

private class IntermediateObj {
    val aabb = aabb()
    var materialTag: String? = null
    val positions = mutableListOf<Float>()
    val texCoords = mutableListOf<Float>()
    val normals = mutableListOf<Float>()
    val indices = mutableListOf<Int>()
}

private class Intermediate {
    val positionList = mutableListOf<Float>()
    val texCoordList = mutableListOf<Float>()
    val normalList = mutableListOf<Float>()

    val objects = mutableListOf(IntermediateObj())

    val current: IntermediateObj
        get() = objects.last()
}