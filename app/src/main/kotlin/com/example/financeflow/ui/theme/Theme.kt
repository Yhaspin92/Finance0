package com.example.financeflow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val subtitle: String, val primaryPreview: Color, val secondaryPreview: Color) {
    EMERALD_VIOLET(
        title = "Esmeralda & Violeta",
        subtitle = "Verde neón con morado fintech y carbón profundo",
        primaryPreview = Color(0xFF00E676),
        secondaryPreview = Color(0xFF7C4DFF)
    ),
    ORANGE_BLUE(
        title = "Naranja & Azul",
        subtitle = "Naranja eléctrico con azul cobalto espacial",
        primaryPreview = Color(0xFFFF6D00),
        secondaryPreview = Color(0xFF2979FF)
    ),
    RED_GRAY(
        title = "Rojo & Gris",
        subtitle = "Rojo carmesí con gris titanio metálico",
        primaryPreview = Color(0xFFFF1744),
        secondaryPreview = Color(0xFF90A4AE)
    )
}

fun getFintechColorScheme(mode: AppThemeMode): ColorScheme {
    return when (mode) {
        AppThemeMode.EMERALD_VIOLET -> {
            FintechBackground = EmeraldBackground
            FintechSurface = EmeraldSurface
            FintechCard = EmeraldCard
            FintechCardBorder = EmeraldCardBorder
            NeonEmerald = EmeraldPrimary
            NeonEmeraldGlow = EmeraldPrimaryGlow
            NeonViolet = EmeraldSecondary
            NeonVioletGlow = EmeraldSecondaryGlow
            NeonCyan = EmeraldAccent

            darkColorScheme(
                primary = EmeraldPrimary,
                onPrimary = EmeraldBackground,
                primaryContainer = EmeraldPrimaryGlow,
                onPrimaryContainer = EmeraldPrimary,
                secondary = EmeraldSecondary,
                onSecondary = TextPrimary,
                secondaryContainer = EmeraldSecondaryGlow,
                onSecondaryContainer = EmeraldSecondary,
                tertiary = EmeraldAccent,
                onTertiary = EmeraldBackground,
                background = EmeraldBackground,
                onBackground = TextPrimary,
                surface = EmeraldSurface,
                onSurface = TextPrimary,
                surfaceVariant = EmeraldCard,
                onSurfaceVariant = TextSecondary,
                outline = EmeraldCardBorder,
                error = FintechExpense,
                onError = TextPrimary
            )
        }

        AppThemeMode.ORANGE_BLUE -> {
            FintechBackground = OrangeBackground
            FintechSurface = OrangeSurface
            FintechCard = OrangeCard
            FintechCardBorder = OrangeCardBorder
            NeonEmerald = OrangePrimary
            NeonEmeraldGlow = OrangePrimaryGlow
            NeonViolet = OrangeSecondary
            NeonVioletGlow = OrangeSecondaryGlow
            NeonCyan = OrangeAccent

            darkColorScheme(
                primary = OrangePrimary,
                onPrimary = OrangeBackground,
                primaryContainer = OrangePrimaryGlow,
                onPrimaryContainer = OrangePrimary,
                secondary = OrangeSecondary,
                onSecondary = TextPrimary,
                secondaryContainer = OrangeSecondaryGlow,
                onSecondaryContainer = OrangeSecondary,
                tertiary = OrangeAccent,
                onTertiary = OrangeBackground,
                background = OrangeBackground,
                onBackground = TextPrimary,
                surface = OrangeSurface,
                onSurface = TextPrimary,
                surfaceVariant = OrangeCard,
                onSurfaceVariant = TextSecondary,
                outline = OrangeCardBorder,
                error = FintechExpense,
                onError = TextPrimary
            )
        }

        AppThemeMode.RED_GRAY -> {
            FintechBackground = RedBackground
            FintechSurface = RedSurface
            FintechCard = RedCard
            FintechCardBorder = RedCardBorder
            NeonEmerald = RedPrimary
            NeonEmeraldGlow = RedPrimaryGlow
            NeonViolet = RedSecondary
            NeonVioletGlow = RedSecondaryGlow
            NeonCyan = RedAccent

            darkColorScheme(
                primary = RedPrimary,
                onPrimary = RedBackground,
                primaryContainer = RedPrimaryGlow,
                onPrimaryContainer = RedPrimary,
                secondary = RedSecondary,
                onSecondary = TextPrimary,
                secondaryContainer = RedSecondaryGlow,
                onSecondaryContainer = RedSecondary,
                tertiary = RedAccent,
                onTertiary = RedBackground,
                background = RedBackground,
                onBackground = TextPrimary,
                surface = RedSurface,
                onSurface = TextPrimary,
                surfaceVariant = RedCard,
                onSurfaceVariant = TextSecondary,
                outline = RedCardBorder,
                error = FintechExpense,
                onError = TextPrimary
            )
        }
    }
}

@Composable
fun FinanceFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.EMERALD_VIOLET,
    content: @Composable () -> Unit
) {
    val colorScheme = getFintechColorScheme(themeMode)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
