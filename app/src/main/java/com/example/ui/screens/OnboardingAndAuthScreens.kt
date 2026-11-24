package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.MarketConfig
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.bounceClickable
import com.example.ui.theme.LedgerDeckBrush
import com.example.ui.theme.LedgerJadeDark
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.ReticleAmber
import com.example.ui.theme.ReticleAmberText
import com.example.ui.theme.SignalIndigoDark
import com.example.ui.theme.TabularDataStyle
import com.example.ui.theme.ViewfinderMuted
import com.example.ui.theme.ViewfinderVoid
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

private data class OnboardingChapter(
  val name: String,
  val headline: String,
  val subtitle: String,
  val highlights: List<Pair<String, String>>,
)

@Composable
fun OnboardingScreen(
  selectedCountryCode: String,
  reducedMotion: Boolean,
  onComplete: (String) -> Unit,
  onContinueGuest: () -> Unit,
) {
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
  val scope = rememberCoroutineScope()
  var pickedCountry by remember { mutableStateOf(selectedCountryCode) }
  val currentPage = pagerState.currentPage

  val chapters = remember {
    listOf(
      OnboardingChapter(
        name = "Barcode & QR Scanner",
        headline = "Scan any retail label in seconds.",
        subtitle = "Decode 13 barcode and QR symbologies with automatic nutrition facts and offline Room history.",
        highlights = listOf(
          "13 Symbologies" to "EAN-13, UPC, QR & DataMatrix",
          "Room Archive" to "Saved automatically on device",
        ),
      ),
      OnboardingChapter(
        name = "Price Comparison",
        headline = "Compare store prices side by side.",
        subtitle = "Check verified merchant quotes, Open Prices history, and lowest available offers before checkout.",
        highlights = listOf(
          "Live Radar" to "Compare retailers by barcode",
          "Source Labels" to "Verified feeds clearly badged",
        ),
      ),
      OnboardingChapter(
        name = "Sandbox Rewards",
        headline = "Turn receipts into audited points.",
        subtitle = "Select your home currency, log purchase receipts in minutes, and redeem points from 100 PTS.",
        highlights = listOf(
          "100 PTS Tier" to "$0 redemption fee vouchers",
          "SHA-256 Check" to "Duplicate-proof ledger",
        ),
      ),
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 620.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          repeat(3) { idx ->
            val pillWidth by animateDpAsState(
              targetValue = if (idx == currentPage) 28.dp else 8.dp,
              animationSpec = tween(durationMillis = if (reducedMotion) 0 else 200, easing = FastOutSlowInEasing),
              label = "onboardingPillWidth_$idx",
            )
            Box(
              modifier = Modifier
                .width(pillWidth)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .bounceClickable { scope.launch { pagerState.animateScrollToPage(idx) } }
                .background(
                  if (idx == currentPage) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outline
                  },
                ),
            )
          }
          Spacer(Modifier.width(6.dp))
          Text(
            text = "${currentPage + 1} of 3",
            style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        AnimatedTextButton(
          onClick = onContinueGuest,
          modifier = Modifier.testTag("onboarding_guest_button"),
        ) {
          Text("Skip as Guest")
        }
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        pageSpacing = 14.dp,
      ) { pageIdx ->
        val chapter = chapters[pageIdx]
        val pageOffset = ((pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction)
        val clampedOffset = abs(pageOffset).coerceIn(0f, 1f)
        val cardScale = if (reducedMotion) 1f else 1f - clampedOffset * 0.04f

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
              scaleX = cardScale
              scaleY = cardScale
              alpha = 1f - clampedOffset * 0.3f
            },
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          OnboardingStageCard(
            step = pageIdx,
            reducedMotion = reducedMotion,
          )

          Text(
            text = chapter.headline,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = chapter.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            chapter.highlights.forEach { (title, sub) ->
              Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }
        }
      }

      if (currentPage == 2) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Home market & currency",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MarketConfig.SUPPORTED_MARKETS.take(4).forEach { market ->
              MarketRegionChip(
                code = market.countryCode,
                currency = market.currencyCode,
                selected = pickedCountry == market.countryCode,
                onClick = { pickedCountry = market.countryCode },
                modifier = Modifier.weight(1f),
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MarketConfig.SUPPORTED_MARKETS.drop(4).forEach { market ->
              MarketRegionChip(
                code = market.countryCode,
                currency = market.currencyCode,
                selected = pickedCountry == market.countryCode,
                onClick = { pickedCountry = market.countryCode },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      Spacer(Modifier.height(2.dp))

      AnimatedButton(
        onClick = {
          if (currentPage < 2) {
            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
          } else {
            onComplete(pickedCountry)
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("onboarding_continue_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
      ) {
        Text(
          text = if (currentPage < 2) "Next" else "Continue to App Demo",
          style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
          Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun MarketRegionChip(
  code: String,
  currency: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .bounceClickable(onClick = onClick),
    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(
      width = 1.dp,
      color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
    ),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = code,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = currency,
        style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
        color = if (selected) {
          MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
      )
    }
  }
}

@Composable
private fun OnboardingStageCard(
  step: Int,
  reducedMotion: Boolean,
) {
  val phase by if (reducedMotion) {
    remember { mutableFloatStateOf(0.45f) }
  } else {
    rememberInfiniteTransition(label = "onboardingHeroAnim_$step").animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "onboardingHeroPhase_$step",
    )
  }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = ViewfinderVoid,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(196.dp)
        .background(LedgerDeckBrush)
        .padding(18.dp),
    ) {
      when (step) {
        0 -> {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Box(
              modifier = Modifier
                .size(width = 144.dp, height = 156.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f)),
              contentAlignment = Alignment.Center,
            ) {
              Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val w = size.width
                val h = size.height
                val barWidths = listOf(4f, 2f, 7f, 3f, 5f, 2f, 6f, 3f, 4f, 2f, 6f)
                var x = w * 0.14f
                barWidths.forEachIndexed { idx, bw ->
                  val barW = bw * 2f
                  drawRoundRect(
                    color = Color.White.copy(alpha = 0.88f),
                    topLeft = Offset(x, h * 0.24f),
                    size = Size(barW, h * (if (idx % 3 == 0) 0.52f else 0.44f)),
                    cornerRadius = CornerRadius(2f, 2f),
                  )
                  x += barW + 4.5f
                }

                val cLen = 16.dp.toPx()
                val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(ReticleAmber, Offset(0f, cLen), Offset(0f, 0f), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(0f, 0f), Offset(cLen, 0f), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(w - cLen, 0f), Offset(w, 0f), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(w, 0f), Offset(w, cLen), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(0f, h - cLen), Offset(0f, h), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(0f, h), Offset(cLen, h), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(w - cLen, h), Offset(w, h), stroke.width, StrokeCap.Round)
                drawLine(ReticleAmber, Offset(w, h - cLen), Offset(w, h), stroke.width, StrokeCap.Round)

                val laserY = h * (0.18f + phase * 0.64f)
                drawLine(
                  color = ReticleAmber,
                  start = Offset(6f, laserY),
                  end = Offset(w - 6f, laserY),
                  strokeWidth = 2.8.dp.toPx(),
                  cap = StrokeCap.Round,
                )
              }
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Surface(color = LedgerJadeDark, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "Optical Lock",
                  style = MaterialTheme.typography.labelSmall,
                  color = ReticleAmberText,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
              Text(
                text = "13 Barcode Types",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
              )
              Text(
                text = "Every scan logs cleanly to your on-device Room history.",
                style = MaterialTheme.typography.bodySmall,
                color = ViewfinderMuted,
              )
            }
          }
        }

        1 -> {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(Icons.Rounded.CompareArrows, contentDescription = null, tint = SignalIndigoDark, modifier = Modifier.size(18.dp))
                Text("Retailer Comparison", style = MaterialTheme.typography.labelLarge, color = Color.White)
              }
              Surface(color = LedgerJadeDark, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "Save $1.20",
                  style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                  color = ReticleAmberText,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }

            val offers = listOf(
              Triple("FreshMart Express", "$3.29", true),
              Triple("City Superstore", "$3.85", false),
              Triple("Metro Grocer", "$4.49", false),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              offers.forEachIndexed { idx, (store, price, best) ->
                val fill = if (reducedMotion) 1f else (0.55f + 0.45f * sin((phase + idx * 0.25f) * PI.toFloat()).coerceIn(0f, 1f))
                val barColor = if (best) LedgerJadeDark else SignalIndigoDark
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  color = Color.White.copy(alpha = if (best) 0.14f else 0.07f),
                  shape = RoundedCornerShape(10.dp),
                  border = if (best) BorderStroke(1.dp, LedgerJadeDark.copy(alpha = 0.7f)) else null,
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Icon(Icons.Rounded.Storefront, contentDescription = null, tint = barColor, modifier = Modifier.size(16.dp))
                      Text(store, style = MaterialTheme.typography.labelLarge, color = Color.White)
                      Box(
                        modifier = Modifier
                          .width((24f * fill).dp)
                          .height(4.dp)
                          .clip(RoundedCornerShape(999.dp))
                          .background(barColor),
                      )
                    }
                    Text(
                      text = price,
                      style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                      color = if (best) LedgerJadeDark else Color.White,
                    )
                  }
                }
              }
            }
          }
        }

        else -> {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Box(
              modifier = Modifier
                .size(width = 144.dp, height = 156.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f)),
              contentAlignment = Alignment.Center,
            ) {
              Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val w = size.width
                val h = size.height
                drawRoundRect(
                  color = Color.White,
                  topLeft = Offset(w * 0.1f, h * 0.14f),
                  size = Size(w * 0.56f, h * 0.72f),
                  cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                )
                repeat(4) { line ->
                  val y = h * (0.28f + line * 0.12f)
                  drawLine(
                    color = if (line == 3) LedgerJadeDark else Color(0xFF94A3B8),
                    start = Offset(w * 0.18f, y),
                    end = Offset(w * (if (line == 3) 0.50f else 0.56f), y),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round,
                  )
                }

                val angle = phase * PI.toFloat() * 1.4f
                val coin1 = Offset(w * 0.78f, h * 0.45f + sin(angle) * 18.dp.toPx())
                val coin2 = Offset(w * 0.72f + cos(angle) * 8.dp.toPx(), h * 0.68f - sin(angle) * 12.dp.toPx())
                drawCircle(color = ReticleAmber, radius = 14.dp.toPx(), center = coin1)
                drawCircle(color = SignalIndigoDark, radius = 10.dp.toPx(), center = coin2)
              }
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Surface(color = ReticleAmber, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "+150 PTS",
                  style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize, fontWeight = FontWeight.Bold),
                  color = ReticleAmberText,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
              Text(
                text = "Audited Receipts",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
              )
              Text(
                text = "Redeem sandbox vouchers starting at 100 PTS with $0 fee.",
                style = MaterialTheme.typography.bodySmall,
                color = ViewfinderMuted,
              )
            }
          }
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
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 620.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_launcher_symbol),
          contentDescription = "SCAN2MONEY",
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp)),
          contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "SCAN2MONEY",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "Live workflow preview and account setup",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Live 60fps Interactive Animated Showcase Reel (Screen 4 focal motion)
      AnimatedShowcaseVideoCard(modifier = Modifier.fillMaxWidth())

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          AuthOptionRow(
            title = "Sign in with Google",
            detail = "Sync scan history across devices, submit receipts, and redeem vouchers.",
            modifier = Modifier.padding(14.dp),
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline)
          AuthOptionRow(
            title = "Start as Guest",
            detail = "Use the 13-format barcode scanner, price radar, and Room history locally.",
            modifier = Modifier.padding(14.dp),
          )
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedButton(
          onClick = onGoogleSignIn,
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        ) {
          if (isSigningIn) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(10.dp))
            Text("Connecting Google Account...")
          } else {
            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
          }
        }

        AnimatedOutlinedButton(
          onClick = onContinueGuest,
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("continue_guest_button"),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text(
            text = "Continue as Guest",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}

@Composable
private fun AuthOptionRow(
  title: String,
  detail: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun AnimatedShowcaseVideoCard(modifier: Modifier = Modifier) {
  val reducedMotion = LocalReducedMotion.current
  var isPlaying by remember(reducedMotion) { mutableStateOf(!reducedMotion) }
  var progress by remember { mutableFloatStateOf(0.08f) }
  val totalSeconds = 9f

  LaunchedEffect(isPlaying, reducedMotion) {
    if (!isPlaying || reducedMotion) return@LaunchedEffect
    var lastFrameNanos = withFrameNanos { it }
    while (isPlaying) {
      val currentNanos = withFrameNanos { it }
      val dtSeconds = ((currentNanos - lastFrameNanos).coerceAtLeast(0L) / 1_000_000_000f).coerceAtMost(0.1f)
      lastFrameNanos = currentNanos
      progress = (progress + dtSeconds / totalSeconds) % 1f
    }
  }

  val activeScene = when {
    progress < 0.333f -> 0
    progress < 0.666f -> 1
    else -> 2
  }
  val sceneLocal = ((progress - activeScene * 0.333f) / 0.333f).coerceIn(0f, 1f)
  val currentSec = (progress * totalSeconds).toInt().coerceIn(0, 8) + 1

  val sceneTitles = listOf("Scan", "Compare", "Earn")
  val sceneCaptions = listOf(
    "Aim camera — optical reticle locks GTIN",
    "Compare live retailer offers side by side",
    "Verify receipt — points post to your ledger",
  )

  Surface(
    modifier = modifier.testTag("auth_animated_video_card"),
    shape = RoundedCornerShape(16.dp),
    color = ViewfinderVoid,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(LedgerDeckBrush)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
            Text(
              text = if (isPlaying) "Demo Reel" else "Paused",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
          Text(
            text = "0:0$currentSec / 0:09",
            style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
            color = ViewfinderMuted,
          )
        }

        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .bounceClickable { isPlaying = !isPlaying }
            .testTag("video_play_pause_button"),
          color = Color.White.copy(alpha = 0.14f),
          shape = RoundedCornerShape(999.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
              contentDescription = if (isPlaying) "Pause animation" else "Play animation",
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = if (isPlaying) "Pause" else "Play",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
            )
          }
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(166.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(ViewfinderVoid)
          .padding(14.dp),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          when (activeScene) {
            0 -> {
              val cardW = w * 0.56f
              val cardH = h * 0.78f
              val cardX = (w - cardW) / 2f
              val cardY = (h - cardH) / 2f + sin(sceneLocal * PI.toFloat() * 2f) * 2.5.dp.toPx()

              drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                topLeft = Offset(cardX, cardY),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
              )
              val barCount = 14
              for (i in 0 until barCount) {
                val bx = cardX + cardW * (0.14f + i * 0.052f)
                val bw = if (i % 3 == 0) 4.5.dp.toPx() else 2.2.dp.toPx()
                drawLine(
                  color = Color.White.copy(alpha = 0.88f),
                  start = Offset(bx, cardY + cardH * 0.24f),
                  end = Offset(bx, cardY + cardH * 0.72f),
                  strokeWidth = bw,
                  cap = StrokeCap.Round,
                )
              }
              val beamY = cardY + cardH * (0.14f + 0.72f * (0.5f - 0.5f * cos(sceneLocal * PI.toFloat() * 2f)))
              drawLine(
                color = ReticleAmber,
                start = Offset(cardX - 8f, beamY),
                end = Offset(cardX + cardW + 8f, beamY),
                strokeWidth = 2.8.dp.toPx(),
                cap = StrokeCap.Round,
              )
            }

            1 -> {
              val stores = listOf(0.84f, 0.56f, 0.94f)
              stores.forEachIndexed { idx, ratio ->
                val rowY = h * (0.14f + idx * 0.28f)
                val slide = ((sceneLocal * 2.1f) - idx * 0.22f).coerceIn(0f, 1f)
                val isBest = idx == 1
                drawRoundRect(
                  color = if (isBest) LedgerJadeDark.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.07f),
                  topLeft = Offset(w * 0.06f, rowY),
                  size = Size(w * 0.88f * slide, h * 0.21f),
                  cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                )
                drawRoundRect(
                  color = if (isBest) LedgerJadeDark else SignalIndigoDark,
                  topLeft = Offset(w * 0.10f, rowY + h * 0.06f),
                  size = Size(w * 0.46f * ratio * slide, h * 0.09f),
                  cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
              }
            }

            else -> {
              val burst = sin(sceneLocal * PI.toFloat())
              drawRoundRect(
                color = Color.White.copy(alpha = 0.95f),
                topLeft = Offset(w * 0.10f, h * 0.14f),
                size = Size(w * 0.28f, h * 0.72f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
              )
              repeat(3) { row ->
                drawLine(
                  color = if (row == 2) LedgerJadeDark else Color(0xFF94A3B8),
                  start = Offset(w * 0.14f, h * (0.30f + row * 0.16f)),
                  end = Offset(w * 0.33f, h * (0.30f + row * 0.16f)),
                  strokeWidth = 3.dp.toPx(),
                  cap = StrokeCap.Round,
                )
              }
              val walletCenter = Offset(w * 0.78f, h * 0.50f)
              drawCircle(
                color = LedgerJadeDark.copy(alpha = 0.24f),
                radius = 28.dp.toPx() * (0.9f + 0.15f * burst),
                center = walletCenter,
              )
              drawCircle(
                color = SignalIndigoDark,
                radius = 18.dp.toPx(),
                center = walletCenter,
              )
              repeat(4) { coinIdx ->
                val t = ((sceneLocal * 1.4f) - coinIdx * 0.14f).coerceIn(0f, 1f)
                val cx = w * (0.38f + 0.40f * t)
                val cy = h * (0.32f + coinIdx * 0.12f - sin(t * PI.toFloat()) * 0.16f)
                drawCircle(
                  color = if (coinIdx % 2 == 0) ReticleAmber else LedgerJadeDark,
                  radius = (5.5.dp.toPx() * (1f - t * 0.25f)).coerceAtLeast(3f),
                  center = Offset(cx, cy),
                )
              }
            }
          }
        }

        Surface(
          modifier = Modifier.align(Alignment.BottomStart),
          color = Color.Black.copy(alpha = 0.65f),
          shape = RoundedCornerShape(8.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = when (activeScene) {
                0 -> Icons.Rounded.QrCodeScanner
                1 -> Icons.Rounded.CompareArrows
                else -> Icons.AutoMirrored.Rounded.ReceiptLong
              },
              contentDescription = null,
              tint = ReticleAmber,
              modifier = Modifier.size(14.dp),
            )
            AnimatedContent(
              targetState = sceneCaptions[activeScene],
              transitionSpec = {
                (fadeIn(tween(180)) + slideInVertically { it / 3 })
                  .togetherWith(fadeOut(tween(120)) + slideOutVertically { -it / 3 })
              },
              label = "cinemaSceneCaption",
            ) { caption ->
              Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        sceneTitles.forEachIndexed { idx, label ->
          val segStart = idx * 0.333f
          val segProgress = ((progress - segStart) / 0.333f).coerceIn(0f, 1f)
          val active = idx == activeScene
          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .bounceClickable { progress = segStart + 0.03f }
              .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.16f)),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(if (idx < activeScene) 1f else if (active) segProgress else 0f)
                  .height(4.dp)
                  .clip(RoundedCornerShape(999.dp))
                  .background(if (active) ReticleAmber else SignalIndigoDark),
              )
            }
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = if (active) Color.White else ViewfinderMuted,
              fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            )
          }
        }
      }
    }
  }
}
