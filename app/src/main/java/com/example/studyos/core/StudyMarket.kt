package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class MarketStock(
    val symbol: String,
    val owner: String,
    val isBot: Boolean,
    val price: Double,
    val totalShares: Int,
    val streak: Int,
    val changePercent: Double,
    val lastEvent: String
)

data class MarketHolding(
    val symbol: String,
    val shares: Int,
    val avgPrice: Double
)

data class MarketEvent(
    val message: String,
    val positive: Boolean,
    val timestamp: Long
)

private data class BotProfile(
    val id: String,
    val name: String,
    val symbol: String,
    val discipline: Double,
    val aggression: Double
)

object StudyMarket {
    const val USER_SYMBOL = "\$YOU"
    private const val TOTAL_SHARES = 1000
    private const val DIVIDEND_RATE = 0.30

    val timerWalnuts = MutableStateFlow(0.0)
    val saleWalnuts = MutableStateFlow(0.0)
    val dividendWalnuts = MutableStateFlow(0.0)
    val stocks = MutableStateFlow<Map<String, MarketStock>>(emptyMap())
    val holdings = MutableStateFlow<Map<String, MarketHolding>>(emptyMap())
    val events = MutableStateFlow<List<MarketEvent>>(emptyList())
    val priceHistory = MutableStateFlow<Map<String, List<Double>>>(emptyMap())

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val random = Random(System.currentTimeMillis())
    private lateinit var prefs: SharedPreferences
    private var started = false

    private val bots = listOf(
        BotProfile("bot_mira", "Mira", "\$MIRA", 0.88, 0.35),
        BotProfile("bot_ayan", "Ayan", "\$AYAN", 0.76, 0.72),
        BotProfile("bot_noor", "Noor", "\$NOOR", 0.93, 0.22),
        BotProfile("bot_zayd", "Zayd", "\$ZAYD", 0.64, 0.86),
        BotProfile("bot_kai", "Kai", "\$KAI", 0.79, 0.58)
    )

    fun init(context: Context) {
        if (started) return
        started = true
        prefs = context.applicationContext.getSharedPreferences("studyos_market", Context.MODE_PRIVATE)
        load()
        ensureStocks()
        scope.launch {
            while (true) {
                delay(15_000L)
                tick()
            }
        }
    }

    fun totalWallet(): Double = timerWalnuts.value + saleWalnuts.value + dividendWalnuts.value

    fun portfolioValue(): Double {
        return holdings.value.values.sumOf { holding ->
            (stocks.value[holding.symbol]?.price ?: 0.0) * holding.shares
        }
    }

    fun buy(symbol: String, shares: Int): Boolean {
        if (shares <= 0 || symbol == USER_SYMBOL) return false
        ensureStocks()
        val stock = stocks.value[symbol] ?: return false
        val cost = stock.price * shares
        if (!deductWallet(cost)) return false
        val map = holdings.value.toMutableMap()
        val current = map[symbol] ?: MarketHolding(symbol, 0, 0.0)
        val newShares = current.shares + shares
        val newAvg = ((current.avgPrice * current.shares) + cost) / newShares
        map[symbol] = current.copy(shares = newShares, avgPrice = newAvg)
        holdings.value = map
        updatePrice(symbol, 0.6, "Demand increased")
        addEvent("You bought $shares $symbol at ${stock.price.f1()} walnuts", true)
        saveHoldings()
        saveWallet()
        return true
    }

    fun sell(symbol: String, shares: Int): Boolean {
        if (shares <= 0 || symbol == USER_SYMBOL) return false
        ensureStocks()
        val holding = holdings.value[symbol] ?: return false
        if (holding.shares < shares) return false
        val stock = stocks.value[symbol] ?: return false
        val revenue = stock.price * shares * 0.98
        saleWalnuts.value += revenue
        val map = holdings.value.toMutableMap()
        val remaining = holding.shares - shares
        if (remaining <= 0) {
            map.remove(symbol)
        } else {
            map[symbol] = holding.copy(shares = remaining)
        }
        holdings.value = map
        updatePrice(symbol, -0.5, "Supply increased")
        addEvent("You sold $shares $symbol for ${revenue.f1()} walnuts", true)
        saveHoldings()
        saveWallet()
        return true
    }

