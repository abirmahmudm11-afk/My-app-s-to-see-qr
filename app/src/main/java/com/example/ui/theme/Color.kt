package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// SCAN2MONEY Dual Fintech Palette
// Light Mode: Crisp Pure White (#FFFFFF) + Elevated Cards + Forest Emerald (#0B7B54)
// Dark Mode: Refined Obsidian Charcoal (#0B0E0D) + Neutral Slate-Charcoal + Mint Emerald (#2CD492)
// ============================================================================

// Light Mode Surface & Ink Hierarchy
val LedgerCanvas = Color(0xFFFFFFFF)
val PaperSheet = Color(0xFFFFFFFF)
val RecessedTrack = Color(0xFFF3F6F4)
val HairlineBorder = Color(0xFFE1E6E3)
val InkPrimary = Color(0xFF101815)
val InkSecondary = Color(0xFF55625D)

// Dark Mode Obsidian Charcoal Surface & Ink Hierarchy
val CarbonCanvas = Color(0xFF0B0E0D)
val CarbonSurface = Color(0xFF131816)
val CarbonElevated = Color(0xFF1B221F)
val CarbonHairline = Color(0xFF2A3330)
val CarbonInkPrimary = Color(0xFFF1F5F3)
val CarbonInkSecondary = Color(0xFF94A19B)

// Primary Brand Hue — Forest Emerald (Light) & Balanced Mint Emerald (Dark)
val SignalIndigo = Color(0xFF0B7B54)
val SignalIndigoSoft = Color(0xFFE0F4EC)
val SignalIndigoDark = Color(0xFF2CD492)
val SignalIndigoDarkContainer = Color(0xFF123226)

// Secondary Accent — Deep Ocean Teal (Light) & Crisp Seafoam Teal (Dark)
val LedgerJade = Color(0xFF0F766E)
val LedgerJadeSoft = Color(0xFFDDF5F2)
val LedgerJadeDark = Color(0xFF38D9A9)
val LedgerJadeDarkContainer = Color(0xFF122E2A)
val CobaltAccent = Color(0xFF2563EB)
val CobaltAccentDark = Color(0xFF60A5FA)

// Tertiary & Status Hues — Warm Bronze/Amber & Coral Crimson
val PrecisionAmber = Color(0xFFB45309)
val PrecisionAmberSoft = Color(0xFFFEF3C7)
val PrecisionAmberDark = Color(0xFFF5B742)
val PrecisionAmberDarkContainer = Color(0xFF31230A)

val LedgerRed = Color(0xFFDC2626)
val LedgerRedSoft = Color(0xFFFEE2E2)
val LedgerRedDark = Color(0xFFF87171)
val LedgerRedDarkContainer = Color(0xFF3B1214)

// Optical Camera Viewfinder Hardware Tokens (Always Dark Surround for Reticle Contrast)
val ViewfinderVoid = Color(0xFF0B0E0D)
val ViewfinderSurface = Color(0xFF151C1A)
val ViewfinderMuted = Color(0xFF9AA8A2)
val ReticleAmber = Color(0xFFF5B742)
val ReticleAmberText = Color(0xFF1A1102)
val MintOnDarkText = Color(0xFF042116)

// Legacy/Shared Gradient Aliases (Prefer LocalExtendedColors in theme-aware screens)
val MintGlowBorderBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFF2CD492).copy(alpha = 0.42f),
    Color(0xFF2A3330),
  ),
)
