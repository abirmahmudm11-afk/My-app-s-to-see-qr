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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalReducedMotion = staticCompositionLocalOf { false }
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

@Immutable
data class Scan2MoneyExtendedColors(
  val isDark: Boolean,
  val heroBrush: Brush,
  val heroBorderBrush: Brush,
  val heroPrimaryText: Color,
  val heroSecondaryText: Color,
  val heroBadgeBg: Color,
  val heroBadgeText: Color,
  val heroChipBg: Color,
  val heroChipBorder: Color,
  val heroCtaBg: Color,
  val heroCtaFg: Color,
  val heroSecondaryBtnBg: Color,
  val heroSecondaryBtnBorder: Color,
  val heroSecondaryBtnFg: Color,
  val heroPositiveAccent: Color,
  val heroWarningAccent: Color,
  val heroProgressTrack: Color,
  val heroRadialTint: Color,
  val bentoDiscoverAccent: Color,
  val bentoEarningsAccent: Color,
  val bentoWalletAccent: Color,
  val bentoHistoryAccent: Color,
  val cardBorder: Color,
  val dividerSubtle: Color,
  val heroImageOverlayBottom: Color,
)

private val LightExtendedColors = Scan2MoneyExtendedColors(
  isDark = false,
  heroBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFFFFFFFF),
      Color(0xFFFAFDFB),
      Color(0xFFF4FAF7),
    ),
  ),
  heroBorderBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFF0B7B54).copy(alpha = 0.22f),
      Color(0xFFD9E3DE),
    ),
  ),
  heroPrimaryText = Color(0xFF0F1915),
  heroSecondaryText = Color(0xFF51615B),
  heroBadgeBg = Color(0xFFE6F6EE),
  heroBadgeText = Color(0xFF065A3D),
  heroChipBg = Color(0xFFF3F7F5),
  heroChipBorder = Color(0xFFDCE6E0),
  heroCtaBg = Color(0xFF0B7B54),
  heroCtaFg = Color(0xFFFFFFFF),
  heroSecondaryBtnBg = Color(0xFFFFFFFF),
  heroSecondaryBtnBorder = Color(0xFFD0DBD5),
  heroSecondaryBtnFg = Color(0xFF101815),
  heroPositiveAccent = Color(0xFF0B7B54),
  heroWarningAccent = Color(0xFFB45309),
  heroProgressTrack = Color(0xFFE3ECE7),
  heroRadialTint = Color(0xFF0B7B54).copy(alpha = 0.04f),
  bentoDiscoverAccent = Color(0xFF0B7B54),
  bentoEarningsAccent = Color(0xFF0F766E),
  bentoWalletAccent = Color(0xFF2563EB),
  bentoHistoryAccent = Color(0xFFB45309),
  cardBorder = HairlineBorder,
  dividerSubtle = HairlineBorder.copy(alpha = 0.7f),
  heroImageOverlayBottom = Color(0xFF0F1915).copy(alpha = 0.14f),
)

private val DarkExtendedColors = Scan2MoneyExtendedColors(
  isDark = true,
  heroBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFF17231F),
      Color(0xFF131917),
      Color(0xFF101513),
    ),
  ),
  heroBorderBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFF2CD492).copy(alpha = 0.42f),
      Color(0xFF2A3330),
    ),
  ),
  heroPrimaryText = Color(0xFFF2F6F4),
  heroSecondaryText = Color(0xFF94A19B),
  heroBadgeBg = Color(0xFF2CD492).copy(alpha = 0.16f),
  heroBadgeText = Color(0xFF4BE0A5),
  heroChipBg = Color(0xFFFFFFFF).copy(alpha = 0.06f),
  heroChipBorder = Color(0xFFFFFFFF).copy(alpha = 0.11f),
  heroCtaBg = Color(0xFF2CD492),
  heroCtaFg = Color(0xFF042116),
  heroSecondaryBtnBg = Color(0xFFFFFFFF).copy(alpha = 0.06f),
  heroSecondaryBtnBorder = Color(0xFFFFFFFF).copy(alpha = 0.18f),
  heroSecondaryBtnFg = Color(0xFFF1F5F3),
  heroPositiveAccent = Color(0xFF38D9A9),
  heroWarningAccent = Color(0xFFF5B742),
  heroProgressTrack = Color(0xFF25302C),
  heroRadialTint = Color(0xFF2CD492).copy(alpha = 0.14f),
  bentoDiscoverAccent = Color(0xFF2CD492),
  bentoEarningsAccent = Color(0xFF38D9A9),
  bentoWalletAccent = Color(0xFF60A5FA),
  bentoHistoryAccent = Color(0xFFF5B742),
  cardBorder = CarbonHairline,
  dividerSubtle = CarbonHairline.copy(alpha = 0.7f),
  heroImageOverlayBottom = Color(0xFF0B0E0D).copy(alpha = 0.58f),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

private val LightColorScheme = lightColorScheme(
  primary = SignalIndigo,
  onPrimary = Color.White,
  primaryContainer = SignalIndigoSoft,
  onPrimaryContainer = Color(0xFF063D29),
  secondary = LedgerJade,
  onSecondary = Color.White,
  secondaryContainer = LedgerJadeSoft,
  onSecondaryContainer = Color(0xFF083935),
  tertiary = PrecisionAmber,
  onTertiary = Color.White,
  tertiaryContainer = PrecisionAmberSoft,
  onTertiaryContainer = Color(0xFF451A03),
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
  onErrorContainer = Color(0xFF7F1D1D),
)

private val DarkColorScheme = darkColorScheme(
  primary = SignalIndigoDark,
  onPrimary = MintOnDarkText,
  primaryContainer = SignalIndigoDarkContainer,
  onPrimaryContainer = Color(0xFF86F0C5),
  secondary = LedgerJadeDark,
  onSecondary = MintOnDarkText,
  secondaryContainer = LedgerJadeDarkContainer,
  onSecondaryContainer = Color(0xFF99F6E4),
  tertiary = PrecisionAmberDark,
  onTertiary = ReticleAmberText,
  tertiaryContainer = PrecisionAmberDarkContainer,
  onTertiaryContainer = Color(0xFFFDE68A),
  background = CarbonCanvas,
  onBackground = CarbonInkPrimary,
  surface = CarbonSurface,
  onSurface = CarbonInkPrimary,
  surfaceVariant = CarbonElevated,
  onSurfaceVariant = CarbonInkSecondary,
  outline = CarbonHairline,
  error = LedgerRedDark,
  onError = Color(0xFF2D0A0A),
  errorContainer = LedgerRedDarkContainer,
  onErrorContainer = Color(0xFFFECACA),
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
  darkTheme: Boolean = false,
  reducedMotion: Boolean = false,
  content: @Composable () -> Unit,
) {
  val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
  CompositionLocalProvider(
    LocalReducedMotion provides reducedMotion,
    LocalExtendedColors provides extendedColors,
  ) {
    MaterialTheme(
      colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
      typography = Scan2MoneyTypography,
      shapes = Scan2MoneyShapes,
      content = content,
    )
  }
}
