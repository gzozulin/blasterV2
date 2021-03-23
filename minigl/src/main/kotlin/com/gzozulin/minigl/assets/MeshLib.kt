package com.gzozulin.minigl.assets

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.scene.PhongMaterial
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

private val whitespaceRegex = "\\s+".toRegex()
private val slashRegex = "/".toRegex()

@Deprecated("Model instead")
data class MeshData(
    val mesh: GlMesh,
    val aabb: aabb
)

private class Intermediate {
    val aabb = aabb()
    val positionList = mutableListOf<Float>()
    val texCoordList = mutableListOf<Float>()
    val normalList = mutableListOf<Float>()
    val positions = mutableListOf<Float>()
    val texCoords = mutableListOf<Float>()
    val normals = mutableListOf<Float>()
    val indicesList = mutableListOf<Int>()
}

open class Material : GlResource()

data class Object(val mesh: GlMesh, val material: Material, val aabb: aabb): GlResource() {
    init {
        addChildren(mesh, material)
    }
}

data class Model(val objects: List<Object>, val aabb: aabb) : GlResource() {
    init {
        addChildren(objects)
    }
}

val meshLib = MeshLib()

private fun String.toVec3f(): vec3 {
    val split = split(whitespaceRegex)
    return vec3(split[1].toDouble().toFloat(), split[2].toDouble().toFloat(), split[3].toDouble().toFloat())
}

private fun String.toFloat(): Float {
    val split = split(whitespaceRegex)
    return split[1].toDouble().toFloat()
}

private fun String.toName(): String {
    val split = split(whitespaceRegex)
    return split[1]
}

class MeshLib internal constructor() {
    private fun openAsset(filename: String) =
        BufferedReader(InputStreamReader(assetStream.openAsset(filename), Charset.defaultCharset()))

    fun load(filename: String, progress: (Float) -> Unit = { println(it.toString()) }): Model {
        return load("$filename.obj", "$filename.mtl", progress)
    }

    fun load(objFilename: String, mtlFilename: String, progress: (Float) -> Unit = { println(it.toString()) }): Model {
        parseMtl(mtlFilename)
        return Model(listOf(), aabb())
    }

    private fun parseObj() {

    }

    private fun parseMtl(mtlFilename: String): MutableList<Pair<String, PhongMaterial>> {
        val parentDir = File(mtlFilename).parent
        val result = mutableListOf<Pair<String, PhongMaterial>>()
        fun updateLast(update: (PhongMaterial) -> PhongMaterial) {
            val last = result.removeLast()
            val updated = update.invoke(last.second)
            result.add(last.first to updated)
        }
        openAsset(mtlFilename).useLines { lines ->
            for (line in lines) {
                if (line.startsWith("#")) {
                    continue
                }
                when (line[0]) {
                    'n' -> result.add(line.toName() to PhongMaterial.DEFAULT)
                    'K' -> {
                        when (line[1]) {
                            'a' -> updateLast { it.copy(ambient = line.toVec3f()) }
                            'd' -> updateLast { it.copy(diffuse = line.toVec3f()) }
                            's' -> updateLast { it.copy(specular = line.toVec3f()) }
                        }
                    }
                    'm' -> {
                        when (line[4]) {
                            'K' -> when (line[5]) {
                                'a' -> updateLast { it.copy(mapAmbient = texturesLib.loadTexture("$parentDir/${line.toName()}")) }
                                'd' -> updateLast { it.copy(mapDiffuse = texturesLib.loadTexture("$parentDir/${line.toName()}")) }
                                's' -> updateLast { it.copy(mapSpecular = texturesLib.loadTexture("$parentDir/${line.toName()}")) }
                            }
                            'N' -> updateLast { it.copy(mapShine = texturesLib.loadTexture("$parentDir/${line.toName()}")) }
                            'd' -> updateLast { it.copy(mapTransparency = texturesLib.loadTexture("$parentDir/${line.toName()}")) }
                        }
                    }
                    'N' -> {
                        when (line[1]) {
                            's' -> updateLast { it.copy(shine = line.toFloat()) }
                        }
                    }
                    'd' -> updateLast { it.copy(transparency = line.toFloat()) }
                }
            }
        }
        return result
    }

