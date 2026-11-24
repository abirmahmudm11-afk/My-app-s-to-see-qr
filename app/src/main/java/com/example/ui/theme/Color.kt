package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light Mode — "Vibrant Mint-Slate Canvas, Rich Teal-Emerald & Electric Citrus"
val MintAlabaster = Color(0xFFEEF5F2)
val CrispWhite = Color(0xFFFFFFFF)
val SoftStone = Color(0xFFE1ECE7)
val DeepObsidian = Color(0xFF0B1F1B)
val ObsidianOnLime = Color(0xFF0A1D19)
val SlateCharcoal = Color(0xFF28413B)
val MutedSage = Color(0xFF567069)
val HairlineMist = Color(0xFFC8DCD3)

// Elevated Dark Header / Hero Obsidian-Emerald Surfaces
val ObsidianElevated = Color(0xFF13332C)
val ObsidianMutedText = Color(0xFFA7C7BE)
val ObsidianDivider = Color(0xFF245247)
val DeepForestTop = Color(0xFF062C25)
val DeepTealMid = Color(0xFF0C4D41)
val DeepIndigoTeal = Color(0xFF10374F)

// Brand Electric Lime & Trust Emerald Semantic Accents
val ElectricLime = Color(0xFFCFF93E)
val EmeraldTrust = Color(0xFF059669)
val EmeraldSoftTint = Color(0xFFCCFBF1)

// Expanded Multi-Hue Vivid Palette (High-Contrast Fintech & Smart Shopping)
val TealVivid = Color(0xFF0D9488)
val TealSoftTint = Color(0xFFCFFAFE)

val AmberNotice = Color(0xFFD97706)
val AmberGold = Color(0xFFF59E0B)
val AmberSoftTint = Color(0xFFFEF0C7)

val TangerineAccent = Color(0xFFEA580C)
val TangerineSoftTint = Color(0xFFFFE5D0)

val CrimsonAlert = Color(0xFFDC2626)
val CrimsonSoftTint = Color(0xFFFFE0E2)

val CoralAccent = Color(0xFFE11D48)
val CoralSoftTint = Color(0xFFFFDFE6)

val CobaltInfo = Color(0xFF2563EB)
val CobaltSoftTint = Color(0xFFD7E8FF)

val VioletAccent = Color(0xFF7C3AED)
val VioletSoftTint = Color(0xFFEADFFE)

val CyanInfo = Color(0xFF0284C7)
val CyanSoftTint = Color(0xFFD6F0FF)

// Vibrant Saturated Bento Swatches
val SwatchSunshine = Color(0xFFFDE69B)
val SwatchMint = Color(0xFFAEF3D8)
val SwatchSky = Color(0xFFC5E1FF)
val SwatchOrchid = Color(0xFFE1CEFF)
val SwatchPeach = Color(0xFFFFD3C4)

// Dark Mode — "Obsidian Night"
val NightSurface = Color(0xFF07110F)
val NightCard = Color(0xFF101E1B)
val NightElevated = Color(0xFF172D28)
val NightBorder = Color(0xFF26473E)
val NightTextPrimary = Color(0xFFF0FAF6)
val NightTextSecondary = Color(0xFF9EC0B6)

// Onboarding Sky Gradients (Warm Coral-Amber Dawn -> Crisp Azure-Mint Daylight -> Deep Emerald Night)
val DawnSkyTop = Color(0xFFFFDFB8)
val DawnSkyBottom = Color(0xFFFFF5E8)
val DaylightSkyTop = Color(0xFFBEE6FF)
val DaylightSkyBottom = Color(0xFFEEF9F5)
val NightSkyTop = Color(0xFF071C19)
val NightSkyBottom = Color(0xFF11352F)

// Reusable Brand Atmospheric Gradient Brushes
val HeroObsidianEmeraldBrush = Brush.linearGradient(
  colors = listOf(DeepForestTop, DeepTealMid, DeepIndigoTeal),
)

val HeaderEmeraldCobaltBrush = Brush.linearGradient(
  colors = listOf(Color(0xFF064E3B), Color(0xFF0F766E), Color(0xFF1D4ED8)),
)

val HeaderTealVioletBrush = Brush.linearGradient(
  colors = listOf(Color(0xFF0F766E), Color(0xFF2563EB), Color(0xFF6D28D9)),
)

val HeaderWarmRewardsBrush = Brush.linearGradient(
  colors = listOf(Color(0xFF064E3B), Color(0xFF0D9488), Color(0xFF0369A1)),
)
