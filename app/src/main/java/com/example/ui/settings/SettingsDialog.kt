package com.example.ui.settings

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils
import java.io.File

@Composable
fun SettingsDialog(
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
    onSaveBusinessInfo: (name: String, address: String, phone: String) -> Unit,
    onSaveDefaultPaymentMethod: (String) -> Unit,
    onUploadQris: (Uri) -> Unit,
    onDeleteQris: () -> Unit,
    onSaveOpeningCapital: (Long) -> Unit,
    onSavePreviousSales: (amount: Long, date: String, note: String) -> Unit,
    onSaveReceiptFooter: (String) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onExportBackup: () -> Unit,
    onResetSettingsOnly: () -> Unit,
    onResetAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    // Local form states
    var businessNameInput by remember { mutableStateOf(currentBusinessName) }
    var addressInput by remember { mutableStateOf(currentAddress) }
    var phoneInput by remember { mutableStateOf(currentPhone) }
    var receiptFooterInput by remember { mutableStateOf(currentReceiptFooter) }

    var openingCapitalInput by remember { mutableStateOf(if (openingCapital > 0L) FormatUtils.formatNumberWithDots(openingCapital) else "") }
    var previousSalesInput by remember { mutableStateOf(if (previousSales > 0L) FormatUtils.formatNumberWithDots(previousSales) else "") }
    var previousSalesDateInput by remember { mutableStateOf(previousSalesDate) }
    var previousSalesNoteInput by remember { mutableStateOf(previousSalesNote) }

    var showDeleteQrisConfirm by remember { mutableStateOf(false) }
    var showResetSettingsConfirm by remember { mutableStateOf(false) }
    var showResetAllDataConfirm by remember { mutableStateOf(false) }

    // Photo picker for QRIS
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onUploadQris(uri)
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .kelolaSoftShadow(shape = KelolaRadius.ShapeSheet, elevation = 8.dp),
            shape = KelolaRadius.ShapeSheet,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pengaturan",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kelola data usaha & preferensi kasir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_close_settings")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // =============================================================
                    // 1. IDENTITAS USAHA
                    // =============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "IDENTITAS USAHA",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            OutlinedTextField(
                                value = businessNameInput,
                                onValueChange = { businessNameInput = it },
                                label = { Text("Nama Usaha / Toko") },
                                placeholder = { Text("Misal: Kantin Mahasiswa") },
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_business_name")
                            )

                            OutlinedTextField(
                                value = addressInput,
                                onValueChange = { addressInput = it },
                                label = { Text("Alamat Usaha (Opsional)") },
                                placeholder = { Text("Misal: Gedung Utama Kampus Lt. 1") },
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_business_phone")
                            )

                            Button(
                                onClick = {
                                    onSaveBusinessInfo(businessNameInput, addressInput, phoneInput)
                                },
                                shape = KelolaRadius.ShapeInput,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_save_business_info")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan Identitas Usaha", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // =============================================================
                    // 2. PEMBAYARAN & QRIS
                    // =============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "METODE PEMBAYARAN",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.6.sp
                                )
                            }

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

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Tunai", "QRIS").forEach { method ->
                                        val isSelected = defaultPaymentMethod == method
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onSaveDefaultPaymentMethod(method) },
                                            shape = KelolaRadius.ShapeSmall,
                                            label = {
                                                Text(
                                                    text = method,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (method == "Tunai") Icons.Default.LocalAtm else Icons.Default.QrCode,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            border = null,
                                            modifier = Modifier.testTag("chip_default_payment_$method")
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
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = WarningAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
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
                                    text = "Gambar QRIS Toko",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (qrisBitmap != null) {
                                    // Preview container
                                    Surface(
                                        shape = KelolaRadius.ShapeCard,
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = KelolaRadius.ShapeSmall,
                                                    color = SuccessGreen.copy(alpha = 0.14f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(SuccessGreen)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "QRIS Aktif",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = SuccessGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = qrisMerchantName.ifBlank { currentBusinessName },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Surface(
                                                shape = KelolaRadius.ShapeInput,
                                                color = Color.White,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Image(
                                                    bitmap = qrisBitmap,
                                                    contentDescription = "Preview QRIS",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .sizeIn(maxHeight = 180.dp)
                                                        .padding(8.dp)
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        photoPickerLauncher.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    },
                                                    shape = KelolaRadius.ShapeSmall,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("button_change_qris")
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Ganti QRIS")
                                                }

                                                Button(
                                                    onClick = { showDeleteQrisConfirm = true },
                                                    shape = KelolaRadius.ShapeSmall,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = DangerRed.copy(alpha = 0.12f),
                                                        contentColor = DangerRed
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("button_delete_qris")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Hapus QRIS")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Empty State
                                    Surface(
                                        shape = KelolaRadius.ShapeCard,
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.QrCode,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Text(
                                                text = "Belum Ada Gambar QRIS",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Unggah barcode QRIS dari galeri HP Anda agar muncul otomatis di kasir saat pembeli memilih bayar QRIS.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Button(
                                                onClick = {
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                shape = KelolaRadius.ShapeInput,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.testTag("button_upload_qris")
                                            ) {
                                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Unggah Gambar QRIS", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =============================================================
                    // 3. DATA KEUANGAN AWAL
                    // =============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DATA KEUANGAN AWAL",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            // Sub-card Modal Awal
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Modal Awal Usaha (Rp)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Saldo modal kas awal saat memulai pencatatan. Modal awal bukan penjualan dan tidak dimasukkan ke laba omzet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val parsedCapital = FormatUtils.parseRupiahInput(openingCapitalInput)
                                    OutlinedTextField(
                                        value = openingCapitalInput,
                                        onValueChange = { input ->
                                            openingCapitalInput = FormatUtils.formatRupiahInput(input)
                                        },
                                        placeholder = { Text("0") },
                                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                        supportingText = {
                                            if (parsedCapital > 0L) {
                                                Text(
                                                    text = "Tercatat: ${FormatUtils.formatRupiah(parsedCapital)}",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_opening_capital")
                                    )

                                    Button(
                                        onClick = {
                                            onSaveOpeningCapital(parsedCapital)
                                        },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("button_save_opening_capital")
                                    ) {
                                        Text("Simpan Modal Awal", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Sub-card Penjualan Sebelumnya
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Data Penjualan Sebelumnya (Historis)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Total penjualan sebelum menggunakan aplikasi POS ini. Disimpan sebagai angka pembanding historis di Laporan, tanpa menambah transaksi kasir palsu.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val parsedSales = FormatUtils.parseRupiahInput(previousSalesInput)
                                    OutlinedTextField(
                                        value = previousSalesInput,
                                        onValueChange = { input ->
                                            previousSalesInput = FormatUtils.formatRupiahInput(input)
                                        },
                                        placeholder = { Text("0") },
                                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                        supportingText = {
                                            if (parsedSales > 0L) {
                                                Text(
                                                    text = "Tercatat: ${FormatUtils.formatRupiah(parsedSales)}",
                                                    color = SuccessGreen,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_previous_sales")
                                    )

                                    OutlinedTextField(
                                        value = previousSalesDateInput,
                                        onValueChange = { previousSalesDateInput = it },
                                        label = { Text("Periode / Tanggal (Opsional)") },
                                        placeholder = { Text("Misal: Jan - Des 2025") },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = previousSalesNoteInput,
                                        onValueChange = { previousSalesNoteInput = it },
                                        label = { Text("Catatan Buku Kas Lama (Opsional)") },
                                        placeholder = { Text("Misal: Rekap pembukuan buku tulis") },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            onSavePreviousSales(parsedSales, previousSalesDateInput, previousSalesNoteInput)
                                        },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("button_save_previous_sales")
                                    ) {
                                        Text("Simpan Data Penjualan Sebelumnya", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // =============================================================
                    // 4. PERSONALISASI & TAMPILAN
                    // =============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PERSONALISASI & TEMA",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            // Switch Light / Dark / Sistem
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Mode Tampilan Aplikasi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val modes = listOf(
                                        Triple("LIGHT", "Terang", Icons.Default.LightMode),
                                        Triple("DARK", "Gelap", Icons.Default.DarkMode),
                                        Triple("SYSTEM", "Sistem", Icons.Default.BrightnessAuto)
                                    )

                                    modes.forEach { (modeKey, modeTitle, icon) ->
                                        val isSelected = themeMode == modeKey
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onUpdateThemeMode(modeKey) },
                                            shape = KelolaRadius.ShapeSmall,
                                            label = {
                                                Text(
                                                    text = modeTitle,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            border = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("theme_mode_$modeKey")
                                        )
                                    }
                                }
                            }

                            // Receipt Footer Customization
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Pesan Footer Struk",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedTextField(
                                    value = receiptFooterInput,
                                    onValueChange = { receiptFooterInput = it },
                                    placeholder = { Text("Terima kasih atas kunjungan Anda!") },
                                    shape = KelolaRadius.ShapeInput,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_receipt_footer")
                                )

                                Button(
                                    onClick = { onSaveReceiptFooter(receiptFooterInput) },
                                    shape = KelolaRadius.ShapeInput,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Simpan Pesan Struk", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // =============================================================
                    // 5. DATA & PEMELIHARAAN
                    // =============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DATA & PEMELIHARAAN",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            // Notice Local SQLite
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Database Lokal SQLite (Offline)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "100% offline tanpa cloud. Semua transaksi dan produk tersimpan aman di HP.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Export JSON Backup
                            Button(
                                onClick = onExportBackup,
                                shape = KelolaRadius.ShapeInput,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_export_backup")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cadangkan Data (Export JSON)", fontWeight = FontWeight.SemiBold)
                            }

                            // Reset Pengaturan Saja (Keep database safe!)
                            Surface(
                                onClick = { showResetSettingsConfirm = true },
                                shape = KelolaRadius.ShapeInput,
                                color = WarningAmber.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reset Pengaturan Saja",
                                        color = WarningAmber,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Reset Seluruh Database (Destructive)
                            Surface(
                                onClick = { showResetAllDataConfirm = true },
                                shape = KelolaRadius.ShapeInput,
                                color = DangerRed.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hapus Semua Data Transaksi & Produk",
                                        color = DangerRed,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    // App Info Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

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
}
