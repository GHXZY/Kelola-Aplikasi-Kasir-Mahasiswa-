package com.example.ui.debts

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
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
 * DebtPaymentScreen:
 * Laman khusus penuh untuk Pelunasan Kasbon (Bayar Nanti).
 * Memberikan pengalaman pembayaran komprehensif: pelunasan lunas / cicil,
 * pilihan metode bayar (Tunai dengan hitung kembalian, QRIS dengan tampilan besar 280dp, dll.),
 * serta input catatan pelunasan.
 */
@Composable
fun DebtPaymentScreen(
    debt: DebtEntity,
    qrisImagePath: String = "",
    qrisMerchantName: String = "",
    bankAccounts: List<BankAccount> = emptyList(),
    onConfirmSettle: (debtId: Long, amount: Long, paymentMethod: String, note: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFullPayment by remember { mutableStateOf(true) }
    var partialAmountInput by remember {
        mutableStateOf(FormatUtils.formatNumberWithDots(debt.remainingAmount))
    }
    var selectedMethod by remember { mutableStateOf("Tunai") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var noteInput by remember { mutableStateOf("") }

    val paymentMethods = listOf("Tunai", "QRIS", "Transfer", "E-Wallet", "Lainnya")

    val parsedPartialAmount = FormatUtils.parseRupiahInput(partialAmountInput)
    val settleAmount = if (isFullPayment) debt.remainingAmount else parsedPartialAmount
    val isValidAmount = settleAmount > 0 && settleAmount <= debt.remainingAmount

    // Tunai cash calculation
    var cashReceivedInput by remember(settleAmount) {
        mutableStateOf(FormatUtils.formatNumberWithDots(settleAmount))
    }
    val cashReceivedLong = FormatUtils.parseRupiahInput(cashReceivedInput)
    val change = (cashReceivedLong - settleAmount).coerceAtLeast(0L)

    // Quick cash suggestions
    val quickSuggestions = remember(settleAmount) {
        val list = mutableListOf<Long>()
        list.add(settleAmount) // Uang Pas
        val denominations = listOf(10000L, 20000L, 50000L, 100000L)
        for (denom in denominations) {
            if (denom > settleAmount && !list.contains(denom)) {
                list.add(denom)
            }
        }
        val nextTen = ((settleAmount / 10000L) + 1L) * 10000L
        if (nextTen > settleAmount && !list.contains(nextTen)) {
            list.add(nextTen)
        }
        val nextFifty = ((settleAmount / 50000L) + 1L) * 50000L
        if (nextFifty > settleAmount && !list.contains(nextFifty)) {
            list.add(nextFifty)
        }
        list.sorted().take(5)
    }

    // Load QRIS Bitmap if available
    val qrisBitmap = remember(qrisImagePath, selectedMethod) {
        if (selectedMethod == "QRIS" && qrisImagePath.isNotBlank()) {
            try {
                val file = File(qrisImagePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. TOP BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .testTag("button_back_debt_payment")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Pelunasan Kasbon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pelanggan: ${debt.customerName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. SCROLLABLE BODY
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(KelolaSpacing.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // A. Debtor & Balance Info Card
            item {
                Card(
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(KelolaRadius.ShapeCard, 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = WarningAmber.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = WarningAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = debt.customerName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (debt.customerPhone.isNotBlank()) {
                                        Text(
                                            text = debt.customerPhone,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Tag Status Kasbon
                            Surface(
                                shape = KelolaRadius.ShapeSmall,
                                color = DangerContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "Belum Lunas",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Financial breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Kasbon", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatRupiah(debt.amount), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sudah Dibayar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatRupiah((debt.amount - debt.remainingAmount).coerceAtLeast(0L)), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Sisa Tagihan", style = MaterialTheme.typography.bodySmall, color = DangerRed, fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatRupiah(debt.remainingAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DangerRed)
                            }
                        }
                    }
                }
            }

            // B. Payment Type Selection: Pelunasan Penuh vs Cicil / Sebagian
            item {
                Card(
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Jenis Pembayaran",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Segmented Toggle
                        Surface(
                            shape = KelolaRadius.ShapeInput,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Tab Lunas
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = if (isFullPayment) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isFullPayment) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(KelolaRadius.ShapeInput)
                                        .clickable {
                                            isFullPayment = true
                                            cashReceivedInput = FormatUtils.formatNumberWithDots(debt.remainingAmount)
                                        }
                                        .testTag("tab_full_settle")
                                ) {
                                    Text(
                                        text = "Pelunasan Penuh",
                                        fontWeight = if (isFullPayment) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }

                                // Tab Cicil
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = if (!isFullPayment) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!isFullPayment) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(KelolaRadius.ShapeInput)
                                        .clickable { isFullPayment = false }
                                        .testTag("tab_partial_settle")
                                ) {
                                    Text(
                                        text = "Cicil / Sebagian",
                                        fontWeight = if (!isFullPayment) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        // Nominal Input if Cicil
                        if (!isFullPayment) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Nominal Cicilan yang Dibayar",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = partialAmountInput,
                                    onValueChange = { input ->
                                        partialAmountInput = FormatUtils.formatRupiahInput(input)
                                        cashReceivedInput = partialAmountInput
                                    },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = KelolaRadius.ShapeInput,
                                    singleLine = true,
                                    isError = !isValidAmount && partialAmountInput.isNotBlank(),
                                    supportingText = {
                                        if (settleAmount > debt.remainingAmount) {
                                            Text("Nominal melebihi sisa tagihan kasbon!", color = DangerRed)
                                        } else if (settleAmount <= 0 && partialAmountInput.isNotBlank()) {
                                            Text("Nominal harus lebih dari Rp 0", color = DangerRed)
                                        } else {
                                            Text("Sisa setelah pembayaran: ${FormatUtils.formatRupiah((debt.remainingAmount - settleAmount).coerceAtLeast(0L))}")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_partial_amount")
                                )

                                // Quick Percentage / Fraction Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val quarter = (debt.remainingAmount * 0.25).toLong()
                                    val half = (debt.remainingAmount * 0.5).toLong()
                                    val threeQuarters = (debt.remainingAmount * 0.75).toLong()

                                    listOf("25%" to quarter, "50%" to half, "75%" to threeQuarters).forEach { (label, amt) ->
                                        if (amt > 0) {
                                            OutlinedButton(
                                                onClick = {
                                                    partialAmountInput = FormatUtils.formatNumberWithDots(amt)
                                                    cashReceivedInput = partialAmountInput
                                                },
                                                shape = KelolaRadius.ShapeSmall,
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // C. Payment Method Selection Chips
            item {
                Card(
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Metode Pembayaran",
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
                                    modifier = Modifier.testTag("chip_settle_method_$method")
                                )
                            }
                        }
                    }
                }
            }

            // D. Dynamic Method Specific Content
            when (selectedMethod) {
                "QRIS" -> {
                    item {
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
                                    text = "Scan QRIS di bawah ini untuk pelunasan kasbon",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (qrisBitmap != null) {
                                    // TAMPILAN QRIS BESAR (Ukuran 280dp)
                                    Surface(
                                        shape = KelolaRadius.ShapeLarge,
                                        color = Color.White,
                                        border = BorderStroke(2.dp, BorderLight),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .size(280.dp)
                                            .testTag("large_qris_debt_image")
                                    ) {
                                        Image(
                                            bitmap = qrisBitmap.asImageBitmap(),
                                            contentDescription = "Kode QRIS Toko",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp)
                                        )
                                    }
                                } else {
                                    // Placeholder jika QRIS belum diunggah
                                    Surface(
                                        shape = KelolaRadius.ShapeLarge,
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
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = WarningAmber,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Text(
                                                text = "Foto QRIS Belum Diunggah",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Anda dapat mengunggah barcode QRIS toko Anda di menu Pengaturan.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                // Info nominal QRIS
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Nominal yang Ditransfer",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = FormatUtils.formatRupiah(settleAmount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Tunai" -> {
                    item {
                        Card(
                            shape = KelolaRadius.ShapeCard,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Pembayaran Tunai",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedTextField(
                                    value = cashReceivedInput,
                                    onValueChange = { input ->
                                        cashReceivedInput = FormatUtils.formatRupiahInput(input)
                                    },
                                    label = { Text("Uang Diterima dari Pelanggan") },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = KelolaRadius.ShapeInput,
                                    singleLine = true,
                                    trailingIcon = {
                                        OutlinedButton(
                                            onClick = { cashReceivedInput = FormatUtils.formatNumberWithDots(settleAmount) },
                                            shape = KelolaRadius.ShapeSmall,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.padding(end = 4.dp).height(32.dp)
                                        ) {
                                            Text("Uang Pas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_cash_settle")
                                )

                                // Quick suggestion chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(quickSuggestions) { denom ->
                                        FilterChip(
                                            selected = cashReceivedLong == denom,
                                            onClick = { cashReceivedInput = FormatUtils.formatNumberWithDots(denom) },
                                            shape = KelolaRadius.ShapeSmall,
                                            label = {
                                                Text(
                                                    text = if (denom == settleAmount) "Pas" else FormatUtils.formatRupiah(denom),
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            border = BorderStroke(1.dp, BorderLight)
                                        )
                                    }
                                }

                                // Kembalian Card
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = if (cashReceivedLong >= settleAmount) SuccessContainer.copy(alpha = 0.5f) else DangerContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (cashReceivedLong >= settleAmount) "Uang Kembalian" else "Uang Kurang",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (cashReceivedLong >= settleAmount) SuccessGreen else DangerRed
                                        )
                                        Text(
                                            text = if (cashReceivedLong >= settleAmount)
                                                FormatUtils.formatRupiah(change)
                                            else
                                                FormatUtils.formatRupiah(settleAmount - cashReceivedLong),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cashReceivedLong >= settleAmount) SuccessGreen else DangerRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Transfer" -> {
                    item {
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
                                            text = "Pilih / salin nomor rekening untuk pelunasan",
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
                                                text = "Atur rekening transfer toko (maks. 5 rekening) di menu Pengaturan > Akun Rekening Bank.",
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
                                                        modifier = Modifier.height(36.dp).testTag("button_settle_copy_account_${acc.id}")
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
                                    text = "Total yang harus ditransfer: ${FormatUtils.formatRupiah(settleAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = "Pastikan pelanggan telah mentransfer sebelum menekan tombol Konfirmasi Pelunasan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        Card(
                            shape = KelolaRadius.ShapeCard,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Pembayaran via $selectedMethod",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pastikan pelanggan telah mentransfer sejumlah ${FormatUtils.formatRupiah(settleAmount)} sebelum mengonfirmasi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // E. Catatan Pelunasan (Opsional)
            item {
                Card(
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Catatan Pelunasan (Opsional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("Misal: Titip via teman, transfer BCA, dsb.") },
                            shape = KelolaRadius.ShapeInput,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 3. STICKY BOTTOM ACTION BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isFullPayment) "Pelunasan Penuh" else "Pembayaran Cicilan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatRupiah(settleAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!isFullPayment && isValidAmount) {
                        Surface(
                            shape = KelolaRadius.ShapeSm,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Sisa: ${FormatUtils.formatRupiah(debt.remainingAmount - settleAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (isValidAmount) {
                            onConfirmSettle(
                                debt.id,
                                settleAmount,
                                selectedMethod,
                                noteInput.trim()
                            )
                        }
                    },
                    enabled = isValidAmount && (selectedMethod != "Tunai" || cashReceivedLong >= settleAmount),
                    shape = KelolaRadius.ShapeInput,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(KelolaSpacing.ButtonHeightCta)
                        .testTag("button_confirm_debt_settle")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Konfirmasi Pelunasan",
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
