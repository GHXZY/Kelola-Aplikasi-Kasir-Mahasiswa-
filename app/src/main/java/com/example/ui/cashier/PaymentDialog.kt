package com.example.ui.cashier

import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import com.example.ui.BankAccount

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import java.io.File
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandDeep
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GradientBrand
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    totalAmount: Long,
    sheetState: SheetState,
    defaultPaymentMethod: String = "Tunai",
    qrisImagePath: String = "",
    qrisMerchantName: String = "",
    bankAccounts: List<BankAccount> = emptyList(),
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
    onOpenDebtsScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    val paymentMethods = listOf("Tunai", "QRIS", "Transfer", "E-Wallet")
    var selectedMethod by remember(defaultPaymentMethod) {
        mutableStateOf(paymentMethods.firstOrNull { it.equals(defaultPaymentMethod, ignoreCase = true) } ?: "Tunai")
    }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var cashReceivedInput by remember { mutableStateOf(FormatUtils.formatNumberWithDots(totalAmount)) }
    val cashReceivedLong = FormatUtils.parseRupiahInput(cashReceivedInput)
    val change = (cashReceivedLong - totalAmount).coerceAtLeast(0L)

    // State untuk opsi Kembalian Belum Diberikan
    var isChangePending by remember { mutableStateOf(false) }
    var buyerNameForChange by remember { mutableStateOf("") }
    var changeNote by remember { mutableStateOf("") }

    // State untuk Bayar Nanti Dialog
    var showDebtInputDialog by remember { mutableStateOf(false) }
    var debtorName by remember { mutableStateOf("") }
    var debtorPhone by remember { mutableStateOf("") }
    var debtorNote by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }

    val quickSuggestions = remember(totalAmount) {
        val list = mutableListOf<Long>()
        list.add(totalAmount) // Uang Pas

        val denominations = listOf(2000L, 5000L, 10000L, 20000L, 50000L, 100000L)
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

    val isFormValid = when (selectedMethod) {
        "Tunai" -> {
            val hasEnoughCash = cashReceivedLong >= totalAmount
            val changeValid = !isChangePending || buyerNameForChange.isNotBlank()
            hasEnoughCash && changeValid
        }
        else -> true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = KelolaRadius.ShapeSheet
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pembayaran",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KelolaRadius.ShapeCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Tagihan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatRupiah(totalAmount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Warning if QRIS is default but image is missing
            if (defaultPaymentMethod == "QRIS" && qrisImagePath.isBlank()) {
                Surface(
                    shape = KelolaRadius.ShapeInput,
                    color = WarningContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                            text = "QRIS belum diatur. Tambahkan gambar QRIS terlebih dahulu di Pengaturan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Payment Methods
            Text(
                text = "Metode Pembayaran",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            val icon = when (method) {
                                "Tunai" -> Icons.Default.LocalAtm
                                "QRIS" -> Icons.Default.QrCode
                                "Transfer" -> Icons.Default.AccountBalance
                                "E-Wallet" -> Icons.Default.CreditCard
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
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderLight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cash input section if Tunai
            if (selectedMethod == "Tunai") {
                Text(
                    text = "Uang Diterima",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick chips
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cashReceivedInput,
                    onValueChange = { input ->
                        cashReceivedInput = FormatUtils.formatRupiahInput(input)
                    },
                    prefix = { Text("Rp ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                    placeholder = { Text("0", color = KelolaTheme.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = KelolaRadius.ShapeInput,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_cash_received")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kembalian Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(
                        containerColor = if (change > 0) SuccessContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kembalian",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (change > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatRupiah(change),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (change > 0) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Tandai Kembalian Belum Diberikan (Pending Change)
                if (change > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChangePending) WarningContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kembalian Belum Diberikan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tandai jika uang pas/kembalian belum diserahkan ke pembeli",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isChangePending,
                                    onCheckedChange = { isChangePending = it }
                                )
                            }

                            if (isChangePending) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = buyerNameForChange,
                                    onValueChange = { buyerNameForChange = it },
                                    label = { Text("Nama Mahasiswa / Pembeli *") },
                                    placeholder = { Text("Contoh: Rizky (Teknik Mesin)") },
                                    singleLine = true,
                                    isError = buyerNameForChange.isBlank(),
                                    shape = KelolaRadius.ShapeInput,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = changeNote,
                                    onValueChange = { changeNote = it },
                                    label = { Text("Catatan Tambahan (Opsional)") },
                                    placeholder = { Text("Contoh: Ambil jam makan siang") },
                                    singleLine = true,
                                    shape = KelolaRadius.ShapeInput,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            } else {
                // Non-cash notice / QRIS card
                val qrisBitmap = remember(qrisImagePath, selectedMethod) {
                    if (selectedMethod == "QRIS" && qrisImagePath.isNotBlank()) {
                        val file = File(qrisImagePath)
                        if (file.exists() && file.length() > 0) {
                            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                        } else null
                    } else null
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedMethod == "Transfer") {
                            // Transfer Bank Accounts
                            Text(
                                text = "Rekening Transfer Toko",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (bankAccounts.isEmpty()) {
                                Surface(
                                    shape = KelolaRadius.ShapeSmall,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Belum ada rekening transfer yang didaftarkan. Atur di menu Pengaturan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    bankAccounts.forEach { acc ->
                                        Surface(
                                            shape = KelolaRadius.ShapeSmall,
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = acc.bankName.uppercase(),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = acc.accountNumber,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = "a.n. ${acc.accountName}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Surface(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(acc.accountNumber))
                                                        Toast.makeText(context, "Nomor rekening ${acc.bankName} berhasil disalin!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = KelolaRadius.ShapeSmall,
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "Total Bayar: ${FormatUtils.formatRupiah(totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (selectedMethod == "QRIS" && qrisBitmap != null) {
                            // Real QRIS Preview Card
                            Text(
                                text = qrisMerchantName.ifBlank { "QRIS Toko" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = KelolaRadius.ShapeInput,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(190.dp)
                            ) {
                                Image(
                                    bitmap = qrisBitmap,
                                    contentDescription = "Kode QRIS Toko",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                            Text(
                                text = "Total Bayar: ${FormatUtils.formatRupiah(totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Minta pelanggan scan kode QRIS di atas dan pastikan bukti pembayaran sukses telah diterima.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedMethod == "QRIS") Icons.Default.QrCode else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = if (selectedMethod == "QRIS") "QRIS Pembayaran" else "Metode: $selectedMethod",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total Bayar: ${FormatUtils.formatRupiah(totalAmount)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (selectedMethod == "QRIS") {
                                    "Gambar QRIS belum diunggah di Pengaturan. Pastikan pembeli telah scan kode QRIS fisik/cetak dan menunjukkan notifikasi sukses."
                                } else {
                                    "Pastikan pembeli telah menunjukkan bukti transfer $selectedMethod yang valid."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Bayar Nanti (Soft Amber, Radius 14px, Tanpa Border)
            Surface(
                onClick = { showDebtInputDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("button_pay_later"),
                shape = KelolaRadius.ShapeInput,
                color = WarningContainer
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = WarningAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bayar Nanti",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarningAmber,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tombol Selesaikan Pembayaran (CTA: Gradient Brand, Radius 14px, Teks Putih)
            Surface(
                onClick = {
                    if (isFormValid) {
                        isSubmitting = true
                        onConfirmSale(
                            selectedMethod,
                            if (selectedMethod == "Tunai") cashReceivedLong else totalAmount,
                            "",
                            "",
                            "",
                            isChangePending,
                            buyerNameForChange,
                            changeNote,
                            null
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(KelolaRadius.ShapeInput)
                    .testTag("button_complete_transaction"),
                shape = KelolaRadius.ShapeInput,
                color = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isFormValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selesaikan Pembayaran",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isFormValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
        }
    }

    // Modal Dialog Bayar Nanti (Radius 32px, Tanpa Border)
    if (showDebtInputDialog) {
        AlertDialog(
            onDismissRequest = { showDebtInputDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WarningContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Catat Bayar Nanti (Kasbon)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = WarningContainer.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Tagihan",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatRupiah(totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }

                    OutlinedTextField(
                        value = debtorName,
                        onValueChange = { debtorName = it },
                        label = { Text("Nama Pelanggan / Kasbon *") },
                        placeholder = { Text("Contoh: Pak Budi, Bu Siti") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        isError = debtorName.isBlank(),
                        supportingText = {
                            if (debtorName.isBlank()) {
                                Text("Nama wajib diisi", color = DangerRed)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debtor_name")
                    )

                    OutlinedTextField(
                        value = debtorPhone,
                        onValueChange = { debtorPhone = it },
                        label = { Text("No. HP / WhatsApp (Opsional)") },
                        placeholder = { Text("08xxxxxxxxxx") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debtor_phone")
                    )

                    OutlinedTextField(
                        value = debtorNote,
                        onValueChange = { debtorNote = it },
                        label = { Text("Catatan / Janji Bayar (Opsional)") },
                        placeholder = { Text("Contoh: Bayar akhir pekan") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        singleLine = true,
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderLight,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debtor_note")
                    )

                    TextButton(
                        onClick = {
                            showDebtInputDialog = false
                            onOpenDebtsScreen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_view_debtors_list")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Laman Kasbon Langsung", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Surface(
                    onClick = {
                        if (debtorName.isNotBlank()) {
                            showDebtInputDialog = false
                            onConfirmSale("Bayar Nanti", 0L, debtorName, debtorPhone, debtorNote, false, "", "", null)
                        }
                    },
                    enabled = debtorName.isNotBlank(),
                    shape = KelolaRadius.ShapeInput,
                    color = if (debtorName.isNotBlank()) WarningAmber else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("button_confirm_debt")
                ) {
                    Text(
                        text = "Simpan ke Kasbon",
                        fontWeight = FontWeight.SemiBold,
                        color = if (debtorName.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDebtInputDialog = false },
                    shape = KelolaRadius.ShapeInput
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = KelolaRadius.ShapeLarge,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
