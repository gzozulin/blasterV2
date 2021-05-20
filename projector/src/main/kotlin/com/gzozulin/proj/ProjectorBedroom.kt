package com.gzozulin.proj

import com.gzozulin.minigl.api.*
import com.gzozulin.minigl.assets.libTextureCreate
import com.gzozulin.minigl.assets.libWavefrontCreate
import com.gzozulin.minigl.assets.libWavefrontGroupUse
import com.gzozulin.minigl.scene.Camera
import com.gzozulin.minigl.scene.ControllerFirstPerson
import com.gzozulin.minigl.scene.PointLight
import com.gzozulin.minigl.scene.WasdInput
import com.gzozulin.minigl.techniques.ShadingPhong
import com.gzozulin.minigl.techniques.glShadingPhongDraw
import com.gzozulin.minigl.techniques.glShadingPhongInstance
import com.gzozulin.minigl.techniques.glShadingPhongUse
import org.lwjgl.glfw.GLFW

private val window = GlWindow(1500, 900, isFullscreen = false, isHoldingCursor = false, isMultisampling = true)

private val bedroomGroup = libWavefrontCreate("models/bedroom/bedroom")
private val empty = libTextureCreate("textures/snow.png")

private val camera = Camera(window)
private val controller = ControllerFirstPerson(position = bedroomGroup.aabb.center(), velocity = 1f)
private val wasdInput = WasdInput(controller)

private val cameraLight = PointLight(bedroomGroup.aabb.center(), vec3(1f), 350f)
private val lights = listOf(
    cameraLight,
    PointLight(vec3(2.732e1f, 150.3f, 5.401e1f),    vec3().white(),  800f),
    PointLight(vec3(178.7f,   91.33f,  -107.6f),    vec3().yellow(), 500f),
    PointLight(vec3(-113.8f,  104.5f,  -91.5f),     vec3().azure(),  1000f),
    PointLight(vec3(28.2f,    112.2f,   193.7f),    vec3().cyan(),   300f),
)

private val modelM = constm4(mat4().identity())
private val viewM = unifm4 { camera.calculateViewM() }
private val eye = unifv3 { camera.position }
private val projM = unifm4 { camera.projectionM }

private val sampler = unifs()
private val diffuseMap = sampler(sampler)

private val matAmbient = constv3(vec3(0.05f))
private val matDiffuse = unifv3()
private val matSpecular = constv3(vec3(0.33f))
private val matShine = constf(1f)
private val matTransparency = constf(1f)

private val shadingPhong = ShadingPhong(
    modelM, viewM, projM, eye, diffuseMap, matAmbient, matDiffuse, matSpecular, matShine, matTransparency)

private var mouseLook = false

fun main() {
    window.create {
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
        window.keyCallback = { key, pressed ->
            wasdInput.onKeyPressed(key, pressed)
            if (pressed && key == GLFW.GLFW_KEY_SPACE) {
                println("Pos: ${controller.position} dir: ${controller.direction}")
            }
        }
        glShadingPhongUse(shadingPhong) {
            libWavefrontGroupUse(bedroomGroup) {
                glTextureUse(empty) {
                    window.show {
                        glClear()
                        controller.apply { position, direction ->
                            camera.setPosition(position)
                            camera.lookAlong(direction)
                        }
                        cameraLight.position.set(camera.position)
                        glDepthTest {
                            glTextureBind(empty) {
                                glShadingPhongDraw(shadingPhong, lights) {
                                    bedroomGroup.objects.forEach { obj ->
                                        if (obj.phong.mapDiffuse != null) {
                                            glTextureBind(obj.phong.mapDiffuse!!) {
                                                sampler.value = obj.phong.mapDiffuse!!
                                                matDiffuse.value = obj.phong.diffuse
                                                glShadingPhongInstance(shadingPhong, obj.mesh)
                                            }
                                        } else {
                                            sampler.value = empty
                                            matDiffuse.value = obj.phong.diffuse
                                            glShadingPhongInstance(shadingPhong, obj.mesh)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
