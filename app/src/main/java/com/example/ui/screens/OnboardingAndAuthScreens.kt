package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.GoogleSignInButton
import com.example.ui.theme.BentoButter
import com.example.ui.theme.BentoLime
import com.example.ui.theme.BentoMint
import com.example.ui.theme.BentoOrchid
import com.example.ui.theme.BentoSky
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianMid

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (isDark) listOf(InkBlack, Color(0xFF0A1F18), InkBlack)
                    else listOf(Color(0xFFF6F7F2), BentoMint.copy(alpha = 0.45f), Color(0xFFE7F9D7))
                )
            )
    ) {
        // Subtle architectural dot matrix & glow circles across backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotColor = if (isDark) Color.White.copy(alpha = 0.06f) else InkBlack.copy(alpha = 0.05f)
            val step = 28.dp.toPx()
            var x = step
            while (x < size.width) {
                var y = step
                while (y < size.height * 0.65f) {
                    drawCircle(color = dotColor, radius = 1.4.dp.toPx(), center = Offset(x, y))
                    y += step
                }
                x += step
            }
            drawCircle(
                color = BentoLime.copy(alpha = if (isDark) 0.08f else 0.30f),
                radius = size.width * 0.48f,
                center = Offset(size.width * 0.85f, size.height * 0.16f)
            )
            drawCircle(
                color = BentoSky.copy(alpha = if (isDark) 0.06f else 0.55f),
                radius = size.width * 0.36f,
                center = Offset(size.width * 0.12f, size.height * 0.42f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Layered Visual Stage with Floating Cards & Badges
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Decorative tilted card behind main showcase
                Surface(
                    color = BentoButter,
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .size(258.dp)
                        .offset(x = (-14).dp, y = 12.dp)
                        .rotate(-6f)
                ) {}

                Surface(
                    color = BentoOrchid,
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .size(258.dp)
                        .offset(x = 14.dp, y = (-8).dp)
                        .rotate(5f)
                ) {}

                // Center Hero Artwork Frame
                Surface(
                    shape = RoundedCornerShape(34.dp),
                    color = Color(0xFFFAF9F6),
                    shadowElevation = 14.dp,
                    border = BorderStroke(3.dp, Color.White),
                    modifier = Modifier.size(264.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_scan_hero),
                        contentDescription = "SCAN2MONEY barcode scanner showcase",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Floating Pill Badge Top-Left
                Surface(
                    color = InkBlack,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 24.dp, start = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = BentoLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "13 FORMATS",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Floating Pill Badge Bottom-Right
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 18.dp, end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE OFFERS",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                    }
                }
            }

            // Bottom Elevated Obsidian Showcase Deck
            Surface(
                color = InkBlack,
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(ObsidianMid, InkBlack)))
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SCAN2MONEY",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.8).sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onContinue,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoLime,
                                contentColor = InkBlack
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .testTag("onboarding_continue_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.size(36.dp))
                                Text(
                                    text = "Continue",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = InkBlack,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = BentoLime,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    BackHandler { onContinueAsGuest() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        Emerald900.copy(alpha = 0.08f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = BentoMint,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(width = 168.dp, height = 124.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_wallet_trophy),
                        contentDescription = "SCAN2MONEY rewards wallet",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Sign in to SCAN2MONEY",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Save scan history, submit receipts, and manage audited rewards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        GoogleSignInButton(
                            onAuthSuccess = {
                                errorMessage = null
                                onAuthSuccess()
                            },
                            onAuthError = { msg -> errorMessage = msg }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onContinueAsGuest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("continue_as_guest_button")
                        ) {
                            Text(
                                text = "Continue without signing in",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Sandbox mode • Cashback payouts disabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
