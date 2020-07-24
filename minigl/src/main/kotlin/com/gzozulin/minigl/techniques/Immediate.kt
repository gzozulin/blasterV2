package com.gzozulin.minigl.techniques

import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.Controller
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.scene.WasdInput
import org.joml.AABBf
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder())

class ImmediateTechnique {
    fun resize(camera: Camera) {
        glCheck {
            backend.glMatrixMode(backend.GL_PROJECTION)
            camera.projectionM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
        }
    }

    private fun line(from: vec3, to: vec3, color: color) {
        backend.glColor3f(color.x, color.y, color.z)
        backend.glVertex3f(from)
        backend.glVertex3f(to)
    }

    fun aabb(camera: Camera, aabb: AABBf, modelM: mat4, color: vec3 = vec3(1f)) {
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            val modelViewM = mat4(camera.calculateViewM()).mul(modelM)
            modelViewM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)
            val bottomLeftBack = vec3(aabb.minX, aabb.minY, aabb.minZ)
            val bottomLeftFront = vec3(aabb.minX, aabb.minY, aabb.maxZ)
            val bottomRightBack = vec3(aabb.maxX, aabb.minY, aabb.minZ)
            val bottomRightFront = vec3(aabb.maxX, aabb.minY, aabb.maxZ)
            val topLeftBack = vec3(aabb.minX, aabb.maxY, aabb.minZ)
            val topLeftFront = vec3(aabb.minX, aabb.maxY, aabb.maxZ)
            val topRightBack = vec3(aabb.maxX, aabb.maxY, aabb.minZ)
            val topRightFront = vec3(aabb.maxX, aabb.maxY, aabb.maxZ)
            line(bottomLeftBack, bottomLeftFront, color)
            line(bottomLeftFront, bottomRightFront, color)
            line(bottomRightFront, bottomRightBack, color)
            line(bottomRightBack, bottomLeftBack, color)
            line(topLeftBack, topLeftFront, color)
            line(topLeftFront, topRightFront, color)
            line(topRightFront, topRightBack, color)
            line(topRightBack, topLeftBack, color)
            line(bottomLeftBack, topLeftBack, color)
            line(bottomLeftFront, topLeftFront, color)
            line(bottomRightBack, topRightBack, color)
            line(bottomRightFront, topRightFront, color)
            backend.glEnd()
        }
    }

    fun marker(camera: Camera, modelM: mat4, color1: vec3, color2: vec3, color3: vec3, scale: Float = 1f) {
        val half = scale / 2f
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            val modelViewM = mat4(camera.calculateViewM()).mul(modelM)
            modelViewM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)
            val center = vec3()
            modelM.translation(center)
            val start = vec3()
            val end = vec3()
            start.set(center)
            start.x -= half
            end.set(center)
            end.x += half
            line(start, end, color1)
            start.set(center)
            start.y -= half
            end.set(center)
            end.y += half
            line(start, end, color2)
            start.set(center)
            start.z -= half
            end.set(center)
            end.z += half
            line(start, end, color3)
            backend.glEnd()
        }
    }

    fun marker(camera: Camera, modelM: mat4, color: color = color().red(), scale: Float = 1f) {
        marker(camera, modelM, color, color, color, scale)
    }
}

private val camera = Camera()
private val controller = Controller()
private val wasdInput = WasdInput(controller)

private val matrixStack = MatrixStack()

fun main() {
    val window = GlWindow()
    window.create {
        val technique = ImmediateTechnique()
        window.resizeCallback = { width, height ->
            camera.setPerspective(width, height)
            technique.resize(camera)
        }
        window.deltaCallback = { delta ->
            wasdInput.onCursorDelta(delta)
        }
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
        }
        window.show {
            glClear()
            controller.apply { position, direction ->
                camera.setPosition(position)
                camera.lookAlong(direction)
            }
            technique.aabb(camera, aabb(vec3(-1f), vec3(1f)), matrixStack.peekMatrix())
            matrixStack.pushMatrix(mat4().identity().translate(vec3(1f))) {
                technique.marker(camera, matrixStack.peekMatrix())
            }
        }
    }
}