    fun convertDividendsToFame(): Int {
        val dividend = dividendWalnuts.value
        val fame = (dividend / 10.0).toInt()
        if (fame <= 0) return 0
        dividendWalnuts.value -= fame * 10.0
        Economy.addFame(fame)
        addEvent("Converted ${fame * 10} dividend walnuts into $fame Fame", true)
        saveWallet()
        return fame
    }

    fun addAdminWalnuts(amount: Double) {
        ensureStocks()
        timerWalnuts.value += amount
        addEvent("Admin added ${amount.toInt()} Golden Walnuts", true)
        saveWallet()
    }

    fun onUserSessionCompleted(minutes: Int, strict: Boolean) {
        ensureStocks()
        val reward = minutes * if (strict) 1.5 else 1.0
        timerWalnuts.value += reward
        val pump = (minutes / 10.0) + if (strict) 4.0 else 2.0
        updatePrice(USER_SYMBOL, pump, "Session completed")
        addEvent(
            "You completed ${minutes}m focus. +${reward.f1()} walnuts. $USER_SYMBOL +${pump.f1()}%",
            true
        )
        saveWallet()
    }

    fun onUserBusted(appName: String) {
        ensureStocks()
        updatePrice(USER_SYMBOL, -12.0, "Busted")
        addEvent("You opened $appName. $USER_SYMBOL crashed 12%", false)
    }

    private fun tick() {
        ensureStocks()
        bots.forEach { simulateBot(it) }
    }

    private fun simulateBot(bot: BotProfile) {
        val studyChance = 0.10 + bot.discipline * 0.14
        val bustChance = (1.0 - bot.discipline) * 0.08
        val roll = random.nextDouble()
        if (roll < studyChance) {
            val minutes = listOf(25, 50, 90).random(random)
            val pump = 1.2 + (minutes / 20.0) * (0.7 + random.nextDouble() * 0.9)
            updatePrice(bot.symbol, pump, "${bot.name} studied")
            payBotDividend(bot, minutes)
            addEvent("${bot.name} completed ${minutes}m. ${bot.symbol} +${pump.f1()}%", true)
        } else if (roll < studyChance + bustChance) {
            val crash = -(4.0 + random.nextDouble() * 14.0)
            updatePrice(bot.symbol, crash, "${bot.name} lost discipline")
            addEvent("${bot.name} got distracted. ${bot.symbol} ${crash.f1()}%", false)
        }
        if (random.nextDouble() < bot.aggression * 0.10) {
            val target = bots.filter { it.id != bot.id }.random(random)
            val buying = random.nextBoolean()
            updatePrice(target.symbol, if (buying) 0.8 else -0.8, "Bot trade")
            addEvent("${bot.name} ${if (buying) "bought" else "sold"} ${target.symbol}", buying)
        }
        if (random.nextDouble() < 0.02) {
            updatePrice(USER_SYMBOL, 1.5, "Bot invested in you")
            addEvent("${bot.name} invested in your potential. $USER_SYMBOL +1.5%", true)
        }
    }

    private fun payBotDividend(bot: BotProfile, minutes: Int) {
        val holding = holdings.value[bot.symbol] ?: return
        if (holding.shares <= 0) return
        val yield = minutes.toDouble()
        val pool = yield * DIVIDEND_RATE
        val amount = pool * holding.shares / TOTAL_SHARES
        if (amount > 0.0) {
            dividendWalnuts.value += amount
            addEvent("${bot.name} paid ${amount.f1()} dividend walnuts from your ${bot.symbol} shares", true)
            saveWallet()
        }
    }

    private fun updatePrice(symbol: String, percent: Double, event: String) {
        val map = stocks.value.toMutableMap()
        val stock = map[symbol] ?: return
        val newPrice = max(1.0, stock.price * (1.0 + percent / 100.0))
        map[symbol] = stock.copy(
            price = newPrice,
            changePercent = percent,
            lastEvent = event,
            streak = if (percent > 0) stock.streak + 1 else 0
        )
        stocks.value = map
        
        val historyMap = priceHistory.value.toMutableMap()
        val history = historyMap[symbol]?.toMutableList() ?: mutableListOf(stock.price)
        history.add(newPrice)
        if (history.size > 20) history.removeAt(0)
        historyMap[symbol] = history
        priceHistory.value = historyMap
    }

