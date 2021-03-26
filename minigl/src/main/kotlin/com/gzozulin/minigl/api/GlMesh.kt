package com.gzozulin.minigl.api

import java.util.*

private val bindStack = Stack<Int>()

class GlMesh(
    private val attributes: List<Pair<GlAttribute, GlBuffer>>,
    private val indicesBuffer: GlBuffer,
    val indicesCount: Int) : GlBindable() {

    init {
        attributes.forEach { addChildren(it.second) }
        addChildren(indicesBuffer)
    }

    private var handle: Int = -1

    override fun onUse() {
        createVAO()
    }

    override fun onRelease() {
        backend.glDeleteVertexArrays(handle)
    }

    override fun onBound() {
        backend.glBindVertexArray(handle)
        bindStack.push(handle)
    }

    override fun onUnbound() {
        bindStack.pop()
        if (bindStack.empty()) {
            backend.glBindVertexArray(0)
        } else {
            backend.glBindVertexArray(bindStack.peek())
        }
    }

    private fun createVAO() {
        handle = backend.glGenVertexArrays()
        backend.glBindVertexArray(handle)
        attributes.forEach { (attribute, buffer) ->
            backend.glEnableVertexAttribArray(attribute.location)
            buffer.bind()
            backend.glVertexAttribPointer(attribute.location, attribute.size, backend.GL_FLOAT, false, 0, 0)
            if (attribute.divisor != 0) {
                backend.glVertexAttribDivisor(attribute.location, attribute.divisor)
            }
        }
        indicesBuffer.bind()
        backend.glBindVertexArray(0)
        indicesBuffer.unbind()
        attributes.forEach { it.second.unbind() }
    }

    companion object {
        fun rect(width: Float, height: Float): GlMesh {
            val hw = width/2f
            val hh = height/2f
            return rect(-hw, hw, -hh, hh)
        }

        fun rect(left: Float = -1f, right: Float = 1f, bottom: Float = -1f, top: Float = 1f,
            additionalAttributes: List<Pair<GlAttribute, GlBuffer>> = listOf()): GlMesh {
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
            val indices = intArrayOf(0, 1, 2, 1, 3, 2)
            val attributes = mutableListOf(
                GlAttribute.ATTRIBUTE_POSITION to GlBuffer.create(backend.GL_ARRAY_BUFFER, vertices),
                GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer.create(backend.GL_ARRAY_BUFFER, texCoords))
            attributes.addAll(additionalAttributes)
            return GlMesh(
                attributes,
                GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, indices),
                indices.size)
        }

        fun triangle(additionalAttributes: List<Pair<GlAttribute, GlBuffer>> = listOf()): GlMesh {
            val vertices = floatArrayOf(
                0f,  1f, 0f,
                -1f, -1f, 0f,
                1f, -1f, 0f)
            val texCoords = floatArrayOf(
                0.5f, 1f,
                0f,   0f,
                1f,   0f)
            val indices = intArrayOf(0, 1, 2)
            val attributes = mutableListOf(
                GlAttribute.ATTRIBUTE_POSITION to GlBuffer.create(backend.GL_ARRAY_BUFFER, vertices),
                GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer.create(backend.GL_ARRAY_BUFFER, texCoords))
            attributes.addAll(additionalAttributes)
            return GlMesh(
                attributes,
                GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, indices),
                indices.size)
        }
    }
}