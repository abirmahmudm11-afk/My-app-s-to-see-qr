package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.MarketConfig
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.GlowingPillBadge
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.theme.TabularDataStyle
import kotlinx.coroutines.launch

private data class OnboardingStepSpec(
  val tabLabel: String,
  val badgeText: String,
  val title: String,
  val description: String,
  val metricLabel: String,
  val metricValue: String,
  val icon: ImageVector,
  val drawableRes: Int,
)

@Composable
fun OnboardingScreen(
  selectedCountryCode: String,
  reducedMotion: Boolean,
  onComplete: (String) -> Unit,
  onContinueGuest: () -> Unit = {},
) {
  val extended = LocalExtendedColors.current
  val scope = rememberCoroutineScope()
  var chosenCountryCode by remember(selectedCountryCode) {
    mutableStateOf(MarketConfig.findByCountry(selectedCountryCode).countryCode)
  }
  val activeMarket = MarketConfig.findByCountry(chosenCountryCode)

  val steps = remember {
    listOf(
      OnboardingStepSpec(
        tabLabel = "1. Scan",
        badgeText = "LIVE CATALOG",
        title = "Live Barcode & QR Radar",
        description = "Scan any retail barcode or QR code for instant Open Food Facts nutrition details and live store prices.",
        metricLabel = "DATA SOURCE",
        metricValue = "Open Food Facts + Open Prices",
        icon = Icons.Rounded.QrCodeScanner,
        drawableRes = R.drawable.img_welcome_radar_light,
      ),
      OnboardingStepSpec(
        tabLabel = "2. Earn 10%",
        badgeText = "REWARDED ADS",
        title = "10% Share on Every Ad",
        description = "Watch a rewarded ad after scanning to unlock an exact 10% USD credit straight into your earnings ledger.",
        metricLabel = "PAYOUT SHARE",
        metricValue = "10% Ad Value -> USD",
        icon = Icons.Rounded.PlayCircle,
        drawableRes = R.drawable.img_auth_vault_light,
      ),
      OnboardingStepSpec(
        tabLabel = "3. Withdraw",
        badgeText = "USDT BEP-20",
        title = "Binance USDT Payouts",
        description = "Track every rewarded ad completion in real time and request a BEP-20 USDT withdrawal from $1.00 USD.",
        metricLabel = "MIN WITHDRAWAL",
        metricValue = "$1.00 USD (BEP-20)",
        icon = Icons.Rounded.CurrencyExchange,
        drawableRes = R.drawable.img_auth_vault_light,
      ),
    )
  }

  val pagerState = rememberPagerState(pageCount = { steps.size })

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center,
  ) {
    val isWide = maxWidth >= 680.dp
    val headlineSize = if (isWide) 36.sp else 30.sp
    val headlineLineHeight = if (isWide) 42.sp else 35.sp

    Column(
      modifier = Modifier
        .widthIn(max = 540.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Brand Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, extended.cardBorder, RoundedCornerShape(10.dp)),
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_launcher_symbol),
              contentDescription = "SCAN2MONEY",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          }
          Column {
            Text(
              text = "SCAN2MONEY",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
              ),
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "${activeMarket.countryCode} · ${activeMarket.currencyCode}",
              style = TabularDataStyle.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }

        GlowingPillBadge(
          text = "10% USD Share",
          icon = Icons.Rounded.AutoAwesome,
        )
      }

      Spacer(Modifier.height(16.dp))

      // Editorial Headline + Interactive 3-Step Showcase
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
          text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
              append("Scan products.\n")
            }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
              append("Earn real USDT.")
            }
          },
          style = MaterialTheme.typography.displayLarge.copy(
            fontSize = headlineSize,
            lineHeight = headlineLineHeight,
            letterSpacing = (-0.7).sp,
          ),
        )

        // Step Selector Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          steps.forEachIndexed { index, step ->
            val selected = pagerState.currentPage == index
            val chipBg by animateColorAsState(
              targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              animationSpec = tween(if (reducedMotion) 0 else 180),
              label = "stepTabBg",
            )
            val chipTextColor by animateColorAsState(
              targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              animationSpec = tween(if (reducedMotion) 0 else 180),
              label = "stepTabText",
            )
            Surface(
              modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  scope.launch {
                    if (reducedMotion) {
                      pagerState.scrollToPage(index)
                    } else {
                      pagerState.animateScrollToPage(index)
                    }
                  }
                }
                .testTag("onboarding_step_tab_$index"),
              shape = RoundedCornerShape(12.dp),
              color = chipBg,
              border = BorderStroke(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else extended.cardBorder,
              ),
            ) {
              Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = step.tabLabel,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  ),
                  color = chipTextColor,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
        }

        // Interactive Pure-White Showcase Pager
        HorizontalPager(
          state = pagerState,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_pager"),
        ) { page ->
          val step = steps[page]
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, extended.cardBorder),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(176.dp)
                  .background(MaterialTheme.colorScheme.surface),
              ) {
                Image(
                  painter = painterResource(id = step.drawableRes),
                  contentDescription = step.title,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop,
                )
                Surface(
                  modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                  shape = RoundedCornerShape(999.dp),
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                  border = BorderStroke(1.dp, extended.cardBorder),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                  ) {
                    Icon(
                      imageVector = step.icon,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text = step.badgeText,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = step.title,
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = step.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  border = BorderStroke(1.dp, extended.dividerSubtle),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(
                      text = step.metricLabel,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      text = step.metricValue,
                      style = TabularDataStyle.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }
          }
        }

        // Pager Dot Indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          repeat(steps.size) { index ->
            val selected = pagerState.currentPage == index
            val dotWidth by animateDpAsState(
              targetValue = if (selected) 24.dp else 7.dp,
              animationSpec = tween(durationMillis = if (reducedMotion) 0 else 200, easing = FastOutSlowInEasing),
              label = "onboardingDotWidth",
            )
            Box(
              modifier = Modifier
                .padding(horizontal = 3.dp)
                .height(7.dp)
                .width(dotWidth)
                .clip(CircleShape)
                .background(
                  if (selected) MaterialTheme.colorScheme.primary
                  else extended.cardBorder,
                ),
            )
          }
        }

        // Market Region & Currency Selector
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.cardBorder),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Region & Local Price Currency",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              MarketConfig.SUPPORTED_MARKETS.forEach { market ->
                val selected = market.countryCode == chosenCountryCode
                Surface(
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { chosenCountryCode = market.countryCode }
                    .testTag("onboarding_market_${market.countryCode}"),
                  shape = RoundedCornerShape(999.dp),
                  color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else extended.dividerSubtle,
                  ),
                ) {
                  Text(
                    text = "${market.countryCode} · ${market.currencyCode}",
                    style = TabularDataStyle.copy(
                      fontSize = 12.sp,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  )
                }
              }
            }
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      // Primary Bottom CTA Dock
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AnimatedButton(
          onClick = { onComplete(chosenCountryCode) },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("onboarding_continue_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
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

        AnimatedTextButton(
          onClick = {
            onComplete(chosenCountryCode)
            onContinueGuest()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("onboarding_quick_guest_button"),
        ) {
          Text(
            text = "Skip directly to scanner",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
  val extended = LocalExtendedColors.current

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center,
  ) {
    val isWide = maxWidth >= 680.dp

    Column(
      modifier = Modifier
        .widthIn(max = 540.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, extended.cardBorder, RoundedCornerShape(10.dp)),
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
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.1.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
          )
        }

        GlowingPillBadge(
          text = "CLOUD VAULT",
          icon = Icons.Rounded.VerifiedUser,
        )
      }

      Spacer(Modifier.height(16.dp))

      // Hero Vault Card + Headline
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.cardBorder),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(168.dp),
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_auth_vault_light),
              contentDescription = "SCAN2MONEY Encrypted Cloud Vault",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
            Surface(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
              shape = RoundedCornerShape(999.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
              border = BorderStroke(1.dp, extended.cardBorder),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp),
                )
                Text(
                  text = "Protected by Google Sign-In",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = buildAnnotatedString {
              withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                append("Protect your earnings,\n")
              }
              withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("withdraw anytime.")
              }
            },
            style = MaterialTheme.typography.displayLarge.copy(
              fontSize = if (isWide) 36.sp else 30.sp,
              lineHeight = if (isWide) 42.sp else 35.sp,
              letterSpacing = (-0.7).sp,
            ),
          )
          Text(
            text = "Connect your Google account to sync live product scans, track your 10% rewarded ad USD balance, and submit Binance USDT BEP-20 withdrawals.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        // Compact Guarantee Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.cardBorder),
        ) {
          Column {
            AuthFeatureRow(
              icon = Icons.Rounded.Sync,
              title = "Real-Time Ledger Sync",
              detail = "Scans, ad completions & receipts stay synced across devices",
              accent = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(color = extended.dividerSubtle)
            AuthFeatureRow(
              icon = Icons.Rounded.AccountBalanceWallet,
              title = "Exact 10% Ad Earnings",
              detail = "Every completed rewarded ad adds 10% ad revenue in USD",
              accent = extended.bentoEarningsAccent,
            )
            HorizontalDivider(color = extended.dividerSubtle)
            AuthFeatureRow(
              icon = Icons.Rounded.Shield,
              title = "Binance USDT BEP-20 Payouts",
              detail = "Request verified wallet payouts starting at $1.00 USD",
              accent = extended.bentoWalletAccent,
            )
          }
        }
      }

      Spacer(Modifier.height(18.dp))

      // Sign-In & Guest Action Dock
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedButton(
          onClick = onGoogleSignIn,
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(16.dp),
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

        AnimatedOutlinedButton(
          onClick = onContinueGuest,
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("continue_guest_button"),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, extended.cardBorder),
        ) {
          Text(
            text = "Continue as Guest",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Text(
          text = "Guest mode saves scans locally on your device. Sign in with Google whenever you are ready to withdraw USDT.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
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
      .padding(horizontal = 16.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Surface(
      color = accent.copy(alpha = 0.12f),
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
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = detail,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
