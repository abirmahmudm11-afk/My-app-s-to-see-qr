@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CatalogProduct
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.VerificationStatus
import com.example.ui.theme.LocalAnimatedVisibilityScope
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSharedTransitionScope
import com.example.ui.theme.TabularDataStyle
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Modifier.sharedProductBounds(key: String): Modifier = composed {
  val reducedMotion = LocalReducedMotion.current
  val sharedScope = LocalSharedTransitionScope.current
  val visibilityScope = LocalAnimatedVisibilityScope.current
  if (reducedMotion || sharedScope == null || visibilityScope == null) {
    this
  } else {
    with(sharedScope) {
      this@composed.sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = visibilityScope,
        boundsTransform = { _, _ ->
          spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
          )
        },
      )
    }
  }
}

/**
 * Crisp tactile press detent (110ms ease-out, zero elastic overshoot).
 * Gives responsive confirmation without making everyday tasks feel sluggish or bouncy.
 */
fun Modifier.pressBounce(
  interactionSource: MutableInteractionSource,
  enabled: Boolean = true,
  pressedScale: Float = 0.975f,
): Modifier = composed {
  val reducedMotion = LocalReducedMotion.current
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed && enabled && !reducedMotion) pressedScale else 1f,
    animationSpec = tween(durationMillis = if (reducedMotion) 0 else 110, easing = FastOutSlowInEasing),
    label = "tactilePressScale",
  )
  graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}

fun Modifier.bounceClickable(
  enabled: Boolean = true,
  pressedScale: Float = 0.975f,
  onClick: () -> Unit,
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  pressBounce(interactionSource, enabled = enabled, pressedScale = pressedScale)
    .clickable(
      interactionSource = interactionSource,
      indication = LocalIndication.current,
      enabled = enabled,
      onClick = onClick,
    )
}

/**
 * Immediate presentation for Operate-mode screens — no forced staggered load-in delays.
 */
@Composable
fun MotionStaggeredEntrance(
  @Suppress("UNUSED_PARAMETER") index: Int,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(modifier = modifier) { content() }
}

/**
 * Sunken segmented control track with lifted pill thumb (Apple/Material hybrid).
 */
@Composable
fun SlidingSegmentedPillBar(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  pillColor: Color = MaterialTheme.colorScheme.surface,
  activeTextColor: Color = MaterialTheme.colorScheme.onSurface,
  inactiveTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  height: Dp = 44.dp,
) {
  val reducedMotion = LocalReducedMotion.current
  val safeIndex = selectedIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0))

  BoxWithConstraints(
    modifier = modifier
      .height(height)
      .clip(RoundedCornerShape(10.dp))
      .background(containerColor)
      .padding(3.dp),
  ) {
    val count = options.size.coerceAtLeast(1)
    val tabWidth = maxWidth / count
    val pillOffset by animateDpAsState(
      targetValue = tabWidth * safeIndex,
      animationSpec = tween(
        durationMillis = if (reducedMotion) 0 else 180,
        easing = FastOutSlowInEasing,
      ),
      label = "slidingSegmentOffset",
    )

    Surface(
      modifier = Modifier
        .offset(x = pillOffset)
        .width(tabWidth)
        .fillMaxHeight(),
      shape = RoundedCornerShape(8.dp),
      color = pillColor,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
    ) {}

    Row(modifier = Modifier.fillMaxSize()) {
      options.forEachIndexed { idx, label ->
        val selected = idx == safeIndex
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect(idx) },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) activeTextColor else inactiveTextColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
fun LivePulseDot(
  color: Color = MaterialTheme.colorScheme.primary,
  size: Dp = 7.dp,
  modifier: Modifier = Modifier,
) {
  val reducedMotion = LocalReducedMotion.current
  val pulse by if (reducedMotion) {
    remember { mutableFloatStateOf(0.5f) }
  } else {
    rememberInfiniteTransition(label = "liveDotPulse").animateFloat(
      initialValue = 0.2f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "liveDotAlpha",
    )
  }

  Box(
    modifier = modifier.size(size * 2f),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(size * 1.8f)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.24f * pulse)),
    )
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(color),
    )
  }
}

