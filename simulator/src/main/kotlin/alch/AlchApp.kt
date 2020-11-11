package alch

import com.gzozulin.minigl.assets.texturesLib
import com.gzozulin.minigl.gl.*
import com.gzozulin.minigl.scene.MatrixStack
import com.gzozulin.minigl.techniques.SimpleTechnique
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

// Alchemist

// potion: color and power
// potion: effect depends on color and power, from cook book
// customers: satisfaction, if more - more customers, if less - gone
// customer requires potion of this color and up to this power
// more precise: more money and satisfaction, less - less
// sell potions to customers for money
// ingredients and bottles costs money, ingredients cheap, potions expensive
// ingredients shop -> your store -> customers line
// "physical" store management - uo backpack
// potions: money, slow time, satisfaction
// enough money - won
// no customers - game over
// complexity: complex colors!, fast dissatisfaction, expensive reagents
// scoreboard with Google Leaderboards
// colorful console text
// nonlinear potion price
// damage if potion strength > 1f
// random death phrases (poison, explosion, fire, police, etc)
// more colors in potion: more complex: more side effects (good and bad)

// presentation independent from mechanics:
//      you can play with inventory but that will not affect mechanics

// todo: horizontal presentation
// todo: present repository
// todo: input system

private const val MILLIS_PER_TICK = 16 // 60 Hz

private const val DISSATISFACTION_PER_TICK = 0.0001f

private const val SATISFACTION_TO_BRING_FRIEND = 0.3f
private const val CHANCE_TO_BRING_CUSTOMER = .001f

private const val BOTTLE_PRICE = 10
private const val INGREDIENT_PRICE = 15
private const val SHOP_PRICE_MULTIPLIER = 1.3f
private const val POTION_PRICE_MULTIPLIER = 100f

private const val POTION_GRID_WIDTH = 5
private const val POTION_GRID_SIDE = 2f

private interface Ware {
    val price: Int
}

private data class Bottle(val xx: Int = 123) : Ware {
    override val price: Int
        get() = BOTTLE_PRICE
}

private data class Ingredient(val color: vec3, val power: Float): Ware {
    override val price: Int
        get() = INGREDIENT_PRICE
}

private data class Potion(val color: vec3, val power: Float): Ware {
    override val price: Int
        get() = (power * POTION_PRICE_MULTIPLIER).toInt()

    companion object {
        fun random() = Potion(vec3().rand(vec3(0f), vec3(1f)), randf(0f, 1f))
    }
}

private data class Shop(val wares: MutableList<Ware> = mutableListOf())

private data class Player(var cash: Int = 100, val wares: MutableList<Ware> = mutableListOf())

private data class Customer(val name: String, var satisfaction: Float, val wealth: Float, var timeout: Long,
                            var currentOrder: Ware? = null)
private data class Line(val customers: MutableList<Customer> = mutableListOf())

private val firstnames = listOf("John", "Mark", "Charles", "Greg", "Andrew")
private val surnames = listOf("Smith", "Doe", "Buck", "Mallet", "Lynn")

private fun generateName() = "${firstnames.random()} ${surnames.random()}"

private val templateBottle      = Bottle()
private val templateBloodMoss   = Ingredient(color = vec3(1f, 0f, 0f), power = .3f)
private val templateNightshade  = Ingredient(color = vec3(0f, 1f, 0f), power = .3f)
private val templateSpidersSilk = Ingredient(color = vec3(0f, 0f, 1f), power = .3f)

private val templateCustomer    = Customer(name = "John Smith", satisfaction = 0.5f, wealth = .5f, timeout = 2000L)

val di = DI {
    bind<Console>()                 with singleton { Console() }
    bind<Repository>()              with singleton { Repository() }
    bind<MechanicShop>()            with singleton { MechanicShop() }
    bind<MechanicPotions>()         with singleton { MechanicPotions() }
    bind<MechanicCustomers>()       with singleton { MechanicCustomers() }
    bind<MechanicsPresentation>()   with singleton { MechanicsPresentation() }
    bind<MechanicInput>()           with singleton { MechanicInput() }
}

private class Console {
    fun say(what: String) = println(what)
}

private class Repository {
    val shop = Shop()
    val player = Player()
    val line = Line()

    // todo: debuggling
    var columnsShopEnd = 0
    var columnsPlayerEnd = 0
    var columnsCustomersEnd = 0
}

private class MechanicShop {
    private val console: Console by di.instance()
    private val repository: Repository by di.instance()

