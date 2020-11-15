package com.gzozulin.alch

import com.gzozulin.minigl.gl.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import kotlin.math.min

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
// customers by satisfaction rate: the higher the level is the worse customer type

// presentation independent from mechanics:
//      you can play with inventory but that will not affect mechanics

// todo: bug with input
// todo: customer orders
// todo: mixing potions
// todo: selling potions
// todo: satisfaction and desatisfaction

private const val MILLIS_PER_TICK = 16 // 60 Hz

private const val DISSATISFACTION_PER_TICK = 0.0001f

private const val SATISFACTION_TO_BRING_FRIEND = 0.3f
private const val CHANCE_TO_BRING_CUSTOMER = .001f

private const val WARES_IN_SHOP = 10

private const val PRICE_BOTTLE = 10
private const val PRICE_REAGENT = 15
private const val MULTIPLIER_BUY = 1.3f
private const val MULTIPLIER_SELL = 0.7f

sealed class Ware

data class Bottle(val xx: Int = 123) : Ware()
data class Reagent(val type: ReagentType, val power: Float): Ware()
data class Order(val color: col3) : Ware()
data class Potion(val color: col3, val power: Float): Ware()

data class Shop(val wares: MutableList<Ware> = mutableListOf())
data class Player(var cash: Int = 100, val wares: MutableList<Ware> = mutableListOf())
data class Customer(val name: String, var satisfaction: Float, val wealth: Float, var timeout: Long,
                    var currentOrder: Order? = null)
data class Line(val customers: MutableList<Customer> = mutableListOf())

private val firstnames = listOf("John", "Mark", "Charles", "Greg", "Andrew")
private val surnames = listOf("Smith", "Doe", "Buck", "Mallet", "Lynn")

private fun generateName() = "${firstnames.random()} ${surnames.random()}"

private val templateBottle      = Bottle()

enum class ReagentType { RED, BLUE, GREEN }
private val templateBloodMoss   = Reagent(type = ReagentType.RED,   power = .3f)
private val templateNightshade  = Reagent(type = ReagentType.GREEN, power = .3f)
private val templateSpidersSilk = Reagent(type = ReagentType.BLUE,  power = .3f)

private val templateCustomer    = Customer(name = "John Smith", satisfaction = 0.5f, wealth = .5f, timeout = 2000L)

val injector = DI {
    bind<GlWindow>()                with singleton { GlWindow() }
    bind<Console>()                 with singleton { Console() }
    bind<Repository>()              with singleton { Repository() }
    bind<MechanicPrice>()           with singleton { MechanicPrice() }
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

class MechanicPrice {
    fun priceBuy(ware: Ware): Int {
        var price = when (ware) {
            is Bottle -> PRICE_BOTTLE
            is Reagent -> PRICE_REAGENT
            is Potion -> PRICE_BOTTLE + PRICE_REAGENT
            is Order -> error("wtf?!")
        }
        price = (price * MULTIPLIER_BUY).toInt()
        return price
    }

    fun priceSell(ware: Ware) {

    }
}

class MechanicShop {
    private val console: Console by injector.instance()
    private val repository: Repository by injector.instance()
    private val mechanicPrice: MechanicPrice by injector.instance()

    fun createShop() {
        repository.shop.wares.addAll(listOf(
            templateBottle,
            templateBloodMoss, templateNightshade, templateSpidersSilk))
    }

    fun throttleShop() {
        if (repository.shop.wares.size < WARES_IN_SHOP) {
            repository.shop.wares.add(Potion(col3().rand(), randf()))
        }
    }

    fun buyWare(idx: Int) {
        check(idx < repository.shop.wares.size)
        val ware = repository.shop.wares[idx]
        val price = mechanicPrice.priceBuy(ware)
        if (repository.player.cash < price) {
            console.say("You cannot afford that!")
            return
        }
        repository.player.cash -= price
        repository.player.wares.add(ware)
    }
}

class MechanicPotions {
    private val repository: Repository by injector.instance()
    private val console: Console by injector.instance()

    fun mixPotion(firstIdx: Int, secondIdx: Int) {
        check(firstIdx < repository.player.wares.size)
        check(secondIdx < repository.player.wares.size)
        val first = repository.player.wares[firstIdx]
        val second = repository.player.wares[secondIdx]
        if (first is Bottle && second is Reagent) {
            repository.player.wares.add(mixBottleReagent(second))
        } else if (first is Reagent && second is Bottle) {
            repository.player.wares.add(mixBottleReagent(first))
        } else if (first is Potion && second is Potion) {
            repository.player.wares.add(mixPotionPotion(first, second))
            repository.player.wares.add(Bottle())
        } else {
            console.say("This combination is impossible!")
        }
        // todo: potion + reagent
        repository.player.wares.remove(first)
        repository.player.wares.remove(second)
    }

    private fun mixBottleReagent(reagent: Reagent) = Potion(color = when (reagent.type) {
        ReagentType.RED -> vec3().red()
        ReagentType.GREEN -> vec3().green()
        ReagentType.BLUE -> vec3().blue()
    }, power = reagent.power)

    private fun mixPotionPotion(first: Potion, second: Potion): Potion {
        val r = min(1f, (first.color.r + second.color.r))
        val g = min(1f, (first.color.g + second.color.g))
        val b = min(1f, (first.color.b + second.color.b))
        return Potion(color = vec3(r, g, b), power = min(1f, first.power + second.power))
    }
}

private class MechanicCustomers {
    private val console: Console by injector.instance()
    private val repository: Repository by injector.instance()

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
                    it.currentOrder = Order(col3().rand())
                }
            }
        }
    }

    fun sellPotion() {
        // the more precise color == the better
        // the more power == the better
    }
}

private val window: GlWindow by injector.instance()
private val mechanicShop: MechanicShop by injector.instance()
private val mechanicPotions: MechanicPotions by injector.instance()
private val mechanicCustomers: MechanicCustomers by injector.instance()
private val mechanicsPresentation: MechanicsPresentation by injector.instance()
private val mechanicInput: MechanicInput by injector.instance()

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
                mechanicShop.throttleShop()
                //mechanicCustomers.throttleDissatisfaction()
                //mechanicCustomers.throttleSatisfaction()
                mechanicCustomers.throttleOrders()
                mechanicsPresentation.drawGrid()
            }
        }
    }
}