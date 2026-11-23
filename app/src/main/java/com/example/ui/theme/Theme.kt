package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Mint100,
    onPrimaryContainer = Emerald900,
    secondary = Charcoal800,
    onSecondary = Color.White,
    secondaryContainer = WarmSurfaceVariant,
    onSecondaryContainer = Charcoal900,
    tertiary = Emerald800,
    onTertiary = Color.White,
    tertiaryContainer = Mint50,
    onTertiaryContainer = Emerald900,
    background = WarmWhite,
    onBackground = Charcoal900,
    surface = WarmSurface,
    onSurface = Charcoal900,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = Slate500,
    outline = OutlineLight,
    outlineVariant = Color(0xFFE7EAE8),
    error = RedAlert,
    onError = Color.White,
    errorContainer = RedContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Emerald900,
    primaryContainer = Emerald800,
    onPrimaryContainer = Mint100,
    secondary = Emerald300,
    onSecondary = Emerald900,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextOnDark,
    tertiary = Mint200,
    onTertiary = Emerald900,
    tertiaryContainer = Color(0xFF0A3A2B),
    onTertiaryContainer = Mint100,
    background = DarkBackground,
    onBackground = TextOnDark,
    surface = DarkSurface,
    onSurface = TextOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMutedOnDark,
    outline = DarkOutline,
    outlineVariant = Color(0xFF232E2A),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = RedContainer
)

val Scan2MoneyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun Scan2MoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Scan2MoneyTypography,
        shapes = Scan2MoneyShapes,
        content = content
    )
}
