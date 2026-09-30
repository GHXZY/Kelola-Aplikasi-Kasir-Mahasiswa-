package com.example.ui.products

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private sealed interface ProductScannerUiState {
    data object Scanning : ProductScannerUiState
    data object PermissionDenied : ProductScannerUiState
    data class CameraError(val message: String) : ProductScannerUiState
}

/**
 * ProductBarcodeScannerDialog:
 * Scanner modal/overlay untuk halaman Tambah / Edit Produk.
 * Menggunakan CameraX & Google ML Kit untuk mendeteksi barcode fisik (EAN-13, EAN-8, UPC, Code 128, QR).
 * Menghentikan stream kamera dan melepaskan resource segera setelah barcode terbaca atau saat dialog ditutup.
 */
@Composable
fun ProductBarcodeScannerDialog(
    onBarcodeScanned: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf<ProductScannerUiState>(ProductScannerUiState.Scanning) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            uiState = ProductScannerUiState.Scanning
        } else {
            uiState = ProductScannerUiState.PermissionDenied
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ProductScannerUiState.Scanning -> {
                    if (hasCameraPermission) {
                        ProductCameraScanningView(
                            onBarcodeDetected = { rawBarcode ->
                                val clean = rawBarcode.trim()
                                if (clean.isNotBlank()) {
                                    onBarcodeScanned(clean)
                                }
                            },
                            onError = { err ->
                                uiState = ProductScannerUiState.CameraError(err)
                            },
                            onClose = onDismissRequest
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }

                is ProductScannerUiState.PermissionDenied -> {
                    ProductScannerErrorCard(
                        icon = Icons.Default.VideocamOff,
                        iconTint = DangerRed,
                        iconBg = DangerContainer,
                        title = "Kamera Tidak Dapat Digunakan",
                        description = "Berikan izin kamera untuk menggunakan fitur Scan Barcode pada Tambah Produk.",
                        primaryButtonText = "Buka Pengaturan",
                        onPrimaryClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        secondaryButtonText = "Tutup",
                        onSecondaryClick = onDismissRequest
                    )
                }

                is ProductScannerUiState.CameraError -> {
                    ProductScannerErrorCard(
                        icon = Icons.Default.ErrorOutline,
                        iconTint = DangerRed,
                        iconBg = DangerContainer,
                        title = "Kamera Gagal Dimuat",
                        description = state.message,
                        primaryButtonText = "Coba Lagi",
                        onPrimaryClick = { uiState = ProductScannerUiState.Scanning },
                        secondaryButtonText = "Tutup",
                        onSecondaryClick = onDismissRequest
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun ProductCameraScanningView(
    onBarcodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isDetectionLocked by remember { mutableStateOf(false) }

    // ML Kit Barcode Scanner Client mendukung EAN-13, EAN-8, UPC-A, UPC-E, CODE-128, dan QR_CODE
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Clean up camera & resources saat scanner ditutup atau berpindah halaman
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                camera?.cameraControl?.enableTorch(false)
            } catch (_: Exception) {}
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !isDetectionLocked) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty() && !isDetectionLocked) {
                                            val firstBarcode = barcodes.firstOrNull { b ->
                                                !b.rawValue.isNullOrBlank() || !b.displayValue.isNullOrBlank()
                                            }
                                            if (firstBarcode != null) {
                                                val value = firstBarcode.rawValue ?: firstBarcode.displayValue
                                                if (!value.isNullOrBlank()) {
                                                    // Kunci scanning agar tidak ada pembacaan ganda dalam satu sesi
                                                    isDetectionLocked = true
                                                    onBarcodeDetected(value)
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        // Ignore continuous frame failures
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        onError("Gagal menginisialisasi kamera: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Viewfinder
        ProductScannerOverlay(
            isTorchOn = isTorchOn,
            onToggleTorch = {
                val newTorch = !isTorchOn
                isTorchOn = newTorch
                try {
                    camera?.cameraControl?.enableTorch(newTorch)
                } catch (_: Exception) {}
            },
            onClose = onClose
        )
    }
}

@Composable
private fun ProductScannerOverlay(
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onClose: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ProductScanlineAnim")
    val scanlineOffset by infiniteTransition.animateFloat(
        initialValue = -75f,
        targetValue = 75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ProductScanlineOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Judul & Tombol Tutup (X)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Scan Barcode Produk",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Arahkan kamera ke barcode kemasan",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .testTag("button_close_product_scanner")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup scanner",
                    tint = Color.White
                )
            }
        }

        // Center Viewfinder: Reticle Box dengan Animasi Garis Pemindai
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 180.dp)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    RoundedCornerShape(16.dp)
                )
                .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Garis pemindai merah laser yang bergerak
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(2.dp)
                    .offset(y = scanlineOffset.dp)
                    .background(Color.Red.copy(alpha = 0.85f))
            )
        }

        // Bottom Controls: Torch Toggle & Format Hint
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Surface(
                onClick = onToggleTorch,
                shape = RoundedCornerShape(24.dp),
                color = if (isTorchOn) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                        contentDescription = if (isTorchOn) "Matikan Flash" else "Nyalakan Flash",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isTorchOn) "Matikan Flash" else "Nyalakan Flash",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "Mendukung EAN-13, EAN-8, UPC, Code 128",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProductScannerErrorCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryClick: () -> Unit
) {
    Surface(
        shape = KelolaRadius.ShapeMedium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = KelolaRadius.ShapeSmall,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = primaryButtonText, fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = secondaryButtonText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
