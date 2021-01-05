package com.gzozulin.alch

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.techniques.SimpleTechnique
import org.kodein.di.instance

private const val POTION_GRID_WIDTH = 5
private const val POTION_GRID_SIDE = 2f

class MechanicsPresentation: GlResource() {
    private val repository: Repository by injector.instance()

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
            renderList.add(customer.order to position())
            nextRow()
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
                when (ware) {
                    is Bottle -> drawBottle()
                    is Reagent -> drawReagent(ware)
                    is Potion -> {
                        drawBottle()
                        drawContent(ware.power, ware.color)
                    }
                    is Order -> drawContent(ware.timeout, ware.color)
                }
            }
        }
    }

    private fun drawBottle() {
        simpleTechnique.instance(rect, bottle, matrixStack.peekMatrix())
    }

    private fun drawReagent(reagent: Reagent) {
        val diffuse = when (reagent.type) {
            ReagentType.RED -> reagentRed
            ReagentType.BLUE -> reagentBlue
            ReagentType.GREEN -> reagentGreen
        }
        simpleTechnique.instance(rect, diffuse, matrixStack.peekMatrix())
    }

    private fun drawContent(power: Float, color: col3) {
        matrixStack.pushMatrix(mat4().identity().translate(0f, (power - 1f), 0f)) {
            matrixStack.pushMatrix(mat4().scale(1f, power, 1f)) {
                simpleTechnique.instance(rect, marble, matrixStack.peekMatrix(), color = color)
            }
        }
    }
}

class MechanicInput {
    private val window: GlWindow by injector.instance()
    private val repository: Repository by injector.instance()
    private val mechanicShop: MechanicShop by injector.instance()
    private val mechanicPotions: MechanicPotions by injector.instance()
    private val mechanicCustomers: MechanicCustomers by injector.instance()

    // click shop: buy from a shop
    // click, click again in player inventory: mix a potion
    // click: click in player same: drink a potion
    // click, click again in customer inventory: sell a potion
    // click right: remove selection

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

    private enum class SelectType { SHOP, PLAYER, CUSTOMER }
    private fun chooseType(cursor: vec2i) = when {
        cursor.x < repository.columnsShopEnd -> SelectType.SHOP
        cursor.x < repository.columnsPlayerEnd -> SelectType.PLAYER
        cursor.x < repository.columnsCustomersEnd -> SelectType.CUSTOMER
        else -> error("wtf?!")
    }

    private fun shopIndex(cursor: vec2i) = cursor.y + cursor.x * POTION_GRID_WIDTH
    private fun playerIndex(cursor: vec2i) = shopIndex(cursor) - repository.columnsShopEnd * POTION_GRID_WIDTH
    private fun customerIndex(cursor: vec2i): Int = shopIndex(cursor) - repository.columnsPlayerEnd * POTION_GRID_WIDTH

    private var prevIndex: Int? = null
    private var prevType: SelectType? = null

    private fun clearChoice() {
        prevIndex = null
        prevType = null
    }

    private fun onRmb() {
        clearChoice()
    }

    private fun onLmb() {
        val currSelect = current()
        val currType = chooseType(currSelect)
        val currIndex = when (currType) {
            SelectType.SHOP -> shopIndex(currSelect)
            SelectType.PLAYER -> playerIndex(currSelect)
            SelectType.CUSTOMER -> customerIndex(currSelect)
        }
        if (currType == SelectType.SHOP) {
            mechanicShop.buyWare(currIndex)
            clearChoice()
        } else if (prevType != null && prevType == SelectType.PLAYER && currType == SelectType.PLAYER) {
            if (currIndex == prevIndex) {
                mechanicPotions.drinkPotion(currIndex)
            } else {
                mechanicPotions.mixPotion(prevIndex!!, currIndex)
            }
            clearChoice()
        } else if (prevType != null && prevType == SelectType.PLAYER && currType == SelectType.CUSTOMER) {
            mechanicCustomers.sellPotion(prevIndex!!, currIndex)
            clearChoice()
        } else {
            prevType = currType
            prevIndex = currIndex
        }
    }
}