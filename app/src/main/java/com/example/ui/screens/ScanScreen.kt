package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScanContext
import com.example.domain.SupportedBarcodeFormat
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedIconButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.BarcodeFeedbackPlayer
import com.example.ui.components.VerificationLabel
import com.example.ui.components.bounceClickable
import com.example.ui.theme.LedgerJadeDark
import com.example.ui.theme.ReticleAmber
import com.example.ui.theme.ReticleAmberText
import com.example.ui.theme.SignalIndigo
import com.example.ui.theme.SignalIndigoDark
import com.example.ui.theme.TabularDataStyle
import com.example.ui.theme.ViewfinderMuted
import com.example.ui.theme.ViewfinderSurface
import com.example.ui.theme.ViewfinderVoid
import com.example.ui.viewmodel.ScannerUiState
import com.example.ui.viewmodel.ScannerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
  scannerViewModel: ScannerViewModel,
  scanContext: ScanContext?,
  reducedMotion: Boolean,
  onClearScanContext: () -> Unit,
  onOpenProductDetail: (String) -> Unit,
  onOpenHistory: () -> Unit,
  onOpenRewardsOrPromo: (String?) -> Unit,
  onShowMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val haptic = LocalHapticFeedback.current
  val uiState by scannerViewModel.uiState.collectAsStateWithLifecycle()
  val detectionPulseSeq by scannerViewModel.detectionPulseSeq.collectAsStateWithLifecycle()

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED,
    )
  }
  var torchEnabled by remember { mutableStateOf(false) }
  var hasFlashUnit by remember { mutableStateOf(false) }
  var boundCamera by remember { mutableStateOf<Camera?>(null) }
  var showManualDialog by remember { mutableStateOf(false) }
  var showIdleTip by remember { mutableStateOf(false) }
  var manualCode by remember(scanContext?.barcodeValue) { mutableStateOf(scanContext?.barcodeValue ?: "") }
  var manualError by remember { mutableStateOf<String?>(null) }

  val feedbackPlayer = remember { BarcodeFeedbackPlayer() }
  val rippleProgress = remember { Animatable(0f) }
  val detectionLaserProgress = remember { Animatable(0f) }
  var detectionLocked by remember { mutableStateOf(false) }
  var isDecodingPhoto by remember { mutableStateOf(false) }

  LaunchedEffect(detectionPulseSeq) {
    if (detectionPulseSeq > 0L) {
      detectionLocked = true
      feedbackPlayer.playSuccessBeep()
      runCatching { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
      if (!reducedMotion) {
        rippleProgress.snapTo(0.01f)
        detectionLaserProgress.snapTo(0.01f)
        val laserJob = launch {
          detectionLaserProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
          )
        }
        rippleProgress.animateTo(
          targetValue = 1f,
          animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        )
        laserJob.join()
        rippleProgress.snapTo(0f)
        detectionLaserProgress.snapTo(0f)
      } else {
        delay(200L)
      }
      detectionLocked = false
    }
  }

  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is ScannerUiState.Matched -> {
        delay(if (reducedMotion) 200L else 480L)
        val code = state.barcode
        scannerViewModel.resetScanner()
        onOpenProductDetail(code)
      }
      is ScannerUiState.NotFound -> {
        delay(if (reducedMotion) 200L else 480L)
        val code = state.barcode
        scannerViewModel.resetScanner()
        onOpenProductDetail(code)
      }
      else -> Unit
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasCameraPermission = granted
  }

  val barcodeOptions = remember {
    BarcodeScannerOptions.Builder()
      .setBarcodeFormats(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E,
        Barcode.FORMAT_CODE_128,
        Barcode.FORMAT_CODE_39,
        Barcode.FORMAT_CODE_93,
        Barcode.FORMAT_QR_CODE,
        Barcode.FORMAT_DATA_MATRIX,
        Barcode.FORMAT_PDF417,
        Barcode.FORMAT_AZTEC,
        Barcode.FORMAT_ITF,
        Barcode.FORMAT_CODABAR,
      )
      .build()
  }
  val mlKitScanner = remember {
    runCatching { BarcodeScanning.getClient(barcodeOptions) }.getOrNull()
  }
  val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

  // Google Play Policy compliant zero-permission Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri: Uri? ->
    if (uri != null) {
      val activeScanner = mlKitScanner
      if (activeScanner == null) {
        onShowMessage("Barcode decoder unavailable in this environment.")
        return@rememberLauncherForActivityResult
      }
      isDecodingPhoto = true
      runCatching {
        val image = InputImage.fromFilePath(context, uri)
        activeScanner.process(image)
          .addOnSuccessListener { barcodes ->
            isDecodingPhoto = false
            val first = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
            if (first != null) {
              scannerViewModel.processScannedBarcode(
                rawValue = first.rawValue.orEmpty(),
                format = SupportedBarcodeFormat.fromMlKitFormat(first.format),
                fromCamera = false,
              )
            } else {
              onShowMessage("No readable barcode found in the selected photo.")
            }
          }
          .addOnFailureListener {
            isDecodingPhoto = false
            onShowMessage("Could not decode barcode from image.")
          }
      }.onFailure {
        isDecodingPhoto = false
        onShowMessage("Unable to open image file.")
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      feedbackPlayer.release()
      runCatching { mlKitScanner?.close() }
      runCatching { analysisExecutor.shutdown() }
    }
  }

  LaunchedEffect(uiState, hasCameraPermission) {
    showIdleTip = false
    if (hasCameraPermission && uiState is ScannerUiState.Idle) {
      delay(12_000L)
      showIdleTip = true
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ViewfinderVoid),
  ) {
    // Clean Optical Viewfinder Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Barcode Scanner",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
          )
          Text(
            text = "13 retail & QR symbologies",
            style = MaterialTheme.typography.bodySmall,
            color = ViewfinderMuted,
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          AnimatedIconButton(
            onClick = {
              if (!hasFlashUnit) {
                onShowMessage("Flashlight hardware unavailable on this device.")
              } else {
                val next = !torchEnabled
                torchEnabled = next
                runCatching { boundCamera?.cameraControl?.enableTorch(next) }
              }
            },
            modifier = Modifier.testTag("scanner_torch_button"),
          ) {
            Icon(
              imageVector = if (torchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
              contentDescription = "Toggle Flashlight",
              tint = if (torchEnabled) ReticleAmber else Color.White,
            )
          }
          AnimatedIconButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
              )
            },
            modifier = Modifier.testTag("scanner_photo_icon_button"),
          ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Scan from photo", tint = Color.White)
          }
          AnimatedIconButton(
            onClick = onOpenHistory,
            modifier = Modifier.testTag("scanner_history_button"),
          ) {
            Icon(Icons.Rounded.History, contentDescription = "Scan History", tint = Color.White)
          }
          AnimatedIconButton(
            onClick = {
              manualError = null
              showManualDialog = true
            },
            modifier = Modifier.testTag("scanner_manual_icon_button"),
          ) {
            Icon(Icons.Rounded.Edit, contentDescription = "Enter GTIN manually", tint = Color.White)
          }
        }
      }

      if (scanContext != null) {
        Surface(
          color = ViewfinderSurface,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Re-scanning ${scanContext.barcodeFormat.ifBlank { "GTIN" }}",
                style = MaterialTheme.typography.labelSmall,
                color = ReticleAmber,
              )
              Text(
                text = listOfNotNull(
                  scanContext.productName.takeIf { it.isNotBlank() },
                  scanContext.brand.takeIf { it.isNotBlank() },
                  scanContext.barcodeValue,
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            AnimatedIconButton(onClick = onClearScanContext) {
              Icon(Icons.Rounded.Close, contentDescription = "Clear re-scan context", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    // Optical Viewfinder Viewport
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 14.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color.Black),
      contentAlignment = Alignment.Center,
    ) {
      if (hasCameraPermission) {
        CameraXBarcodeViewfinder(
          lifecycleOwner = lifecycleOwner,
          scanner = mlKitScanner,
          analysisExecutor = analysisExecutor,
          paused = uiState !is ScannerUiState.Idle,
          onCameraBound = { cam ->
            boundCamera = cam
            hasFlashUnit = cam.cameraInfo.hasFlashUnit()
          },
          onBarcodeDetected = { raw, format ->
            scannerViewModel.processScannedBarcode(raw, format, fromCamera = true)
          },
        )
      } else {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            Icons.Rounded.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp),
          )
          Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
          )
          Text(
            text = "Enable camera permission for live scanning, or pick a photo or enter a barcode below.",
            style = MaterialTheme.typography.bodySmall,
            color = ViewfinderMuted,
          )
          AnimatedButton(
            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            colors = ButtonDefaults.buttonColors(
              containerColor = SignalIndigo,
              contentColor = Color.White,
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("enable_camera_button"),
          ) {
            Text("Enable Camera")
          }
        }
      }

      val isProcessingScan = isDecodingPhoto || uiState is ScannerUiState.LookingUp
      val isLiveScanning = hasCameraPermission && uiState is ScannerUiState.Idle && !isDecodingPhoto

      if (hasCameraPermission || uiState !is ScannerUiState.Idle || isDecodingPhoto) {
        ScanningReticleOverlay(
          isLiveScanning = isLiveScanning,
          isProcessingScan = isProcessingScan,
          detectionLaserProgress = detectionLaserProgress.value,
          detectionRippleProgress = rippleProgress.value,
          isDetectionLocked = detectionLocked || uiState is ScannerUiState.Matched || uiState is ScannerUiState.NotFound,
          reducedMotion = reducedMotion,
          modifier = Modifier.fillMaxSize(),
        )
      }

      // Live viewfinder scanning indicator pill (Top-Start)
      androidx.compose.animation.AnimatedVisibility(
        visible = isLiveScanning && !detectionLocked,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(120)),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(12.dp),
      ) {
        val livePulseAlpha = if (!reducedMotion) {
          val pulseTransition = rememberInfiniteTransition(label = "liveScanDot")
          val dotAlpha by pulseTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
              animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
              repeatMode = RepeatMode.Reverse,
            ),
            label = "dotAlpha",
          )
          dotAlpha
        } else {
          1f
        }
        Surface(
          color = ViewfinderSurface.copy(alpha = 0.88f),
          shape = RoundedCornerShape(999.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
          modifier = Modifier.testTag("scanner_live_status_pill"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ReticleAmber.copy(alpha = livePulseAlpha)),
            )
            Text(
              text = "Scanning",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
            )
          }
        }
      }

      // Accessible >=48dp Floating In-Preview Flashlight Toggle
      if (hasCameraPermission) {
        val flashBg by animateColorAsState(
          targetValue = if (torchEnabled) ReticleAmber else ViewfinderSurface.copy(alpha = 0.88f),
          animationSpec = tween(if (reducedMotion) 0 else 180),
          label = "flashPillBg",
        )
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(999.dp))
            .bounceClickable {
              if (!hasFlashUnit) {
                onShowMessage("Flashlight hardware unavailable on this device.")
              } else {
                val next = !torchEnabled
                torchEnabled = next
                runCatching { boundCamera?.cameraControl?.enableTorch(next) }
              }
            }
            .testTag("camera_preview_flashlight_button"),
          color = flashBg,
          shape = RoundedCornerShape(999.dp),
          border = BorderStroke(1.dp, if (torchEnabled) ReticleAmber else Color.White.copy(alpha = 0.25f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = if (torchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
              contentDescription = if (torchEnabled) "Turn flashlight off" else "Turn flashlight on",
              tint = if (torchEnabled) ReticleAmberText else Color.White,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = if (torchEnabled) "Flash On" else "Flash",
              style = MaterialTheme.typography.labelMedium,
              color = if (torchEnabled) ReticleAmberText else Color.White,
            )
          }
        }
      }

      androidx.compose.animation.AnimatedVisibility(
        visible = isProcessingScan || detectionLocked || uiState is ScannerUiState.Matched || uiState is ScannerUiState.NotFound,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(120)),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 14.dp),
      ) {
        val badgeBorderColor by animateColorAsState(
          targetValue = if (isProcessingScan && !detectionLocked) SignalIndigoDark else LedgerJadeDark,
          animationSpec = tween(if (reducedMotion) 0 else 180),
          label = "scanBadgeBorder",
        )
        Surface(
          color = ViewfinderVoid.copy(alpha = 0.92f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, badgeBorderColor),
          modifier = Modifier.testTag("scanner_detection_laser_badge"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
          ) {
            if (isProcessingScan && uiState !is ScannerUiState.Matched && uiState !is ScannerUiState.NotFound) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = SignalIndigoDark,
                strokeWidth = 2.dp,
              )
            } else {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Barcode detected",
                tint = LedgerJadeDark,
                modifier = Modifier.size(16.dp),
              )
            }
            val lockLabel = when {
              isDecodingPhoto -> "Scanning photo…"
              uiState is ScannerUiState.Matched -> "Locked · ${(uiState as ScannerUiState.Matched).barcode}"
              uiState is ScannerUiState.LookingUp -> "Reading · ${(uiState as ScannerUiState.LookingUp).barcode}"
              uiState is ScannerUiState.NotFound -> "Locked · ${(uiState as ScannerUiState.NotFound).barcode}"
              else -> "Barcode Detected"
            }
            Text(
              text = lockLabel,
              style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize),
              color = Color.White,
            )
          }
        }
      }

      androidx.compose.animation.AnimatedVisibility(
        visible = showIdleTip && uiState is ScannerUiState.Idle,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(14.dp),
      ) {
        Surface(
          color = ViewfinderVoid.copy(alpha = 0.9f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        ) {
          Text(
            text = "Align barcode in frame, or select a photo or sample below.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      AnimatedContent(
        targetState = uiState,
        transitionSpec = {
          val dur = if (reducedMotion) 0 else 200
          (fadeIn(tween(dur)) + slideInVertically(tween(dur, easing = FastOutSlowInEasing)) { it / 4 })
            .togetherWith(fadeOut(tween(dur / 2)))
        },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth(),
        label = "scannerOverlayCard",
      ) { state ->
        when (state) {
          is ScannerUiState.LookingUp -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
              contentAlignment = Alignment.Center,
            ) {
              Surface(
                color = ViewfinderVoid.copy(alpha = 0.94f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SignalIndigoDark.copy(alpha = 0.6f)),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ReticleAmber, strokeWidth = 2.dp)
                  Column {
                    Text(
                      text = "Looking up ${state.barcode}",
                      style = MaterialTheme.typography.titleSmall,
                      color = Color.White,
                    )
                    Text(
                      text = state.format.label,
                      style = MaterialTheme.typography.labelSmall,
                      color = ViewfinderMuted,
                    )
                  }
                }
              }
            }
          }

          is ScannerUiState.Matched -> {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("scanner_matched_card"),
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  VerificationLabel(statusRaw = state.product.verificationStatus)
                  Text(
                    text = if (state.fromOfflineCache) "Offline Cache" else state.format.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                Text(state.product.name, style = MaterialTheme.typography.titleLarge)
                Text(
                  text = listOfNotNull(
                    state.product.brand.takeIf { it.isNotBlank() },
                    state.barcode,
                  ).joinToString(" · "),
                  style = TabularDataStyle,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  AnimatedOutlinedButton(
                    onClick = { scannerViewModel.resetScanner() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Scan Next")
                  }
                  AnimatedButton(
                    onClick = {
                      val code = state.barcode
                      scannerViewModel.resetScanner()
                      onOpenProductDetail(code)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Inspect Item")
                  }
                }
              }
            }
          }

          is ScannerUiState.NotFound -> {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text("Not Found in Catalog", style = MaterialTheme.typography.titleMedium)
                Text(
                  text = "Barcode ${state.barcode} (${state.format.label}) was saved to History. You can add details or compare web prices.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  AnimatedOutlinedButton(
                    onClick = { scannerViewModel.resetScanner() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Scan Again")
                  }
                  AnimatedButton(
                    onClick = {
                      val code = state.barcode
                      scannerViewModel.resetScanner()
                      onOpenProductDetail(code)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Product")
                  }
                }
              }
            }
          }

          is ScannerUiState.QrUrlDetected -> {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text("QR Product Link", style = MaterialTheme.typography.titleMedium)
                  Text(state.format.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                  text = state.url,
                  style = TabularDataStyle,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  AnimatedOutlinedButton(
                    onClick = { scannerViewModel.resetScanner() },
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Dismiss")
                  }
                  if (!state.embeddedBarcode.isNullOrBlank()) {
                    AnimatedButton(
                      onClick = { scannerViewModel.lookupEmbeddedBarcode(state.embeddedBarcode) },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp),
                    ) {
                      Text("Inspect GTIN")
                    }
                  }
                  AnimatedButton(
                    onClick = {
                      val target = state.url
                      scannerViewModel.resetScanner()
                      runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                      }.onFailure {
                        onShowMessage("No browser available to open URL.")
                      }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Open Link")
                  }
                }
              }
            }
          }

          is ScannerUiState.QrPromoDetected -> {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Promo Code Detected", style = MaterialTheme.typography.titleMedium)
                  }
                  Text(state.format.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = RoundedCornerShape(8.dp),
                ) {
                  Text(
                    text = "Code: ${state.promoCode}",
                    style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  AnimatedOutlinedButton(
                    onClick = { scannerViewModel.resetScanner() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Dismiss")
                  }
                  AnimatedButton(
                    onClick = {
                      val hint = state.campaignHint ?: state.promoCode
                      scannerViewModel.resetScanner()
                      onShowMessage("Promo '$hint' applied to receipt claim.")
                      onOpenRewardsOrPromo(state.campaignHint)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Claim Promo")
                  }
                }
              }
            }
          }

          is ScannerUiState.Error -> {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text("Could Not Read Code", style = MaterialTheme.typography.titleMedium)
                Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  AnimatedOutlinedButton(
                    onClick = { showManualDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Enter Manually")
                  }
                  AnimatedButton(
                    onClick = { scannerViewModel.resetScanner() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text("Try Again")
                  }
                }
              }
            }
          }

          ScannerUiState.Idle -> Unit
        }
      }
    }

    // Bottom Action Bar + Sample Test Barcodes
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AnimatedOutlinedButton(
          onClick = {
            manualError = null
            showManualDialog = true
          },
          modifier = Modifier
            .weight(1f)
            .testTag("scanner_manual_entry_button"),
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
          contentPadding = PaddingValues(vertical = 10.dp),
        ) {
          Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Manual GTIN", color = Color.White)
        }
        AnimatedOutlinedButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("scanner_photo_button"),
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
          contentPadding = PaddingValues(vertical = 10.dp),
        ) {
          Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Scan Photo", color = Color.White)
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Sample:", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
        val sampleSpecs = listOf(
          Triple("3017620422003", "Nutella", SupportedBarcodeFormat.EAN_13),
          Triple("https://world.openfoodfacts.org/product/5449000000996", "QR URL", SupportedBarcodeFormat.QR_CODE),
          Triple("PROMO:BONUS100", "QR Promo", SupportedBarcodeFormat.QR_CODE),
        )
        sampleSpecs.forEach { (code, label, fmt) ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .bounceClickable {
                scannerViewModel.processScannedBarcode(code, fmt, fromCamera = false)
              }
              .testTag("sample_scan_$label"),
            color = ViewfinderSurface,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }
        }
      }
    }
  }

  if (showManualDialog) {
    AlertDialog(
      onDismissRequest = {
        manualError = null
        showManualDialog = false
      },
      title = { Text("Enter Barcode / GTIN") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Supports EAN-13, EAN-8, UPC-A, UPC-E (with Modulo-10 check), Code 128, or QR.",
            style = MaterialTheme.typography.bodySmall,
          )
          OutlinedTextField(
            value = manualCode,
            onValueChange = {
              manualCode = it
              manualError = null
            },
            label = { Text("Barcode value") },
            singleLine = true,
            isError = manualError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("manual_barcode_input"),
          )
          manualError?.let { err ->
            Text(
              text = err,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.testTag("manual_barcode_error"),
            )
          }
        }
      },
      confirmButton = {
        AnimatedButton(
          onClick = {
            val trimmed = manualCode.trim()
            if (trimmed.isEmpty()) {
              manualError = "Enter a barcode or QR value first."
              return@AnimatedButton
            }
            manualError = null
            showManualDialog = false
            scannerViewModel.processScannedBarcode(
              rawValue = trimmed,
              format = SupportedBarcodeFormat.MANUAL,
              fromCamera = false,
            )
          },
          modifier = Modifier.testTag("manual_barcode_submit_button"),
        ) {
          Text("Look Up")
        }
      },
      dismissButton = {
        AnimatedTextButton(
          onClick = {
            manualError = null
            showManualDialog = false
          },
        ) { Text("Cancel") }
      },
    )
  }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraXBarcodeViewfinder(
  lifecycleOwner: androidx.lifecycle.LifecycleOwner,
  scanner: com.google.mlkit.vision.barcode.BarcodeScanner?,
  analysisExecutor: java.util.concurrent.ExecutorService,
  paused: Boolean,
  onCameraBound: (Camera) -> Unit,
  onBarcodeDetected: (String, SupportedBarcodeFormat) -> Unit,
) {
  val context = LocalContext.current
  val previewView = remember {
    PreviewView(context).apply {
      scaleType = PreviewView.ScaleType.FILL_CENTER
      implementationMode = PreviewView.ImplementationMode.PERFORMANCE
    }
  }
  val pausedState = remember { mutableStateOf(paused) }
  pausedState.value = paused
  val latestOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)
  val isProcessingFrame = remember { AtomicBoolean(false) }

  DisposableEffect(lifecycleOwner) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val mainExecutor = ContextCompat.getMainExecutor(context)
    var providerRef: ProcessCameraProvider? = null
    var cameraRef: Camera? = null
    var isDisposed = false
    var lastAnalyzedMs = 0L

    cameraProviderFuture.addListener(
      {
        if (isDisposed) return@addListener
        runCatching {
          val cameraProvider = cameraProviderFuture.get()
          if (isDisposed) {
            runCatching { cameraProvider.unbindAll() }
            return@addListener
          }
          providerRef = cameraProvider
          val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
          }

          val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
              ResolutionStrategy(
                Size(1280, 720),
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
              ),
            )
            .build()

          val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

          imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
            val nowMs = android.os.SystemClock.elapsedRealtime()
            if (isDisposed || pausedState.value || (nowMs - lastAnalyzedMs) < 110L || !isProcessingFrame.compareAndSet(false, true)) {
              imageProxy.close()
              return@setAnalyzer
            }
            val mediaImage = imageProxy.image
            if (mediaImage != null && scanner != null) {
              lastAnalyzedMs = nowMs
              val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
              scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                  if (isDisposed) return@addOnSuccessListener
                  val match = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                  if (match != null) {
                    val value = match.rawValue.orEmpty()
                    val fmt = SupportedBarcodeFormat.fromMlKitFormat(match.format)
                    mainExecutor.execute {
                      if (!isDisposed) {
                        latestOnBarcodeDetected(value, fmt)
                      }
                    }
                  }
                }
                .addOnCompleteListener {
                  isProcessingFrame.set(false)
                  imageProxy.close()
                }
            } else {
              isProcessingFrame.set(false)
              imageProxy.close()
            }
          }

          cameraProvider.unbindAll()
          val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis,
          )
          cameraRef = camera
          onCameraBound(camera)
        }
      },
      mainExecutor,
    )

    onDispose {
      isDisposed = true
      runCatching { cameraRef?.cameraControl?.enableTorch(false) }
      runCatching { providerRef?.unbindAll() }
    }
  }

  AndroidView(
    factory = { previewView },
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun ScanningReticleOverlay(
  isLiveScanning: Boolean,
  isProcessingScan: Boolean,
  detectionLaserProgress: Float,
  detectionRippleProgress: Float,
  isDetectionLocked: Boolean,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "scanningOverlay")

  val sweepPhase = if ((isLiveScanning || isProcessingScan) && !reducedMotion) {
    val phase by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
      label = "harmonicSweepPhase",
    )
    phase
  } else {
    0.25f
  }

  val orbitPhase = if (isProcessingScan && !reducedMotion) {
    val phase by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1150, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
      label = "processingOrbitPhase",
    )
    phase
  } else {
    0f
  }

  val isLocked = isDetectionLocked || detectionRippleProgress > 0f || detectionLaserProgress > 0f

  val activeAccentColor by animateColorAsState(
    targetValue = when {
      isLocked -> LedgerJadeDark
      isProcessingScan -> SignalIndigoDark
      else -> ReticleAmber
    },
    animationSpec = tween(durationMillis = if (reducedMotion) 0 else 200),
    label = "reticleAccentColor",
  )

  val lockInsetFraction by animateFloatAsState(
    targetValue = if (isLocked) 1f else 0f,
    animationSpec = tween(durationMillis = if (reducedMotion) 0 else 180, easing = FastOutSlowInEasing),
    label = "reticleLockInset",
  )

  val scrimPath = remember { Path().apply { fillType = PathFillType.EvenOdd } }
  val reticleOutlinePath = remember { Path() }
  val orbitSegmentPath = remember { Path() }
  val pathMeasure = remember { PathMeasure() }

  Canvas(modifier = modifier) {
    val lockInset = 4.dp.toPx() * lockInsetFraction
    val boxW = (size.width * 0.78f) - (lockInset * 2f)
    val boxH = (size.height * 0.44f) - (lockInset * 2f)
    val left = (size.width - boxW) / 2f
    val top = (size.height - boxH) / 2f
    val right = left + boxW
    val bottom = top + boxH
    val radius = 14.dp.toPx()
    val corner = 24.dp.toPx()

    // Optical cutout scrim: dims exterior while keeping the reticle window crystal clear
    scrimPath.reset()
    scrimPath.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
    scrimPath.addRoundRect(
      RoundRect(
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        cornerRadius = CornerRadius(radius, radius),
      ),
    )
    drawPath(
      path = scrimPath,
      color = ViewfinderVoid.copy(alpha = if (isProcessingScan) 0.56f else 0.42f),
    )

    reticleOutlinePath.reset()
    reticleOutlinePath.addRoundRect(
      RoundRect(
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        cornerRadius = CornerRadius(radius, radius),
      ),
    )

    if (isLocked) {
      val flashAlpha = if (detectionLaserProgress > 0f) {
        (1f - detectionLaserProgress).coerceIn(0f, 1f) * 0.24f
      } else {
        0.16f
      }
      drawPath(
        path = reticleOutlinePath,
        color = LedgerJadeDark.copy(alpha = flashAlpha),
      )
    } else if (isProcessingScan) {
      drawPath(
        path = reticleOutlinePath,
        color = SignalIndigoDark.copy(alpha = 0.08f),
      )
    }

    // Fine reticle glass border
    drawPath(
      path = reticleOutlinePath,
      color = if (isLocked) LedgerJadeDark.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.24f),
      style = Stroke(width = if (isLocked) 2.2.dp.toPx() else 1.dp.toPx()),
    )

    // Smooth dual-segment perimeter orbit while a scan/lookup is actively in progress
    if (isProcessingScan && !isLocked && !reducedMotion) {
      pathMeasure.setPath(reticleOutlinePath, forceClosed = true)
      val totalLength = pathMeasure.length
      if (totalLength > 0f) {
        val segLen = totalLength * 0.22f
        val startA = orbitPhase * totalLength
        val endA = startA + segLen

        orbitSegmentPath.reset()
        if (endA <= totalLength) {
          pathMeasure.getSegment(startA, endA, orbitSegmentPath, true)
        } else {
          pathMeasure.getSegment(startA, totalLength, orbitSegmentPath, true)
          pathMeasure.getSegment(0f, endA - totalLength, orbitSegmentPath, true)
        }
        drawPath(
          path = orbitSegmentPath,
          color = SignalIndigoDark,
          style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
        )

        val startB = ((orbitPhase + 0.5f) % 1f) * totalLength
        val endB = startB + segLen
        orbitSegmentPath.reset()
        if (endB <= totalLength) {
          pathMeasure.getSegment(startB, endB, orbitSegmentPath, true)
        } else {
          pathMeasure.getSegment(startB, totalLength, orbitSegmentPath, true)
          pathMeasure.getSegment(0f, endB - totalLength, orbitSegmentPath, true)
        }
        drawPath(
          path = orbitSegmentPath,
          color = LedgerJadeDark.copy(alpha = 0.9f),
          style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
        )
      }
    }

    // Precision corner brackets
    val strokeW = if (isLocked) 3.8.dp.toPx() else 3.2.dp.toPx()
    val bracketAlpha = if (isLiveScanning && !reducedMotion && !isLocked) {
      0.82f + 0.18f * abs(sin(sweepPhase * 2f * PI.toFloat()))
    } else {
      1f
    }
    val bracketColor = activeAccentColor.copy(alpha = bracketAlpha)
    drawLine(bracketColor, Offset(left, top + radius * 0.35f), Offset(left, top + corner), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(left + radius * 0.35f, top), Offset(left + corner, top), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(right - corner, top), Offset(right - radius * 0.35f, top), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(right, top + radius * 0.35f), Offset(right, top + corner), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(left + radius * 0.35f, bottom), Offset(left + corner, bottom), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(left, bottom - corner), Offset(left, bottom - radius * 0.35f), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(right - corner, bottom), Offset(right - radius * 0.35f, bottom), strokeW, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(right, bottom - corner), Offset(right, bottom - radius * 0.35f), strokeW, cap = StrokeCap.Round)

    // Subtle side alignment ticks
    val tickLen = 7.dp.toPx()
    val midY = top + boxH / 2f
    drawLine(Color.White.copy(alpha = 0.36f), Offset(left, midY), Offset(left + tickLen, midY), 1.4.dp.toPx(), cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.36f), Offset(right - tickLen, midY), Offset(right, midY), 1.4.dp.toPx(), cap = StrokeCap.Round)

    // Smooth harmonic optical laser sweep with directional glow trail
    if ((isLiveScanning || isProcessingScan) && !isLocked) {
      val angle = sweepPhase * 2f * PI.toFloat()
      val yNorm = if (reducedMotion) 0.5f else (0.5f - 0.5f * cos(angle))
      val scanY = top + 12.dp.toPx() + (boxH - 24.dp.toPx()) * yNorm
      val movingDown = sweepPhase < 0.5f
      val speedFactor = if (reducedMotion) 0.4f else abs(sin(angle)).coerceIn(0.25f, 1f)
      val trailHeight = (30.dp.toPx() * speedFactor).coerceAtLeast(10.dp.toPx())

      val beamColor = if (isProcessingScan) SignalIndigoDark else ReticleAmber
      val trailTop = if (movingDown) scanY - trailHeight else scanY
      val trailBottom = if (movingDown) scanY else scanY + trailHeight
      val trailColors = if (movingDown) {
        listOf(Color.Transparent, beamColor.copy(alpha = 0.26f * speedFactor))
      } else {
        listOf(beamColor.copy(alpha = 0.26f * speedFactor), Color.Transparent)
      }

      if (!reducedMotion) {
        drawRect(
          brush = Brush.verticalGradient(
            colors = trailColors,
            startY = trailTop,
            endY = trailBottom,
          ),
          topLeft = Offset(left + 10.dp.toPx(), trailTop),
          size = androidx.compose.ui.geometry.Size(boxW - 20.dp.toPx(), trailHeight),
        )
      }

      // Soft bloom around laser core
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(Color.Transparent, beamColor.copy(alpha = 0.45f), beamColor.copy(alpha = 0.45f), Color.Transparent),
          startX = left + 8.dp.toPx(),
          endX = right - 8.dp.toPx(),
        ),
        start = Offset(left + 8.dp.toPx(), scanY),
        end = Offset(right - 8.dp.toPx(), scanY),
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round,
      )

      // Sharp optical laser core
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(Color.Transparent, beamColor, Color.White, beamColor, Color.Transparent),
          startX = left + 8.dp.toPx(),
          endX = right - 8.dp.toPx(),
        ),
        start = Offset(left + 8.dp.toPx(), scanY),
        end = Offset(right - 8.dp.toPx(), scanY),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    // Rapid detection confirmation beam
    if (detectionLaserProgress > 0f) {
      val sweepFraction = if (detectionLaserProgress <= 0.5f) {
        detectionLaserProgress * 2f
      } else {
        1f - ((detectionLaserProgress - 0.5f) * 2f)
      }.coerceIn(0.06f, 0.94f)
      val lockY = top + (boxH * sweepFraction)
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(Color.Transparent, Color.White, LedgerJadeDark, Color.White, Color.Transparent),
          startX = left + 8.dp.toPx(),
          endX = right - 8.dp.toPx(),
        ),
        start = Offset(left + 8.dp.toPx(), lockY),
        end = Offset(right - 8.dp.toPx(), lockY),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    // Expanding detection lock halo
    if (detectionRippleProgress > 0f) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = minOf(boxW, boxH) * 0.72f
      val outerRadius = maxRadius * detectionRippleProgress
      val outerAlpha = (1f - detectionRippleProgress).coerceIn(0f, 1f) * 0.8f

      drawCircle(
        color = LedgerJadeDark.copy(alpha = outerAlpha * 0.2f),
        radius = outerRadius,
        center = center,
      )
      drawCircle(
        color = LedgerJadeDark.copy(alpha = outerAlpha),
        radius = outerRadius,
        center = center,
        style = Stroke(width = 3.dp.toPx()),
      )
    }
  }
}
