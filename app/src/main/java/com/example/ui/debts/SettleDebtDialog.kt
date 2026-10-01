package com.example.ui.debts

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.util.FormatUtils

@Composable
fun SettleDebtDialog(
    debt: DebtEntity,
    onConfirmSettle: (debtId: Long, amount: Long, paymentMethod: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isFullPayment by remember { mutableStateOf(true) }
    var partialAmountInput by remember { mutableStateOf(FormatUtils.formatNumberWithDots(debt.remainingAmount)) }
    var selectedMethod by remember { mutableStateOf("Tunai") }
    var noteInput by remember { mutableStateOf("") }

    val paymentMethods = listOf("Tunai", "QRIS", "Transfer", "E-Wallet", "Lainnya")

    val parsedPartialAmount = FormatUtils.parseRupiahInput(partialAmountInput)
    val settleAmount = if (isFullPayment) debt.remainingAmount else parsedPartialAmount

    val isAmountValid = settleAmount > 0 && settleAmount <= debt.remainingAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PriceCheck,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pelunasan Kasbon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Debtor overview card (Tanpa border)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KelolaRadius.ShapeInput,
                    colors = CardDefaults.cardColors(containerColor = WarningContainer.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = debt.customerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (debt.customerPhone.isNotBlank()) {
                            Text(
                                text = "No. Telp: ${debt.customerPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = WarningAmber.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Awal:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.formatRupiah(debt.amount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sisa Kasbon:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = DangerRed
                            )
                            Text(
                                text = FormatUtils.formatRupiah(debt.remainingAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }
                    }
                }

                // Payment Type options
                Text(
                    text = "Pilihan Pembayaran",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isFullPayment,
                        onClick = {
                            isFullPayment = true
                            partialAmountInput = FormatUtils.formatNumberWithDots(debt.remainingAmount)
                        },
                        shape = KelolaRadius.ShapeSmall,
                        label = { Text("Pelunasan Penuh") },
                        leadingIcon = if (isFullPayment) {
                            {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_full_payment")
                    )

                    FilterChip(
                        selected = !isFullPayment,
                        onClick = {
                            isFullPayment = false
                            if (parsedPartialAmount <= 0) {
                                partialAmountInput = FormatUtils.formatNumberWithDots(debt.remainingAmount / 2)
                            }
                        },
                        shape = KelolaRadius.ShapeSmall,
                        label = { Text("Cicil / Sebagian") },
                        leadingIcon = if (!isFullPayment) {
                            {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_partial_payment")
                    )
                }

                // If partial payment, show amount input field
                if (!isFullPayment) {
                    OutlinedTextField(
                        value = partialAmountInput,
                        onValueChange = { partialAmountInput = FormatUtils.formatRupiahInput(it) },
                        label = { Text("Nominal Bayar (Rp)") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !isAmountValid,
                        supportingText = {
                            if (parsedPartialAmount > debt.remainingAmount) {
                                Text("Tidak boleh melebihi sisa kasbon", color = DangerRed)
                            } else if (parsedPartialAmount <= 0) {
                                Text("Nominal bayar harus lebih dari Rp 0", color = DangerRed)
                            } else {
                                val sisa = debt.remainingAmount - parsedPartialAmount
                                Text("Sisa kasbon setelah bayar: ${FormatUtils.formatRupiah(sisa)}")
                            }
                        },
                        shape = KelolaRadius.ShapeInput,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settle_partial_amount")
                    )
                }

                // Total to be paid summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KelolaRadius.ShapeInput,
                    colors = CardDefaults.cardColors(containerColor = SuccessContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nominal yang Dilunasi:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = SuccessGreen
                        )
                        Text(
                            text = FormatUtils.formatRupiah(settleAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }

                // Payment Method Selector
                Text(
                    text = "Metode Pembayaran",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(paymentMethods) { method ->
                        val isSelected = method == selectedMethod
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMethod = method },
                            shape = KelolaRadius.ShapeSmall,
                            label = { Text(method) },
                            leadingIcon = {
                                val icon = when (method) {
                                    "Tunai" -> Icons.Default.LocalAtm
                                    "QRIS" -> Icons.Default.QrCode
                                    "Transfer" -> Icons.Default.AccountBalance
                                    "E-Wallet" -> Icons.Default.CreditCard
                                    else -> Icons.Default.Receipt
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null
                        )
                    }
                }

                // Note input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Catatan Pelunasan (Opsional)") },
                    placeholder = { Text("Contoh: Titip uang lewat kasir, via transfer") },
                    singleLine = true,
                    shape = KelolaRadius.ShapeInput,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_settle_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isAmountValid) {
                        onConfirmSettle(debt.id, settleAmount, selectedMethod, noteInput)
                    }
                },
                enabled = isAmountValid,
                shape = KelolaRadius.ShapeInput,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.testTag("button_confirm_settle_debt")
            ) {
                Text("Konfirmasi Pelunasan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Surface(
                onClick = onDismiss,
                shape = KelolaRadius.ShapeInput,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                    Text("Batal", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
