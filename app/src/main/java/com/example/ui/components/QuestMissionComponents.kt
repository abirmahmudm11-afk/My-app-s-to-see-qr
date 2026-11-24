package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.ShopperMission
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.SwatchMint
import com.example.ui.theme.SwatchOrchid
import com.example.ui.theme.SwatchSky
import com.example.ui.theme.SwatchSunshine
import com.example.ui.theme.TangerineAccent
import com.example.ui.theme.TangerineSoftTint
import com.example.ui.theme.TealVivid
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletSoftTint

@Composable
fun MissionProgressRing(
  progress: Float,
  isCompleted: Boolean,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
  ringSize: Dp = 54.dp,
  strokeWidth: Dp = 5.5.dp,
  accentColor: Color = EmeraldTrust,
  secondaryAccent: Color = ElectricLime,
  centerLabel: String? = null,
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = tween(
      durationMillis = if (reducedMotion) 0 else 650,
      easing = FastOutSlowInEasing,
    ),
    label = "missionRingSweep",
  )

  val pulseAlpha = if (!reducedMotion && !isCompleted && progress > 0f) {
    val transition = rememberInfiniteTransition(label = "missionMotionIndicator")
    val alpha by transition.animateFloat(
      initialValue = 0.35f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "pulseAlpha",
    )
    alpha
  } else {
    1f
  }

  val trackColor = accentColor.copy(alpha = 0.16f)

  Box(
    modifier = modifier.size(ringSize),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokePx = strokeWidth.toPx()
      val inset = strokePx / 2f
      val arcSize = Size(size.width - strokePx, size.height - strokePx)

      drawArc(
        color = trackColor,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(inset, inset),
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round),
      )

      val sweep = 360f * animatedProgress
      if (sweep > 0.5f) {
        drawArc(
          brush = Brush.sweepGradient(listOf(accentColor, secondaryAccent, accentColor)),
          startAngle = -90f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = Offset(inset, inset),
          size = arcSize,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
      } else {
        drawCircle(
          color = accentColor.copy(alpha = 0.55f),
          radius = strokePx * 0.48f,
          center = Offset(size.width / 2f, inset),
        )
      }
    }

    if (isCompleted) {
      Surface(
        color = accentColor,
        shape = CircleShape,
        modifier = Modifier.size(ringSize - strokeWidth * 2.8f),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.Check,
            contentDescription = "Mission Completed",
            tint = Color.White,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    } else {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = centerLabel ?: "${(progress * 100).toInt()}%",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (progress > 0f) {
          Spacer(Modifier.height(1.dp))
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = pulseAlpha)),
          )
        }
      }
    }
  }
}

@Composable
fun QuestMissionsSectionCard(
  missions: List<ShopperMission>,
  reducedMotion: Boolean,
  onOpenScan: () -> Unit,
  onOpenReceiptSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val completedCount = missions.count { it.isCompleted }
  val totalProgress = if (missions.isEmpty()) 0f else {
    missions.map { it.progressFraction }.average().toFloat()
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("missions_progress_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.horizontalGradient(
              listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
              ),
            ),
          )
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "SHOPPER MISSIONS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Progress Milestones ($completedCount/${missions.size})",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        MissionProgressRing(
          progress = totalProgress,
          isCompleted = completedCount == missions.size && missions.isNotEmpty(),
          reducedMotion = reducedMotion,
          ringSize = 50.dp,
          strokeWidth = 5.5.dp,
          accentColor = TealVivid,
          secondaryAccent = CobaltInfo,
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        val rows = missions.chunked(2)
        rows.forEach { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            pair.forEach { mission ->
              MissionTile(
                mission = mission,
                reducedMotion = reducedMotion,
                onClick = {
                  if (mission.id == "receipt_check") onOpenReceiptSubmit() else onOpenScan()
                },
                modifier = Modifier.weight(1f),
              )
            }
            if (pair.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

private data class MissionPalette(
  val lightTileTint: Color,
  val darkTileTint: Color,
  val border: Color,
  val accent: Color,
  val badgeBg: Color,
)

@Composable
private fun MissionTile(
  mission: ShopperMission,
  reducedMotion: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
  val palette = when (mission.id) {
    "first_scan" -> MissionPalette(
      lightTileTint = Color(0xFFE0EEFF),
      darkTileTint = Color(0xFF13263F),
      border = CobaltInfo.copy(alpha = if (isDark) 0.55f else 0.35f),
      accent = if (isDark) Color(0xFF60A5FA) else CobaltInfo,
      badgeBg = if (isDark) Color(0xFF1B3557) else CobaltSoftTint,
    )
    "daily_trio" -> MissionPalette(
      lightTileTint = Color(0xFFD6F9EA),
      darkTileTint = Color(0xFF0F2E26),
      border = EmeraldTrust.copy(alpha = if (isDark) 0.55f else 0.35f),
      accent = if (isDark) Color(0xFF34D399) else EmeraldTrust,
      badgeBg = if (isDark) Color(0xFF164336) else EmeraldSoftTint,
    )
    "qr_discovery" -> MissionPalette(
      lightTileTint = Color(0xFFEDE4FF),
      darkTileTint = Color(0xFF251942),
      border = VioletAccent.copy(alpha = if (isDark) 0.55f else 0.35f),
      accent = if (isDark) Color(0xFFA78BFA) else VioletAccent,
      badgeBg = if (isDark) Color(0xFF332359) else VioletSoftTint,
    )
    else -> MissionPalette(
      lightTileTint = Color(0xFFFFEED2),
      darkTileTint = Color(0xFF332110),
      border = TangerineAccent.copy(alpha = if (isDark) 0.55f else 0.35f),
      accent = if (isDark) Color(0xFFFB923C) else TangerineAccent,
      badgeBg = if (isDark) Color(0xFF472C15) else TangerineSoftTint,
    )
  }

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("mission_tile_${mission.id}"),
    color = if (isDark) palette.darkTileTint else palette.lightTileTint,
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(
      width = if (mission.isCompleted) 1.5.dp else 1.dp,
      color = palette.border,
    ),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        MissionProgressRing(
          progress = mission.progressFraction,
          isCompleted = mission.isCompleted,
          reducedMotion = reducedMotion,
          ringSize = 46.dp,
          strokeWidth = 4.5.dp,
          accentColor = palette.accent,
          secondaryAccent = ElectricLime,
          centerLabel = "${mission.currentStep}/${mission.targetStep}",
        )
        Surface(
          color = if (mission.isCompleted) palette.accent else palette.badgeBg,
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text = if (mission.isCompleted) "Done" else mission.rewardHint,
            style = MaterialTheme.typography.labelSmall,
            color = if (mission.isCompleted) Color.White else palette.accent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = mission.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = mission.subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
