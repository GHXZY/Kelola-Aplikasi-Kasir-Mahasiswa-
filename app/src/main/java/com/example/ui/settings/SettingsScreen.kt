package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import com.example.ui.BankAccount

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Crop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import com.example.ui.theme.AdaptiveContainer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.KelolaLogoBadge
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils
import java.io.File

/**
 * SettingsScreen:
 * Laman penuh mandiri (bukan popup/dialog) untuk mengelola pengaturan usaha,
 * metode pembayaran QRIS, modal awal, format struk, tema, dan pencadangan database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentBusinessName: String,
    currentAddress: String,
    currentPhone: String,
    currentReceiptFooter: String,
    defaultPaymentMethod: String,
    qrisImagePath: String,
    qrisMerchantName: String,
    openingCapital: Long,
    previousSales: Long,
    previousSalesDate: String,
    previousSalesNote: String,
    themeMode: String,
    colorTheme: String = "DEFAULT",
    viewportWidth: String = "412dp",
    onSaveViewportWidth: (String) -> Unit = {},
    bankAccounts: List<BankAccount> = emptyList(),
    onAddBankAccount: (bankName: String, accountName: String, accountNumber: String) -> Unit = { _, _, _ -> },
    onRemoveBankAccount: (String) -> Unit = {},
    onSaveBusinessInfo: (name: String, address: String, phone: String) -> Unit,
    onSaveDefaultPaymentMethod: (String) -> Unit,
    onUploadQris: (Uri) -> Unit,
    onSaveCroppedQris: (Bitmap) -> Unit = {},
    onDeleteQris: () -> Unit,
    onSaveOpeningCapital: (Long) -> Unit,
    onSavePreviousSales: (amount: Long, date: String, note: String) -> Unit,
    onSaveReceiptFooter: (String) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateColorTheme: (String) -> Unit = {},
    onExportBackup: () -> Unit,
    onResetSettingsOnly: () -> Unit,
    onResetAllData: () -> Unit,
    onPreviewOpeningScreen: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var businessNameInput by remember { mutableStateOf(currentBusinessName) }
    var addressInput by remember { mutableStateOf(currentAddress) }
    var phoneInput by remember { mutableStateOf(currentPhone) }
    var receiptFooterInput by remember { mutableStateOf(currentReceiptFooter) }

    var openingCapitalInput by remember { mutableStateOf(if (openingCapital > 0L) FormatUtils.formatNumberWithDots(openingCapital) else "") }
    var previousSalesInput by remember { mutableStateOf(if (previousSales > 0L) FormatUtils.formatNumberWithDots(previousSales) else "") }
    var previousSalesDateInput by remember { mutableStateOf(previousSalesDate) }
    var previousSalesNoteInput by remember { mutableStateOf(previousSalesNote) }

    val context = LocalContext.current
    var rawBitmapForCrop by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    var showDeleteQrisConfirm by remember { mutableStateOf(false) }
    var showAddBankDialog by remember { mutableStateOf(false) }
    var newBankName by remember { mutableStateOf("") }
    var newAccountName by remember { mutableStateOf("") }
    var newAccountNumber by remember { mutableStateOf("") }
    var bankInputError by remember { mutableStateOf(false) }
    var showResetSettingsConfirm by remember { mutableStateOf(false) }
    var showResetAllDataConfirm by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var successMessage by remember { mutableStateOf<String?>(null) }
    var hideJob by remember { mutableStateOf<Job?>(null) }

    fun showSuccess(msg: String) {
        hideJob?.cancel()
        successMessage = msg
        hideJob = coroutineScope.launch {
            delay(2200)
            if (successMessage == msg) {
                successMessage = null
            }
        }
    }


    // Photo picker for QRIS
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val bmp = context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                    if (bmp != null) {
                        rawBitmapForCrop = bmp
                        showCropDialog = true
                    } else {
                        onUploadQris(uri)
                    }
                } catch (_: Exception) {
                    onUploadQris(uri)
                }
            }
        }
    )

    // Load QRIS Bitmap
    val qrisBitmap = remember(qrisImagePath) {
        if (qrisImagePath.isNotBlank()) {
            val file = File(qrisImagePath)
            if (file.exists() && file.length() > 0) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else null
        } else null
    }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        focusedPrefixColor = MaterialTheme.colorScheme.onSurface,
        unfocusedPrefixColor = MaterialTheme.colorScheme.onSurface
    )

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .statusBarsPadding()
                            .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = KelolaSpacing.Space3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(KelolaSpacing.MinTouchTarget)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("button_back_from_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Beranda",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Pengaturan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Kelola data usaha & preferensi kasir",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        KelolaLogoBadge(
                            size = 38.dp,
                            iconSize = 22.dp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AdaptiveContainer(maxWidth = 760.dp) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = KelolaSpacing.ScreenMargin),
                verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
            ) {
            item {
                Spacer(modifier = Modifier.height(KelolaSpacing.Space2))
            }

            // =============================================================
            // 1. IDENTITAS USAHA
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "IDENTITAS USAHA & TOKO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        OutlinedTextField(
                            value = businessNameInput,
                            onValueChange = { businessNameInput = it },
                            label = { Text("Nama Usaha / Toko") },
                            placeholder = { Text("Misal: Warung Kelola Berkah") },
                            shape = KelolaRadius.ShapeInput,
                            colors = inputColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_business_name")
                        )

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Alamat Usaha (Opsional)") },
                            placeholder = { Text("Misal: Jl. Raya Pasar Baru No. 12") },
                            shape = KelolaRadius.ShapeInput,
                            colors = inputColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_business_address")
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Nomor Telepon / WhatsApp (Opsional)") },
                            placeholder = { Text("Misal: 0812-3456-7890") },
                            shape = KelolaRadius.ShapeInput,
                            colors = inputColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_business_phone")
                        )

                        Button(
                            onClick = {
                                onSaveBusinessInfo(businessNameInput, addressInput, phoneInput)
                                showSuccess("Informasi usaha berhasil disimpan!")
                            },
                            shape = KelolaRadius.ShapeInput,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_save_business_info")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text("Simpan Identitas Usaha", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = onPreviewOpeningScreen,
                            shape = KelolaRadius.ShapeInput,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("button_preview_opening_screen")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text("Lihat Layar Pembuka (Opening Screen)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // =============================================================
            // 2. PEMBAYARAN & QRIS
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "METODE PEMBAYARAN & QRIS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Default Payment Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Metode Pembayaran Bawaan Kasir",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Metode ini langsung terpilih saat kasir membuka dialog pembayaran.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(listOf("Tunai", "QRIS", "Transfer", "E-Wallet")) { method ->
                                    val isSelected = defaultPaymentMethod.equals(method, ignoreCase = true) || (method == "E-Wallet" && defaultPaymentMethod.equals("Emoney", ignoreCase = true))
                                    val methodIcon = when (method) {
                                        "Tunai" -> Icons.Default.LocalAtm
                                        "QRIS" -> Icons.Default.QrCode
                                        "Transfer" -> Icons.Default.AccountBalance
                                        "E-Wallet" -> Icons.Default.CreditCard
                                        else -> Icons.Default.Payments
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onSaveDefaultPaymentMethod(method)
                                            showSuccess("Metode utama diubah ke $method")
                                        },
                                        shape = KelolaRadius.ShapeSmall,
                                        label = {
                                            Text(
                                                text = method,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = methodIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .heightIn(min = 44.dp)
                                            .testTag("chip_default_payment_$method")
                                    )
                                }
                            }
                        }

                        // Warning if QRIS is default but not uploaded
                        if (defaultPaymentMethod == "QRIS" && qrisImagePath.isBlank()) {
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = WarningContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "QRIS belum diatur. Tambahkan gambar QRIS terlebih dahulu di bawah ini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // QRIS Toko Card & Preview
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Gambar QRIS Merchant Toko",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (qrisBitmap != null) {
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Image(
                                            bitmap = qrisBitmap,
                                            contentDescription = "QRIS Merchant",
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(KelolaRadius.ShapeInput),
                                            contentScale = ContentScale.Fit
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (qrisImagePath.isNotBlank()) {
                                                        val file = File(qrisImagePath)
                                                        if (file.exists()) {
                                                            val bmp = BitmapFactory.decodeFile(file.absolutePath)
                                                            if (bmp != null) {
                                                                rawBitmapForCrop = bmp
                                                                showCropDialog = true
                                                            }
                                                        }
                                                    }
                                                },
                                                shape = KelolaRadius.ShapeInput,
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.height(44.dp)
                                            ) {
                                                Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Potong", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    photoPickerLauncher.launch(
                                                        androidx.activity.result.PickVisualMediaRequest(
                                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                                        )
                                                    )
                                                },
                                                shape = KelolaRadius.ShapeInput,
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.height(44.dp)
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ganti", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            }

                                            OutlinedButton(
                                                onClick = { showDeleteQrisConfirm = true },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                                shape = KelolaRadius.ShapeInput,
                                                modifier = Modifier.height(44.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Hapus", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    shape = KelolaRadius.ShapeInput,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("button_upload_qris")
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pilih Gambar QRIS dari Galeri", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // =============================================================
            // 2B. AKUN REKENING TRANSFER BANK (Maksimal 5 Akun)
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Akun Rekening Bank",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = KelolaRadius.ShapeSmall,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${bankAccounts.size}/5 Akun",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Daftar Akun Bank yang Ada
                        if (bankAccounts.isEmpty()) {
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Belum ada rekening transfer yang didaftarkan. Tambahkan rekening agar pelanggan dapat melihat detail transfer saat berbelanja.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                bankAccounts.forEach { acc ->
                                    Card(
                                        shape = KelolaRadius.ShapeInput,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = KelolaRadius.ShapeSmall,
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                    ) {
                                                        Text(
                                                            text = acc.bankName.uppercase(),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = acc.accountNumber,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "a.n. ${acc.accountName}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    onRemoveBankAccount(acc.id)
                                                    showSuccess("Rekening ${acc.bankName} dihapus")
                                                },
                                                modifier = Modifier.size(36.dp).testTag("button_delete_bank_${acc.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus Rekening",
                                                    tint = DangerRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Tombol Tambah Rekening (Maksimal 5)
                        if (bankAccounts.size < 5) {
                            Button(
                                onClick = {
                                    newBankName = ""
                                    newAccountName = ""
                                    newAccountNumber = ""
                                    bankInputError = false
                                    showAddBankDialog = true
                                },
                                shape = KelolaRadius.ShapeInput,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("button_add_bank_account")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tambah Rekening Bank", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text(
                                text = "Batas maksimum 5 akun rekening telah tercapai.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // =============================================================
            // 3. MODAL AWAL & SALDO
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "MODAL AWAL KASIR",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        OutlinedTextField(
                            value = openingCapitalInput,
                            onValueChange = { openingCapitalInput = FormatUtils.formatRupiahInput(it) },
                            label = { Text("Modal Kasir Awal Hari Ini") },
                            placeholder = { Text("0") },
                            prefix = { Text("Rp ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = KelolaRadius.ShapeInput,
                            colors = inputColors,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_opening_capital")
                        )

                        Button(
                            onClick = {
                                val amount = FormatUtils.parseRupiahInput(openingCapitalInput)
                                onSaveOpeningCapital(amount)
                                showSuccess("Modal awal kasir berhasil disimpan!")
                            },
                            shape = KelolaRadius.ShapeInput,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_save_opening_capital")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text("Simpan Modal Awal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // =============================================================
            // 4. PENGATURAN STRUK
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "CATATAN STRUK PEMBELIAN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        OutlinedTextField(
                            value = receiptFooterInput,
                            onValueChange = { receiptFooterInput = it },
                            label = { Text("Pesan Bawah Struk (Footer)") },
                            placeholder = { Text("Terima kasih atas kunjungan Anda!") },
                            shape = KelolaRadius.ShapeInput,
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onSaveReceiptFooter(receiptFooterInput)
                                showSuccess("Pesan bawah struk berhasil disimpan!")
                            },
                            shape = KelolaRadius.ShapeInput,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text("Simpan Pesan Struk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // =============================================================
            // 5. TEMA & TAMPILAN
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BrightnessMedium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "TEMA & TAMPILAN APLIKASI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // --- PILIHAN TEMA WARNA APLIKASI ---
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Warna Tema Utama",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ColorTheme.entries.forEach { themeItem ->
                                    val isCurrentThemeSelected = colorTheme.equals(themeItem.key, ignoreCase = true)
                                    Surface(
                                        shape = KelolaRadius.ShapeInput,
                                        color = if (isCurrentThemeSelected) themeItem.previewColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(
                                            width = if (isCurrentThemeSelected) 2.dp else 1.dp,
                                            color = if (isCurrentThemeSelected) themeItem.previewColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(KelolaRadius.ShapeInput)
                                            .clickable {
                                                onUpdateColorTheme(themeItem.key)
                                                showSuccess("Warna tema diubah ke ${themeItem.displayName}!")
                                            }
                                            .testTag("theme_swatch_${themeItem.key}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(themeItem.previewColor)
                                                    .border(
                                                        width = if (isCurrentThemeSelected) 2.dp else 0.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCurrentThemeSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = themeItem.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isCurrentThemeSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrentThemeSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                        Text(
                            text = "Mode Tampilan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val modes = listOf(
                            Triple("LIGHT", "Mode Terang", Icons.Default.LightMode),
                            Triple("DARK", "Mode Gelap", Icons.Default.DarkMode),
                            Triple("SYSTEM", "Ikuti Sistem Perangkat", Icons.Default.BrightnessMedium)
                        )

                        modes.forEach { (mode, label, icon) ->
                            val isSelected = themeMode == mode
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KelolaRadius.ShapeInput)
                                    .clickable {
                                        onUpdateThemeMode(mode)
                                        showSuccess("Tema tampilan berhasil diubah!")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            onUpdateThemeMode(mode)
                                            showSuccess("Tema tampilan berhasil diubah!")
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =============================================================
            // 5b. UKURAN TAMPILAN LAYAR (VIEWPORT)
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "UKURAN TAMPILAN LAYAR (VIEWPORT)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Text(
                            text = "Sesuaikan lebar tampilan layar untuk simulasi dan kenyamanan antarmuka perangkat:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        val viewportOptions = listOf(
                            Triple("360dp", "Ringkas (360dp)", "Layar kecil / kompak"),
                            Triple("412dp", "Standar (412dp)", "Standar Google Pixel / Galaxy"),
                            Triple("430dp", "Lega (430dp)", "Layar lebar / Max / Plus")
                        )

                        viewportOptions.forEach { (widthValue, title, desc) ->
                            val isSelected = viewportWidth == widthValue
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KelolaRadius.ShapeInput)
                                    .clickable {
                                        onSaveViewportWidth(widthValue)
                                        showSuccess("Ukuran tampilan diubah ke $title")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            onSaveViewportWidth(widthValue)
                                            showSuccess("Ukuran tampilan diubah ke $title")
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =============================================================
            // 5c. BANTUAN & INFORMASI APLIKASI
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "BANTUAN & INFORMASI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Shortcut 1: Panduan Penggunaan
                        Surface(
                            shape = KelolaRadius.ShapeInput,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenGuide() }
                                .testTag("shortcut_guide")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Panduan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Petunjuk lengkap & mudah menggunakan seluruh fitur aplikasi",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Shortcut 2: Tentang Aplikasi
                        Surface(
                            shape = KelolaRadius.ShapeInput,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAbout() }
                                .testTag("shortcut_about")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Tentang Aplikasi",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Pengembang, versi Kelola 2.5 & kebijakan privasi",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =============================================================
            // 6. CADANGAN & RESET DATA
            // =============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "MANAJEMEN DATA & PEMELIHARAAN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Export
                        Button(
                            onClick = onExportBackup,
                            shape = KelolaRadius.ShapeInput,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cadangkan Data (Export JSON)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Reset Pengaturan Saja
                        Surface(
                            onClick = { showResetSettingsConfirm = true },
                            shape = KelolaRadius.ShapeInput,
                            color = WarningAmber.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reset Pengaturan Saja",
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Reset Seluruh Database
                        Surface(
                            onClick = { showResetAllDataConfirm = true },
                            shape = KelolaRadius.ShapeInput,
                            color = DangerRed.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hapus Semua Data Transaksi & Produk",
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Info Footer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = KelolaSpacing.Space3),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kelola • Versi 2.5 (Offline Local POS)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(KelolaSpacing.Space6).navigationBarsPadding())
            }
        }
        }
    }

    // Animated Success Floating Pill Banner
    AnimatedVisibility(
        visible = successMessage != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            .zIndex(99f)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = com.example.ui.theme.SuccessGreen,
            contentColor = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .clickable { successMessage = null }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = successMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
} // Box closing

    // Confirmation Dialogs
    if (showDeleteQrisConfirm) {
        ConfirmationDialog(
            title = "Hapus Gambar QRIS?",
            message = "Gambar QRIS akan dihapus dari penyimpanan perangkat. Transaksi QRIS tetap bisa dilakukan tanpa tampilan kode QR.",
            confirmText = "Hapus",
            isDestructive = true,
            onConfirm = {
                showDeleteQrisConfirm = false
                onDeleteQris()
                showSuccess("Foto QRIS berhasil dihapus")
            },
            onDismiss = { showDeleteQrisConfirm = false }
        )
    }

    if (showResetSettingsConfirm) {
        ConfirmationDialog(
            title = "Reset Pengaturan ke Bawaan?",
            message = "Pengaturan nama toko, QRIS, tema, dan data awal akan dikembalikan ke default. Riwayat transaksi, kasir, dan daftar produk TETAP AMAN dan TIDAK akan dihapus.",
            confirmText = "Reset Pengaturan",
            isDestructive = false,
            onConfirm = {
                showResetSettingsConfirm = false
                onResetSettingsOnly()
                showSuccess("Pengaturan berhasil di-reset ke bawaan")
            },
            onDismiss = { showResetSettingsConfirm = false }
        )
    }

    if (showResetAllDataConfirm) {
        ConfirmationDialog(
            title = "Hapus Seluruh Database?",
            message = "PERINGATAN: Semua riwayat transaksi kasir, hutang kasbon, dan pengeluaran akan dihapus bersih. Tindakan ini tidak dapat dibatalkan.",
            confirmText = "Hapus Seluruh Data",
            isDestructive = true,
            onConfirm = {
                showResetAllDataConfirm = false
                onResetAllData()
            },
            onDismiss = { showResetAllDataConfirm = false }
        )
    }

    if (showCropDialog && rawBitmapForCrop != null) {
        QrisCropDialog(
            rawBitmap = rawBitmapForCrop!!,
            onConfirmCrop = { cropped ->
                showCropDialog = false
                rawBitmapForCrop = null
                onSaveCroppedQris(cropped)
                showSuccess("Kode QRIS berhasil disimpan!")
            },
            onDismiss = {
                showCropDialog = false
                rawBitmapForCrop = null
            }
        )
    }

    // Dialog Tambah Akun Bank (Maksimal 5 Akun)
    if (showAddBankDialog) {
        AlertDialog(
            onDismissRequest = { showAddBankDialog = false },
            shape = KelolaRadius.ShapeCard,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Tambah Rekening Bank",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Masukkan rincian akun bank transfer untuk toko Anda (${bankAccounts.size + 1}/5):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newBankName,
                        onValueChange = { newBankName = it },
                        label = { Text("Nama Bank (Misal: BCA, Mandiri, BRI, Jago)") },
                        placeholder = { Text("Contoh: BCA") },
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        modifier = Modifier.fillMaxWidth().testTag("input_bank_name")
                    )

                    OutlinedTextField(
                        value = newAccountNumber,
                        onValueChange = { newAccountNumber = it.filter { c -> c.isDigit() || c == '-' } },
                        label = { Text("Nomor Rekening") },
                        placeholder = { Text("Contoh: 1234567890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        modifier = Modifier.fillMaxWidth().testTag("input_account_number")
                    )

                    OutlinedTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it },
                        label = { Text("Atas Nama (Pemilik Rekening)") },
                        placeholder = { Text("Contoh: Toko Kelola / Budi") },
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        modifier = Modifier.fillMaxWidth().testTag("input_account_name")
                    )

                    if (bankInputError) {
                        Text(
                            text = "Nama Bank dan Nomor Rekening wajib diisi.",
                            color = DangerRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBankName.isBlank() || newAccountNumber.isBlank()) {
                            bankInputError = true
                        } else {
                            onAddBankAccount(newBankName.trim(), newAccountName.trim(), newAccountNumber.trim())
                            showAddBankDialog = false
                            showSuccess("Rekening ${newBankName.trim()} berhasil ditambahkan")
                        }
                    },
                    shape = KelolaRadius.ShapeInput,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("button_confirm_save_bank")
                ) {
                    Text("Simpan Rekening", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBankDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

}
