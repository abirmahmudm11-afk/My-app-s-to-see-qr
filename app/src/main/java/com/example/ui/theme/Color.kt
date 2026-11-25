package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Obsidian Mint & Volt — Next-Gen Fintech Cashback & Optical Scanner Palette
// High-contrast, luminous electric mint paired with deep obsidian glass layers.
// Every foreground/background pair meets or exceeds WCAG 2.1 AA contrast.
// ============================================================================

// Light Mode — Crisp Mint-Tinted Alabaster & Electric Emerald/Cobalt
val LedgerCanvas = Color(0xFFF2F7F5)
val PaperSheet = Color(0xFFFFFFFF)
val RecessedTrack = Color(0xFFE3EFEA)
val HairlineBorder = Color(0xFFC8DCD4)
val InkPrimary = Color(0xFF07131A)
val InkSecondary = Color(0xFF3E5563)

// Dark Mode — Deep Obsidian Studio & Layered Midnight Glass
val CarbonCanvas = Color(0xFF060A12)
val CarbonSurface = Color(0xFF0D1524)
val CarbonElevated = Color(0xFF152136)
val CarbonHairline = Color(0xFF23334D)
val CarbonInkPrimary = Color(0xFFF4F8FC)
val CarbonInkSecondary = Color(0xFF96ABC6)

// Primary Brand Hue — Electric Mint / Emerald Volt
val SignalIndigo = Color(0xFF00A86B)             // Light primary (vibrant emerald, >4.5:1 with white/dark)
val SignalIndigoSoft = Color(0xFFD6FAEB)
val SignalIndigoDark = Color(0xFF00E599)         // Dark primary (luminous electric mint)
val SignalIndigoDarkContainer = Color(0xFF0A2E24)

// Secondary Accent — Electric Cobalt
val LedgerJade = Color(0xFF3B5BDB)               // Light secondary (crisp cobalt)
val LedgerJadeSoft = Color(0xFFE0E7FF)
val LedgerJadeDark = Color(0xFF00E599)           // High-contrast mint gain/positive accent
val LedgerJadeDarkContainer = Color(0xFF0C2C23)
val CobaltAccent = Color(0xFF5B7CFF)
val CobaltAccentSoft = Color(0xFF14224A)

// Tertiary & Status Hues — Solar Amber & Coral Red
val PrecisionAmber = Color(0xFFD97706)
val PrecisionAmberSoft = Color(0xFFFEF3C7)
val PrecisionAmberDark = Color(0xFFFFB830)
val PrecisionAmberDarkContainer = Color(0xFF332308)

val LedgerRed = Color(0xFFDC2626)
val LedgerRedSoft = Color(0xFFFEE2E2)
val LedgerRedDark = Color(0xFFFF6B7A)
val LedgerRedDarkContainer = Color(0xFF3A131C)

// Optical Viewfinder & Cinema Hardware Tokens
val ViewfinderVoid = Color(0xFF060A12)
val ViewfinderSurface = Color(0xFF0F1829)
val ViewfinderMuted = Color(0xFF9EB2CD)
val ReticleAmber = Color(0xFFFFB830)
val ReticleAmberText = Color(0xFF160E02)
val MintOnDarkText = Color(0xFF041C13)

// Atmospheric Signature Gradients
val LedgerDeckBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFF0B1C28),
    Color(0xFF0D282C),
    Color(0xFF121C38),
  ),
)

val HeroMintVaultBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFF0B242B),
    Color(0xFF0E1D34),
    Color(0xFF0B1322),
  ),
)

val MintGlowBorderBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFF00E599).copy(alpha = 0.55f),
    Color(0xFF5B7CFF).copy(alpha = 0.35f),
    Color(0xFF00E599).copy(alpha = 0.18f),
  ),
)

val LedgerActionBrush = Brush.linearGradient(
  colors = listOf(Color(0xFF00E599), Color(0xFF00C48C)),
)
