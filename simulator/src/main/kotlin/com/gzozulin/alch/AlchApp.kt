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

private const val WARES_IN_SHOP = 9

private const val PRICE_BOTTLE = 10
private const val PRICE_REAGENT = 15
private const val PRICE_POTION = 50

sealed class Ware

data class Bottle(val xx: Int = 123) : Ware()
data class Reagent(val type: ReagentType, val power: Float): Ware()
data class Order(val color: col3) : Ware()
data class Potion(val color: col3, val power: Float): Ware()

data class Shop(val wares: MutableList<Ware> = mutableListOf())
data class Player(var cash: Int = 1000, val wares: MutableList<Ware> = mutableListOf())
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
    val player = Player()

    val shop = Shop(wares = mutableListOf(
        templateBottle,
        templateBloodMoss,
        templateNightshade,
        templateSpidersSilk))

    val line = Line(customers = mutableListOf(
        templateCustomer.copy(name = generateName()),
        templateCustomer.copy(name = generateName()),
        templateCustomer.copy(name = generateName())))

    // todo: debuggling
    var columnsShopEnd = 0
    var columnsPlayerEnd = 0
    var columnsCustomersEnd = 0
}

class MechanicPrice {
    fun priceBuy(ware: Ware) = when (ware) {
        is Bottle -> PRICE_BOTTLE
        is Reagent -> PRICE_REAGENT
        is Potion -> PRICE_BOTTLE + PRICE_REAGENT
        is Order -> error("wtf?!")
    }

    fun priceSell(potion: Potion, customerColor: col3)
        = ((3f - potion.color.distance(customerColor) / 3f) * potion.power * PRICE_POTION).toInt()
}

class MechanicShop {
    private val console: Console by injector.instance()
    private val repository: Repository by injector.instance()
    private val mechanicPrice: MechanicPrice by injector.instance()

    fun throttleShop() {
        if (repository.shop.wares.size < WARES_IN_SHOP) {
            repository.shop.wares.add(Potion(col3().rand(), randf()))
        }
    }

    fun buyWare(idx: Int) {
        if (idx >= repository.shop.wares.size) {
            console.say("Ware does not exists!")
            return
        }
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
        if (firstIdx >= repository.player.wares.size || secondIdx >= repository.player.wares.size) {
            console.say("Potion does not exists!")
            return
        }
        val first = repository.player.wares[firstIdx]
        val second = repository.player.wares[secondIdx]
        val shouldRemove =
            if (first is Bottle && second is Reagent) {
                repository.player.wares.add(mixBottleReagent(second))
                true
            } else if (first is Reagent && second is Bottle) {
                repository.player.wares.add(mixBottleReagent(first))
                true
            } else if (first is Potion && second is Potion) {
                repository.player.wares.add(mixPotionPotion(first, second))
                repository.player.wares.add(Bottle())
                true
            } else {
                console.say("This combination is impossible!")
                false
            }
        // todo: potion + reagent
        if (shouldRemove) {
            repository.player.wares.remove(first)
            repository.player.wares.remove(second)
        }
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

    fun drinkPotion(idx: Int) {
        // todo: cannot drink bottles and reagents
        if (idx >= repository.player.wares.size) {
            console.say("Potion does not exists!")
            return
        }
        repository.player.wares.removeAt(idx)
        console.say("Potion is consumed!")
    }
}

class MechanicCustomers {
    private val console: Console by injector.instance()
    private val repository: Repository by injector.instance()
    private val mechanicPrice: MechanicPrice by injector.instance()

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
                    val color = when (randi(3)) {
                        0 -> col3().red()
                        1 -> col3().green()
                        2 -> col3().blue()
                        else -> error("wtf?!")
                    }
                    it.currentOrder = Order(color)
                }
            }
        }
    }

    fun sellPotion(playerIndex: Int, customerIndex: Int) {
        if (playerIndex >= repository.player.wares.size ||
            customerIndex >= repository.line.customers.size ||
            repository.line.customers[customerIndex].currentOrder == null) {
            console.say("Impossible transaction!")
            return
        }
        val ware = repository.player.wares[playerIndex]
        if (ware !is Potion) {
            console.say("Only potions can be sold!")
            return
        }
        val price =
            mechanicPrice.priceSell(ware, repository.line.customers[customerIndex].currentOrder!!.color)
        console.say("Potion sold! $playerIndex, $customerIndex for $price")
        repository.player.wares.removeAt(playerIndex)
        repository.line.customers[customerIndex].currentOrder = null
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