package com.example.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
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
        expirationDate: Long?
    ) -> Unit,
    onOpenAddCategory: () -> Unit,
    onDismiss: () -> Unit
) {
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
        mutableStateOf(if (initialProduct != null) FormatUtils.formatNumberWithDots(initialProduct.stock.toLong()) else "0")
    }
    var minStockText by remember {
        mutableStateOf(if (initialProduct != null) FormatUtils.formatNumberWithDots(initialProduct.minimumStock.toLong()) else "5")
    }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "pcs") }
    var expirationDate by remember { mutableStateOf<Long?>(initialProduct?.expirationDate) }
    var showCustomExpiryDialog by remember { mutableStateOf(false) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    val commonUnits = listOf("pcs", "bungkus", "botol", "gelas", "porsi", "pack", "kotak")

    val isEditing = initialProduct != null
    val selectedCategoryName = categories.find { it.id == categoryId }?.name ?: "Pilih Kategori"

    androidx.compose.runtime.LaunchedEffect(categories) {
        if (categories.isNotEmpty() && categories.none { it.id == categoryId }) {
            categoryId = categories.first().id
        }
    }

    var nameError by remember { mutableStateOf(false) }
    var sellingPriceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isEditing) "Edit Produk" else "Tambah Produk Baru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nama Produk *") },
                    placeholder = { Text("Misal: Es Teh Manis") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Nama wajib diisi", color = DangerRed) },
                    singleLine = true,
                    shape = KelolaRadius.ShapeInput,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                // Category Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            shape = KelolaRadius.ShapeInput,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        categoryId = cat.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onOpenAddCategory,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah Kategori",
                            tint = PrimaryBlue
                        )
                    }
                }

                // Prices: HPP and Selling Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = FormatUtils.formatRupiahInput(it) },
                        label = { Text("Harga Modal (HPP)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = {
                            sellingPriceText = FormatUtils.formatRupiahInput(it)
                            sellingPriceError = FormatUtils.parseRupiahInput(sellingPriceText) <= 0
                        },
                        label = { Text("Harga Jual *") },
                        prefix = { Text("Rp ") },
                        isError = sellingPriceError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_price")
                    )
                }

                // Stock & Minimum Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = FormatUtils.formatRupiahInput(it) },
                        label = { Text(if (isEditing) "Stok Saat Ini" else "Stok Awal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_stock")
                    )

                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = FormatUtils.formatRupiahInput(it) },
                        label = { Text("Batas Min. Stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = unitDropdownExpanded,
                    onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Satuan Barang") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = unitDropdownExpanded,
                        onDismissRequest = { unitDropdownExpanded = false }
                    ) {
                        commonUnits.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unit = u
                                    unitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Expiration Date & Time Setting (Custom)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kadaluarsa (Tanggal & Jam)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (expirationDate != null) {
                            TextButton(onClick = { expirationDate = null }) {
                                Text("Hapus", color = DangerRed, fontSize = 12.sp)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeInput,
                        color = if (expirationDate != null) PrimaryBlueContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (expirationDate != null) {
                                        "Kadaluarsa: ${FormatUtils.formatDateTime(expirationDate!!)}"
                                    } else {
                                        "Belum ditentukan (Tanpa tanggal kadaluarsa)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (expirationDate != null) FontWeight.Bold else FontWeight.Normal,
                                    color = if (expirationDate != null) PrimaryBlue else TextMuted
                                )
                            }

                            OutlinedButton(
                                onClick = { showCustomExpiryDialog = true },
                                shape = KelolaRadius.ShapeInput,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("button_open_custom_expiry_picker")
                            ) {
                                Text(
                                    text = if (expirationDate == null) "Pilih Custom" else "Ubah",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    // Quick Expiry Presets (Hours & Days)
                    Text(
                        text = "Pilihan Cepat (Jam & Hari):",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presets = listOf(
                            "+1 Jam" to 1L * 3600000L,
                            "+3 Jam" to 3L * 3600000L,
                            "+6 Jam" to 6L * 3600000L,
                            "+12 Jam" to 12L * 3600000L,
                            "+1 Hari" to 1L * 86400000L,
                            "+3 Hari" to 3L * 86400000L,
                            "+7 Hari" to 7L * 86400000L,
                            "+1 Bulan" to 30L * 86400000L,
                            "+3 Bulan" to 90L * 86400000L,
                            "+6 Bulan" to 180L * 86400000L,
                            "+1 Tahun" to 365L * 86400000L
                        )
                        items(presets) { (label, duration) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    expirationDate = System.currentTimeMillis() + duration
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                shape = KelolaRadius.ShapeInput
                            )
                        }
                    }
                }

                if (showCustomExpiryDialog) {
                    CustomExpirationPickerDialog(
                        currentExpiration = expirationDate,
                        onConfirm = { expirationDate = it },
                        onClear = { expirationDate = null },
                        onDismiss = { showCustomExpiryDialog = false }
                    )
                }
            }
        },
        confirmButton = {
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
                        expirationDate
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = KelolaRadius.ShapeInput,
                modifier = Modifier.testTag("button_save_product")
            ) {
                Text("Simpan Produk", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}

@Composable
fun RestockDialog(
    product: ProductEntity,
    onConfirmRestock: (addQty: Int, note: String, recordExpense: Boolean, totalCost: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var addQtyText by remember { mutableStateOf("10") }
    var note by remember { mutableStateOf("Belanja stok baru") }
    var recordExpense by remember { mutableStateOf(product.costPrice > 0) }

    val addQty = FormatUtils.parseRupiahInput(addQtyText).toInt()
    val totalCost = addQty * product.costPrice
    val finalStock = product.stock + addQty

    val dialogFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Tambah Stok (Restock)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stok saat ini: ${product.stock} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = addQtyText,
                    onValueChange = { addQtyText = FormatUtils.formatRupiahInput(it) },
                    label = { Text("Jumlah Tambah Stok") },
                    suffix = { Text(product.unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = KelolaRadius.ShapeInput,
                    colors = dialogFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Stok setelah restock: $finalStock ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    placeholder = { Text("Misal: Kulakan di grosir") },
                    shape = KelolaRadius.ShapeInput,
                    colors = dialogFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (product.costPrice > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = recordExpense,
                            onCheckedChange = { recordExpense = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Catat sebagai pengeluaran",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total modal: ${FormatUtils.formatRupiah(totalCost)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (addQty > 0) {
                        onConfirmRestock(addQty, note, recordExpense, totalCost)
                    }
                },
                enabled = addQty > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = KelolaRadius.ShapeInput
            ) {
                Text("Simpan Stok", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun ReduceStockDialog(
    product: ProductEntity,
    onConfirmReduce: (reduceQty: Int, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reduceQtyText by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("Barang rusak") }
    val commonReasons = listOf("Barang rusak", "Kadaluarsa / Expired", "Konsumsi sendiri", "Hilang", "Koreksi stok")

    val reduceQty = FormatUtils.parseRupiahInput(reduceQtyText).toInt()
    val finalStock = (product.stock - reduceQty).coerceAtLeast(0)
    val isLossReason = reason.contains("rusak", ignoreCase = true) || reason.contains("expired", ignoreCase = true) || reason.contains("kadaluarsa", ignoreCase = true)
    val estimatedLoss = product.costPrice * reduceQty

    val reduceFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DangerRed,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        focusedLabelColor = DangerRed,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Kurangi Stok",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DangerRed
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Stok saat ini: ${product.stock} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = reduceQtyText,
                    onValueChange = { reduceQtyText = FormatUtils.formatRupiahInput(it) },
                    label = { Text("Jumlah Pengurangan") },
                    suffix = { Text(product.unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = KelolaRadius.ShapeInput,
                    colors = reduceFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Sisa stok menjadi: $finalStock ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DangerRed
                )

                // Quick Reason Chips
                Text(
                    text = "Pilih Alasan:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(commonReasons) { itemReason ->
                        val isSelected = reason == itemReason
                        FilterChip(
                            selected = isSelected,
                            onClick = { reason = itemReason },
                            label = { Text(itemReason, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            shape = KelolaRadius.ShapeInput,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (itemReason.contains("rusak", ignoreCase = true) || itemReason.contains("expired", ignoreCase = true)) DangerRed else MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Lengkap") },
                    placeholder = { Text("Misal: Rusak, Expired, dll") },
                    shape = KelolaRadius.ShapeInput,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notice if this counts as business loss
                if (isLossReason && reduceQty > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeInput,
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Otomatis Dicatat ke Laporan Kerugian",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                                Text(
                                    text = "Nilai kerugian: ${FormatUtils.formatRupiah(estimatedLoss)} (HPP x $reduceQty)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reduceQty > 0) {
                        onConfirmReduce(reduceQty, reason)
                    }
                },
                enabled = reduceQty > 0 && reduceQty <= product.stock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed,
                    contentColor = Color.White
                ),
                shape = KelolaRadius.ShapeInput
            ) {
                Text("Kurangi Stok", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onSaveCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Tambah Kategori Baru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = {
                    categoryName = it
                    isError = it.isBlank()
                },
                label = { Text("Nama Kategori") },
                placeholder = { Text("Misal: ATK / Perlengkapan") },
                isError = isError,
                shape = KelolaRadius.ShapeInput,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onSaveCategory(categoryName.trim())
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = KelolaRadius.ShapeInput
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
