package com.gzozulin.alch

import com.gzozulin.minigl.gl.*
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

// todo: buying wares
// todo: customer orders
// todo: mixing potions
// todo: selling potions

private const val MILLIS_PER_TICK = 16 // 60 Hz

private const val DISSATISFACTION_PER_TICK = 0.0001f

private const val SATISFACTION_TO_BRING_FRIEND = 0.3f
private const val CHANCE_TO_BRING_CUSTOMER = .001f

private const val BOTTLE_PRICE = 10
private const val INGREDIENT_PRICE = 15
private const val SHOP_PRICE_MULTIPLIER = 1.3f
private const val POTION_PRICE_MULTIPLIER = 100f

interface Ware {
    val price: Int
}

data class Bottle(val xx: Int = 123) : Ware {
    override val price: Int
        get() = BOTTLE_PRICE
}

data class Reagent(val type: ReagentType, val power: Float): Ware {
    override val price: Int
        get() = INGREDIENT_PRICE
}

data class Potion(val color: vec3, val power: Float): Ware {
    override val price: Int
        get() = (power * POTION_PRICE_MULTIPLIER).toInt()

    companion object {
        fun random() = Potion(vec3().rand(vec3(0f), vec3(1f)), randf(0f, 1f))
    }
}

data class Shop(val wares: MutableList<Ware> = mutableListOf())
data class Player(var cash: Int = 100, val wares: MutableList<Ware> = mutableListOf())
data class Customer(val name: String, var satisfaction: Float, val wealth: Float, var timeout: Long,
                    var currentOrder: Potion? = null)
data class Line(val customers: MutableList<Customer> = mutableListOf())

private val firstnames = listOf("John", "Mark", "Charles", "Greg", "Andrew")
private val surnames = listOf("Smith", "Doe", "Buck", "Mallet", "Lynn")

private fun generateName() = "${firstnames.random()} ${surnames.random()}"

private val templateBottle      = Bottle()

enum class ReagentType { RED, BLUE, GREEN }
private val templateBloodMoss   = Reagent(type = ReagentType.RED,   power = .3f)
private val templateNightshade  = Reagent(type = ReagentType.GREEN, power = .3f)
private val templateSpidersSilk = Reagent(type = ReagentType.BLUE,  power = .3f)

private val templatePotionRed   = Potion(color = vec3().red(),   power = .5f)
private val templatePotionGreen = Potion(color = vec3().green(), power = .5f)
private val templatePotionBlue  = Potion(color = vec3().blue(),  power = .5f)

private val templateCustomer    = Customer(name = "John Smith", satisfaction = 0.5f, wealth = .5f, timeout = 2000L)

val di = DI {
    bind<GlWindow>()                with singleton { GlWindow() }
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

class Repository {
    val shop = Shop()
    val player = Player()
    val line = Line()

    // todo: debuggling
    var columnsShopEnd = 0
    var columnsPlayerEnd = 0
    var columnsCustomersEnd = 0
}

class MechanicShop {
    private val console: Console by di.instance()
    private val repository: Repository by di.instance()

    fun createShop() {
        repository.shop.wares.addAll(listOf(
            templateBottle,
            templateBloodMoss, templateNightshade, templateSpidersSilk,
            templatePotionRed, templatePotionGreen, templatePotionBlue))
    }

    fun buyWare(idx: Int) {
        check(idx < repository.shop.wares.size)
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

class MechanicPotions {
    private val repository: Repository by di.instance()

    fun mixPotion(firstIdx: Int, secondIdx: Int) {
        println("Mixing $firstIdx with $secondIdx")
    }
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

private val window: GlWindow by di.instance()
private val mechanicShop: MechanicShop by di.instance()
private val mechanicPotions: MechanicPotions by di.instance()
private val mechanicCustomers: MechanicCustomers by di.instance()
private val mechanicsPresentation: MechanicsPresentation by di.instance()
private val mechanicInput: MechanicInput by di.instance()

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