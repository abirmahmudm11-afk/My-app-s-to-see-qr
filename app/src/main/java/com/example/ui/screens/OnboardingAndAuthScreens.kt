package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.MarketConfig
import com.example.ui.theme.DawnSkyBottom
import com.example.ui.theme.DawnSkyTop
import com.example.ui.theme.DaylightSkyBottom
import com.example.ui.theme.DaylightSkyTop
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.NightSkyBottom
import com.example.ui.theme.NightSkyTop
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.SwatchMint
import com.example.ui.theme.SwatchSky
import com.example.ui.theme.SwatchSunshine

private data class OnboardingChapter(
  val chapterTag: String,
  val headline: String,
  val subtitle: String,
  val heroAnchorValue: String,
  val heroAnchorLabel: String,
  val popoutTitle: String,
  val popoutDetail: String,
  val credentials: List<Pair<String, String>>,
  val imageRes: Int,
)

@Composable
fun OnboardingScreen(
  selectedCountryCode: String,
  reducedMotion: Boolean,
  onComplete: (String) -> Unit,
  onContinueGuest: () -> Unit,
) {
  var step by remember { mutableIntStateOf(0) }
  var pickedCountry by remember { mutableStateOf(selectedCountryCode) }

  val chapters = remember {
    listOf(
      OnboardingChapter(
        chapterTag = "CHAPTER 01 • DAWN",
        headline = "Know every shelf item in under 2 seconds.",
        subtitle = "Open the camera, catch any retail barcode, and verify live product provenance with calm clarity.",
        heroAnchorValue = "$0",
        heroAnchorLabel = "to scan unlimited barcodes across 13 formats",
        popoutTitle = "4-Tier Verified Lookup",
        popoutDetail = "Firestore Merchant -> Open Food Facts -> Offline Room -> Community",
        credentials = listOf(
          "13 Symbologies" to "EAN-13, UPC-A/E, QR, DataMatrix, PDF417+",
          "GS1 Modulo-10" to "Strict check-digit validation on entry",
        ),
        imageRes = R.drawable.img_scan_hero,
      ),
      OnboardingChapter(
        chapterTag = "CHAPTER 02 • DAYLIGHT",
        headline = "Honest labels. Real price comparisons.",
        subtitle = "We surface verified merchant sources separately from community observations—no invented savings.",
        heroAnchorValue = "< 2s",
        heroAnchorLabel = "median lookup speed with offline Room fallback",
        popoutTitle = "Zero-Spin Provenance",
        popoutDetail = "Verified Merchant • Licensed Provider • Open Food Facts • Unverified",
        credentials = listOf(
          "4 Source Tiers" to "Clear trust label on every product card",
          "6 Global Markets" to "USD, GBP, EUR, JPY, SGD & CAD formatting",
        ),
        imageRes = R.drawable.img_discover_basket,
      ),
      OnboardingChapter(
        chapterTag = "CHAPTER 03 • NIGHT",
        headline = "Turn eligible receipts into auditable rewards.",
        subtitle = "Log store receipts in 1–3 minutes with SHA-256 duplicate protection and track every point ledger entry.",
        heroAnchorValue = "1–3 min",
        heroAnchorLabel = "to submit a receipt; 100 PTS minimum sandbox voucher",
        popoutTitle = "SHA-256 Receipt Guard",
        popoutDetail = "Human-reviewed approvals • Idempotent point ledger • $0 fee",
        credentials = listOf(
          "100 PTS Start" to "Low voucher redemption threshold",
          "SHA-256 Hash" to "Automatic duplicate receipt prevention",
        ),
        imageRes = R.drawable.img_wallet_trophy,
      ),
    )
  }

  val chapter = chapters[step]
  val isNightChapter = step == 2
  val animDuration = if (reducedMotion) 0 else 450
  val topColor by animateColorAsState(
    targetValue = when (step) {
      0 -> DawnSkyTop
      1 -> DaylightSkyTop
      else -> NightSkyTop
    },
    animationSpec = tween(animDuration),
    label = "skyTop",
  )
  val bottomColor by animateColorAsState(
    targetValue = when (step) {
      0 -> DawnSkyBottom
      1 -> DaylightSkyBottom
      else -> NightSkyBottom
    },
    animationSpec = tween(animDuration),
    label = "skyBottom",
  )

  val primaryTextColor = if (isNightChapter) Color(0xFFF0F5F2) else DeepObsidian
  val secondaryTextColor = if (isNightChapter) Color(0xFFA8BBB1) else Color(0xFF3E4A44)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
      .statusBarsPadding(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top step indicator + Skip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          repeat(3) { idx ->
            Box(
              modifier = Modifier
                .width(if (idx == step) 30.dp else 10.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                  when {
                    idx == step && isNightChapter -> ElectricLime
                    idx == step -> DeepObsidian
                    isNightChapter -> Color.White.copy(alpha = 0.25f)
                    else -> DeepObsidian.copy(alpha = 0.2f)
                  },
                ),
            )
          }
        }
        TextButton(onClick = onContinueGuest) {
          Text(
            "Guest Preview",
            color = primaryTextColor,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }

      Text(
        text = chapter.chapterTag,
        style = MaterialTheme.typography.labelMedium,
        color = if (isNightChapter) ElectricLime else EmeraldTrust,
      )

      Text(
        text = chapter.headline,
        style = MaterialTheme.typography.displayMedium,
        color = primaryTextColor,
      )

      Text(
        text = chapter.subtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = secondaryTextColor,
      )

      // Visual Hierarchy Pop-out Payoff Card overlapping the story artwork
      Box(modifier = Modifier.fillMaxWidth()) {
        Image(
          painter = painterResource(id = chapter.imageRes),
          contentDescription = chapter.headline,
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp)),
          contentScale = ContentScale.Crop,
        )

        Card(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp)),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isNightChapter) Color(0xFF17221E) else Color.White,
          ),
          border = BorderStroke(
            1.dp,
            if (isNightChapter) ElectricLime.copy(alpha = 0.4f) else DeepObsidian.copy(alpha = 0.08f),
          ),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = chapter.heroAnchorValue,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 30.sp,
                color = if (isNightChapter) ElectricLime else EmeraldTrust,
              )
              Text(
                text = chapter.heroAnchorLabel,
                style = MaterialTheme.typography.bodySmall,
                color = if (isNightChapter) Color(0xFFA8BBB1) else Color(0xFF3E4A44),
                modifier = Modifier.padding(bottom = 4.dp),
              )
            }
            Text(
              text = chapter.popoutTitle,
              style = MaterialTheme.typography.titleMedium,
              color = if (isNightChapter) Color.White else DeepObsidian,
            )
            Text(
              text = chapter.popoutDetail,
              style = MaterialTheme.typography.labelSmall,
              color = if (isNightChapter) SwatchMint else EmeraldTrust,
            )
          }
        }
      }

      // Halo Effect Credential Strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        chapter.credentials.forEach { (title, detail) ->
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = if (isNightChapter) Color(0xFF17221E) else Color.White.copy(alpha = 0.92f),
            border = BorderStroke(
              1.dp,
              if (isNightChapter) Color(0xFF2B3D36) else DeepObsidian.copy(alpha = 0.1f),
            ),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  Icons.Rounded.Verified,
                  contentDescription = null,
                  tint = if (isNightChapter) ElectricLime else EmeraldTrust,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = title,
                  style = MaterialTheme.typography.labelLarge,
                  color = primaryTextColor,
                )
              }
              Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
              )
            }
          }
        }
      }

      // Market selector on the final chapter
      if (step == 2) {
        Text(
          text = "Select your store currency & region:",
          style = MaterialTheme.typography.labelLarge,
          color = primaryTextColor,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MarketConfig.SUPPORTED_MARKETS.take(3).forEach { market ->
            val selected = pickedCountry == market.countryCode
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { pickedCountry = market.countryCode },
              shape = RoundedCornerShape(12.dp),
              color = if (selected) ElectricLime else Color(0xFF17221E),
              border = BorderStroke(1.dp, if (selected) ElectricLime else Color(0xFF2B3D36)),
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  market.countryCode,
                  style = MaterialTheme.typography.titleMedium,
                  color = if (selected) ObsidianOnLime else Color.White,
                )
                Text(
                  market.currencyCode,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (selected) ObsidianOnLime else Color(0xFFA8BBB1),
                )
              }
            }
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MarketConfig.SUPPORTED_MARKETS.drop(3).forEach { market ->
            val selected = pickedCountry == market.countryCode
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { pickedCountry = market.countryCode },
              shape = RoundedCornerShape(12.dp),
              color = if (selected) ElectricLime else Color(0xFF17221E),
              border = BorderStroke(1.dp, if (selected) ElectricLime else Color(0xFF2B3D36)),
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  market.countryCode,
                  style = MaterialTheme.typography.titleMedium,
                  color = if (selected) ObsidianOnLime else Color.White,
                )
                Text(
                  market.currencyCode,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (selected) ObsidianOnLime else Color(0xFFA8BBB1),
                )
              }
            }
          }
        }
      }

      Spacer(Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (step > 0) {
          OutlinedButton(
            onClick = { step -= 1 },
            modifier = Modifier.height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
              1.dp,
              if (isNightChapter) Color(0xFF3E524A) else DeepObsidian.copy(alpha = 0.25f),
            ),
          ) {
            Text("Back", color = primaryTextColor)
          }
        }

        Button(
          onClick = {
            if (step < 2) {
              step += 1
            } else {
              onComplete(pickedCountry)
            }
          },
          modifier = Modifier
            .weight(1f)
            .height(54.dp),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isNightChapter) ElectricLime else DeepObsidian,
            contentColor = if (isNightChapter) ObsidianOnLime else Color.White,
          ),
        ) {
          Text(
            text = if (step < 2) "Continue Story" else "Start Scanning ($0)",
            style = MaterialTheme.typography.labelLarge,
          )
          Spacer(Modifier.width(8.dp))
          Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun AuthScreen(
  isSigningIn: Boolean,
  onGoogleSignIn: () -> Unit,
  onEmailSignIn: (email: String, password: String) -> Unit,
  onEmailSignUp: (displayName: String, email: String, password: String) -> Unit,
  onSendPasswordReset: (email: String) -> Unit,
  onContinueGuest: () -> Unit,
) {
  var isSignUpMode by remember { mutableStateOf(false) }
  var displayName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Spacer(Modifier.height(4.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_launcher_symbol),
        contentDescription = "SCAN2MONEY",
        modifier = Modifier
          .size(50.dp)
          .clip(RoundedCornerShape(14.dp)),
        contentScale = ContentScale.Crop,
      )
      Column {
        Text(
          text = "SCAN2MONEY",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = "Barcode Discovery & Receipt Rewards",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = DeepObsidian),
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          color = ElectricLime,
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text = "EMAIL AUTH • GOOGLE CREDENTIAL MANAGER",
            style = MaterialTheme.typography.labelSmall,
            color = ObsidianOnLime,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        Text(
          text = "$0 to join. Sync every scan & request payouts.",
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AuthAnchorMetric(value = "< 2s", label = "Catalog lookup", modifier = Modifier.weight(1f))
          AuthAnchorMetric(value = "1–3m", label = "Receipt log", modifier = Modifier.weight(1f))
          AuthAnchorMetric(value = "100p", label = "Payout start", modifier = Modifier.weight(1f))
        }
      }
    }

    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(false to "Email Sign In", true to "Create Account").forEach { (signUpTab, label) ->
            val selected = isSignUpMode == signUpTab
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { isSignUpMode = signUpTab },
              color = if (selected) DeepObsidian else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(12.dp),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) ElectricLime else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
              )
            }
          }
        }

        if (isSignUpMode) {
          androidx.compose.material3.OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        androidx.compose.material3.OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          singleLine = true,
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
          ),
          modifier = Modifier.fillMaxWidth(),
        )

        androidx.compose.material3.OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password (min 6 chars)") },
          singleLine = true,
          visualTransformation = if (showPassword) {
            androidx.compose.ui.text.input.VisualTransformation.None
          } else {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
          },
          trailingIcon = {
            TextButton(onClick = { showPassword = !showPassword }) {
              Text(if (showPassword) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )

        if (!isSignUpMode) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onSendPasswordReset(email) }) {
              Text("Forgot Password?", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        Button(
          onClick = {
            if (isSignUpMode) {
              onEmailSignUp(displayName, email, password)
            } else {
              onEmailSignIn(email, password)
            }
          },
          enabled = !isSigningIn,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = DeepObsidian,
            contentColor = Color.White,
          ),
        ) {
          if (isSigningIn) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = ElectricLime,
            )
            Spacer(Modifier.width(8.dp))
            Text("Authenticating...")
          } else {
            Text(
              text = if (isSignUpMode) "Sign Up with Email" else "Sign In with Email",
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }

    Button(
      onClick = onGoogleSignIn,
      enabled = !isSigningIn,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = EmeraldSoftTint,
        contentColor = DeepObsidian,
      ),
    ) {
      Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(10.dp))
      Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
    }

    OutlinedButton(
      onClick = onContinueGuest,
      enabled = !isSigningIn,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
      Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(8.dp))
      Text("Explore Scanner in Guest Mode", style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun AuthAnchorMetric(value: String, label: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF192420),
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(value, style = MaterialTheme.typography.titleLarge, color = ElectricLime)
      Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFB5C4BD))
    }
  }
}
