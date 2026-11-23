package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = DeepObsidian,
  onPrimary = CrispWhite,
  primaryContainer = ElectricLime,
  onPrimaryContainer = ObsidianOnLime,
  secondary = EmeraldTrust,
  onSecondary = CrispWhite,
  secondaryContainer = EmeraldSoftTint,
  onSecondaryContainer = DeepObsidian,
  tertiary = AmberNotice,
  onTertiary = CrispWhite,
  tertiaryContainer = AmberSoftTint,
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
  secondary = EmeraldTrust,
  onSecondary = CrispWhite,
  secondaryContainer = NightElevated,
  onSecondaryContainer = SwatchMint,
  tertiary = AmberNotice,
  onTertiary = NightSurface,
  tertiaryContainer = NightElevated,
  onTertiaryContainer = SwatchSunshine,
  background = NightSurface,
  onBackground = NightTextPrimary,
  surface = NightCard,
  onSurface = NightTextPrimary,
  surfaceVariant = NightElevated,
  onSurfaceVariant = NightTextSecondary,
  outline = NightBorder,
  error = CrimsonAlert,
  onError = CrispWhite,
  errorContainer = CrimsonSoftTint,
  onErrorContainer = CrimsonAlert,
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
