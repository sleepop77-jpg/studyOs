package com.example.studyos.ui.stock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Economy
import com.example.studyos.core.MarketEvent
import com.example.studyos.core.MarketStock
import com.example.studyos.core.StudyMarket
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.RedAura
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun StocksScreen(back: () -> Unit) {
    val stocks by StudyMarket.stocks.collectAsState()
    val holdings by StudyMarket.holdings.collectAsState()
    val events by StudyMarket.events.collectAsState()
    val timerWalnuts by StudyMarket.timerWalnuts.collectAsState()
    val saleWalnuts by StudyMarket.saleWalnuts.collectAsState()
    val dividendWalnuts by StudyMarket.dividendWalnuts.collectAsState()
    val priceHistory by StudyMarket.priceHistory.collectAsState()
    val fame by Economy.fame.collectAsState()
    val haptic = LocalHapticFeedback.current
    var selected by remember { mutableStateOf<String?>(null) }
    val totalWallet = timerWalnuts + saleWalnuts + dividendWalnuts
    val portfolio = holdings.values.sumOf { holding ->
        (stocks[holding.symbol]?.price ?: 0.0) * holding.shares
    }
    val bgBrush = homeBrush()

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        AnimatedBackground()
        RedAura()

        if (selected != null) {
            StockDetailScreen(symbol = selected!!, onBack = { selected = null })
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 4.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = back) {
                        Icon(SIcons.Back, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        "STUDY STOCK MARKET",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        WalletCard(
                            totalWallet = totalWallet,
                            dividendWalnuts = dividendWalnuts,
                            portfolio = portfolio,
                            fame = fame
                        )
                    }
                    item {
                        Text("STOCKS", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp)
                    }
                    items(items = stocks.values.toList().sortedByDescending { it.price }, key = { it.symbol }) { stock ->
                        StockRow(
                            stock = stock,
                            owned = holdings[stock.symbol]?.shares ?: 0,
                            wallet = totalWallet,
                            history = priceHistory[stock.symbol] ?: emptyList(),
                            onTrade = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            onSelect = { selected = stock.symbol }
                        )
                    }
                    item {
                        Text("MARKET EVENTS", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp)
                    }
                    items(events) { event -> EventRow(event) }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StockDetailScreen(symbol: String, onBack: () -> Unit) {
    val stocks by StudyMarket.stocks.collectAsState()
    val holdings by StudyMarket.holdings.collectAsState()
    val history by StudyMarket.priceHistory.collectAsState()
    val events by StudyMarket.events.collectAsState()
    val timerW by StudyMarket.timerWalnuts.collectAsState()
    val saleW by StudyMarket.saleWalnuts.collectAsState()
    val divW by StudyMarket.dividendWalnuts.collectAsState()
    val haptic = LocalHapticFeedback.current
    val wallet = timerW + saleW + divW
    val stock = stocks[symbol] ?: return
    val owned = holdings[symbol]?.shares ?: 0
    val prices = history[symbol] ?: emptyList()
    val high = prices.maxOrNull() ?: stock.price
    val low = prices.minOrNull() ?: stock.price
    val stockEvents = events.filter { it.message.contains(symbol) }.take(20)
    val changeColor = if (stock.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)

    Box(modifier = Modifier.fillMaxSize().background(homeBrush())) {
        AnimatedBackground()
        RedAura()
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(SIcons.Back, contentDescription = "Back", tint = Color.White) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text(if (stock.isBot) "${stock.owner} • Bot" else "${stock.owner} • You", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${stock.price.f1()} walnuts", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(
                        if (stock.changePercent >= 0) "+${stock.changePercent.f1()}%" else "${stock.changePercent.f1()}%",
                        color = changeColor, fontWeight = FontWeight.Black, fontSize = 12.sp
                    )
                }
            }

            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SESSION CHART", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    CandlestickChart(
                        prices = prices,
                        chunk = 4,
                        showVolume = true,
                        showGrid = true,
                        modifier = Modifier.fillMaxWidth().height(240.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("HIGH", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text(high.f1(), color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LOW", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text(low.f1(), color = Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("STREAK", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text("${stock.streak} green", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                    Text(stock.lastEvent, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            if (stock.symbol != StudyMarket.USER_SYMBOL) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TradeButton(text = "Buy 1", enabled = wallet >= stock.price, modifier = Modifier.weight(1f)) { StudyMarket.buy(stock.symbol, 1); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                    TradeButton(text = "Buy 10", enabled = wallet >= stock.price * 10, modifier = Modifier.weight(1f)) { StudyMarket.buy(stock.symbol, 10); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                    TradeButton(text = "Sell 1", enabled = owned >= 1, modifier = Modifier.weight(1f)) { StudyMarket.sell(stock.symbol, 1); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                    TradeButton(text = "Sell 10", enabled = owned >= 10, modifier = Modifier.weight(1f)) { StudyMarket.sell(stock.symbol, 10); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                }
                if (owned > 0) {
                    Text("Owned: $owned shares", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Text("Your personal stock. It rises when you study and crashes when you get busted.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }

            Text("HISTORY", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp)
            if (stockEvents.isEmpty()) {
                Text("No recorded events for this stock yet.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            stockEvents.forEach { event -> EventRow(event) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WalletCard(
    totalWallet: Double,
    dividendWalnuts: Double,
    portfolio: Double,
    fame: Int
) {
    val fameGain = (dividendWalnuts / 10.0).toInt()
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("GOLDEN WALNUTS", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Wallet", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                Text(totalWallet.f1(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Portfolio", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                Text(portfolio.f1(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dividend wallet", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                Text(dividendWalnuts.f1(), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fame", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                Text("$fame", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Button(
                onClick = { StudyMarket.convertDividendsToFame() },
                enabled = fameGain > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF4A2C2C)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(if (fameGain > 0) "Convert dividends to Fame (+$fameGain)" else "Need at least 10 dividend walnuts", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            Text("Only dividend walnuts can become Fame. Timer and sale walnuts are for investing.", color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun StockRow(
    stock: MarketStock,
    owned: Int,
    wallet: Double,
    history: List<Double> = emptyList(),
    onTrade: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    val changeColor = if (stock.changePercent >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
    val changeText = if (stock.changePercent >= 0) "+${stock.changePercent.f1()}%" else "${stock.changePercent.f1()}%"
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(if (stock.isBot) "${stock.owner} • Bot" else "${stock.owner} • You", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${stock.price.f1()} walnuts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(changeText, color = changeColor, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
            if (history.size >= 2) {
                CandlestickChart(prices = history, chunk = 2, modifier = Modifier.fillMaxWidth().height(30.dp))
            }
            Text(stock.lastEvent, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            if (owned > 0) {
                Text("Owned: $owned shares", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            if (stock.symbol == StudyMarket.USER_SYMBOL) {
                Text("Your personal stock. Tap to see the full chart.", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TradeButton(text = "Buy 1", enabled = wallet >= stock.price, modifier = Modifier.weight(1f)) { StudyMarket.buy(stock.symbol, 1); onTrade() }
                    TradeButton(text = "Buy 10", enabled = wallet >= stock.price * 10, modifier = Modifier.weight(1f)) { StudyMarket.buy(stock.symbol, 10); onTrade() }
                    TradeButton(text = "Sell 1", enabled = owned >= 1, modifier = Modifier.weight(1f)) { StudyMarket.sell(stock.symbol, 1); onTrade() }
                    TradeButton(text = "Sell 10", enabled = owned >= 10, modifier = Modifier.weight(1f)) { StudyMarket.sell(stock.symbol, 10); onTrade() }
                }
            }
        }
    }
}

@Composable
private fun CandlestickChart(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    chunk: Int = 4,
    showVolume: Boolean = false,
    showGrid: Boolean = false
) {
    if (prices.size < 2) return
    val candles = remember(prices, chunk) {
        prices.chunked(chunk).map { c ->
            floatArrayOf(c.first().toFloat(), c.max().toFloat(), c.min().toFloat(), c.last().toFloat())
        }
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val volumeH = if (showVolume) h * 0.16f else 0f
        val gap = if (showVolume) 6.dp.toPx() else 0f
        val chartH = h - volumeH - gap
        val allHigh = candles.maxOf { it[1] }
        val allLow = candles.minOf { it[2] }
        val range = (allHigh - allLow).coerceAtLeast(0.01f)

        if (showGrid) {
            for (g in 0 until 4) {
                val gy = chartH * (g + 0.5f) / 4f
                drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, gy), Offset(w, gy), 1.dp.toPx())
            }
        }

        fun y(v: Float) = chartH - ((v - allLow) / range) * (chartH * 0.9f) - chartH * 0.05f

        val n = candles.size
        val slot = w / n
        val bodyW = (slot * 0.55f).coerceAtLeast(1.5f.dp.toPx())

        candles.forEachIndexed { i, c ->
            val up = c[3] >= c[0]
            val color = if (up) Color(0xFF4CAF50) else Color(0xFFFF5252)
            val x = i * slot + slot / 2f
            drawLine(color, Offset(x, y(c[1])), Offset(x, y(c[2])), 1.2f.dp.toPx())
            val top = y(maxOf(c[0], c[3]))
            val bot = y(minOf(c[0], c[3]))
            drawRect(color, topLeft = Offset(x - bodyW / 2f, top), size = Size(bodyW, (bot - top).coerceAtLeast(1.5f.dp.toPx())))
            if (showVolume) {
                val vol = (c[1] - c[2]) / range
                val vh = vol * volumeH + 1.dp.toPx()
                drawRect(color.copy(alpha = 0.45f), topLeft = Offset(x - bodyW / 2f, h - vh), size = Size(bodyW, vh))
            }
        }

        val lastY = y(candles.last()[3])
        drawLine(
            Color(0xFFFFD700).copy(alpha = 0.6f),
            Offset(0f, lastY),
            Offset(w, lastY),
            1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
    }
}

@Composable
private fun TradeButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF4A2C2C)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(34.dp)
    ) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EventRow(event: MarketEvent) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (event.positive) Color(0xFF4CAF50) else Color(0xFFFF5252)))
            Text(event.message, color = Color.White, fontSize = 12.sp)
        }
    }
}

private fun Double.f1(): String = String.format(Locale.US, "%.1f", this)