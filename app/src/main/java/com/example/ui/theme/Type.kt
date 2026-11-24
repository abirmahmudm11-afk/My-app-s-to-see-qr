package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Non-reflex domain typography: Archivo (display geometry) + Public Sans (legible UI copy)
// JetBrains Mono is reserved strictly for DataMonoStyle (barcodes, currency figures, timestamps).
val ArchivoFamily = FontFamily(
  Font(R.font.archivo, FontWeight.Medium),
  Font(R.font.archivo, FontWeight.SemiBold),
  Font(R.font.archivo, FontWeight.Bold),
)

val PublicSansFamily = FontFamily(
  Font(R.font.public_sans, FontWeight.Normal),
  Font(R.font.public_sans, FontWeight.Medium),
  Font(R.font.public_sans, FontWeight.SemiBold),
  Font(R.font.public_sans, FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.Medium),
  Font(R.font.jetbrains_mono, FontWeight.Bold),
)

// Tabular monospace style strictly for GTIN barcodes, prices, point balances, and hashes
val TabularDataStyle = TextStyle(
  fontFamily = JetBrainsMonoFamily,
  fontWeight = FontWeight.Medium,
  fontSize = 13.sp,
  lineHeight = 18.sp,
  fontFeatureSettings = "tnum",
)

val TabularFigureLargeStyle = TextStyle(
  fontFamily = ArchivoFamily,
  fontWeight = FontWeight.Bold,
  fontSize = 36.sp,
  lineHeight = 40.sp,
  letterSpacing = (-0.6).sp,
  fontFeatureSettings = "tnum",
)

val Scan2MoneyTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.8).sp,
    fontFeatureSettings = "tnum",
  ),
  displayMedium = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.5).sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.3).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.2).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = PublicSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.2.sp,
  ),
)
