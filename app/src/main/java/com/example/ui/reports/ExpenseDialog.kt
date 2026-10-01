package com.example.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExpenseEntity
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    initialExpense: ExpenseEntity? = null,
    onSaveExpense: (category: String, amount: Long, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember(initialExpense) {
        mutableStateOf(if (initialExpense != null && initialExpense.amount > 0) FormatUtils.formatRupiahInput(initialExpense.amount.toString()) else "")
    }
    var selectedCategory by remember(initialExpense) {
        mutableStateOf(initialExpense?.category ?: "Belanja Stok")
    }
    var note by remember(initialExpense) {
        mutableStateOf(initialExpense?.note ?: "")
    }

    val categories = listOf(
        "Belanja Stok",
        "Transportasi",
        "Kemasan",
        "Listrik & Air",
        "Sewa Tempat",
        "Makan / Minum",
        "Lainnya"
    )
    var expanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val amount = FormatUtils.parseRupiahInput(amountText)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (initialExpense != null) "Edit Pengeluaran" else "Catat Pengeluaran",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DangerRed
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = FormatUtils.formatRupiahInput(it)
                        amountError = FormatUtils.parseRupiahInput(amountText) <= 0
                    },
                    label = { Text("Jumlah Pengeluaran *") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    placeholder = { Text("10.000", color = TextMuted) },
                    isError = amountError,
                    supportingText = { if (amountError) Text("Jumlah harus lebih dari 0", color = DangerRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = KelolaRadius.ShapeInput,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DangerRed,
                        unfocusedBorderColor = BorderLight
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount")
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Pengeluaran") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Pengeluaran") },
                    placeholder = { Text("Misal: Beli cup gelas plastik dan sedotan") },
                    shape = KelolaRadius.ShapeInput,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount > 0) {
                        onSaveExpense(selectedCategory, amount, note.trim())
                    } else {
                        amountError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = KelolaRadius.ShapeInput,
                modifier = Modifier.testTag("button_save_expense")
            ) {
                Text(
                    text = if (initialExpense != null) "Simpan Perubahan" else "Simpan Pengeluaran",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    )
}