@Composable
fun AnimatedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(10.dp),
  colors: ButtonColors = ButtonDefaults.buttonColors(),
  border: BorderStroke? = null,
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  Button(
    onClick = onClick,
    modifier = modifier
      .minimumInteractiveComponentSize()
      .pressBounce(interactionSource, enabled = enabled),
    enabled = enabled,
    shape = shape,
    colors = colors,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun AnimatedOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(10.dp),
  colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
  border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .minimumInteractiveComponentSize()
      .pressBounce(interactionSource, enabled = enabled),
    enabled = enabled,
    shape = shape,
    colors = colors,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun AnimatedTextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(8.dp),
  colors: ButtonColors = ButtonDefaults.textButtonColors(),
  contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  TextButton(
    onClick = onClick,
    modifier = modifier
      .minimumInteractiveComponentSize()
      .pressBounce(interactionSource, enabled = enabled),
    enabled = enabled,
    shape = shape,
    colors = colors,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun AnimatedIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
  content: @Composable () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  IconButton(
    onClick = onClick,
    modifier = modifier
      .minimumInteractiveComponentSize()
      .pressBounce(interactionSource, enabled = enabled, pressedScale = 0.92f),
    enabled = enabled,
    colors = colors,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun VerificationLabel(
  statusRaw: String,
  modifier: Modifier = Modifier,
) {
  val status = VerificationStatus.fromString(statusRaw)
  val (bg, fg, icon) = when (status) {
    VerificationStatus.VERIFIED_MERCHANT -> Triple(
      MaterialTheme.colorScheme.secondaryContainer,
      MaterialTheme.colorScheme.secondary,
      Icons.Rounded.CheckCircle,
    )
    VerificationStatus.LICENSED_PROVIDER -> Triple(
      MaterialTheme.colorScheme.primaryContainer,
      MaterialTheme.colorScheme.primary,
      Icons.Rounded.Shield,
    )
    VerificationStatus.OPEN_FOOD_FACTS -> Triple(
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      Icons.Rounded.Public,
    )
    VerificationStatus.USER_SUBMITTED_UNVERIFIED -> Triple(
      MaterialTheme.colorScheme.tertiaryContainer,
      MaterialTheme.colorScheme.tertiary,
      Icons.Rounded.WarningAmber,
    )
    VerificationStatus.UNKNOWN -> Triple(
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      Icons.Rounded.Info,
    )
  }
  Surface(
    modifier = modifier,
    color = bg,
    shape = RoundedCornerShape(6.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
      Text(
        text = status.label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
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
    ReceiptReviewStatus.APPROVED -> listOf(
      MaterialTheme.colorScheme.secondaryContainer,
      MaterialTheme.colorScheme.secondary,
      Icons.Rounded.CheckCircle,
      "Approved",
    )
    ReceiptReviewStatus.REJECTED -> listOf(
      MaterialTheme.colorScheme.errorContainer,
      MaterialTheme.colorScheme.error,
      Icons.Rounded.WarningAmber,
      "Rejected",
    )
    ReceiptReviewStatus.PENDING -> listOf(
      MaterialTheme.colorScheme.tertiaryContainer,
      MaterialTheme.colorScheme.tertiary,
      Icons.Rounded.Schedule,
      "Pending Review",
    )
  }
  val fgColor = fg as Color
  Surface(
    modifier = modifier,
    color = bg as Color,
    shape = RoundedCornerShape(6.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
        color = fgColor,
      )
    }
  }
}

@Composable
fun SandboxCashbackBanner(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
      ) {
        Icon(
          Icons.Rounded.VerifiedUser,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(8.dp).size(18.dp),
        )
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "Sandbox Point Ledger",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Points (PTS) track verified receipts and campaign checks in Firestore. Non-cash demo currency.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun GuestModeBanner(
  onSignInClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
      ) {
        Icon(
          Icons.Rounded.PersonOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(8.dp).size(18.dp),
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "Guest Mode",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Scanning and local history stay on device. Sign in to sync receipts and claim rewards.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      AnimatedButton(
        onClick = onSignInClick,
        modifier = Modifier.testTag("guest_banner_sign_in_button"),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
      ) {
        Text("Sign In", style = MaterialTheme.typography.labelLarge)
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
  @Suppress("UNUSED_PARAMETER") accentColor: Color = Color.Unspecified,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    if (actionLabel != null && onAction != null) {
      AnimatedTextButton(onClick = onAction) {
        Text(
          text = actionLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
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
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .sharedProductBounds("product_card_${product.barcode}")
      .clip(RoundedCornerShape(14.dp))
      .bounceClickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
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
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentScale = ContentScale.Crop,
        )
      } else {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.Rounded.QrCode2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp),
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (product.brand.isNotBlank()) {
            Text(
              text = product.brand,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text("·", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(
            text = product.barcode,
            style = TabularDataStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        VerificationLabel(statusRaw = product.verificationStatus)
      }
      Icon(
        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = "Inspect product",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
      )
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
  return SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.US).format(Date(millis))
}
