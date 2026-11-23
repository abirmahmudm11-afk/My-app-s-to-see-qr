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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.ShopperMission
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust

@Composable
fun MissionProgressRing(
  progress: Float,
  isCompleted: Boolean,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
  ringSize: Dp = 54.dp,
  strokeWidth: Dp = 5.5.dp,
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

  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val primaryAccent = MaterialTheme.colorScheme.primary
  val sweepSecondary = ElectricLime

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
          brush = Brush.sweepGradient(listOf(primaryAccent, sweepSecondary, primaryAccent)),
          startAngle = -90f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = Offset(inset, inset),
          size = arcSize,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
      } else {
        // Subtle minimum progress dot so 0-step missions stay visually grounded
        drawCircle(
          color = primaryAccent.copy(alpha = 0.45f),
          radius = strokePx * 0.48f,
          center = Offset(size.width / 2f, inset),
        )
      }
    }

    if (isCompleted) {
      Surface(
        color = EmeraldSoftTint,
        shape = CircleShape,
        modifier = Modifier.size(ringSize - strokeWidth * 2.8f),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.Check,
            contentDescription = "Mission Completed",
            tint = EmeraldTrust,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    } else {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = centerLabel ?: "${(progress * 100).toInt()}%",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (progress > 0f) {
          Spacer(Modifier.height(1.dp))
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(primaryAccent.copy(alpha = pulseAlpha)),
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
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "SHOPPER MISSIONS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
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
          ringSize = 48.dp,
          strokeWidth = 5.dp,
        )
      }

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

@Composable
private fun MissionTile(
  mission: ShopperMission,
  reducedMotion: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("mission_tile_${mission.id}"),
    color = if (mission.isCompleted) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    },
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(
      1.dp,
      if (mission.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
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
          centerLabel = "${mission.currentStep}/${mission.targetStep}",
        )
        Surface(
          color = if (mission.isCompleted) EmeraldSoftTint else MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text = if (mission.isCompleted) "Done" else mission.rewardHint,
            style = MaterialTheme.typography.labelSmall,
            color = if (mission.isCompleted) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = mission.title,
          style = MaterialTheme.typography.titleSmall,
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
