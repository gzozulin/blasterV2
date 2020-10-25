package com.gzozulin.alch

import com.gzozulin.minigl.gl.*
import org.kodein.di.*

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
// complexity: complex colors, fast dissatisfaction, expensive reagents
// scoreboard with Google Leaderboards
// colorful console text
// nonlinear potion price
// damage if potion strength > 1f
// random death phrases (poison, explosion, fire, police, etc)

// todo mix potions
// todo generate order with consideration to wealth
// todo sale potions
// todo drink potions
// todo mechanics presentation

private const val MILLIS_PER_TICK = 16 // 60 Hz

private const val DISSATISFACTION_PER_TICK = 0.0001f

private const val SATISFACTION_TO_BRING_FRIEND = 0.3f
private const val CHANCE_TO_BRING_CUSTOMER = .001f

private const val BOTTLE_PRICE = 10
private const val INGREDIENT_PRICE = 15
private const val SHOP_PRICE_MULTIPLIER = 1.3f
private const val POTION_PRICE_MULTIPLIER = 100f

private data class Player(var cash: Int = 100)

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
}

private data class Shop(val wares: MutableList<Ware> = mutableListOf())
private data class Store(val wares: MutableList<Ware> = mutableListOf())

private data class Customer(val name: String, var satisfaction: Float, val wealth: Float, var timeout: Long,
                            var currentOrder: Potion? = null)
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
}

private class Console {
    fun say(what: String) = println(what)
}

private class Repository {
    val player = Player()
    val shop = Shop()
    val store = Store()
    val line = Line()
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
        repository.store.wares.add(ware)
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

    fun drawShopWares() {

    }
}

class AlchApp: GlResource() {
    private val mechanicShop: MechanicShop by di.instance()
    private val mechanicPotions: MechanicPotions by di.instance()
    private val mechanicCustomers: MechanicCustomers by di.instance()
    private val mechanicsPresentation: MechanicsPresentation by di.instance()

    init {
        addChildren(mechanicsPresentation)
    }

    override fun use() {
        super.use()
        mechanicShop.createShop()
        mechanicCustomers.createCustomers()
    }

    fun tick() {
        mechanicCustomers.throttleDissatisfaction()
        mechanicCustomers.throttleSatisfaction()
        mechanicCustomers.throttleOrders()
        mechanicsPresentation.drawShopWares()
    }
}

private val window = GlWindow()
private val app = AlchApp()

fun main() {
    window.create(isHoldingCursor = false) {
        glUse(app) {
            window.show {
                app.tick()
            }
        }
    }
}