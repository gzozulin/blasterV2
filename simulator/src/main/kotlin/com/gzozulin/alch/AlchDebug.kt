package com.gzozulin.alch

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.techniques.SimpleTechnique
import org.kodein.di.instance

private const val POTION_GRID_WIDTH = 5
private const val POTION_GRID_SIDE = 2f

class MechanicsPresentation: GlResource() {
    private val repository: Repository by di.instance()

    private val simpleTechnique = SimpleTechnique()

    private val rect = GlMesh.rect()
    private val bottle = texturesLib.loadTexture("textures/bottle.png")
    private val reagentRed = texturesLib.loadTexture("textures/blood_moss.jpg")
    private val reagentGreen = texturesLib.loadTexture("textures/nightshade.jpg")
    private val reagentBlue = texturesLib.loadTexture("textures/spiders_silk.png")
    private val marble = texturesLib.loadTexture("textures/marble.jpg")
    private val matrixStack = MatrixStack()

    init {
        addChildren(simpleTechnique, rect, bottle, reagentRed, reagentGreen, reagentBlue, marble)
    }

    fun drawGrid() {
        var column = 0
        var row = 0
        fun nextRow() {
            row++
            if (row % POTION_GRID_WIDTH == 0) {
                row = 0
                column++
            }
        }
        fun endColumn() {
            if (row % POTION_GRID_WIDTH != 0) {
                column +=1
                row = 0
            }
        }
        fun skipColumn() {
            row = 0
            column += 1
        }
        fun position() = vec3(column * POTION_GRID_SIDE, row * -POTION_GRID_SIDE, 0f)
        val renderList = mutableListOf<Pair<Ware, vec3>>()
        repository.shop.wares.forEach { ware ->
            renderList.add(ware to position())
            nextRow()
        }
        endColumn()
        skipColumn()
        repository.columnsShopEnd = column
        repository.player.wares.forEach { ware ->
            renderList.add(ware to position())
            nextRow()
        }
        endColumn()
        skipColumn()
        repository.columnsPlayerEnd = column
        repository.line.customers.forEach { customer ->
            if (customer.currentOrder != null) {
                renderList.add(customer.currentOrder!! to position())
                nextRow()
            }
        }
        endColumn()
        repository.columnsCustomersEnd = column
        val width = column * POTION_GRID_SIDE
        val height = POTION_GRID_WIDTH * POTION_GRID_SIDE
        val left = 0f - POTION_GRID_SIDE /2f
        val right = width - POTION_GRID_SIDE /2f
        val bottom = -height + POTION_GRID_SIDE /2f
        val top = 0f + POTION_GRID_SIDE /2f
        val projM = mat4().identity().ortho(left, right, bottom, top, 10000f, -1f)
        val viewM = mat4().identity()
        renderList.forEach { pair ->
            drawWare(pair.first, pair.second, viewM, projM)
        }
    }

    private fun drawWare(ware: Ware, position: vec3, viewM: mat4, projM: mat4) {
        simpleTechnique.draw(viewM, projM) {
            matrixStack.pushMatrix(mat4().identity().translate(position)) {
                if (ware is Reagent) {
                    val diffuse = when (ware.type) {
                        ReagentType.RED -> reagentRed
                        ReagentType.BLUE -> reagentBlue
                        ReagentType.GREEN -> reagentGreen
                    }
                    simpleTechnique.instance(rect, diffuse, matrixStack.peekMatrix())
                } else {
                    simpleTechnique.instance(rect, bottle, matrixStack.peekMatrix())
                    if (ware is Potion) {
                        matrixStack.pushMatrix(mat4().identity().translate(0f, (ware.power - 1f), 0f)) {
                            matrixStack.pushMatrix(mat4().scale(1f, ware.power, 1f)) {
                                simpleTechnique.instance(rect, marble, matrixStack.peekMatrix(), color = ware.color)
                            }
                        }
                    }
                }
            }
        }
    }
}

class MechanicInput {
    private val window: GlWindow by di.instance()
    private val repository: Repository by di.instance()

    // todo: click shop: buy from a shop
    // todo: click, click again in player inventory: mix a potion
    // todo: click right: drink a potion
    // todo: click, click again in customer inventory: sell a potion

    private val cursor = vec2()
    private fun current(): vec2i {
        val normalized = vec2(cursor.x / window.width, cursor.y / window.height)
        return vec2i(
            (repository.columnsCustomersEnd * normalized.x).toInt(),
            (POTION_GRID_WIDTH * normalized.y).toInt())
    }

    fun onCursorPosition(position: vec2) {
        cursor.set(position)
    }

    fun onButtonPressed(button: MouseButton, pressed: Boolean) {
        if (pressed) {
            when (button) {
                MouseButton.LEFT -> onLmb()
                MouseButton.RIGHT -> onRmb()
            }
        }
    }

    private fun onLmb() {
        val current = current()
        when {
            current.x < repository.columnsShopEnd -> {
                println("shop!")
            }
            current.x < repository.columnsPlayerEnd -> {
                println("player!")
            }
            current.x < repository.columnsCustomersEnd -> {
                println("customer!")
            }
            else -> {
                error("wtf?!")
            }
        }
    }

    private fun onRmb() {

    }
}