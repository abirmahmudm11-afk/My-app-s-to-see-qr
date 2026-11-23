package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.SupportedBarcodeFormat
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.Emerald400
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.ScannerLookupState
import com.example.ui.viewmodel.ScannerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

private fun mapMlKitFormat(mlKitFormat: Int): SupportedBarcodeFormat = when (mlKitFormat) {
    Barcode.FORMAT_EAN_13 -> SupportedBarcodeFormat.EAN_13
    Barcode.FORMAT_EAN_8 -> SupportedBarcodeFormat.EAN_8
    Barcode.FORMAT_UPC_A -> SupportedBarcodeFormat.UPC_A
    Barcode.FORMAT_UPC_E -> SupportedBarcodeFormat.UPC_E
    Barcode.FORMAT_CODE_128 -> SupportedBarcodeFormat.CODE_128
    Barcode.FORMAT_CODE_39 -> SupportedBarcodeFormat.CODE_39
    Barcode.FORMAT_CODE_93 -> SupportedBarcodeFormat.CODE_93
    Barcode.FORMAT_QR_CODE -> SupportedBarcodeFormat.QR_CODE
    Barcode.FORMAT_DATA_MATRIX -> SupportedBarcodeFormat.DATA_MATRIX
    Barcode.FORMAT_PDF417 -> SupportedBarcodeFormat.PDF417
    Barcode.FORMAT_AZTEC -> SupportedBarcodeFormat.AZTEC
    Barcode.FORMAT_ITF -> SupportedBarcodeFormat.ITF
    Barcode.FORMAT_CODABAR -> SupportedBarcodeFormat.CODABAR
    else -> SupportedBarcodeFormat.CODE_128
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun ScanScreen(
    viewModel: ScannerViewModel,
    reducedMotion: Boolean,
    onOpenProductDetail: (String) -> Unit,
    onNavigateSearchFallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val lookupState by viewModel.lookupState.collectAsStateWithLifecycle()
    val torchEnabled by viewModel.torchEnabled.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraUnavailableError by remember { mutableStateOf<String?>(null) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualBarcodeInput by remember { mutableStateOf("") }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
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
                Barcode.FORMAT_CODABAR
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val first = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                        if (first != null && first.rawValue != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onBarcodeDetected(
                                rawValue = first.rawValue!!,
                                format = mapMlKitFormat(first.format),
                                fromCameraFrame = false
                            )
                        } else {
                            viewModel.reportError("No supported barcode detected in selected image.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("ScanScreen", "Image scan failed", e)
                        viewModel.reportError("Unable to process selected image for barcodes.")
                    }
            } catch (e: Exception) {
                Log.w("ScanScreen", "Unable to read selected image", e)
                viewModel.reportError("Unable to open selected image.")
            }
        }
    }

    // Sync torch control with CameraX
    LaunchedEffect(torchEnabled, boundCamera) {
        try {
            boundCamera?.cameraControl?.enableTorch(torchEnabled)
        } catch (e: Exception) {
            Log.w("ScanScreen", "Torch toggle failed", e)
        }
    }

    // 15-second timeout while actively scanning
    LaunchedEffect(lookupState, hasCameraPermission, cameraUnavailableError) {
        if (hasCameraPermission && cameraUnavailableError == null && lookupState is ScannerLookupState.Scanning) {
            delay(15_000L)
            viewModel.onTimeoutReached()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F0D))
    ) {
        if (hasCameraPermission && cameraUnavailableError == null) {
            val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
            DisposableEffect(Unit) {
                onDispose { analysisExecutor.shutdown() }
            }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && viewModel.lookupState.value is ScannerLookupState.Scanning) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val candidate = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                                            if (candidate?.rawValue != null) {
                                                val accepted = viewModel.onBarcodeDetected(
                                                    rawValue = candidate.rawValue!!,
                                                    format = mapMlKitFormat(candidate.format),
                                                    fromCameraFrame = true
                                                )
                                                if (accepted) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                            boundCamera = camera
                        } catch (e: Exception) {
                            Log.w("ScanScreen", "Camera unavailable", e)
                            cameraUnavailableError = e.localizedMessage ?: "Camera hardware unavailable on this device."
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scan-area Viewfinder Overlay
            ScanViewfinderOverlay(
                isScanning = lookupState is ScannerLookupState.Scanning,
                reducedMotion = reducedMotion,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission Denied or Camera Unavailable Fallback
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFFFAF9F6),
                            modifier = Modifier.size(124.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_scan_hero),
                                contentDescription = "Barcode scanner illustration",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (cameraUnavailableError != null) "Camera Unavailable" else "Enable Smart Scanner",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cameraUnavailableError
                                ?: "Barcodes are decoded locally on-device with ML Kit across 13 retail & 2D formats.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        if (cameraUnavailableError == null) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("grant_camera_permission_button")
                            ) {
                                Text("Enable Live Camera", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        FilledTonalButton(
                            onClick = { showManualDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Enter Barcode Manually")
                        }
                    }
                }
            }
        }

        // Top Controls Bar (Title + Flashlight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.62f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(
                        text = "Barcode Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "13 formats • GS1 check-digit verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald400
                    )
                }
            }
            if (hasCameraPermission && cameraUnavailableError == null) {
                IconButton(
                    onClick = { viewModel.toggleTorch() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (torchEnabled) Emerald400 else Color.Black.copy(alpha = 0.62f),
                        contentColor = if (torchEnabled) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("scanner_torch_button")
                ) {
                    Icon(
                        imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (torchEnabled) "Turn off flashlight" else "Turn on flashlight"
                    )
                }
            }
        }

        // Bottom Controls & Lookup Result Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (val state = lookupState) {
                is ScannerLookupState.Scanning -> {
                    Surface(
                        color = Color.Black.copy(alpha = 0.68f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Align EAN, UPC, Code 128, QR, Data Matrix, PDF417, Aztec, ITF, or Codabar inside the frame.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is ScannerLookupState.ValidatingAndLookingUp -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Looking up ${state.barcode}...", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Format: ${state.formatLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                is ScannerLookupState.Found -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VerificationBadge(statusRaw = state.product.verificationStatus)
                                Text(
                                    text = if (state.fromOfflineCache) "Offline Cache" else state.formatLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.product.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = state.product.brand.ifBlank { "Brand not provided by source" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "GTIN: ${state.product.barcode}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onOpenProductDetail(state.product.barcode) },
                                    modifier = Modifier.weight(1f).testTag("open_scanned_product_button")
                                ) {
                                    Text("View Product & Offers")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.resetScanner() },
                                    modifier = Modifier.testTag("scan_again_button")
                                ) {
                                    Text("Scan Again")
                                }
                            }
                        }
                    }
                }
                is ScannerLookupState.NotFound -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Product Not Found in Feeds", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Validated ${state.formatLabel} (${state.barcode}), but no verified or Open Food Facts record was found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onOpenProductDetail(state.barcode) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Submit Product Info")
                                }
                                OutlinedButton(onClick = { viewModel.resetScanner() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is ScannerLookupState.TimedOut -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Scan Timed Out", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "No barcode detected in 15 seconds. Improve lighting, toggle flashlight, or enter the code manually.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.resetScanner() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry Scan")
                                }
                                OutlinedButton(onClick = { showManualDialog = true }) {
                                    Text("Manual")
                                }
                            }
                        }
                    }
                }
                is ScannerLookupState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Barcode Validation Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetScanner() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }

            // Action Strip: Image Scan, Manual Entry, Search Fallback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("scan_from_image_button")
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Photo", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { showManualDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("manual_barcode_button")
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manual", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = onNavigateSearchFallback,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("scan_search_fallback_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search", maxLines = 1)
                }
            }
        }
    }

    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text("Enter Barcode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Barcodes are validated (including GS1 check digits for EAN-13, EAN-8, UPC-A) before product lookup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = manualBarcodeInput,
                        onValueChange = { manualBarcodeInput = it },
                        label = { Text("Barcode / GTIN (e.g. 3017620422003)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_barcode_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManualDialog = false
                        viewModel.lookupManualBarcode(manualBarcodeInput)
                    },
                    modifier = Modifier.testTag("manual_barcode_submit")
                ) {
                    Text("Verify & Lookup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ScanViewfinderOverlay(
    isScanning: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val lineProgress = if (isScanning && !reducedMotion) {
        val transition = rememberInfiniteTransition(label = "scan_beam")
        transition.animateFloat(
            initialValue = 0.08f,
            targetValue = 0.92f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "beam_y"
        ).value
    } else {
        0.5f
    }

    Canvas(modifier = modifier) {
        val boxWidth = size.width * 0.76f
        val boxHeight = (boxWidth * 0.58f).coerceAtMost(size.height * 0.34f)
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2.2f

        // Subtle dimmed frame border
        drawRoundRect(
            color = Emerald400,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(28f, 28f),
            style = Stroke(width = 5f)
        )

        if (isScanning) {
            val beamY = top + (boxHeight * lineProgress)
            drawLine(
                color = Emerald400.copy(alpha = 0.85f),
                start = Offset(left + 20f, beamY),
                end = Offset(left + boxWidth - 20f, beamY),
                strokeWidth = 4f
            )
        }
    }
}