    fun loadModel(meshFilename: String, progress: (Float) -> Unit = {}): MeshData {
        val result = Intermediate()
        var linesCount = 0
        var lastProgress = 0f
        openAsset(meshFilename).useLines { linesCount = it.count() }
        openAsset(meshFilename).useLines { lines ->
            lines.forEachIndexed { index, line ->
                parseLine(line, result)
                val currentProgress = index.toFloat() / linesCount.toFloat()
                if (currentProgress - lastProgress > 0.01f) {
                    progress.invoke(currentProgress)
                    lastProgress = currentProgress
                }
            }
        }
        val positionBuff = toByteBufferFloat(result.positions)
        val texCoordBuff = toByteBufferFloat(result.texCoords)
        val normalBuff = toByteBufferFloat(result.normals)
        val indicesBuff = toByteBufferInt(result.indicesList)
        val mesh = GlMesh(
            listOf(GlAttribute.ATTRIBUTE_POSITION to GlBuffer(backend.GL_ARRAY_BUFFER, positionBuff),
                GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer(backend.GL_ARRAY_BUFFER, texCoordBuff),
                GlAttribute.ATTRIBUTE_NORMAL to GlBuffer(backend.GL_ARRAY_BUFFER, normalBuff)),
            GlBuffer(backend.GL_ELEMENT_ARRAY_BUFFER, indicesBuff), result.indicesList.size
        )
        return MeshData(mesh, result.aabb)
    }

    private fun parseLine(line: String, result: Intermediate) {
        if (line.isEmpty()) {
            return
        }
        when (line[0]) {
            'v' -> parseVertexAttribute(line, result)
            'f' -> parsePolygon(line, result)
        }
    }

    private fun parseVertexAttribute(line: String, result: Intermediate) {
        when (line[1]) {
            ' ' -> parsePosition(line, result)
            't' -> parseTexCoordinate(line, result)
            'n' -> parseNormal(line, result)
            else -> error("Unknown vertex attribute! $line")
        }
    }

    private fun parsePosition(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        result.positionList.add(split[1].toFloat())
        result.positionList.add(split[2].toFloat())
        result.positionList.add(split[3].toFloat())
    }

    private fun parseTexCoordinate(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        result.texCoordList.add(split[1].toFloat())
        result.texCoordList.add(split[2].toFloat())
    }

    private fun parseNormal(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        result.normalList.add(split[1].toFloat())
        result.normalList.add(split[2].toFloat())
        result.normalList.add(split[3].toFloat())
    }

    private fun parsePolygon(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        val verticesCnt = split.size - 1
        val indices = mutableListOf<Int>()
        var nextIndex = result.positions.size / 3
        for (vertex in 0 until verticesCnt) {
            addVertex(split[vertex + 1], result)
            indices.add(nextIndex)
            nextIndex++
        }
        val triangleCnt = verticesCnt - 2
        for (triangle in 0 until triangleCnt) {
            addTriangle(indices[0], indices[triangle + 1], indices[triangle + 2], result.indicesList)
        }
    }

    private fun addVertex(vertex: String, result: Intermediate) {
        val vertSplit = vertex.split(slashRegex)
        var vertIndex = vertSplit[0].toInt() - 1
        if (vertIndex < 0) {
            vertIndex += result.positionList.size / 3 + 1
        }
        val vx = result.positionList[vertIndex * 3 + 0]
        val vy = result.positionList[vertIndex * 3 + 1]
        val vz = result.positionList[vertIndex * 3 + 2]
        result.positions.add(vx)
        result.positions.add(vy)
        result.positions.add(vz)
        updateAabb(result.aabb, vx, vy, vz)
        if (result.texCoordList.isNotEmpty()) {
            var texIndex = vertSplit[1].toInt()  - 1
            if (texIndex < 0) {
                texIndex += result.texCoordList.size / 2 + 1
            }
            result.texCoords.add(result.texCoordList[texIndex  * 2 + 0])
            result.texCoords.add(result.texCoordList[texIndex  * 2 + 1])
        } else {
            result.texCoords.add(1f)
            result.texCoords.add(1f)
        }
        if (result.normalList.isNotEmpty()) {
            var normIndex = vertSplit[2].toInt() - 1
            if (normIndex < 0) {
                normIndex += result.normalList.size / 3 + 1
            }
            result.normals.add(result.normalList[normIndex * 3 + 0])
            result.normals.add(result.normalList[normIndex * 3 + 1])
            result.normals.add(result.normalList[normIndex * 3 + 2])
        } else {
            result.normals.add(0f)
            result.normals.add(1f)
            result.normals.add(0f)
        }
    }

    private fun addTriangle(ind0: Int, ind1: Int, ind2: Int, indicesList: MutableList<Int>) {
        indicesList.add(ind0)
        indicesList.add(ind1)
        indicesList.add(ind2)
    }

    private fun updateAabb(aabb: aabb, vx: Float, vy: Float, vz: Float) {
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

    private fun toByteBufferFloat(list: List<Float>): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
        val typed = buffer.asFloatBuffer()
        list.forEach { typed.put(it) }
        buffer.position(0)
        return buffer
    }

    private fun toByteBufferInt(list: List<Int>): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
        val typed = buffer.asIntBuffer()
        list.forEach { typed.put(it) }
        buffer.position(0)
        return buffer
    }
}

fun main() {
    val model = meshLib.load("models/akai/akai")
}