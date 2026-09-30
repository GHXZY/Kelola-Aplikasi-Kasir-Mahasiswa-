package com.example.ui.cashier

import android.Manifest
import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.entity.ProductEntity
import com.example.ui.CartSummary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.util.FormatUtils
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

sealed interface ScannerUiState {
    data object Scanning : ScannerUiState
    data class Success(val product: ProductEntity) : ScannerUiState
    data class NotFound(val barcode: String) : ScannerUiState
    data class OutOfStock(val product: ProductEntity) : ScannerUiState
    data class MaxStockReached(val product: ProductEntity, val maxStock: Int) : ScannerUiState
    data object PermissionDenied : ScannerUiState
    data class CameraError(val message: String) : ScannerUiState
}

@Composable
fun BarcodeScannerDialog(
    products: List<ProductEntity>,
    cart: CartSummary,
    onProductScanned: (ProductEntity) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf<ScannerUiState>(ScannerUiState.Scanning) }

    // Cek permission kamera
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
            uiState = ScannerUiState.Scanning
        } else {
            uiState = ScannerUiState.PermissionDenied
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
                is ScannerUiState.Scanning -> {
                    if (hasCameraPermission) {
                        CameraScanningView(
                            onBarcodeDetected = { rawBarcode ->
                                val clean = rawBarcode.trim()
                                val foundProduct = products.find {
                                    it.barcode != null && it.barcode.equals(clean, ignoreCase = true)
                                }

                                if (foundProduct == null) {
                                    uiState = ScannerUiState.NotFound(clean)
                                } else {
                                    val currentInCart = cart.items.find { it.product.id == foundProduct.id }?.quantity ?: 0
                                    if (foundProduct.stock <= 0) {
                                        uiState = ScannerUiState.OutOfStock(foundProduct)
                                    } else if (currentInCart >= foundProduct.stock) {
                                        uiState = ScannerUiState.MaxStockReached(foundProduct, foundProduct.stock)
                                    } else {
                                        onProductScanned(foundProduct)
                                        uiState = ScannerUiState.Success(foundProduct)
                                    }
                                }
                            },
                            onError = { err ->
                                uiState = ScannerUiState.CameraError(err)
                            },
                            onClose = onDismissRequest
                        )
                    } else {
                        // Tampilkan loading izin
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }

                is ScannerUiState.Success -> {
                    ScannerResultCard(
                        icon = Icons.Default.CheckCircle,
                        iconTint = SuccessGreen,
                        iconBg = SuccessContainer,
                        title = "Produk Berhasil Ditambahkan",
                        description = "${state.product.name} telah dimasukkan ke dalam keranjang.",
                        detailsContent = {
                            Surface(
                                shape = KelolaRadius.ShapeSmall,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = state.product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Harga: ${FormatUtils.formatRupiah(state.product.sellingPrice)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "Tersedia: ${state.product.stock} ${state.product.unit}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        primaryButtonText = "Scan Barang Lain",
                        onPrimaryClick = { uiState = ScannerUiState.Scanning },
                        secondaryButtonText = "Selesai",
                        onSecondaryClick = onDismissRequest
                    )
                }

                is ScannerUiState.NotFound -> {
                    ScannerResultCard(
                        icon = Icons.Default.SearchOff,
                        iconTint = DangerRed,
                        iconBg = DangerContainer,
                        title = "Produk Tidak Ditemukan",
                        description = "Barcode '${state.barcode}' belum terdaftar di database produk.",
                        primaryButtonText = "Scan Lagi",
                        onPrimaryClick = { uiState = ScannerUiState.Scanning },
                        secondaryButtonText = "Tutup",
                        onSecondaryClick = onDismissRequest
                    )
                }

                is ScannerUiState.OutOfStock -> {
                    ScannerResultCard(
                        icon = Icons.Default.Inventory2,
                        iconTint = DangerRed,
                        iconBg = DangerContainer,
                        title = "Stok Produk Habis",
                        description = "Produk '${state.product.name}' saat ini memiliki stok 0 dan tidak dapat ditambahkan ke transaksi.",
                        primaryButtonText = "Scan Produk Lain",
                        onPrimaryClick = { uiState = ScannerUiState.Scanning },
                        secondaryButtonText = "Tutup",
                        onSecondaryClick = onDismissRequest
                    )
                }

                is ScannerUiState.MaxStockReached -> {
                    ScannerResultCard(
                        icon = Icons.Default.ShoppingCart,
                        iconTint = WarningAmber,
                        iconBg = WarningContainer,
                        title = "Batas Stok Tercapai",
                        description = "Jumlah '${state.product.name}' di keranjang belanja sudah mencapai stok maksimum (${state.maxStock} ${state.product.unit}).",
                        primaryButtonText = "Scan Produk Lain",
                        onPrimaryClick = { uiState = ScannerUiState.Scanning },
                        secondaryButtonText = "Tutup",
                        onSecondaryClick = onDismissRequest
                    )
                }

                is ScannerUiState.PermissionDenied -> {
                    ScannerResultCard(
                        icon = Icons.Default.VideocamOff,
                        iconTint = DangerRed,
                        iconBg = DangerContainer,
                        title = "Kamera Tidak Dapat Digunakan",
                        description = "Berikan izin kamera untuk menggunakan fitur Scan Barcode kasir.",
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

                is ScannerUiState.CameraError -> {
                    ScannerResultCard(
                        icon = Icons.Default.ErrorOutline,
                        iconTint = DangerRed,
                        iconBg = DangerContainer,
                        title = "Kamera Gagal Dimuat",
                        description = state.message,
                        primaryButtonText = "Coba Lagi",
                        onPrimaryClick = { uiState = ScannerUiState.Scanning },
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
private fun CameraScanningView(
    onBarcodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isDetectionLocked by remember { mutableStateOf(false) }

    // ML Kit Barcode Scanner Client
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

    // Camera Executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Clean up lifecycle
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
                                                    isDetectionLocked = true
                                                    onBarcodeDetected(value)
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        // Abaikan frame yang gagal saat scanning kontinu
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
        ScannerOverlay(
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
private fun ScannerOverlay(
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onClose: () -> Unit
) {
    // Animasi garis pemindai (scanline)
    val infiniteTransition = rememberInfiniteTransition(label = "ScanlineAnim")
    val scanlineOffset by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanlineOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = onToggleTorch) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = if (isTorchOn) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "Scan Barcode Produk",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Scanner",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Viewfinder Reticle Frame
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            // Animasi Garis Laser Scanline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = scanlineOffset.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
            )
        }

        // Bottom Instruction
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Arahkan kamera tepat ke barcode produk",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ScannerResultCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    detailsContent: (@Composable () -> Unit)? = null,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryClick: () -> Unit
) {
    Surface(
        shape = KelolaRadius.ShapeCard,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
            .testTag("scanner_result_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (detailsContent != null) {
                detailsContent()
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Button(
                onClick = onPrimaryClick,
                shape = KelolaRadius.ShapeInput,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("button_scanner_primary_action")
            ) {
                Text(
                    text = primaryButtonText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            TextButton(
                onClick = onSecondaryClick,
                shape = KelolaRadius.ShapeInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("button_scanner_secondary_action")
            ) {
                Text(
                    text = secondaryButtonText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}
