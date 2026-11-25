package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Expressive geometric pairing: Space Grotesk (display/headlines) + Plus Jakarta Sans (clean UI body)
// JetBrains Mono is reserved strictly for TabularDataStyle (barcodes, prices, hashes, timestamps).
val SpaceGroteskFamily = FontFamily(
  Font(R.font.space_grotesk, FontWeight.Medium),
  Font(R.font.space_grotesk, FontWeight.SemiBold),
  Font(R.font.space_grotesk, FontWeight.Bold),
)

val PlusJakartaSansFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
  Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.Medium),
  Font(R.font.jetbrains_mono, FontWeight.Bold),
)

val TabularDataStyle = TextStyle(
  fontFamily = JetBrainsMonoFamily,
  fontWeight = FontWeight.Medium,
  fontSize = 13.sp,
  lineHeight = 18.sp,
  fontFeatureSettings = "tnum",
)

val TabularFigureLargeStyle = TextStyle(
  fontFamily = SpaceGroteskFamily,
  fontWeight = FontWeight.Bold,
  fontSize = 40.sp,
  lineHeight = 44.sp,
  letterSpacing = (-1.0).sp,
  fontFeatureSettings = "tnum",
)

val Scan2MoneyTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.9).sp,
    fontFeatureSettings = "tnum",
  ),
  displayMedium = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.6).sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.4).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.25).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.25.sp,
  ),
)
