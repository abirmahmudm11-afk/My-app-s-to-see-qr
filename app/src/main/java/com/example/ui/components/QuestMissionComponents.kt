package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.ShopperMission
import com.example.ui.theme.TabularDataStyle

@Composable
fun MissionProgressRing(
  progress: Float,
  isCompleted: Boolean,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
  ringSize: Dp = 44.dp,
  strokeWidth: Dp = 4.dp,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  centerLabel: String? = null,
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = tween(
      durationMillis = if (reducedMotion) 0 else 420,
      easing = FastOutSlowInEasing,
    ),
    label = "missionRingSweep",
  )

  val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
  val strokeColor = if (isCompleted) MaterialTheme.colorScheme.secondary else accentColor

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
          color = strokeColor,
          startAngle = -90f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = Offset(inset, inset),
          size = arcSize,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
      }
    }

    if (isCompleted) {
      Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.size(ringSize - strokeWidth * 2.8f),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.Check,
            contentDescription = "Completed",
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(15.dp),
          )
        }
      }
    } else {
      Text(
        text = centerLabel ?: "${(progress * 100).toInt()}%",
        style = TabularDataStyle.copy(
          fontSize = MaterialTheme.typography.labelSmall.fontSize,
          fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.onSurface,
      )
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

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("missions_progress_card"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Shopper Milestones",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "$completedCount of ${missions.size} milestones complete",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        MissionProgressRing(
          progress = totalProgress,
          isCompleted = completedCount == missions.size && missions.isNotEmpty(),
          reducedMotion = reducedMotion,
          ringSize = 46.dp,
          strokeWidth = 4.5.dp,
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        val rows = missions.chunked(2)
        rows.forEach { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

@Composable
private fun MissionTile(
  mission: ShopperMission,
  reducedMotion: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val completed = mission.isCompleted
  val containerColor = if (completed) {
    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
  }
  val accentColor = if (completed) {
    MaterialTheme.colorScheme.secondary
  } else {
    MaterialTheme.colorScheme.primary
  }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(containerColor)
      .bounceClickable(onClick = onClick)
      .padding(12.dp)
      .testTag("mission_tile_${mission.id}"),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      MissionProgressRing(
        progress = mission.progressFraction,
        isCompleted = completed,
        reducedMotion = reducedMotion,
        ringSize = 40.dp,
        strokeWidth = 4.dp,
        accentColor = accentColor,
        centerLabel = "${mission.currentStep}/${mission.targetStep}",
      )
      Surface(
        color = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp),
      ) {
        Text(
          text = if (completed) "Done" else mission.rewardHint,
          style = MaterialTheme.typography.labelSmall,
          color = if (completed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
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
