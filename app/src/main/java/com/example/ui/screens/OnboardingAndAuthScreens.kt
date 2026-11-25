package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.theme.CarbonCanvas
import com.example.ui.theme.CarbonHairline
import com.example.ui.theme.CarbonInkPrimary
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CobaltAccent
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.MintGlowBorderBrush
import com.example.ui.theme.MintOnDarkText
import com.example.ui.theme.SignalIndigoDark
import com.example.ui.theme.ViewfinderMuted
import com.example.ui.theme.ViewfinderVoid

@Composable
fun OnboardingScreen(
  selectedCountryCode: String,
  reducedMotion: Boolean,
  onComplete: (String) -> Unit,
  @Suppress("UNUSED_PARAMETER") onContinueGuest: () -> Unit = {},
) {
  val glowPulse by if (reducedMotion) {
    remember { mutableFloatStateOf(0.8f) }
  } else {
    rememberInfiniteTransition(label = "welcomeGlow").animateFloat(
      initialValue = 0.65f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "welcomeGlowAlpha",
    )
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonCanvas)
      .drawBehind {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(SignalIndigoDark.copy(alpha = 0.22f * glowPulse), Color.Transparent),
            center = Offset(size.width * 0.18f, size.height * 0.16f),
            radius = size.maxDimension * 0.6f,
          ),
          radius = size.maxDimension * 0.6f,
          center = Offset(size.width * 0.18f, size.height * 0.16f),
        )
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(CobaltAccent.copy(alpha = 0.16f * glowPulse), Color.Transparent),
            center = Offset(size.width * 0.88f, size.height * 0.62f),
            radius = size.maxDimension * 0.52f,
          ),
          radius = size.maxDimension * 0.52f,
          center = Offset(size.width * 0.88f, size.height * 0.62f),
        )
      },
    contentAlignment = Alignment.Center,
  ) {
    val isWide = maxWidth >= 680.dp
    val heroHeight = if (isWide) 320.dp else (maxHeight * 0.40f).coerceIn(220.dp, 310.dp)
    val headlineSize = if (isWide) 44.sp else 38.sp
    val headlineLineHeight = if (isWide) 50.sp else 43.sp

    Column(
      modifier = Modifier
        .widthIn(max = 520.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Quiet Luxury Wordmark Header
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(0.75.dp, MintGlowBorderBrush, RoundedCornerShape(10.dp)),
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_launcher_symbol),
            contentDescription = "SCAN2MONEY",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        }
        Text(
          text = "SCAN2MONEY",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
          ),
          color = CarbonInkPrimary,
        )
      }

      Spacer(Modifier.height(24.dp))

      // Clean Editorial Artwork Frame (Zero Overlaid Clutter)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(heroHeight),
        shape = RoundedCornerShape(26.dp),
        color = ViewfinderVoid,
        border = BorderStroke(0.75.dp, MintGlowBorderBrush),
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          Image(
            painter = painterResource(id = R.drawable.img_onboarding_hero_scan),
            contentDescription = "SCAN2MONEY Optical Scanner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    CarbonCanvas.copy(alpha = 0.78f),
                  ),
                ),
              ),
          )
        }
      }

      Spacer(Modifier.height(28.dp))

      // Single Editorial Title + Single Primary CTA
      Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
          text = buildAnnotatedString {
            withStyle(SpanStyle(color = CarbonInkPrimary)) {
              append("Scan smarter.\n")
            }
            withStyle(SpanStyle(color = SignalIndigoDark)) {
              append("Earn everywhere.")
            }
          },
          style = MaterialTheme.typography.displayLarge.copy(
            fontSize = headlineSize,
            lineHeight = headlineLineHeight,
            letterSpacing = (-0.8).sp,
          ),
        )

        AnimatedButton(
          onClick = { onComplete(selectedCountryCode) },
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("onboarding_continue_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SignalIndigoDark,
            contentColor = MintOnDarkText,
          ),
        ) {
          Text(
            text = "Get Started",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Spacer(Modifier.width(10.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@Composable
fun AuthScreen(
  isSigningIn: Boolean,
  onGoogleSignIn: () -> Unit,
  onContinueGuest: () -> Unit,
) {
  val reducedMotion = LocalReducedMotion.current
  val glowPulse by if (reducedMotion) {
    remember { mutableFloatStateOf(0.8f) }
  } else {
    rememberInfiniteTransition(label = "authGlow").animateFloat(
      initialValue = 0.65f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "authGlowAlpha",
    )
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonCanvas)
      .drawBehind {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(SignalIndigoDark.copy(alpha = 0.18f * glowPulse), Color.Transparent),
            center = Offset(size.width * 0.85f, size.height * 0.14f),
            radius = size.maxDimension * 0.55f,
          ),
          radius = size.maxDimension * 0.55f,
          center = Offset(size.width * 0.85f, size.height * 0.14f),
        )
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(CobaltAccent.copy(alpha = 0.14f * glowPulse), Color.Transparent),
            center = Offset(size.width * 0.14f, size.height * 0.65f),
            radius = size.maxDimension * 0.48f,
          ),
          radius = size.maxDimension * 0.48f,
          center = Offset(size.width * 0.14f, size.height * 0.65f),
        )
      },
    contentAlignment = Alignment.Center,
  ) {
    val isWide = maxWidth >= 680.dp

    Column(
      modifier = Modifier
        .widthIn(max = 520.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(0.75.dp, MintGlowBorderBrush, RoundedCornerShape(10.dp)),
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_launcher_symbol),
            contentDescription = "SCAN2MONEY",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        }
        Text(
          text = "SCAN2MONEY",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
          ),
          color = CarbonInkPrimary,
        )
      }

      Spacer(Modifier.height(24.dp))

      Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = buildAnnotatedString {
              withStyle(SpanStyle(color = CarbonInkPrimary)) {
                append("Your retail vault,\n")
              }
              withStyle(SpanStyle(color = SignalIndigoDark)) {
                append("synced everywhere.")
              }
            },
            style = MaterialTheme.typography.displayLarge.copy(
              fontSize = if (isWide) 40.sp else 34.sp,
              lineHeight = if (isWide) 46.sp else 39.sp,
              letterSpacing = (-0.6).sp,
            ),
          )
          Text(
            text = "Sync real product scans, audited receipts, and point rewards with your Google account or explore locally as a guest.",
            style = MaterialTheme.typography.bodyMedium,
            color = ViewfinderMuted,
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = CarbonSurface.copy(alpha = 0.88f),
          border = BorderStroke(0.75.dp, CarbonHairline.copy(alpha = 0.75f)),
        ) {
          Column {
            AuthFeatureRow(
              icon = Icons.Rounded.QrCodeScanner,
              title = "Live Retail Catalog & Price Radar",
              detail = "Open Food Facts data and multi-store quotes",
              accent = SignalIndigoDark,
            )
            HorizontalDivider(color = CarbonHairline.copy(alpha = 0.5f))
            AuthFeatureRow(
              icon = Icons.Rounded.Verified,
              title = "Audited Receipt Ledger",
              detail = "Duplicate-proof SHA-256 claims from 100 PTS",
              accent = CobaltAccent,
            )
            HorizontalDivider(color = CarbonHairline.copy(alpha = 0.5f))
            AuthFeatureRow(
              icon = Icons.Rounded.Shield,
              title = "Cloud Sync & Guest Portability",
              detail = "Device history stays intact when you sign in",
              accent = SignalIndigoDark,
            )
          }
        }
      }

      Spacer(Modifier.height(24.dp))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedButton(
          onClick = onGoogleSignIn,
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SignalIndigoDark,
            contentColor = MintOnDarkText,
          ),
        ) {
          if (isSigningIn) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MintOnDarkText,
            )
            Spacer(Modifier.width(10.dp))
            Text(
              text = "Connecting Google Account...",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
          } else {
            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
              text = "Continue with Google",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
          }
        }

        AnimatedTextButton(
          onClick = onContinueGuest,
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("continue_guest_button"),
          shape = RoundedCornerShape(14.dp),
        ) {
          Text(
            text = "Continue as Guest",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = ViewfinderMuted,
          )
        }
      }
    }
  }
}

@Composable
private fun AuthFeatureRow(
  icon: ImageVector,
  title: String,
  detail: String,
  accent: Color,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 15.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Surface(
      color = accent.copy(alpha = 0.14f),
      shape = RoundedCornerShape(10.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accent,
        modifier = Modifier
          .padding(9.dp)
          .size(18.dp),
      )
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = CarbonInkPrimary,
      )
      Text(
        text = detail,
        style = MaterialTheme.typography.bodySmall,
        color = ViewfinderMuted,
      )
    }
  }
}
