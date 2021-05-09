package com.gzozulin.minigl.api2

import com.gzozulin.minigl.api.backend

data class GlMesh(internal val positions: GlBuffer, internal val texCoords: GlBuffer, internal val normals: GlBuffer,
                  internal val indices: GlBuffer, internal val indicesCnt: Int, internal var handle: Int? = null)

internal fun glUseMesh(mesh: GlMesh, callback: Callback) {
    check(mesh.handle == null) { "GlMesh is already in use!" }
    mesh.handle = backend.glGenVertexArrays()
    glBindMesh(mesh) {
        glUseBuffer(mesh.positions, backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW) {
            backend.glEnableVertexAttribArray(0)
            backend.glVertexAttribPointer(0, 3, backend.GL_FLOAT, false, 0, 0)
            glUseBuffer(mesh.texCoords, backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW) {
                backend.glEnableVertexAttribArray(1)
                backend.glVertexAttribPointer(1, 2, backend.GL_FLOAT, false, 0, 0)
                glUseBuffer(mesh.normals, backend.GL_ARRAY_BUFFER, backend.GL_STATIC_DRAW) {
                    backend.glEnableVertexAttribArray(2)
                    backend.glVertexAttribPointer(2, 3, backend.GL_FLOAT, false, 0, 0)
                    glUseBuffer(mesh.indices, backend.GL_ELEMENT_ARRAY_BUFFER, backend.GL_STATIC_DRAW) {
                        callback.invoke()
                    }
                }
            }
        }
    }
    backend.glDeleteVertexArrays(mesh.handle!!)
    mesh.handle = null
}

private var prevBinding: Int? = null
internal fun glBindMesh(mesh: GlMesh, callback: Callback) {
    check(mesh.handle != null) { "GlMesh is not used!" }
    val prev = prevBinding
    backend.glBindVertexArray(mesh.handle!!)
    prevBinding = mesh.handle
    callback.invoke()
    backend.glBindVertexArray(prev ?: 0)
    prevBinding = prev
}

internal fun glCreateRect(): GlMesh {
    TODO()
}