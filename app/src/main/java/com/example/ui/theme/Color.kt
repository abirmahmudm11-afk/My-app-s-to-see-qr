package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Optical Ivory & Carbon Ledger — Precision Retail Price Instrument Palette
// Grounded in physical price labels, optical scanner viewfinders & tabular ledgers.
// Every foreground/background pair meets or exceeds WCAG 2.1 AA (>= 4.5:1 body, >= 3:1 large).
// ============================================================================

// Light Mode Architectural Neutrals (Cool slate-paper, zero warm cream)
val LedgerCanvas = Color(0xFFF4F6F9)
val PaperSheet = Color(0xFFFFFFFF)
val RecessedTrack = Color(0xFFE7ECF2)
val HairlineBorder = Color(0xFFD5DCE6)
val InkPrimary = Color(0xFF0E1726)       // ~17.5:1 on PaperSheet
val InkSecondary = Color(0xFF475569)     // ~7.5:1 on PaperSheet

// Dark Mode Studio Carbon Neutrals (Authored surface steps, zero muddy gray)
val CarbonCanvas = Color(0xFF090E18)
val CarbonSurface = Color(0xFF111827)
val CarbonElevated = Color(0xFF1A2438)
val CarbonHairline = Color(0xFF2B3952)
val CarbonInkPrimary = Color(0xFFF3F6FA) // ~16.5:1 on CarbonSurface
val CarbonInkSecondary = Color(0xFF9FB0C8) // ~7.5:1 on CarbonSurface

// Primary Interactive Brand Hue — Signal Indigo (reserved for primary CTAs & active state)
val SignalIndigo = Color(0xFF1D4ED8)       // 6.7:1 on PaperSheet
val SignalIndigoSoft = Color(0xFFE8EFFF)
val SignalIndigoDark = Color(0xFF60A5FA)   // 7.3:1 on CarbonSurface; paired with InkPrimary on solid buttons
val SignalIndigoDarkContainer = Color(0xFF172B54)

// Disciplined Semantic Status Hues (Verified savings, Attention/Laser, Alert)
val LedgerJade = Color(0xFF047857)         // 5.4:1 on PaperSheet
val LedgerJadeSoft = Color(0xFFDFF7EE)
val LedgerJadeDark = Color(0xFF34D399)
val LedgerJadeDarkContainer = Color(0xFF0F2D23)

val PrecisionAmber = Color(0xFFB45309)     // 5.0:1 on PaperSheet
val PrecisionAmberSoft = Color(0xFFFEF3C7)
val PrecisionAmberDark = Color(0xFFFBBF24)
val PrecisionAmberDarkContainer = Color(0xFF33230A)

val LedgerRed = Color(0xFFB91C1C)          // 6.4:1 on PaperSheet
val LedgerRedSoft = Color(0xFFFEE2E2)
val LedgerRedDark = Color(0xFFF87171)
val LedgerRedDarkContainer = Color(0xFF3E151B)

// Optical Viewfinder & Cinema Hardware Tokens (high-contrast dark optical chamber)
val ViewfinderVoid = Color(0xFF070B14)
val ViewfinderSurface = Color(0xFF101828)
val ViewfinderMuted = Color(0xFFA7B7CE)
val ReticleAmber = Color(0xFFF59E0B)
val ReticleAmberText = Color(0xFF1A1102)

// Authoritative Studio Deck Surfaces (Light/Dark balance & showcase stage)
val LedgerDeckBrush = Brush.linearGradient(
  colors = listOf(Color(0xFF0E1726), Color(0xFF152544)),
)

val LedgerActionBrush = Brush.linearGradient(
  colors = listOf(SignalIndigo, Color(0xFF1E40AF)),
)
