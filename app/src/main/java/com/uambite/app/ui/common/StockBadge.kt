package com.uambite.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uambite.app.ui.theme.Amber100
import com.uambite.app.ui.theme.Amber800
import com.uambite.app.ui.theme.Green100
import com.uambite.app.ui.theme.Green800
import com.uambite.app.ui.theme.Red100
import com.uambite.app.ui.theme.Red800

enum class StockLevel {
    OK, LOW, OUT
}

fun resolveStockLevel(stock: Int): StockLevel = when {
    stock > 10 -> StockLevel.OK
    stock > 0 -> StockLevel.LOW
    else -> StockLevel.OUT
}

fun stockText(stock: Int): String = when {
    stock > 10 -> "Stock: $stock"
    stock > 0 -> "Quedan $stock"
    else -> "Agotado"
}

@Composable
fun StockBadge(
    stock: Int,
    modifier: Modifier = Modifier
) {
    val level = resolveStockLevel(stock)
    val containerColor = when (level) {
        StockLevel.OK -> Green100
        StockLevel.LOW -> Amber100
        StockLevel.OUT -> Red100
    }
    val contentColor = when (level) {
        StockLevel.OK -> Green800
        StockLevel.LOW -> Amber800
        StockLevel.OUT -> Red800
    }

    Text(
        text = stockText(stock),
        color = contentColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
