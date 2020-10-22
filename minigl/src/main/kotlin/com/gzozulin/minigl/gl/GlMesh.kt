package com.gzozulin.minigl.gl

class GlMesh(
    private val attributes: List<Pair<GlAttribute, GlBuffer>>,
    private val indicesBuffer: GlBuffer,
    val indicesCount: Int) : GlBindable() {

    init {
        attributes.forEach { addChild(it.second) }
        addChild(indicesBuffer)
    }

    private var handle: Int = -1

    override fun use() {
        super.use()
        handle = backend.glGenVertexArrays()
        createVAO()
    }

    override fun release() {
        backend.glDeleteVertexArrays(handle)
        super.release()
    }

    override fun bind() {
        super.bind()
        backend.glBindVertexArray(handle)
    }

    override fun unbind() {
        backend.glBindVertexArray(0)
        super.unbind()
    }

    private fun createVAO() {
        backend.glBindVertexArray(handle)
        attributes.forEach {
            backend.glEnableVertexAttribArray(it.first.location)
            it.second.bind()
            backend.glVertexAttribPointer(it.first.location, it.first.size, backend.GL_FLOAT, false, 0, 0)
            if (it.first.divisor != 0) {
                backend.glVertexAttribDivisor(it.first.location, it.first.divisor)
            }
        }
        indicesBuffer.bind()
        backend.glBindVertexArray(0)
        indicesBuffer.unbind()
        attributes.forEach { it.second.unbind() }
    }

    companion object {
        fun rect(left: Float = -1f, right: Float = 1f, top: Float = 1f, bottom: Float = -1f,
            additionalAttributes: List<Pair<GlAttribute, GlBuffer>> = listOf()): GlMesh {
            val vertices = floatArrayOf(
                left,  top,     0f,
                left,  bottom,  0f,
                right, top,     0f,
                right, bottom,  0f)
            val texCoords = floatArrayOf(
                0f,     top,
                0f,     0f,
                right,  top,
                right,  0f)
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