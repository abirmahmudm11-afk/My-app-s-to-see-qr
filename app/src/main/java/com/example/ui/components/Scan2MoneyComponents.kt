package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CatalogProduct
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.VerificationStatus
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.SwatchMint
import com.example.ui.theme.SwatchSky
import com.example.ui.theme.TangerineAccent
import com.example.ui.theme.TealSoftTint
import com.example.ui.theme.TealVivid
import com.example.ui.theme.VioletAccent
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VerificationLabel(
  statusRaw: String,
  modifier: Modifier = Modifier,
) {
  val status = VerificationStatus.fromString(statusRaw)
  val (bg, fg, icon) = when (status) {
    VerificationStatus.VERIFIED_MERCHANT -> Triple(EmeraldSoftTint, EmeraldTrust, Icons.Rounded.CheckCircle)
    VerificationStatus.LICENSED_PROVIDER -> Triple(CobaltSoftTint, CobaltInfo, Icons.Rounded.Shield)
    VerificationStatus.OPEN_FOOD_FACTS -> Triple(TealSoftTint, TealVivid, Icons.Rounded.Public)
    VerificationStatus.USER_SUBMITTED_UNVERIFIED -> Triple(AmberSoftTint, AmberNotice, Icons.Rounded.WarningAmber)
    VerificationStatus.UNKNOWN -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Rounded.Info)
  }
  Surface(
    modifier = modifier,
    color = bg,
    shape = RoundedCornerShape(9.dp),
    border = BorderStroke(1.dp, fg.copy(alpha = 0.28f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
      Text(
        text = status.label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun ReceiptStatusBadge(
  statusRaw: String,
  modifier: Modifier = Modifier,
) {
  val status = ReceiptReviewStatus.fromString(statusRaw)
  val (bg, fg, icon, text) = when (status) {
    ReceiptReviewStatus.APPROVED -> listOf(EmeraldSoftTint, EmeraldTrust, Icons.Rounded.CheckCircle, "APPROVED")
    ReceiptReviewStatus.REJECTED -> listOf(CrimsonSoftTint, CrimsonAlert, Icons.Rounded.WarningAmber, "REJECTED")
    ReceiptReviewStatus.PENDING -> listOf(AmberSoftTint, AmberNotice, Icons.Rounded.Schedule, "PENDING REVIEW")
  }
  @Suppress("UNCHECKED_CAST")
  val fgColor = fg as Color
  Surface(
    modifier = modifier,
    color = bg as Color,
    shape = RoundedCornerShape(9.dp),
    border = BorderStroke(1.dp, fgColor.copy(alpha = 0.28f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        icon as androidx.compose.ui.graphics.vector.ImageVector,
        contentDescription = null,
        tint = fgColor,
        modifier = Modifier.size(13.dp),
      )
      Text(
        text = text as String,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = fgColor,
      )
    }
  }
}

@Composable
fun SandboxCashbackBanner(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, TealVivid.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(SwatchMint.copy(alpha = 0.72f), SwatchSky.copy(alpha = 0.72f)),
          ),
        )
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        color = TealVivid,
        shape = RoundedCornerShape(10.dp),
      ) {
        Icon(
          Icons.Rounded.Verified,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.padding(7.dp).size(18.dp),
        )
      }
      Text(
        text = "Sandbox Rewards Mode: Points (PTS) track verified receipt & campaign checks in Firestore. Non-cash demo ledger.",
        style = MaterialTheme.typography.bodySmall,
        color = DeepObsidian,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
fun GuestModeBanner(
  onSignInClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(AmberSoftTint, Color(0xFFFFE5D0)),
          ),
        )
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        color = TangerineAccent,
        shape = RoundedCornerShape(10.dp),
      ) {
        Icon(
          Icons.Rounded.AutoAwesome,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.padding(7.dp).size(18.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Guest Preview Mode",
          style = MaterialTheme.typography.labelLarge,
          color = DeepObsidian,
        )
        Text(
          text = "Scanning, Room history & missions work locally. Sign in with Google to sync & submit receipts.",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF4A3018),
        )
      }
      Button(
        onClick = onSignInClick,
        modifier = Modifier.testTag("guest_banner_sign_in_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DeepObsidian,
          contentColor = ElectricLime,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      ) {
        Text("Sign In", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  accentColor: Color = EmeraldTrust,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(20.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(
            Brush.verticalGradient(listOf(accentColor, CobaltInfo)),
          ),
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    if (actionLabel != null && onAction != null) {
      TextButton(onClick = onAction) {
        Text(
          actionLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
fun ProductSummaryCard(
  product: CatalogProduct,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (product.imageUrl.isNotBlank()) {
        AsyncImage(
          model = product.imageUrl,
          contentDescription = product.name,
          modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(EmeraldSoftTint),
          contentScale = ContentScale.Crop,
        )
      } else {
        Box(
          modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.linearGradient(listOf(EmeraldTrust, CobaltInfo)),
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.Rounded.QrCode2,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
          )
        }
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = listOfNotNull(
            product.brand.takeIf { it.isNotBlank() },
            product.barcode,
          ).joinToString(" • "),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        VerificationLabel(statusRaw = product.verificationStatus)
      }
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        shape = RoundedCornerShape(10.dp),
      ) {
        Icon(
          Icons.AutoMirrored.Rounded.KeyboardArrowRight,
          contentDescription = "Inspect product",
          tint = EmeraldTrust,
          modifier = Modifier.padding(6.dp).size(18.dp),
        )
      }
    }
  }
}

fun formatTimestamp(timestamp: Timestamp?, fallbackMs: Long = 0L): String {
  val millis = timestamp?.toDate()?.time ?: fallbackMs
  if (millis <= 0L) return "Just now"
  return SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(millis))
}

fun formatDetailedTimestamp(timestamp: Timestamp?, fallbackMs: Long = 0L): String {
  val millis = timestamp?.toDate()?.time ?: fallbackMs
  if (millis <= 0L) return "Just now"
  return SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US).format(Date(millis))
}