    private fun deductWallet(amount: Double): Boolean {
        if (totalWallet() < amount) return false
        var remaining = amount
        val timer = timerWalnuts.value
        val useTimer = min(timer, remaining)
        timerWalnuts.value = timer - useTimer
        remaining -= useTimer
        val sale = saleWalnuts.value
        val useSale = min(sale, remaining)
        saleWalnuts.value = sale - useSale
        remaining -= useSale
        val dividend = dividendWalnuts.value
        val useDividend = min(dividend, remaining)
        dividendWalnuts.value = dividend - useDividend
        return true
    }

    private fun ensureStocks() {
        if (stocks.value.isNotEmpty()) return
        stocks.value = listOf(
            MarketStock(USER_SYMBOL, "You", false, 25.0, TOTAL_SHARES, 0, 0.0, "Your discipline moves this stock"),
            MarketStock("\$MIRA", "Mira", true, 42.0, TOTAL_SHARES, 6, 0.0, "Very disciplined bot"),
            MarketStock("\$AYAN", "Ayan", true, 28.0, TOTAL_SHARES, 3, 0.0, "High risk bot trader"),
            MarketStock("\$NOOR", "Noor", true, 55.0, TOTAL_SHARES, 9, 0.0, "Elite focus bot"),
            MarketStock("\$ZAYD", "Zayd", true, 18.0, TOTAL_SHARES, 1, 0.0, "Chaotic but ambitious bot"),
            MarketStock("\$KAI", "Kai", true, 33.0, TOTAL_SHARES, 4, 0.0, "Balanced bot")
        ).associateBy { it.symbol }
        
        priceHistory.value = stocks.value.mapValues { (_, stock) ->
            listOf(stock.price, stock.price * 0.97, stock.price * 1.02, stock.price * 0.99, stock.price)
        }
    }

    private fun load() {
        if (!::prefs.isInitialized) return
        timerWalnuts.value = prefs.getFloat("timer_walnuts", 0f).toDouble()
        saleWalnuts.value = prefs.getFloat("sale_walnuts", 0f).toDouble()
        dividendWalnuts.value = prefs.getFloat("dividend_walnuts", 0f).toDouble()
        val raw = prefs.getStringSet("holdings", emptySet()) ?: emptySet()
        val map = mutableMapOf<String, MarketHolding>()
        raw.forEach { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                val symbol = parts[0]
                val shares = parts[1].toIntOrNull() ?: 0
                val avg = parts[2].toDoubleOrNull() ?: 0.0
                if (shares > 0) {
                    map[symbol] = MarketHolding(symbol, shares, avg)
                }
            }
        }
        holdings.value = map
        
        val historyRaw = prefs.getStringSet("price_history", emptySet()) ?: emptySet()
        val historyMap = mutableMapOf<String, List<Double>>()
        historyRaw.forEach { line ->
            val parts = line.split("|")
            if (parts.size == 2) {
                val symbol = parts[0]
                val prices = parts[1].split(",").mapNotNull { it.toDoubleOrNull() }
                if (prices.isNotEmpty()) historyMap[symbol] = prices
            }
        }
        if (historyMap.isNotEmpty()) priceHistory.value = historyMap
    }

    private fun saveWallet() {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putFloat("timer_walnuts", timerWalnuts.value.toFloat())
            .putFloat("sale_walnuts", saleWalnuts.value.toFloat())
            .putFloat("dividend_walnuts", dividendWalnuts.value.toFloat())
            .apply()
        saveHistory()
    }
    
    private fun saveHistory() {
        if (!::prefs.isInitialized) return
        val raw = priceHistory.value.map { (symbol, prices) ->
            "$symbol|${prices.joinToString(",")}"
        }.toSet()
        prefs.edit().putStringSet("price_history", raw).apply()
    }

    private fun saveHoldings() {
        if (!::prefs.isInitialized) return
        val raw = holdings.value.values.map { "${it.symbol}|${it.shares}|${it.avgPrice}" }.toSet()
        prefs.edit().putStringSet("holdings", raw).apply()
    }

    private fun addEvent(message: String, positive: Boolean) {
        events.value = (listOf(MarketEvent(message, positive, System.currentTimeMillis())) + events.value).take(30)
    }

    private fun Double.f1(): String = String.format(Locale.US, "%.1f", this)
}