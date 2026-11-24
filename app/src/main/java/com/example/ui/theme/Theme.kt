package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = EmeraldTrust,
  onPrimary = CrispWhite,
  primaryContainer = EmeraldSoftTint,
  onPrimaryContainer = DeepObsidian,
  secondary = DeepObsidian,
  onSecondary = ElectricLime,
  secondaryContainer = SwatchSky,
  onSecondaryContainer = DeepObsidian,
  tertiary = CobaltInfo,
  onTertiary = CrispWhite,
  tertiaryContainer = CobaltSoftTint,
  onTertiaryContainer = DeepObsidian,
  background = MintAlabaster,
  onBackground = DeepObsidian,
  surface = CrispWhite,
  onSurface = DeepObsidian,
  surfaceVariant = SoftStone,
  onSurfaceVariant = SlateCharcoal,
  outline = HairlineMist,
  error = CrimsonAlert,
  onError = CrispWhite,
  errorContainer = CrimsonSoftTint,
  onErrorContainer = CrimsonAlert,
)

private val DarkColorScheme = darkColorScheme(
  primary = ElectricLime,
  onPrimary = ObsidianOnLime,
  primaryContainer = NightElevated,
  onPrimaryContainer = ElectricLime,
  secondary = TealVivid,
  onSecondary = CrispWhite,
  secondaryContainer = Color(0xFF153447),
  onSecondaryContainer = Color(0xFFBFE3FF),
  tertiary = Color(0xFF60A5FA),
  onTertiary = DeepObsidian,
  tertiaryContainer = Color(0xFF1A2947),
  onTertiaryContainer = NightTextPrimary,
  background = NightSurface,
  onBackground = NightTextPrimary,
  surface = NightCard,
  onSurface = NightTextPrimary,
  surfaceVariant = NightElevated,
  onSurfaceVariant = NightTextSecondary,
  outline = NightBorder,
  error = Color(0xFFF87171),
  onError = DeepObsidian,
  errorContainer = Color(0xFF45121B),
  onErrorContainer = Color(0xFFFFB4BE),
)

val Scan2MoneyShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun Scan2MoneyTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography = Scan2MoneyTypography,
    shapes = Scan2MoneyShapes,
    content = content,
  )
}
