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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScanContext
import com.example.domain.SupportedBarcodeFormat
import com.example.ui.components.BarcodeFeedbackPlayer
import com.example.ui.components.VerificationLabel
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.ObsidianDivider
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianMutedText
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.viewmodel.ScannerUiState
import com.example.ui.viewmodel.ScannerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

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
  var manualCode by remember { mutableStateOf(scanContext?.barcodeValue ?: "") }

  val feedbackPlayer = remember { BarcodeFeedbackPlayer() }
  val rippleProgress = remember { Animatable(0f) }

  LaunchedEffect(detectionPulseSeq) {
    if (detectionPulseSeq > 0L) {
      feedbackPlayer.playSuccessBeep()
      runCatching { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
      if (!reducedMotion) {
        rippleProgress.snapTo(0.01f)
        rippleProgress.animateTo(
          targetValue = 1f,
          animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        )
        rippleProgress.snapTo(0f)
      }
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
  val mlKitScanner = remember { BarcodeScanning.getClient(barcodeOptions) }
  val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

  // Google Play Policy compliant zero-permission Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        val image = InputImage.fromFilePath(context, uri)
        mlKitScanner.process(image)
          .addOnSuccessListener { barcodes ->
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
            onShowMessage("Could not decode barcode from image.")
          }
      }.onFailure {
        onShowMessage("Unable to open image file.")
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      feedbackPlayer.release()
      runCatching { mlKitScanner.close() }
      runCatching { analysisExecutor.shutdown() }
    }
  }

  // Non-blocking tip after 12s of idle scanning instead of interrupting the camera with an error card
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
      .background(DeepObsidian),
  ) {
    // Top Viewfinder Controls + Re-scan Context Banner
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(
            text = "LIVE BARCODE ENGINE",
            style = MaterialTheme.typography.labelSmall,
            color = ElectricLime,
          )
          Text(
            text = "13 Formats • Instant Feedback",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (hasFlashUnit) {
            IconButton(
              onClick = {
                val next = !torchEnabled
                torchEnabled = next
                runCatching { boundCamera?.cameraControl?.enableTorch(next) }
              },
              modifier = Modifier.testTag("scanner_torch_button"),
            ) {
              Icon(
                imageVector = if (torchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                contentDescription = "Toggle Flash",
                tint = if (torchEnabled) ElectricLime else Color.White,
              )
            }
          }
          IconButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
              )
            },
            modifier = Modifier.testTag("scanner_photo_icon_button"),
          ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Scan from photo", tint = Color.White)
          }
          IconButton(
            onClick = onOpenHistory,
            modifier = Modifier.testTag("scanner_history_button"),
          ) {
            Icon(Icons.Rounded.History, contentDescription = "Scan History", tint = Color.White)
          }
          IconButton(
            onClick = { showManualDialog = true },
            modifier = Modifier.testTag("scanner_manual_icon_button"),
          ) {
            Icon(Icons.Rounded.Edit, contentDescription = "Manual GTIN entry", tint = ElectricLime)
          }
        }
      }

      if (scanContext != null) {
        Surface(
          color = ObsidianElevated,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.45f)),
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
                text = "RE-SCAN CONTEXT • ${scanContext.barcodeFormat.ifBlank { "GTIN" }}",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricLime,
              )
              Text(
                text = listOfNotNull(
                  scanContext.productName.takeIf { it.isNotBlank() },
                  scanContext.brand.takeIf { it.isNotBlank() },
                  scanContext.barcodeValue,
                ).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            IconButton(onClick = onClearScanContext, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Rounded.Close, contentDescription = "Clear re-scan context", tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    // Main Camera Viewfinder Area
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(Color(0xFF0A100E)),
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
            tint = ElectricLime,
            modifier = Modifier.size(44.dp),
          )
          Text(
            text = "Camera Access Needed",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
          )
          Text(
            text = "Allow camera permission to scan retail barcodes live, or choose a photo / enter a GTIN manually below.",
            style = MaterialTheme.typography.bodySmall,
            color = ObsidianMutedText,
          )
          Button(
            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricLime,
              contentColor = ObsidianOnLime,
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("enable_camera_button"),
          ) {
            Text("Enable Camera")
          }
        }
      }

      // Animated Reticle, Scanning Sweep & Detection Ripple Overlay
      ScanningReticleOverlay(
        isActive = uiState is ScannerUiState.Idle,
        detectionRippleProgress = rippleProgress.value,
        reducedMotion = reducedMotion,
        modifier = Modifier.fillMaxSize(),
      )

      // Subtle non-blocking idle hint pill at the bottom of the viewfinder
      androidx.compose.animation.AnimatedVisibility(
        visible = showIdleTip && uiState is ScannerUiState.Idle,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(14.dp),
      ) {
        Surface(
          color = DeepObsidian.copy(alpha = 0.88f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.4f)),
        ) {
          Text(
            text = "Hold barcode steady in the reticle, or pick a photo / sample below.",
            style = MaterialTheme.typography.bodySmall,
            color = EmeraldSoftTint,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
          )
        }
      }

      // Overlay State Cards (LookingUp, Matched, NotFound, QrUrlDetected, QrPromoDetected, Error)
      when (val state = uiState) {
        is ScannerUiState.LookingUp -> {
          Surface(
            color = DeepObsidian.copy(alpha = 0.94f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, ElectricLime),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ElectricLime, strokeWidth = 2.dp)
              Column {
                Text("Looking up ${state.barcode}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("${state.format.label} • Validated format", style = MaterialTheme.typography.labelSmall, color = ElectricLime)
              }
            }
          }
        }
        is ScannerUiState.Matched -> {
          Card(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(14.dp)
              .testTag("scanner_matched_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                  color = EmeraldTrust,
                )
              }
              Text(state.product.name, style = MaterialTheme.typography.titleLarge)
              Text(
                listOfNotNull(
                  state.product.brand.takeIf { it.isNotBlank() },
                  state.barcode,
                ).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = { scannerViewModel.resetScanner() },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Scan Next")
                }
                Button(
                  onClick = {
                    val code = state.barcode
                    scannerViewModel.resetScanner()
                    onOpenProductDetail(code)
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Open Details")
                }
              }
            }
          }
        }
        is ScannerUiState.NotFound -> {
          Card(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text("Product Not Found in Catalog", style = MaterialTheme.typography.titleMedium)
              Text(
                "Barcode ${state.barcode} (${state.format.label}) was saved to your History but not returned by Open Food Facts or merchant records.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = { scannerViewModel.resetScanner() },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Retry Scan")
                }
                Button(
                  onClick = {
                    val code = state.barcode
                    scannerViewModel.resetScanner()
                    onOpenProductDetail(code)
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
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
          Card(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text("QR Product URL Detected", style = MaterialTheme.typography.titleMedium)
                Text(state.format.label, style = MaterialTheme.typography.labelSmall, color = EmeraldTrust)
              }
              Text(
                state.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = { scannerViewModel.resetScanner() },
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Scan Next")
                }
                if (!state.embeddedBarcode.isNullOrBlank()) {
                  Button(
                    onClick = { scannerViewModel.lookupEmbeddedBarcode(state.embeddedBarcode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                  ) {
                    Text("Inspect GTIN")
                  }
                }
                Button(
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
                  shape = RoundedCornerShape(12.dp),
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
          Card(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                  Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = EmeraldTrust, modifier = Modifier.size(18.dp))
                  Text("QR Promo Code Detected", style = MaterialTheme.typography.titleMedium)
                }
                Text(state.format.label, style = MaterialTheme.typography.labelSmall, color = EmeraldTrust)
              }
              Surface(
                color = EmeraldSoftTint,
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(
                  text = "Code: ${state.promoCode}",
                  style = MaterialTheme.typography.titleMedium,
                  color = DeepObsidian,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
              }
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = { scannerViewModel.resetScanner() },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Scan Next")
                }
                Button(
                  onClick = {
                    val hint = state.campaignHint ?: state.promoCode
                    scannerViewModel.resetScanner()
                    onShowMessage("Promo '$hint' applied to receipt claim.")
                    onOpenRewardsOrPromo(state.campaignHint)
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Claim Promo")
                }
              }
            }
          }
        }
        is ScannerUiState.Error -> {
          Card(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text("Scanner Notice", style = MaterialTheme.typography.titleMedium)
              Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = { showManualDialog = true },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Enter Manually")
                }
                Button(
                  onClick = { scannerViewModel.resetScanner() },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
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

    // Quick Manual / Photo / Sample Barcode Bar
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
        OutlinedButton(
          onClick = { showManualDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("scanner_manual_entry_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, ObsidianDivider),
          contentPadding = PaddingValues(vertical = 10.dp),
        ) {
          Icon(Icons.Rounded.Edit, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Manual GTIN", color = Color.White)
        }
        OutlinedButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("scanner_photo_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, ObsidianDivider),
          contentPadding = PaddingValues(vertical = 10.dp),
        ) {
          Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Scan Photo", color = Color.White)
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Sample:", style = MaterialTheme.typography.labelSmall, color = ObsidianMutedText)
        listOf(
          Triple("3017620422003", "Nutella", SupportedBarcodeFormat.EAN_13),
          Triple("https://world.openfoodfacts.org/product/5449000000996", "QR URL", SupportedBarcodeFormat.QR_CODE),
          Triple("PROMO:BONUS100", "QR Promo", SupportedBarcodeFormat.QR_CODE),
        ).forEach { (code, label, fmt) ->
          Surface(
            modifier = Modifier
              .clickable {
                scannerViewModel.processScannedBarcode(code, fmt, fromCamera = false)
              }
              .testTag("sample_scan_$label"),
            color = ObsidianElevated,
            shape = RoundedCornerShape(8.dp),
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = ElectricLime,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            )
          }
        }
      }
    }
  }

  if (showManualDialog) {
    AlertDialog(
      onDismissRequest = { showManualDialog = false },
      title = { Text("Enter Barcode / GTIN") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Supports EAN-13, EAN-8, UPC-A, UPC-E (with GS1 Modulo-10 validation), Code 128, or QR.",
            style = MaterialTheme.typography.bodySmall,
          )
          OutlinedTextField(
            value = manualCode,
            onValueChange = { manualCode = it },
            label = { Text("Barcode value") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showManualDialog = false
            scannerViewModel.processScannedBarcode(
              rawValue = manualCode,
              format = SupportedBarcodeFormat.MANUAL,
              fromCamera = false,
            )
          },
        ) {
          Text("Lookup")
        }
      },
      dismissButton = {
        TextButton(onClick = { showManualDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraXBarcodeViewfinder(
  lifecycleOwner: androidx.lifecycle.LifecycleOwner,
  scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
  analysisExecutor: java.util.concurrent.ExecutorService,
  paused: Boolean,
  onCameraBound: (Camera) -> Unit,
  onBarcodeDetected: (String, SupportedBarcodeFormat) -> Unit,
) {
  val context = LocalContext.current
  val previewView = remember {
    PreviewView(context).apply {
      scaleType = PreviewView.ScaleType.FILL_CENTER
      implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
  }
  val pausedState = remember { mutableStateOf(paused) }
  pausedState.value = paused

  DisposableEffect(lifecycleOwner) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val mainExecutor = ContextCompat.getMainExecutor(context)
    var providerRef: ProcessCameraProvider? = null

    cameraProviderFuture.addListener(
      {
        runCatching {
          val cameraProvider = cameraProviderFuture.get()
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
            if (pausedState.value) {
              imageProxy.close()
              return@setAnalyzer
            }
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
              val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
              scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                  val match = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                  if (match != null) {
                    val value = match.rawValue.orEmpty()
                    val fmt = SupportedBarcodeFormat.fromMlKitFormat(match.format)
                    mainExecutor.execute { onBarcodeDetected(value, fmt) }
                  }
                }
                .addOnCompleteListener { imageProxy.close() }
            } else {
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
          onCameraBound(camera)
        }
      },
      mainExecutor,
    )

    onDispose {
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
  isActive: Boolean,
  detectionRippleProgress: Float,
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val scanProgress = if (isActive && !reducedMotion) {
    val transition = rememberInfiniteTransition(label = "scanLaser")
    val anim by transition.animateFloat(
      initialValue = 0.06f,
      targetValue = 0.94f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1900, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "laserY",
    )
    anim
  } else {
    0.5f
  }

  Canvas(modifier = modifier) {
    val boxW = size.width * 0.76f
    val boxH = size.height * 0.44f
    val left = (size.width - boxW) / 2f
    val top = (size.height - boxH) / 2f
    val right = left + boxW
    val bottom = top + boxH
    val corner = 24.dp.toPx()

    // Subtle obsidian scrim outside scan frame
    drawRect(color = Color(0xFF060A09).copy(alpha = 0.28f))

    // Reticle frame border
    val isLocked = detectionRippleProgress > 0f
    drawRect(
      color = if (isLocked) ElectricLime.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.28f),
      topLeft = Offset(left, top),
      size = androidx.compose.ui.geometry.Size(boxW, boxH),
      style = Stroke(width = if (isLocked) 2.5.dp.toPx() else 1.5.dp.toPx()),
    )

    // Electric Lime corner brackets
    val strokeW = 3.5.dp.toPx()
    val bracketColor = ElectricLime
    // Top-left
    drawLine(bracketColor, Offset(left, top), Offset(left + corner, top), strokeW)
    drawLine(bracketColor, Offset(left, top), Offset(left, top + corner), strokeW)
    // Top-right
    drawLine(bracketColor, Offset(right - corner, top), Offset(right, top), strokeW)
    drawLine(bracketColor, Offset(right, top), Offset(right, top + corner), strokeW)
    // Bottom-left
    drawLine(bracketColor, Offset(left, bottom), Offset(left + corner, bottom), strokeW)
    drawLine(bracketColor, Offset(left, bottom - corner), Offset(left, bottom), strokeW)
    // Bottom-right
    drawLine(bracketColor, Offset(right - corner, bottom), Offset(right, bottom), strokeW)
    drawLine(bracketColor, Offset(right, bottom - corner), Offset(right, bottom), strokeW)

    // Subtle animated scanning laser line inside the camera frame
    if (isActive) {
      val lineY = top + (boxH * scanProgress)
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color.Transparent, ElectricLime.copy(alpha = 0.22f), Color.Transparent),
          startY = lineY - 14.dp.toPx(),
          endY = lineY + 14.dp.toPx(),
        ),
        topLeft = Offset(left + 6.dp.toPx(), lineY - 14.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(boxW - 12.dp.toPx(), 28.dp.toPx()),
      )
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(Color.Transparent, ElectricLime, Color.Transparent),
          startX = left + 8.dp.toPx(),
          endX = right - 8.dp.toPx(),
        ),
        start = Offset(left + 8.dp.toPx(), lineY),
        end = Offset(right - 8.dp.toPx(), lineY),
        strokeWidth = 2.5.dp.toPx(),
      )
    }

    // Detection ripple animation expanding outward on barcode lock
    if (detectionRippleProgress > 0f) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = minOf(boxW, boxH) * 0.72f
      val outerRadius = maxRadius * detectionRippleProgress
      val innerRadius = maxRadius * (detectionRippleProgress * 0.65f)
      val outerAlpha = (1f - detectionRippleProgress).coerceIn(0f, 1f) * 0.78f
      val innerAlpha = (1f - detectionRippleProgress * 0.7f).coerceIn(0f, 1f) * 0.55f

      drawCircle(
        color = ElectricLime.copy(alpha = outerAlpha * 0.2f),
        radius = outerRadius,
        center = center,
      )
      drawCircle(
        color = ElectricLime.copy(alpha = outerAlpha),
        radius = outerRadius,
        center = center,
        style = Stroke(width = 3.dp.toPx()),
      )
      if (innerRadius > 4f) {
        drawCircle(
          color = EmeraldSoftTint.copy(alpha = innerAlpha),
          radius = innerRadius,
          center = center,
          style = Stroke(width = 2.dp.toPx()),
        )
      }
    }
  }
}
