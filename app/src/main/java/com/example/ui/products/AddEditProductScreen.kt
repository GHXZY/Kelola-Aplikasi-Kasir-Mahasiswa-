package com.example.ui.products

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.util.BarcodeUtils
import com.example.util.FormatUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AddEditProductScreen:
 * Tampilan Tambah/Edit Produk yang SAMA PERSIS 1:1 dengan localhost preview (AddEditProductScreen.tsx).
 * Dilengkapi Card Section Barcode Opsional untuk Generate, Preview, dan Download Barcode PNG.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    initialProduct: ProductEntity? = null,
    categories: List<CategoryEntity>,
    onSaveProduct: (
        name: String,
        categoryId: Long,
        costPrice: Long,
        sellingPrice: Long,
        stock: Int,
        minStock: Int,
        unit: String,
        expirationDate: Long?,
        barcode: String?
    ) -> Unit,
    onGenerateUniqueBarcode: (suspend () -> String?)? = null,
    onCheckBarcodeAvailability: (suspend (String) -> Boolean)? = null,
    onLookupProduct: (suspend (String) -> ProductEntity?)? = null,
    onSelectProductToEdit: ((ProductEntity) -> Unit)? = null,
    onOpenAddCategory: () -> Unit = {},
    onAddCategoryCustom: (name: String, onCreated: (Long) -> Unit) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditing = initialProduct != null

    // Form fields matching preview
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var categoryId by remember {
        mutableStateOf(initialProduct?.categoryId ?: (categories.firstOrNull()?.id ?: 1L))
    }
    var costPriceText by remember {
        mutableStateOf(if (initialProduct?.costPrice != null && initialProduct.costPrice > 0L) FormatUtils.formatNumberWithDots(initialProduct.costPrice) else "")
    }
    var sellingPriceText by remember {
        mutableStateOf(if (initialProduct?.sellingPrice != null && initialProduct.sellingPrice > 0L) FormatUtils.formatNumberWithDots(initialProduct.sellingPrice) else "")
    }
    var stockText by remember {
        mutableStateOf(if (initialProduct != null) FormatUtils.formatNumberWithDots(initialProduct.stock.toLong()) else "10")
    }
    var minStockText by remember {
        mutableStateOf(if (initialProduct != null) FormatUtils.formatNumberWithDots(initialProduct.minimumStock.toLong()) else "3")
    }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "pcs") }

    val units = listOf("pcs", "botol", "cup", "porsi", "bungkus", "karung", "pouch")

    // Category selection dropdown state
    var showCategoryDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && categories.none { it.id == categoryId }) {
            categoryId = categories.first().id
        }
    }

    val selectedCategoryName = categories.find { it.id == categoryId }?.name ?: "Pilih Kategori"

    // Expiry state matching preview (DATE_ONLY vs DATE_TIME)
    val initialExpiry = initialProduct?.expirationDate?.let { Date(it) }
    var hasExpiry by remember { mutableStateOf(initialExpiry != null) }
    var expiryMode by remember { mutableStateOf("DATE_ONLY") } // "DATE_ONLY" | "DATE_TIME"

    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var expiryDate by remember {
        mutableStateOf(initialExpiry?.let { sdfDate.format(it) } ?: "")
    }
    var expiryTime by remember {
        mutableStateOf(initialExpiry?.let { sdfTime.format(it) } ?: "23:59")
    }

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Barcode state (Opsional)
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingBarcode by remember { mutableStateOf(false) }
    var isDownloadingBarcode by remember { mutableStateOf(false) }
    var barcodeErrorMessage by remember { mutableStateOf<String?>(null) }
    var showRegenerateConfirmDialog by remember { mutableStateOf(false) }
    var showManualBarcodeField by remember { mutableStateOf(false) }
    var manualBarcodeInput by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var showScanBarcodeDialog by remember { mutableStateOf(false) }
    var duplicateProductDetected by remember { mutableStateOf<ProductEntity?>(null) }

    // Memperbarui form state jika initialProduct berubah (misalnya setelah memilih Lihat/Edit Produk dari hasil scan)
    LaunchedEffect(initialProduct) {
        if (initialProduct != null) {
            name = initialProduct.name
            categoryId = initialProduct.categoryId
            costPriceText = if (initialProduct.costPrice > 0L) FormatUtils.formatNumberWithDots(initialProduct.costPrice) else ""
            sellingPriceText = if (initialProduct.sellingPrice > 0L) FormatUtils.formatNumberWithDots(initialProduct.sellingPrice) else ""
            stockText = FormatUtils.formatNumberWithDots(initialProduct.stock.toLong())
            minStockText = FormatUtils.formatNumberWithDots(initialProduct.minimumStock.toLong())
            unit = initialProduct.unit
            barcode = initialProduct.barcode ?: ""
            manualBarcodeInput = initialProduct.barcode ?: ""
            val exp = initialProduct.expirationDate?.let { Date(it) }
            hasExpiry = exp != null
            if (exp != null) {
                expiryDate = sdfDate.format(exp)
                expiryTime = sdfTime.format(exp)
            }
        }
    }

    // Memperbarui Bitmap Preview Barcode setiap kali kode barcode atau nama produk berubah
    LaunchedEffect(barcode, name) {
        if (barcode.isNotBlank()) {
            try {
                barcodeBitmap = BarcodeUtils.createBarcodeBitmap(
                    barcode = barcode,
                    productName = name.ifBlank { null },
                    width = 720,
                    height = 340,
                    includeText = true
                )
                barcodeErrorMessage = null
            } catch (e: Exception) {
                barcodeBitmap = null
                barcodeErrorMessage = "Format barcode belum dapat dirender: ${e.message}"
            }
        } else {
            barcodeBitmap = null
        }
    }

    // Helper to calculate preset days
    fun setPresetDays(days: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        expiryDate = sdfDate.format(cal.time)
        hasExpiry = true
    }

    // Helper to compute timestamp
    fun calculateExpiryTimestamp(): Long? {
        if (!hasExpiry || expiryDate.isBlank()) return null
        return try {
            val timeStr = if (expiryMode == "DATE_ONLY") "23:59:59" else "${expiryTime.ifBlank { "00:00" }}:00"
            val fullSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            fullSdf.parse("$expiryDate $timeStr")?.time
        } catch (_: Exception) {
            null
        }
    }

    // Error states
    var nameError by remember { mutableStateOf(false) }
    var sellingPriceError by remember { mutableStateOf(false) }

    // Date / Time picker dialog openers
    fun openDatePicker() {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                expiryDate = sdfDate.format(selectedCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance()
        val tpd = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formattedHour = if (hourOfDay < 10) "0$hourOfDay" else "$hourOfDay"
                val formattedMinute = if (minute < 10) "0$minute" else "$minute"
                expiryTime = "$formattedHour:$formattedMinute"
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        )
        tpd.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // =========================================================================
        // 1. TOP BAR (Sama persis dengan AddEditProductScreen.tsx)
        // =========================================================================
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNavigateBack() }
                        .testTag("button_back_product_form"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEditing) "Edit Informasi Produk" else "Tambah Produk Baru",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Lengkapi detail barang dagangan toko",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // TOP-RIGHT AREA: Tombol Scan Barcode
                Surface(
                    onClick = { showScanBarcodeDialog = true },
                    shape = KelolaRadius.ShapeChip,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("button_scan_barcode_product_form")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Scan Barcode",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 2. SCROLLABLE FORM (Sama persis dengan AddEditProductScreen.tsx)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -------------------------------------------------------------
            // CARD 1: INFORMASI DETAIL PRODUK
            // -------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nama Barang *
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Nama Barang *",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = it.isBlank()
                            },
                            placeholder = {
                                Text("Misal: Es Kopi Susu Aren", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            singleLine = true,
                            isError = nameError,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("input_product_name")
                        )
                        if (nameError) {
                            Text("Nama barang wajib diisi", color = DangerRed, fontSize = 11.sp)
                        }
                    }

                    // Kategori Produk (+ Tombol Tambah Kategori)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Kategori Produk",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dropdown selector
                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clickable { showCategoryDropdown = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedCategoryName,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "▼",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showCategoryDropdown,
                                    onDismissRequest = { showCategoryDropdown = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name, fontSize = 13.sp) },
                                            onClick = {
                                                categoryId = cat.id
                                                showCategoryDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Plus Button for New Category
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onOpenAddCategory() }
                                    .testTag("button_add_category_modal")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah Kategori Baru",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Grid 2 Kolom: Harga Modal & Harga Jual *
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Harga Modal (Rp)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Harga Modal (Rp)",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            OutlinedTextField(
                                value = costPriceText,
                                onValueChange = { costPriceText = FormatUtils.formatRupiahInput(it) },
                                placeholder = { Text("0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }

                        // Harga Jual (Rp) *
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Harga Jual (Rp) *",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            OutlinedTextField(
                                value = sellingPriceText,
                                onValueChange = {
                                    sellingPriceText = FormatUtils.formatRupiahInput(it)
                                    sellingPriceError = FormatUtils.parseRupiahInput(sellingPriceText) <= 0
                                },
                                placeholder = { Text("0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                isError = sellingPriceError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.primary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.primary
                                ),
                                textStyle = TextStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("input_product_selling_price")
                            )
                        }
                    }

                    // Grid 2 Kolom: Stok Awal & Batas Menipis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stok Awal
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Stok Awal",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            OutlinedTextField(
                                value = stockText,
                                onValueChange = { stockText = FormatUtils.formatRupiahInput(it) },
                                placeholder = { Text("0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }

                        // Batas Menipis
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Batas Menipis",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            OutlinedTextField(
                                value = minStockText,
                                onValueChange = { minStockText = FormatUtils.formatRupiahInput(it) },
                                placeholder = { Text("3", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }

                    // Satuan Barang
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Satuan Barang",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(units) { u ->
                                val isSelected = unit == u
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { unit = u }
                                ) {
                                    Text(
                                        text = u,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // CARD 2: MASA SIMPAN & KADALUARSA (Sama persis dengan AddEditProductScreen.tsx)
            // -------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Card 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Masa Simpan & Kadaluarsa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable {
                                hasExpiry = !hasExpiry
                                if (hasExpiry && expiryDate.isBlank()) {
                                    setPresetDays(30)
                                }
                            }
                        ) {
                            Text(
                                text = "Ada Kadaluarsa",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Checkbox(
                                checked = hasExpiry,
                                onCheckedChange = { checked ->
                                    hasExpiry = checked
                                    if (checked && expiryDate.isBlank()) {
                                        setPresetDays(30)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    if (hasExpiry) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Segmented Toggle: Tanggal Saja vs Tanggal & Jam
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tanggal Saja
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (expiryMode == "DATE_ONLY") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (expiryMode == "DATE_ONLY") MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { expiryMode = "DATE_ONLY" }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (expiryMode == "DATE_ONLY") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tanggal Saja",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (expiryMode == "DATE_ONLY") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Tanggal & Jam
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (expiryMode == "DATE_TIME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (expiryMode == "DATE_TIME") MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { expiryMode = "DATE_TIME" }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (expiryMode == "DATE_TIME") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tanggal & Jam",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (expiryMode == "DATE_TIME") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Date / Time Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tanggal Kadaluarsa
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Tanggal Kadaluarsa",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { openDatePicker() }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = expiryDate.ifBlank { "Pilih Tanggal" },
                                            fontSize = 12.sp,
                                            color = if (expiryDate.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            // Jam Spesifik (if DATE_TIME)
                            if (expiryMode == "DATE_TIME") {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Jam Spesifik",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { openTimePicker() }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = expiryTime.ifBlank { "00:00" },
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Presets Cepat
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Preset Cepat:",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            val presets = listOf(
                                "+3 Hari" to 3,
                                "+7 Hari" to 7,
                                "+14 Hari" to 14,
                                "+1 Bulan" to 30,
                                "+6 Bulan" to 180,
                                "+1 Tahun" to 365
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(presets) { (label, days) ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { setPresetDays(days) }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Summary Notice
                        if (expiryDate.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Otomatis tercatat rugi jika kadaluarsa: $expiryDate ${if (expiryMode == "DATE_TIME") "pukul $expiryTime" else "23:59"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            hasExpiry = false
                                            expiryDate = ""
                                        },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text(
                                            text = "Hapus",
                                            color = DangerRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // CARD 3: SECTION BARCODE (Fitur Opsional)
            // -------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_barcode_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Barcode Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Barcode Produk",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Badge Status: Opsional vs Tersedia
                        if (barcode.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tersedia",
                                        color = SuccessGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Opsional",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Error Message
                    if (barcodeErrorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DangerContainer,
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = barcodeErrorMessage!!,
                                    fontSize = 12.sp,
                                    color = DangerRed,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { barcodeErrorMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup pesan error",
                                        tint = DangerRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (barcode.isBlank()) {
                        // =========================================================
                        // STATE 1: BELUM ADA BARCODE
                        // =========================================================
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum memiliki barcode? Buat barcode unik untuk produk ini agar dapat dicetak dan dipindai secara instan di kasir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            // Tombol Utama: Generate Barcode
                            Button(
                                onClick = {
                                    if (isGeneratingBarcode) return@Button
                                    coroutineScope.launch {
                                        isGeneratingBarcode = true
                                        barcodeErrorMessage = null
                                        try {
                                            val newBarcode = onGenerateUniqueBarcode?.invoke()
                                                ?: BarcodeUtils.generateEan13Candidate("899")
                                            val isAvailable = onCheckBarcodeAvailability?.invoke(newBarcode) ?: true
                                            if (isAvailable) {
                                                barcode = newBarcode
                                                manualBarcodeInput = newBarcode
                                            } else {
                                                barcodeErrorMessage = "Barcode sudah digunakan. Silakan generate barcode baru."
                                            }
                                        } catch (e: Exception) {
                                            barcodeErrorMessage = "Barcode gagal dibuat. Silakan coba lagi."
                                        } finally {
                                            isGeneratingBarcode = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("button_generate_barcode"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isGeneratingBarcode) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Membuat Barcode...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Generate Barcode",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Opsi manual input jika produk sudah memiliki barcode kemasan pabrik
                            if (!showManualBarcodeField) {
                                TextButton(
                                    onClick = { showManualBarcodeField = true },
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Atau masukkan kode barcode manual",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "Kode Barcode Manual:",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = manualBarcodeInput,
                                            onValueChange = { manualBarcodeInput = it.filter { c -> c.isLetterOrDigit() || c == '-' } },
                                            placeholder = { Text("Contoh: 8991234567890", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                focusedContainerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )
                                        Button(
                                            onClick = {
                                                val clean = manualBarcodeInput.trim()
                                                if (clean.isBlank()) {
                                                    barcodeErrorMessage = "Kode barcode tidak boleh kosong"
                                                    return@Button
                                                }
                                                coroutineScope.launch {
                                                    val isAvailable = onCheckBarcodeAvailability?.invoke(clean) ?: true
                                                    if (isAvailable) {
                                                        barcode = clean
                                                        showManualBarcodeField = false
                                                        barcodeErrorMessage = null
                                                    } else {
                                                        barcodeErrorMessage = "Barcode sudah digunakan. Silakan generate barcode baru."
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("Terapkan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // =========================================================
                        // STATE 2 / STATE 3: BARCODE TERSEDIA (PREVIEW & DOWNLOAD)
                        // =========================================================
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Container Preview Barcode (Background putih solid, border subtle, quiet zone rapi)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("barcode_preview_container")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (barcodeBitmap != null) {
                                        Image(
                                            bitmap = barcodeBitmap!!.asImageBitmap(),
                                            contentDescription = "Preview Barcode $barcode",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            // Rincian Nilai Barcode + Tombol Salin
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Nomor Barcode:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = barcode,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(barcode))
                                            Toast.makeText(context, "Nomor barcode disalin: $barcode", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Salin Barcode",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Salin",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Action: Tombol Download PNG (Resolusi Tinggi Siap Cetak)
                            Button(
                                onClick = {
                                    if (isDownloadingBarcode) return@Button
                                    isDownloadingBarcode = true
                                    coroutineScope.launch {
                                        try {
                                            val highResBitmap = BarcodeUtils.createBarcodeBitmap(
                                                barcode = barcode,
                                                productName = name.ifBlank { null },
                                                width = 960,
                                                height = 440,
                                                includeText = true
                                            )
                                            val fileName = BarcodeUtils.generateSafeFileName(name, barcode)
                                            val result = BarcodeUtils.saveBarcodeToGallery(context, highResBitmap, fileName)
                                            if (result.isSuccess) {
                                                Toast.makeText(
                                                    context,
                                                    "Barcode berhasil disimpan ke Galeri/Pictures: $fileName",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                barcodeErrorMessage = "Gagal mengunduh barcode: ${result.exceptionOrNull()?.message}"
                                            }
                                        } catch (e: Exception) {
                                            barcodeErrorMessage = "Gagal mengunduh PNG: ${e.message}"
                                        } finally {
                                            isDownloadingBarcode = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("button_download_barcode_png"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isDownloadingBarcode) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Menyimpan PNG...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Download PNG",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Secondary Actions: Generate Barcode Baru (opsional) & Hapus Barcode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (initialProduct?.barcode?.isNotBlank() == true) {
                                            showRegenerateConfirmDialog = true
                                        } else {
                                            coroutineScope.launch {
                                                isGeneratingBarcode = true
                                                try {
                                                    val candidate = onGenerateUniqueBarcode?.invoke()
                                                        ?: BarcodeUtils.generateEan13Candidate("899")
                                                    barcode = candidate
                                                    manualBarcodeInput = candidate
                                                } catch (e: Exception) {
                                                    barcodeErrorMessage = "Barcode gagal dibuat. Silakan coba lagi."
                                                } finally {
                                                    isGeneratingBarcode = false
                                                }
                                            }
                                        }
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Generate Barcode Baru",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        barcode = ""
                                        manualBarcodeInput = ""
                                        barcodeBitmap = null
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = DangerRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Hapus Barcode",
                                        fontSize = 11.sp,
                                        color = DangerRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dialog Konfirmasi Re-Generate jika produk lama sudah memiliki barcode
            if (showRegenerateConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showRegenerateConfirmDialog = false },
                    title = {
                        Text(
                            text = "Ganti Barcode Produk?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Barcode saat ini ($barcode) akan diganti dengan nomor baru. Pastikan mencetak ulang label barcode baru jika label lama sudah terpasang pada fisik produk.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRegenerateConfirmDialog = false
                                coroutineScope.launch {
                                    isGeneratingBarcode = true
                                    try {
                                        val candidate = onGenerateUniqueBarcode?.invoke()
                                            ?: BarcodeUtils.generateEan13Candidate("899")
                                        barcode = candidate
                                        manualBarcodeInput = candidate
                                    } catch (e: Exception) {
                                        barcodeErrorMessage = "Barcode gagal dibuat. Silakan coba lagi."
                                    } finally {
                                        isGeneratingBarcode = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Ya, Buat Barcode Baru")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRegenerateConfirmDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // -------------------------------------------------------------
            // 3. SUBMIT BUTTON (Sama persis dengan AddEditProductScreen.tsx)
            // -------------------------------------------------------------
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val sp = FormatUtils.parseRupiahInput(sellingPriceText)
                    if (sp <= 0) {
                        sellingPriceError = true
                        return@Button
                    }

                    val cp = FormatUtils.parseRupiahInput(costPriceText)
                    val stk = FormatUtils.parseRupiahInput(stockText).toInt()
                    val minStk = FormatUtils.parseRupiahInput(minStockText).toInt().coerceAtLeast(0)

                    onSaveProduct(
                        name.trim(),
                        categoryId,
                        cp,
                        sp,
                        stk,
                        minStk,
                        unit.trim(),
                        calculateExpiryTimestamp(),
                        barcode.trim().ifBlank { null }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("button_save_product"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Perbarui Produk" else "Simpan Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // =========================================================================
        // DIALOG: SCAN BARCODE KAMERA
        // =========================================================================
        if (showScanBarcodeDialog) {
            ProductBarcodeScannerDialog(
                onBarcodeScanned = { scannedCode ->
                    showScanBarcodeDialog = false
                    coroutineScope.launch {
                        val cleanCode = scannedCode.trim()
                        val existingProduct = onLookupProduct?.invoke(cleanCode)
                        if (existingProduct != null && existingProduct.id != initialProduct?.id) {
                            // Barcode sudah digunakan oleh produk lain
                            duplicateProductDetected = existingProduct
                        } else {
                            // Barcode unik atau milik produk yang sedang diedit
                            barcode = cleanCode
                            manualBarcodeInput = cleanCode
                            barcodeErrorMessage = null
                            showManualBarcodeField = true
                        }
                    }
                },
                onDismissRequest = {
                    showScanBarcodeDialog = false
                }
            )
        }

        // =========================================================================
        // DIALOG: PERINGATAN BARCODE DUPLIKAT
        // =========================================================================
        if (duplicateProductDetected != null) {
            val dupProduct = duplicateProductDetected!!
            AlertDialog(
                onDismissRequest = { duplicateProductDetected = null },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DangerContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Barcode Sudah Digunakan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Barcode ini sudah terdaftar pada produk berikut:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = KelolaRadius.ShapeSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = dupProduct.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Harga: ${FormatUtils.formatRupiah(dupProduct.sellingPrice)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Stok: ${dupProduct.stock} ${dupProduct.unit}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!dupProduct.barcode.isNullOrBlank()) {
                                    Text(
                                        text = "Kode Barcode: ${dupProduct.barcode}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Setiap produk harus memiliki barcode unik agar sistem kasir dapat memindai barang dengan akurat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = dupProduct
                            duplicateProductDetected = null
                            onSelectProductToEdit?.invoke(target)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = KelolaRadius.ShapeSmall
                    ) {
                        Text("Lihat/Edit Produk")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { duplicateProductDetected = null }
                    ) {
                        Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
