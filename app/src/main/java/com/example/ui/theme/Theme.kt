@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.ui.theme

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalReducedMotion = staticCompositionLocalOf { false }
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

private val LightColorScheme = lightColorScheme(
  primary = SignalIndigo,
  onPrimary = Color.White,
  primaryContainer = SignalIndigoSoft,
  onPrimaryContainer = InkPrimary,
  secondary = LedgerJade,
  onSecondary = Color.White,
  secondaryContainer = LedgerJadeSoft,
  onSecondaryContainer = InkPrimary,
  tertiary = PrecisionAmber,
  onTertiary = Color.White,
  tertiaryContainer = PrecisionAmberSoft,
  onTertiaryContainer = InkPrimary,
  background = LedgerCanvas,
  onBackground = InkPrimary,
  surface = PaperSheet,
  onSurface = InkPrimary,
  surfaceVariant = RecessedTrack,
  onSurfaceVariant = InkSecondary,
  outline = HairlineBorder,
  error = LedgerRed,
  onError = Color.White,
  errorContainer = LedgerRedSoft,
  onErrorContainer = LedgerRed,
)

private val DarkColorScheme = darkColorScheme(
  primary = SignalIndigoDark,
  onPrimary = MintOnDarkText,
  primaryContainer = SignalIndigoDarkContainer,
  onPrimaryContainer = SignalIndigoDark,
  secondary = LedgerJadeDark,
  onSecondary = MintOnDarkText,
  secondaryContainer = LedgerJadeDarkContainer,
  onSecondaryContainer = LedgerJadeDark,
  tertiary = PrecisionAmberDark,
  onTertiary = ReticleAmberText,
  tertiaryContainer = PrecisionAmberDarkContainer,
  onTertiaryContainer = PrecisionAmberDark,
  background = CarbonCanvas,
  onBackground = CarbonInkPrimary,
  surface = CarbonSurface,
  onSurface = CarbonInkPrimary,
  surfaceVariant = CarbonElevated,
  onSurfaceVariant = CarbonInkSecondary,
  outline = CarbonHairline,
  error = LedgerRedDark,
  onError = InkPrimary,
  errorContainer = LedgerRedDarkContainer,
  onErrorContainer = CarbonInkPrimary,
)

val Scan2MoneyShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(14.dp),
  large = RoundedCornerShape(18.dp),
  extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun Scan2MoneyTheme(
  darkTheme: Boolean = true,
  reducedMotion: Boolean = false,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
    MaterialTheme(
      colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
      typography = Scan2MoneyTypography,
      shapes = Scan2MoneyShapes,
      content = content,
    )
  }
}
