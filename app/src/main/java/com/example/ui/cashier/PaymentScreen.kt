package com.example.ui.cashier

import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import com.example.ui.BankAccount

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import com.example.ui.theme.AdaptiveContainer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import com.example.data.local.entity.CustomerEntity
import com.example.ui.customers.AddEditCustomerDialog
import com.example.ui.customers.CustomerPickerDialog
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandDeep
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSky
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.KelolaTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils
import java.io.File

/**
 * PaymentScreen:
 * Laman khusus pembayaran setelah preview pembelian di kasir.
 * Menampilkan QRIS dalam ukuran besar ketika metode QRIS dipilih.
 */
@Composable
fun PaymentScreen(
    totalAmount: Long,
    totalItemCount: Int = 0,
    defaultPaymentMethod: String = "Tunai",
    qrisImagePath: String = "",
    qrisMerchantName: String = "",
    bankAccounts: List<BankAccount> = emptyList(),
    customers: List<CustomerEntity> = emptyList(),
    onAddNewCustomer: (CustomerEntity, (CustomerEntity) -> Unit) -> Unit = { _, _ -> },
    onConfirmSale: (
        paymentMethod: String,
        amountPaid: Long,
        customerName: String,
        customerPhone: String,
        debtNotes: String,
        isChangePending: Boolean,
        buyerNameForChange: String,
        changeNote: String,
        customerId: Long?
    ) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paymentMethods = listOf("Tunai", "QRIS", "Transfer", "E-Wallet", "Bayar Nanti")
    var selectedMethod by remember(defaultPaymentMethod) {
        mutableStateOf(paymentMethods.firstOrNull { it.equals(defaultPaymentMethod, ignoreCase = true) } ?: "Tunai")
    }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var cashReceivedInput by remember {
        mutableStateOf(FormatUtils.formatNumberWithDots(totalAmount))
    }
    val cashReceivedLong = FormatUtils.parseRupiahInput(cashReceivedInput)
    val change = (cashReceivedLong - totalAmount).coerceAtLeast(0L)

    // State untuk Pelanggan (Customer selection & quick add)
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showQuickAddCustomer by remember { mutableStateOf(false) }
    var manualCustomerName by remember { mutableStateOf("") }
    var saveAsNewCustomerChecked by remember { mutableStateOf(false) }

    // State untuk opsi Kembalian Belum Diberikan (Pending Change)
    var isChangePending by remember { mutableStateOf(false) }
    var buyerNameForChange by remember { mutableStateOf("") }
    var changeNote by remember { mutableStateOf("") }

    // State untuk Bayar Nanti (Kasbon)
    var debtorName by remember { mutableStateOf("") }
    var debtorPhone by remember { mutableStateOf("") }
    var debtorNote by remember { mutableStateOf("") }

    val quickSuggestions = remember(totalAmount) {
        val list = mutableListOf<Long>()
        list.add(totalAmount) // Uang Pas

        val denominations = listOf(10000L, 20000L, 50000L, 100000L)
        for (denom in denominations) {
            if (denom > totalAmount && !list.contains(denom)) {
                list.add(denom)
            }
        }
        val nextTen = ((totalAmount / 10000L) + 1L) * 10000L
        if (nextTen > totalAmount && !list.contains(nextTen)) {
            list.add(nextTen)
        }
        val nextFifty = ((totalAmount / 50000L) + 1L) * 50000L
        if (nextFifty > totalAmount && !list.contains(nextFifty)) {
            list.add(nextFifty)
        }
        list.sorted().take(5)
    }

    val qrisBitmap = remember(qrisImagePath, selectedMethod) {
        if (selectedMethod == "QRIS" && qrisImagePath.isNotBlank()) {
            val file = File(qrisImagePath)
            if (file.exists() && file.length() > 0) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else null
        } else null
    }

    val isFormValid = when (selectedMethod) {
        "Tunai" -> {
            val hasEnoughCash = cashReceivedLong >= totalAmount
            val changeValid = !isChangePending || buyerNameForChange.isNotBlank() || selectedCustomer != null
            hasEnoughCash && changeValid
        }
        "Bayar Nanti" -> debtorName.isNotBlank() || selectedCustomer != null
        else -> true
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- TOP APP BAR ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_back_from_payment")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Kasir",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Laman Pembayaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selesaikan transaksi belanja pelanggan",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- CONTENT BODY ---
        AdaptiveContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            maxWidth = 760.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Total Tagihan Banner Card
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .kelolaSoftShadow(KelolaRadius.ShapeCard, 2.dp),
                shape = KelolaRadius.ShapeCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BrandSky.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL TAGIHAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatRupiah(totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (totalItemCount > 0) {
                        Surface(
                            shape = KelolaRadius.ShapeSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "$totalItemCount Item",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Pelanggan (Opsional)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .kelolaSoftShadow(KelolaRadius.ShapeMedium, 1.dp),
                shape = KelolaRadius.ShapeMedium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pelanggan (Opsional)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (selectedCustomer != null) {
                            TextButton(
                                onClick = {
                                    selectedCustomer = null
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Lepas", color = DangerRed, fontSize = 12.sp)
                            }
                        }
                    }

                    if (selectedCustomer != null) {
                        // Selected Customer Card
                        Surface(
                            shape = KelolaRadius.ShapeSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = selectedCustomer!!.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = selectedCustomer!!.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (selectedCustomer!!.phone.isNotBlank()) {
                                            Text(
                                                text = selectedCustomer!!.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showCustomerPicker = true },
                                    shape = KelolaRadius.ShapeSmall,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Ganti", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        // Action buttons to pick from list or add new
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCustomerPicker = true },
                                shape = KelolaRadius.ShapeInput,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("button_pick_customer_payment")
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pilih Pelanggan", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = { showQuickAddCustomer = true },
                                shape = KelolaRadius.ShapeInput,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("button_add_customer_payment")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Manual Customer name field (keeps 100% backward compatibility)
                        OutlinedTextField(
                            value = manualCustomerName,
                            onValueChange = { manualCustomerName = it },
                            label = { Text("Nama Pelanggan Manual (Opsional)") },
                            placeholder = { Text("Ketik nama pembeli jika tidak memilih dari list") },
                            singleLine = true,
                            shape = KelolaRadius.ShapeInput,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("input_manual_customer_name")
                        )

                        if (manualCustomerName.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { saveAsNewCustomerChecked = !saveAsNewCustomerChecked },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = saveAsNewCustomerChecked,
                                    onCheckedChange = { saveAsNewCustomerChecked = it }
                                )
                                Text(
                                    text = "Simpan sebagai pelanggan baru",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 2. Payment Method Selector Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih Metode Pembayaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(paymentMethods) { method ->
                        val isSelected = method == selectedMethod
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMethod = method },
                            shape = KelolaRadius.ShapeSmall,
                            label = {
                                Text(
                                    text = method,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                val icon = when (method) {
                                    "Tunai" -> Icons.Default.LocalAtm
                                    "QRIS" -> Icons.Default.QrCode
                                    "Transfer" -> Icons.Default.AccountBalance
                                    "E-Wallet" -> Icons.Default.CreditCard
                                    "Bayar Nanti" -> Icons.Default.HourglassTop
                                    else -> Icons.Default.Receipt
                                }
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
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else BorderLight
                            ),
                            modifier = Modifier.testTag("chip_payment_method_$method")
                        )
                    }
                }
            }

            // 3. Dynamic Method Content Area
            when (selectedMethod) {
                "QRIS" -> {
                    // =========================================================================
                    // QRIS DISPLAY BESAR & JELAS
                    // =========================================================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kelolaSoftShadow(KelolaRadius.ShapeCard, 4.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = qrisMerchantName.ifBlank { "QRIS TOKO KELOLA" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Scan QRIS di bawah ini untuk membayar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (qrisBitmap != null) {
                                // TAMPILAN QRIS BESAR (Ukuran 280dp - 300dp)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(2.dp, BorderLight),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .size(280.dp)
                                        .testTag("large_qris_image_container")
                                ) {
                                    Image(
                                        bitmap = qrisBitmap,
                                        contentDescription = "Kode QRIS Toko",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp)
                                    )
                                }
                            } else {
                                // Placeholder jika belum upload QRIS
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = WarningContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.5.dp, WarningAmber.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = WarningAmber,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Text(
                                            text = "Gambar QRIS Belum Diunggah",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        )
                                        Text(
                                            text = "Anda dapat mengunggah gambar QRIS toko Anda di menu Pengaturan > QRIS Toko agar kode QR tampil besar di sini.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 1.dp
                            )

                            Text(
                                text = "Nominal yang harus dibayar: ${FormatUtils.formatRupiah(totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Pastikan pembeli telah menunjukkan bukti transfer / pembayaran sukses sebelum mengonfirmasi.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                "Tunai" -> {
                    // =========================================================================
                    // TUNAI MODE
                    // =========================================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Uang Tunai Diterima",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Quick Presets
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(quickSuggestions) { amount ->
                                    val isUangPas = amount == totalAmount
                                    val isSelected = cashReceivedLong == amount
                                    Surface(
                                        shape = KelolaRadius.ShapeSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderLight),
                                        modifier = Modifier.clickable {
                                            cashReceivedInput = FormatUtils.formatNumberWithDots(amount)
                                        }
                                    ) {
                                        Text(
                                            text = if (isUangPas) "Uang Pas" else FormatUtils.formatRupiah(amount),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = cashReceivedInput,
                                onValueChange = { input ->
                                    cashReceivedInput = FormatUtils.formatRupiahInput(input)
                                },
                                prefix = { Text("Rp ", fontWeight = FontWeight.SemiBold) },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                isError = cashReceivedLong < totalAmount,
                                supportingText = {
                                    if (cashReceivedLong < totalAmount) {
                                        Text(
                                            text = "Kurang ${FormatUtils.formatRupiah(totalAmount - cashReceivedLong)}",
                                            color = DangerRed
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_cash_received")
                            )

                            // Kembalian Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = KelolaRadius.ShapeInput,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (cashReceivedLong < totalAmount) DangerContainer else SuccessContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kembalian:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (cashReceivedLong < totalAmount) DangerRed else SuccessGreen
                                    )
                                    Text(
                                        text = if (cashReceivedLong < totalAmount) "Uang Belum Cukup" else FormatUtils.formatRupiah(change),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cashReceivedLong < totalAmount) DangerRed else SuccessGreen
                                    )
                                }
                            }

                            // Opsi Kembalian Belum Diberikan (Pending Change)
                            if (change > 0 && cashReceivedLong >= totalAmount) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = KelolaRadius.ShapeCard,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isChangePending) WarningContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChangePending) WarningAmber else Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Kembalian Belum Diberikan?",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isChangePending) WarningAmber else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Catat sebagai hutang kembalian ke pembeli",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = isChangePending,
                                                onCheckedChange = { isChangePending = it },
                                                modifier = Modifier.testTag("switch_pending_change")
                                            )
                                        }

                                        if (isChangePending) {
                                            OutlinedTextField(
                                                value = buyerNameForChange,
                                                onValueChange = { buyerNameForChange = it },
                                                label = { Text("Nama Pembeli (Wajib)*") },
                                                placeholder = { Text("Contoh: Kak Dedi / Meja 3") },
                                                singleLine = true,
                                                shape = KelolaRadius.ShapeInput,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = WarningAmber,
                                                    unfocusedBorderColor = BorderLight
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("input_buyer_name_change")
                                            )

                                            OutlinedTextField(
                                                value = changeNote,
                                                onValueChange = { changeNote = it },
                                                label = { Text("Catatan Tambahan (Opsional)") },
                                                placeholder = { Text("Misal: Mau diambil nanti pas bubaran kelas") },
                                                singleLine = true,
                                                shape = KelolaRadius.ShapeInput,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = WarningAmber,
                                                    unfocusedBorderColor = BorderLight
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Bayar Nanti" -> {
                    // =========================================================================
                    // BAYAR NANTI (KASBON)
                    // =========================================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Catat Kasbon / Bayar Nanti",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Pelanggan dapat membawa belanjaan sekarang dan melunasinya di kemudian hari.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = debtorName,
                                onValueChange = { debtorName = it },
                                label = { Text("Nama Pelanggan (Wajib)*") },
                                placeholder = { Text("Nama teman / mahasiswa") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("input_debtor_name")
                            )

                            OutlinedTextField(
                                value = debtorPhone,
                                onValueChange = { debtorPhone = it },
                                label = { Text("No. HP / WhatsApp (Opsional)") },
                                placeholder = { Text("08xxxxxxxxxx") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = debtorNote,
                                onValueChange = { debtorNote = it },
                                label = { Text("Catatan / Jatuh Tempo (Opsional)") },
                                placeholder = { Text("Misal: Janji bayar besok lusa") },
                                singleLine = true,
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = BorderLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                "Transfer" -> {
                    // =========================================================================
                    // REKENING TRANSFER TOKO (DENGAN TOMBOL SALIN)
                    // =========================================================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kelolaSoftShadow(KelolaRadius.ShapeCard, 4.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Rekening Transfer Toko",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Pilih / salin nomor rekening untuk ditransfer pembeli",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (bankAccounts.isEmpty()) {
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = WarningContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Belum Ada Rekening Terdaftar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = WarningAmber
                                        )
                                        Text(
                                            text = "Anda dapat menambahkan hingga 5 akun rekening bank di menu Pengaturan > Akun Rekening Bank.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    bankAccounts.forEach { acc ->
                                        Card(
                                            shape = KelolaRadius.ShapeInput,
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                                                    Surface(
                                                        shape = KelolaRadius.ShapeSmall,
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                    ) {
                                                        Text(
                                                            text = acc.bankName.uppercase(),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = acc.accountNumber,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "a.n. ${acc.accountName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                // Tombol Salin
                                                Surface(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(acc.accountNumber))
                                                        Toast.makeText(
                                                            context,
                                                            "Nomor rekening ${acc.bankName} berhasil disalin!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    shape = KelolaRadius.ShapeSmall,
                                                    color = MaterialTheme.colorScheme.surface,
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                                    modifier = Modifier.height(36.dp).testTag("button_copy_account_${acc.id}")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ContentCopy,
                                                            contentDescription = "Salin",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Salin",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 1.dp
                            )

                            Text(
                                text = "Total yang harus ditransfer: ${FormatUtils.formatRupiah(totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Pastikan mutasi atau bukti transfer sudah dicek sebelum konfirmasi pembayaran.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                else -> {
                    // E-Wallet
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedMethod == "Transfer") Icons.Default.AccountBalance else Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Pembayaran via $selectedMethod",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pastikan dana sejumlah ${FormatUtils.formatRupiah(totalAmount)} telah berhasil masuk ke rekening / akun e-wallet toko Anda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- BOTTOM ACTION BAR ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val amountPaid = when (selectedMethod) {
                            "Tunai" -> cashReceivedLong
                            else -> totalAmount
                        }

                        val finalCustName = selectedCustomer?.name
                            ?: manualCustomerName.trim().ifEmpty {
                                if (selectedMethod == "Bayar Nanti") debtorName.trim()
                                else if (isChangePending) buyerNameForChange.trim()
                                else ""
                            }
                        val finalCustPhone = selectedCustomer?.phone
                            ?: (if (selectedMethod == "Bayar Nanti") debtorPhone.trim() else "")
                        val finalDebtorName = if (selectedMethod == "Bayar Nanti") {
                            debtorName.trim().ifEmpty { finalCustName }
                        } else debtorName.trim()
                        val finalBuyerNameChange = if (isChangePending) {
                            buyerNameForChange.trim().ifEmpty { finalCustName }
                        } else buyerNameForChange.trim()

                        if (selectedCustomer != null) {
                            onConfirmSale(
                                selectedMethod,
                                amountPaid,
                                finalCustName,
                                finalCustPhone,
                                debtorNote,
                                isChangePending,
                                finalBuyerNameChange,
                                changeNote,
                                selectedCustomer!!.id
                            )
                        } else if (saveAsNewCustomerChecked && finalCustName.isNotBlank()) {
                            val newCustomer = CustomerEntity(
                                name = finalCustName,
                                phone = finalCustPhone
                            )
                            onAddNewCustomer(newCustomer) { saved ->
                                onConfirmSale(
                                    selectedMethod,
                                    amountPaid,
                                    saved.name,
                                    saved.phone,
                                    debtorNote,
                                    isChangePending,
                                    finalBuyerNameChange,
                                    changeNote,
                                    saved.id
                                )
                            }
                        } else {
                            onConfirmSale(
                                selectedMethod,
                                amountPaid,
                                finalCustName,
                                finalCustPhone,
                                debtorNote,
                                isChangePending,
                                finalBuyerNameChange,
                                changeNote,
                                null
                            )
                        }
                    },
                    enabled = isFormValid,
                    shape = KelolaRadius.ShapeInput,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMethod == "Bayar Nanti") WarningAmber else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(KelolaSpacing.ButtonHeightCta)
                        .testTag("button_confirm_payment")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (selectedMethod) {
                            "QRIS" -> "Konfirmasi Pembayaran QRIS"
                            "Tunai" -> "Terima Tunai & Selesaikan"
                            "Bayar Nanti" -> "Catat Kasbon"
                            else -> "Konfirmasi $selectedMethod"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
    }

    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = customers,
            selectedCustomer = selectedCustomer,
            onSelectCustomer = { cust ->
                selectedCustomer = cust
                if (selectedMethod == "Bayar Nanti") {
                    debtorName = cust.name
                    if (cust.phone.isNotBlank()) debtorPhone = cust.phone
                }
                if (isChangePending) {
                    buyerNameForChange = cust.name
                }
            },
            onAddNewCustomer = {
                showQuickAddCustomer = true
            },
            onClearSelection = {
                selectedCustomer = null
            },
            onDismiss = { showCustomerPicker = false }
        )
    }

    if (showQuickAddCustomer) {
        AddEditCustomerDialog(
            initialCustomer = null,
            onSave = { newCust ->
                onAddNewCustomer(newCust) { saved ->
                    selectedCustomer = saved
                    if (selectedMethod == "Bayar Nanti") {
                        debtorName = saved.name
                        if (saved.phone.isNotBlank()) debtorPhone = saved.phone
                    }
                    if (isChangePending) {
                        buyerNameForChange = saved.name
                    }
                }
                showQuickAddCustomer = false
            },
            onDismiss = { showQuickAddCustomer = false }
        )
    }
}
