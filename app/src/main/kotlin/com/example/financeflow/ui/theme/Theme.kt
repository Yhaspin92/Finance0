package com.example.financeflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FintechColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = FintechBackground,
    primaryContainer = NeonEmeraldGlow,
    onPrimaryContainer = NeonEmerald,
    secondary = NeonViolet,
    onSecondary = TextPrimary,
    secondaryContainer = NeonVioletGlow,
    onSecondaryContainer = NeonViolet,
    tertiary = NeonCyan,
    onTertiary = FintechBackground,
    background = FintechBackground,
    onBackground = TextPrimary,
    surface = FintechSurface,
    onSurface = TextPrimary,
    surfaceVariant = FintechCard,
    onSurfaceVariant = TextSecondary,
    outline = FintechCardBorder,
    error = FintechExpense,
    onError = TextPrimary
)

@Composable
fun FinanceFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FintechColorScheme,
        typography = Typography,
        content = content
    )
}
