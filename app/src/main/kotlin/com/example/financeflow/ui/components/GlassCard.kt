package com.example.financeflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.financeflow.ui.theme.FintechCard
import com.example.financeflow.ui.theme.FintechCardBorder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    private val mxLocale = Locale.forLanguageTag("es-MX")
    private val currencyFormat = NumberFormat.getCurrencyInstance(mxLocale)
    private val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", mxLocale)
    private val shortDateFormat = SimpleDateFormat("dd MMM", mxLocale)

    fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 20.dp,
    borderColor: Color = FintechCardBorder,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = FintechCard,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(shapeRadius)
    val backgroundBrush = if (accentGlow != null) {
        Brush.verticalGradient(
            colors = listOf(
                accentGlow.copy(alpha = 0.15f),
                backgroundColor
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.95f),
                backgroundColor.copy(alpha = 0.85f)
            )
        )
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .clip(shape)
                .border(BorderStroke(borderWidth, borderColor), shape),
            shape = shape,
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .background(backgroundBrush)
                    .padding(16.dp),
                content = content
            )
        }
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .border(BorderStroke(borderWidth, borderColor), shape)
                .background(backgroundBrush)
                .padding(16.dp)
        ) {
            Column(content = content)
        }
    }
}