    fun createShop() {
        repository.shop.wares.addAll(listOf(templateBottle,
            templateBloodMoss, templateNightshade, templateSpidersSilk))
    }

    fun buyWare(idx: Int) {
        val ware = repository.shop.wares[idx]
        val price = ware.price * SHOP_PRICE_MULTIPLIER
        if (repository.player.cash < price) {
            console.say("You cannot afford that!")
            return
        }
        repository.player.cash -= ware.price
        repository.player.wares.add(ware)
    }
}

private class MechanicPotions {
    private val repository: Repository by di.instance()

    fun mixPotion() {}
}

private class MechanicCustomers {
    private val console: Console by di.instance()
    private val repository: Repository by di.instance()

    fun createCustomers() {
        repository.line.customers.addAll(listOf(
            templateCustomer.copy(name = generateName()),
            templateCustomer.copy(name = generateName()),
            templateCustomer.copy(name = generateName())))
    }

    fun throttleDissatisfaction() {
        val toRemove = mutableListOf<Customer>()
        repository.line.customers.forEach {
            it.satisfaction -= DISSATISFACTION_PER_TICK
            if (it.satisfaction < 0f) {
                console.say("${it.name} decided to leave your shop!")
                toRemove.add(it)
            }
        }
        repository.line.customers.removeAll(toRemove)
    }

    fun throttleSatisfaction() {
        val toAdd = mutableListOf<Customer>()
        repository.line.customers.forEach {
            if (it.satisfaction > SATISFACTION_TO_BRING_FRIEND) {
                if (CHANCE_TO_BRING_CUSTOMER > randf()) {
                    console.say("${it.name} brought a friend!")
                    toAdd.add(templateCustomer.copy(name = generateName()))
                }
            }
        }
        repository.line.customers.addAll(toAdd)
    }

    fun throttleOrders() {
        repository.line.customers.forEach {
            if (it.currentOrder == null) {
                it.timeout -= MILLIS_PER_TICK
                if (it.timeout < 0) {
                    // todo
                    it.currentOrder = Potion(vec3(1f), 1f)
                }
            }
        }
    }
}

private class MechanicsPresentation: GlResource() {
    private val repository: Repository by di.instance()

    private val simpleTechnique = SimpleTechnique()

    private val rect = GlMesh.rect()
    private val bottle = texturesLib.loadTexture("textures/bottle.png")
    private val marble = texturesLib.loadTexture("textures/marble.jpg")
    private val matrixStack = MatrixStack()

    init {
        addChildren(simpleTechnique, rect, bottle, marble)
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
        val left = 0f - POTION_GRID_SIDE/2f
        val right = width - POTION_GRID_SIDE/2f
        val bottom = -height + POTION_GRID_SIDE/2f
        val top = 0f + POTION_GRID_SIDE/2f
        val projM = mat4().identity().ortho(left, right, bottom, top, 10000f, -1f)
        val viewM = mat4().identity()
        renderList.forEach { pair ->
            drawWare(pair.first, pair.second, viewM, projM)
        }
    }

    private fun drawWare(ware: Ware, position: vec3, viewM: mat4, projM: mat4) {
        when (ware) {
            is Potion -> {
                simpleTechnique.draw(viewM, projM) {
                    matrixStack.pushMatrix(mat4().identity().translate(position)) {
                        simpleTechnique.instance(rect, bottle, matrixStack.peekMatrix())
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

private class MechanicInput {
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

private val repository: Repository by di.instance()
private val mechanicShop: MechanicShop by di.instance()
private val mechanicPotions: MechanicPotions by di.instance()
private val mechanicCustomers: MechanicCustomers by di.instance()
private val mechanicsPresentation: MechanicsPresentation by di.instance()
private val mechanicInput: MechanicInput by di.instance()

private val window = GlWindow()

fun main() {
    window.create(isHoldingCursor = false) {
        mechanicShop.createShop()
        mechanicCustomers.createCustomers()
        window.buttonCallback = { button, pressed ->
            mechanicInput.onButtonPressed(button, pressed)
        }
        window.positionCallback = { position ->
            mechanicInput.onCursorPosition(position)
        }
        glUse(mechanicsPresentation) {
            window.show {
                glClear(color = vec3().grey())
                // repository.oneMore()
                mechanicCustomers.throttleDissatisfaction()
                // mechanicCustomers.throttleSatisfaction()
                mechanicCustomers.throttleOrders()
                mechanicsPresentation.drawGrid()
            }
        }
    }
}