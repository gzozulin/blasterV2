package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend

data class GlMesh(internal val vertices: GlBuffer, internal val texCoords: GlBuffer, internal val normals: GlBuffer,
                  internal val indices: GlBuffer, internal val indicesCnt: Int, internal var handle: Int? = null)

private fun glMeshUpload(mesh: GlMesh) {
    check(mesh.handle == null) { "GlMesh is already in use!" }
    mesh.handle = backend.glGenVertexArrays()
    glBufferUpload(mesh.vertices)
    glBufferUpload(mesh.texCoords)
    glBufferUpload(mesh.normals)
    glBufferUpload(mesh.indices)
    backend.glBindVertexArray(mesh.handle!!)
    backend.glBindBuffer(mesh.vertices.target, mesh.vertices.handle!!)
    backend.glEnableVertexAttribArray(0)
    backend.glVertexAttribPointer(0, 3, backend.GL_FLOAT, false, 0, 0)
    backend.glBindBuffer(mesh.texCoords.target, mesh.texCoords.handle!!)
    backend.glEnableVertexAttribArray(1)
    backend.glVertexAttribPointer(1, 2, backend.GL_FLOAT, false, 0, 0)
    backend.glBindBuffer(mesh.normals.target, mesh.normals.handle!!)
    backend.glEnableVertexAttribArray(2)
    backend.glVertexAttribPointer(2, 3, backend.GL_FLOAT, false, 0, 0)
    backend.glBindBuffer(mesh.indices.target, mesh.indices.handle!!)
    backend.glBindVertexArray(0)
}

private fun glMeshDelete(mesh: GlMesh) {
    backend.glDeleteVertexArrays(mesh.handle!!)
    glBufferDelete(mesh.vertices)
    glBufferDelete(mesh.texCoords)
    glBufferDelete(mesh.normals)
    glBufferDelete(mesh.indices)
    mesh.handle = null
}

fun glMeshUse(mesh: GlMesh, callback: Callback) {
    glMeshUpload(mesh)
    callback.invoke()
    glMeshDelete(mesh)
}

internal fun glMeshUse(meshes: Collection<GlMesh>, callback: Callback) {
    meshes.forEach { glMeshUpload(it) }
    callback.invoke()
    meshes.forEach { glMeshDelete(it) }
}

private var currBinding: Int? = null
internal fun glMeshBind(mesh: GlMesh, callback: Callback) {
    check(mesh.handle != null) { "GlMesh is not used!" }
    val prev = currBinding
    backend.glBindVertexArray(mesh.handle!!)
    currBinding = mesh.handle
    callback.invoke()
    backend.glBindVertexArray(prev ?: 0)
    currBinding = prev
}

internal fun glMeshCheckBound() {
    check(currBinding != null) { "No GlMesh is bound!" }
}

fun glMeshCreateRect(left: Float = -1f, right: Float = 1f, bottom: Float = -1f, top: Float = 1f): GlMesh {
    val vertices = floatArrayOf(
        left,  top,     0f,
        left,  bottom,  0f,
        right, top,     0f,
        right, bottom,  0f)
    val texCoords = floatArrayOf(
        0f,  1f,
        0f,  0f,
        1f,  1f,
        1f,  0f)
    val normals = floatArrayOf(
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f)
    val indices = intArrayOf(0, 1, 2, 1, 3, 2)
    return GlMesh(
        glBufferCreate(backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW, vertices),
        glBufferCreate(backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW, texCoords),
        glBufferCreate(backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW, normals),
        glBufferCreate(backend.GL_ELEMENT_ARRAY_BUFFER, backend.GL_STATIC_DRAW, indices), 6)
}

fun glMeshCreateRect(width: Float, height: Float): GlMesh {
    val hw = width/2f
    val hh = height/2f
    return glMeshCreateRect(-hw, hw, -hh, hh)
}