package com.example.ui.debts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.ui.theme.AdaptiveContainer
import com.example.ui.theme.LocalWindowSizeClass
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.SearchField
import com.example.ui.components.SummaryCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandDeep
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GradientBrand
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils

@Composable
fun DebtsScreen(
    debts: List<DebtEntity>,
    onSettleDebt: (DebtEntity) -> Unit,
    onEditDebtItems: (DebtEntity) -> Unit = {},
    onSelectTransactionNumber: (String) -> Unit = {},
    onDeleteDebt: (DebtEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    val filterOptions = listOf("Semua", "Belum Lunas", "Dicicil", "Lunas")

    // Filter logic
    val filteredDebts = remember(debts, searchQuery, selectedFilter) {
        debts.filter { debt ->
            val matchQuery = searchQuery.isBlank() ||
                    debt.customerName.contains(searchQuery, ignoreCase = true) ||
                    debt.customerPhone.contains(searchQuery, ignoreCase = true) ||
                    debt.transactionNumber.contains(searchQuery, ignoreCase = true)

            val matchFilter = when (selectedFilter) {
                "Belum Lunas" -> debt.status == "UNPAID"
                "Dicicil" -> debt.status == "PARTIALLY_PAID"
                "Lunas" -> debt.status == "PAID"
                else -> true
            }

            matchQuery && matchFilter
        }
    }

    // Stats calculations
    val totalUnpaidAmount = remember(debts) {
        debts.filter { it.status != "PAID" }.sumOf { it.remainingAmount }
    }
    val unpaidCount = remember(debts) {
        debts.count { it.status != "PAID" }
    }
    val totalPaidAmount = remember(debts) {
        debts.sumOf { it.amount - it.remainingAmount }
    }

    val windowSize = LocalWindowSizeClass.current

    AdaptiveContainer(maxWidth = 1040.dp) {
        if (windowSize.isCompact) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Header
                    Column {
                        Text(
                            text = "Daftar Kasbon",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kelola tagihan bayar nanti dan pelunasan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Summary Stats (2 cards)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Sisa Kasbon",
                            value = FormatUtils.formatRupiah(totalUnpaidAmount),
                            subtitle = "$unpaidCount orang belum lunas",
                            icon = Icons.Default.HourglassTop,
                            contentColor = WarningAmber,
                            modifier = Modifier.weight(1f),
                            testTag = "summary_unpaid_debt"
                        )

                        SummaryCard(
                            title = "Total Dilunasi",
                            value = FormatUtils.formatRupiah(totalPaidAmount),
                            subtitle = "Sudah diterima",
                            icon = Icons.Default.AssignmentTurnedIn,
                            contentColor = SuccessGreen,
                            modifier = Modifier.weight(1f),
                            testTag = "summary_paid_debt"
                        )
                    }
                }

                // Search and Filter Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Cari nama atau no. transaksi...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions) { filter ->
                                val isSelected = filter == selectedFilter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = filter },
                                    shape = KelolaRadius.ShapeSmall,
                                    label = { Text(filter, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderLight)
                                )
                            }
                        }
                    }
                }

                // Debt Items List
                if (filteredDebts.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.HourglassBottom,
                            title = if (debts.isEmpty()) "Belum Ada Kasbon" else "Tidak Ditemukan",
                            description = if (debts.isEmpty()) {
                                "Transaksi dengan metode pembayaran 'Bayar Nanti' akan otomatis tercatat di list ini."
                            } else {
                                "Tidak ada data penghutang yang sesuai dengan kata kunci atau filter saat ini."
                            },
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    items(filteredDebts, key = { it.id }) { debt ->
                        DebtItemCard(
                            debt = debt,
                            onSettle = { onSettleDebt(debt) },
                            onEditItems = { onEditDebtItems(debt) },
                            onViewTransaction = { onSelectTransactionNumber(debt.transactionNumber) },
                            onDelete = { onDeleteDebt(debt) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            // Adaptive Grid for Tablet / Landscape
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daftar Kasbon",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kelola tagihan bayar nanti dan pelunasan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Sisa Kasbon",
                            value = FormatUtils.formatRupiah(totalUnpaidAmount),
                            subtitle = "$unpaidCount orang belum lunas",
                            icon = Icons.Default.HourglassTop,
                            contentColor = WarningAmber,
                            modifier = Modifier.weight(1f),
                            testTag = "summary_unpaid_debt"
                        )

                        SummaryCard(
                            title = "Total Dilunasi",
                            value = FormatUtils.formatRupiah(totalPaidAmount),
                            subtitle = "Sudah diterima",
                            icon = Icons.Default.AssignmentTurnedIn,
                            contentColor = SuccessGreen,
                            modifier = Modifier.weight(1f),
                            testTag = "summary_paid_debt"
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Cari nama atau no. transaksi...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions) { filter ->
                                val isSelected = filter == selectedFilter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = filter },
                                    shape = KelolaRadius.ShapeSmall,
                                    label = { Text(filter, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderLight)
                                )
                            }
                        }
                    }
                }

                if (filteredDebts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            icon = Icons.Default.HourglassBottom,
                            title = if (debts.isEmpty()) "Belum Ada Kasbon" else "Tidak Ditemukan",
                            description = if (debts.isEmpty()) {
                                "Transaksi dengan metode pembayaran 'Bayar Nanti' akan otomatis tercatat di list ini."
                            } else {
                                "Tidak ada data penghutang yang sesuai dengan kata kunci atau filter saat ini."
                            },
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    items(filteredDebts, key = { it.id }) { debt ->
                        DebtItemCard(
                            debt = debt,
                            onSettle = { onSettleDebt(debt) },
                            onEditItems = { onEditDebtItems(debt) },
                            onViewTransaction = { onSelectTransactionNumber(debt.transactionNumber) },
                            onDelete = { onDeleteDebt(debt) }
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DebtItemCard(
    debt: DebtEntity,
    onSettle: () -> Unit,
    onEditItems: () -> Unit = {},
    onViewTransaction: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isPaid = debt.status == "PAID"
    val isPartiallyPaid = debt.status == "PARTIALLY_PAID"

    val paidAmount = debt.amount - debt.remainingAmount
    val progress = if (debt.amount > 0) {
        (paidAmount.toFloat() / debt.amount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 3.dp)
            .testTag("debt_card_${debt.id}"),
        shape = KelolaRadius.ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Debtor Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPaid) SuccessContainer
                                else if (isPartiallyPaid) MaterialTheme.colorScheme.surfaceVariant
                                else WarningContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isPaid) SuccessGreen
                            else if (isPartiallyPaid) MaterialTheme.colorScheme.primary
                            else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = debt.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${debt.transactionNumber} • ${FormatUtils.formatDateTime(debt.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = when (debt.status) {
                        "PAID" -> SuccessContainer
                        "PARTIALLY_PAID" -> MaterialTheme.colorScheme.surfaceVariant
                        else -> WarningContainer
                    }
                ) {
                    Text(
                        text = when (debt.status) {
                            "PAID" -> "Lunas"
                            "PARTIALLY_PAID" -> "Dicicil"
                            else -> "Belum lunas"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when (debt.status) {
                            "PAID" -> SuccessGreen
                            "PARTIALLY_PAID" -> MaterialTheme.colorScheme.primary
                            else -> WarningAmber
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Optional: Phone & WhatsApp link
            if (debt.customerPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = debt.customerPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${debt.customerPhone}")
                            }
                            context.startActivity(intent)
                        },
                        shape = KelolaRadius.ShapeSmall,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text("Hubungi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Optional: Note
            if (debt.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: ${debt.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, KelolaRadius.ShapeSmall)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isPaid) "Total Tagihan Lunas" else "Sisa Kasbon",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPaid) SuccessGreen else DangerRed,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = FormatUtils.formatRupiah(if (isPaid) debt.amount else debt.remainingAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) SuccessGreen else DangerRed
                    )
                }

                if (!isPaid) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Awal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatRupiah(debt.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Progress bar if partially paid
            if (isPartiallyPaid) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sudah dicicil: ${FormatUtils.formatRupiah(paidAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // View Transaction receipt
                    IconButton(
                        onClick = onViewTransaction,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = "Lihat Struk",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete debt record
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Data Hutang",
                            tint = DangerRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Actions if not paid
                if (!isPaid) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onEditItems,
                            shape = KelolaRadius.ShapeSmall,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, BorderLight),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("button_edit_items_debt_${debt.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit Barang",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            onClick = onSettle,
                            shape = KelolaRadius.ShapeSmall,
                            color = if (isPartiallyPaid) MaterialTheme.colorScheme.primary else SuccessGreen,
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("button_settle_debt_${debt.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPartiallyPaid) "Cicil" else "Lunasi",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = KelolaRadius.ShapeSmall,
                        color = SuccessContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lunas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
