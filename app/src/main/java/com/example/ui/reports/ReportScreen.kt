package com.example.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.AdaptiveContainer
import com.example.ui.theme.LocalWindowSizeClass
import com.example.ui.theme.WindowSizeClassType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.LossRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.ReportStats
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.SearchField
import com.example.ui.components.SummaryCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandDeep
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GradientBrand
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.KelolaTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils

data class CashflowItem(
    val id: Long,
    val isIncome: Boolean,
    val title: String,
    val subtitle: String,
    val amount: Long,
    val date: Long,
    val rawExpense: ExpenseEntity? = null,
    val rawTx: TransactionEntity? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportStats: ReportStats,
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit,
    transactions: List<TransactionEntity>,
    expenses: List<ExpenseEntity>,
    incomes: List<IncomeEntity>,
    openingCapital: Long = 0L,
    previousSales: Long = 0L,
    previousSalesDate: String = "",
    previousSalesNote: String = "",
    onSelectTransaction: (TransactionEntity) -> Unit,
    onOpenAddExpense: () -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit = {},
    onDeleteTransaction: (Long) -> Unit = {},
    onCancelLoss: (lossId: Long, restoreStock: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val periods = listOf("Hari Ini", "7 Hari Terakhir", "Bulan Ini", "Semua")
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Ringkasan", "Riwayat", "Arus Kas", "Kerugian")

    var transactionSearch by remember { mutableStateOf("") }
    var menuExpandedItemId by remember { mutableStateOf<Long?>(null) }
    var itemToDelete by remember { mutableStateOf<CashflowItem?>(null) }
    var lossRecordToCancel by remember { mutableStateOf<LossRecordEntity?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val filteredTransactions = remember(transactions, transactionSearch) {
        if (transactionSearch.isBlank()) transactions
        else transactions.filter {
            it.transactionNumber.contains(transactionSearch, ignoreCase = true) ||
                    it.paymentMethod.contains(transactionSearch, ignoreCase = true)
        }
    }

    val cashflowTimeline = remember(expenses, incomes, transactions) {
        val incList = transactions.filter { it.status == "COMPLETED" }.map {
            CashflowItem(
                id = it.id,
                isIncome = true,
                title = "Penjualan Kasir",
                subtitle = "${it.transactionNumber} (${it.paymentMethod})",
                amount = it.total,
                date = it.createdAt,
                rawTx = it
            )
        } + incomes.filter { it.source != "Penjualan" }.map {
            CashflowItem(
                id = it.id,
                isIncome = true,
                title = it.source,
                subtitle = it.note ?: "Pemasukan lain",
                amount = it.amount,
                date = it.date
            )
        }
        val expList = expenses.map {
            CashflowItem(
                id = it.id,
                isIncome = false,
                title = it.category,
                subtitle = it.note.ifBlank { "Pengeluaran operasional" },
                amount = it.amount,
                date = it.date,
                rawExpense = it
            )
        }
        (incList + expList).sortedByDescending { it.date }
    }

    AdaptiveContainer(maxWidth = 1040.dp) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 0, easing = FastOutSlowInEasing)) +
                            slideInVertically(animationSpec = tween(300, delayMillis = 0, easing = FastOutSlowInEasing)) { it / 6 }
                ) {
                    // Period Filter Chips (Radius 10px, Tanpa Border)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(periods) { period ->
                            val isSelected = period == selectedPeriod
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectPeriod(period) },
                                shape = KelolaRadius.ShapeSmall,
                                label = {
                                    Text(
                                        text = period,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
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

            // Key Metric Summary Cards
            item {
                val windowSizeClass = LocalWindowSizeClass.current
                val isTablet = windowSizeClass != WindowSizeClassType.COMPACT

                if (isTablet) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 60, easing = FastOutSlowInEasing)) +
                                slideInVertically(animationSpec = tween(300, delayMillis = 60, easing = FastOutSlowInEasing)) { it / 6 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryCard(
                                title = "Penjualan",
                                value = FormatUtils.formatRupiah(reportStats.totalSales),
                                subtitle = "${reportStats.transactionCount} transaksi",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            SummaryCard(
                                title = "Keuntungan (Kotor)",
                                value = FormatUtils.formatRupiah(reportStats.grossProfit),
                                subtitle = "${reportStats.itemsSold} item terjual",
                                icon = Icons.Default.MonetizationOn,
                                contentColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )

                            SummaryCard(
                                title = "Pengeluaran",
                                value = FormatUtils.formatRupiah(reportStats.totalExpense),
                                subtitle = "Beban usaha",
                                icon = Icons.Default.TrendingDown,
                                contentColor = DangerRed,
                                modifier = Modifier.weight(1f)
                            )

                            SummaryCard(
                                title = "Arus Kas Bersih",
                                value = FormatUtils.formatRupiah(reportStats.netCashflow),
                                subtitle = "Pemasukan - beban",
                                icon = Icons.Default.Receipt,
                                contentColor = SecondaryTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(300, delayMillis = 60, easing = FastOutSlowInEasing)) +
                                    slideInVertically(animationSpec = tween(300, delayMillis = 60, easing = FastOutSlowInEasing)) { it / 6 }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryCard(
                                    title = "Penjualan",
                                    value = FormatUtils.formatRupiah(reportStats.totalSales),
                                    subtitle = "${reportStats.transactionCount} transaksi",
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )

                                SummaryCard(
                                    title = "Keuntungan (Kotor)",
                                    value = FormatUtils.formatRupiah(reportStats.grossProfit),
                                    subtitle = "${reportStats.itemsSold} item terjual",
                                    icon = Icons.Default.MonetizationOn,
                                    contentColor = SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(300, delayMillis = 120, easing = FastOutSlowInEasing)) +
                                    slideInVertically(animationSpec = tween(300, delayMillis = 120, easing = FastOutSlowInEasing)) { it / 6 }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryCard(
                                    title = "Pengeluaran",
                                    value = FormatUtils.formatRupiah(reportStats.totalExpense),
                                    subtitle = "Beban usaha",
                                    icon = Icons.Default.TrendingDown,
                                    contentColor = DangerRed,
                                    modifier = Modifier.weight(1f)
                                )

                                SummaryCard(
                                    title = "Arus Kas Bersih",
                                    value = FormatUtils.formatRupiah(reportStats.netCashflow),
                                    subtitle = "Pemasukan - beban",
                                    icon = Icons.Default.Receipt,
                                    contentColor = SecondaryTeal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

        // Subtabs: Ringkasan & Tren, Transaksi, Arus Kas
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 180, easing = FastOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = 180, easing = FastOutSlowInEasing)) { it / 6 }
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.height(48.dp),
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }
            }
        }

        // TAB 0: Ringkasan & Tren + Produk Terlaris
        if (selectedTab == 0) {
            // Data Keuangan Awal & Historis Card (Kelola Radius Card 20px, Tanpa Border)
            if (previousSales > 0L || openingCapital > 0L) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "DATA KEUANGAN AWAL & HISTORIS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )

                            // Penjualan Aplikasi
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Penjualan Aplikasi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${reportStats.transactionCount} transaksi dicatat via POS",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = FormatUtils.formatRupiah(reportStats.totalSales),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Penjualan Sebelumnya
                            if (previousSales > 0L) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Penjualan Sebelumnya",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (previousSalesNote.isNotBlank()) previousSalesNote else "Pencatatan manual sebelum aplikasi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = FormatUtils.formatRupiah(previousSales),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }

                                // Total Historis
                                Surface(
                                    shape = KelolaRadius.ShapeInput,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Historis Gabungan",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = FormatUtils.formatRupiah(reportStats.totalSales + previousSales),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Modal Awal
                            if (openingCapital > 0L) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Modal Awal Usaha",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Saldo modal awal (bukan penjualan)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = FormatUtils.formatRupiah(openingCapital),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Sales Visualizer Bar Chart (Kelola Radius Card 20px, Soft Shadow, Tanpa Border)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 3.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grafik Penjualan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxAmount = reportStats.dailySales.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            reportStats.dailySales.forEach { dayStat ->
                                val heightRatio = (dayStat.amount.toFloat() / maxAmount.toFloat()).coerceIn(0.08f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (dayStat.amount > 0) {
                                        Text(
                                            text = if (dayStat.amount >= 1000) "${dayStat.amount / 1000}k" else "${dayStat.amount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxSize(heightRatio)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (dayStat.amount > 0) GradientBrand
                                                else androidx.compose.ui.graphics.Brush.linearGradient(
                                                    listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dayStat.dayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Products Section
            item {
                Text(
                    text = "Produk Terlaris",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (reportStats.topProducts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.PointOfSale,
                        title = "Belum Ada Data Penjualan",
                        description = "Data produk terlaris akan otomatis terakumulasi setelah transaksi dilakukan."
                    )
                }
            } else {
                items(reportStats.topProducts.mapIndexed { idx, p -> Pair(idx + 1, p) }) { (rank, prod) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (rank <= 3) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$rank",
                                        fontWeight = FontWeight.Bold,
                                        color = if (rank <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${prod.quantitySold} terjual",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = FormatUtils.formatRupiah(prod.totalRevenue),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: Riwayat Transaksi
        if (selectedTab == 1) {
            item {
                SearchField(
                    query = transactionSearch,
                    onQueryChange = { transactionSearch = it },
                    placeholder = "Cari nomor transaksi atau metode...",
                    testTag = "input_search_history"
                )
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Receipt,
                        title = "Tidak Ada Transaksi",
                        description = "Belum ada transaksi yang sesuai pada periode ini."
                    )
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTransaction(tx) }
                            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.transactionNumber,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = KelolaRadius.ShapeSmall,
                                        color = if (tx.status == "COMPLETED") SuccessContainer else DangerContainer
                                    ) {
                                        Text(
                                            text = if (tx.status == "COMPLETED") "Selesai" else "Batal",
                                            color = if (tx.status == "COMPLETED") SuccessGreen else DangerRed,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${FormatUtils.formatDateTime(tx.createdAt)} • ${tx.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatRupiah(tx.total),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.status == "COMPLETED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lihat detail →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: Buku Kas / Arus Kas
        if (selectedTab == 2) {
            item {
                CashflowAreaChart(
                    timeline = cashflowTimeline,
                    selectedPeriod = selectedPeriod
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mutasi Arus Kas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (cashflowTimeline.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Receipt,
                        title = "Belum Ada Catatan Arus Kas",
                        description = "Arus kas akan mencatat pemasukan dari kasir dan pengeluaran operasional harian."
                    )
                }
            } else {
                items(cashflowTimeline) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(KelolaSpacing.IconContainerSize)
                                        .clip(CircleShape)
                                        .background(if (item.isIncome) SuccessContainer else DangerContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (item.isIncome) SuccessGreen else DangerRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = FormatUtils.formatDateTime(item.date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (item.isIncome) "+${FormatUtils.formatRupiah(item.amount)}" else "-${FormatUtils.formatRupiah(item.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isIncome) SuccessGreen else DangerRed
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Box {
                                    IconButton(
                                        onClick = {
                                            menuExpandedItemId = if (menuExpandedItemId == item.id) null else item.id
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Opsi Data",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpandedItemId == item.id,
                                        onDismissRequest = { menuExpandedItemId = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit Data", fontSize = 13.sp) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpandedItemId = null
                                                if (item.rawExpense != null) {
                                                    onEditExpense(item.rawExpense)
                                                } else if (item.rawTx != null) {
                                                    onSelectTransaction(item.rawTx)
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Hapus Data", fontSize = 13.sp, color = DangerRed) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = DangerRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpandedItemId = null
                                                itemToDelete = item
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: Laporan Kerugian (Barang Kadaluarsa & Rusak)
        if (selectedTab == 3) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = DangerContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Total Kerugian Tercatat",
                                style = MaterialTheme.typography.labelMedium,
                                color = DangerRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = FormatUtils.formatRupiah(reportStats.totalLoss),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                            Text(
                                text = "${reportStats.lossRecords.size} kejadian barang kadaluarsa / rusak",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            if (reportStats.lossRecords.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "Belum Ada Catatan Kerugian",
                        description = "Semua stok barang aman dan belum ada kerugian tercatat.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(reportStats.lossRecords, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KelolaRadius.ShapeCard)
                            .clickable { lossRecordToCancel = record }
                            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = record.productName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = KelolaRadius.ShapeSmall,
                                        color = DangerContainer
                                    ) {
                                        Text(
                                            text = record.reason,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DangerRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Jumlah: ${record.quantity} pcs • HPP: ${FormatUtils.formatRupiah(record.costPrice)}/pcs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatDateTime(record.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "-${FormatUtils.formatRupiah(record.totalLoss)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )

                                Surface(
                                    onClick = { lossRecordToCancel = record },
                                    shape = KelolaRadius.ShapeSmall,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "Batalkan",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Batalkan",
                                            style = MaterialTheme.typography.labelSmall,
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
        }
    }
    }

    itemToDelete?.let { item ->
        val isExp = item.rawExpense != null
        ConfirmationDialog(
            title = if (isExp) "Hapus Pengeluaran?" else "Hapus Transaksi Kasir?",
            message = "Yakin ingin menghapus ${item.title} sebesar ${FormatUtils.formatRupiah(item.amount)} dari catatan arus kas?",
            confirmText = "Hapus Data",
            isDestructive = true,
            onConfirm = {
                if (isExp) {
                    onDeleteExpense(item.id)
                } else {
                    onDeleteTransaction(item.id)
                }
                itemToDelete = null
            },
            onDismiss = {
                itemToDelete = null
            }
        )
    }

    // Dialog Pembatalan Kerugian & Pengembalian Dana ke Kas
    lossRecordToCancel?.let { loss ->
        var restoreStock by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { lossRecordToCancel = null },
            shape = KelolaRadius.ShapeCard,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Batalkan Kerugian?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rincian produk yang dibatalkan kerugiannya
                    Surface(
                        shape = KelolaRadius.ShapeInput,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = loss.productName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Jumlah: ${loss.quantity} pcs • HPP: ${FormatUtils.formatRupiah(loss.costPrice)}/pcs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Alasan: ${loss.reason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(loss.totalLoss),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            }
                        }
                    }

                    // Banner info bahwa dana akan otomatis masuk ke kas
                    Surface(
                        shape = KelolaRadius.ShapeInput,
                        color = SuccessContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dana Otomatis Masuk Kas",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Dana rugi sebesar ${FormatUtils.formatRupiah(loss.totalLoss)} akan otomatis dicatat kembali sebagai pemasukan kas usaha.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Checkbox kembalikan stok
                    Surface(
                        onClick = { restoreStock = !restoreStock },
                        shape = KelolaRadius.ShapeInput,
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = restoreStock,
                                onCheckedChange = { restoreStock = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Kembalikan stok produk (+${loss.quantity} pcs)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tambah kembali stok barang ke katalog usaha",
                                    style = MaterialTheme.typography.labelSmall,
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
                        onCancelLoss(loss.id, restoreStock)
                        lossRecordToCancel = null
                    },
                    shape = KelolaRadius.ShapeInput,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Batalkan Kerugian", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { lossRecordToCancel = null },
                    shape = KelolaRadius.ShapeInput
                ) {
                    Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}


// =========================================================================
// GRAFIK AREA CHART ARUS KAS (Pemasukan VS Pengeluaran)
// =========================================================================
data class CashflowDataPoint(
    val label: String,
    val income: Long,
    val expense: Long
)

fun generateCashflowDataPoints(timeline: List<CashflowItem>, selectedPeriod: String): List<CashflowDataPoint> {
    val cal = Calendar.getInstance()
    val points = mutableListOf<CashflowDataPoint>()

    if (selectedPeriod == "Hari Ini") {
        // Bagi menjadi 6 interval waktu hari ini
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayStart = today.timeInMillis

        val intervals = listOf(
            "04:00" to (todayStart to todayStart + 4 * 3600 * 1000L),
            "08:00" to (todayStart + 4 * 3600 * 1000L to todayStart + 8 * 3600 * 1000L),
            "12:00" to (todayStart + 8 * 3600 * 1000L to todayStart + 12 * 3600 * 1000L),
            "16:00" to (todayStart + 12 * 3600 * 1000L to todayStart + 16 * 3600 * 1000L),
            "20:00" to (todayStart + 16 * 3600 * 1000L to todayStart + 20 * 3600 * 1000L),
            "24:00" to (todayStart + 20 * 3600 * 1000L to todayStart + 24 * 3600 * 1000L)
        )

        for ((lbl, range) in intervals) {
            val inc = timeline.filter { it.isIncome && it.date in range.first..range.second }.sumOf { it.amount }
            val exp = timeline.filter { !it.isIncome && it.date in range.first..range.second }.sumOf { it.amount }
            points.add(CashflowDataPoint(lbl, inc, exp))
        }
    } else {
        // Tampilkan 7 hari mundur dari hari ini
        val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        for (i in 6 downTo 0) {
            val dCal = Calendar.getInstance()
            dCal.add(Calendar.DAY_OF_YEAR, -i)
            dCal.set(Calendar.HOUR_OF_DAY, 0)
            dCal.set(Calendar.MINUTE, 0)
            dCal.set(Calendar.SECOND, 0)
            dCal.set(Calendar.MILLISECOND, 0)
            val dayStart = dCal.timeInMillis
            val dayEnd = dayStart + 24 * 3600 * 1000L - 1L

            val inc = timeline.filter { it.isIncome && it.date in dayStart..dayEnd }.sumOf { it.amount }
            val exp = timeline.filter { !it.isIncome && it.date in dayStart..dayEnd }.sumOf { it.amount }
            val lbl = dayFormat.format(dCal.time).replace(".", "").take(3)
            points.add(CashflowDataPoint(lbl, inc, exp))
        }
    }

    return points
}

@Composable
fun CashflowAreaChart(
    timeline: List<CashflowItem>,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    val dataPoints = remember(timeline, selectedPeriod) {
        generateCashflowDataPoints(timeline, selectedPeriod)
    }

    val totalIncome = remember(timeline) {
        timeline.filter { it.isIncome }.sumOf { it.amount }
    }
    val totalExpense = remember(timeline) {
        timeline.filter { !it.isIncome }.sumOf { it.amount }
    }
    val netCashflow = totalIncome - totalExpense

    Card(
        modifier = modifier
            .fillMaxWidth()
            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
        shape = KelolaRadius.ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Arus Kas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pemasukan VS Pengeluaran ($selectedPeriod)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = if (netCashflow >= 0) SuccessContainer else DangerContainer
                ) {
                    Text(
                        text = if (netCashflow >= 0) "Surplus: ${FormatUtils.formatRupiah(netCashflow)}"
                               else "Defisit: -${FormatUtils.formatRupiah(kotlin.math.abs(netCashflow))}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (netCashflow >= 0) SuccessGreen else DangerRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Summary Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Total Pemasukan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatRupiah(totalIncome), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }

                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Total Pengeluaran", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatRupiah(totalExpense), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Area Chart Canvas
            val maxAmount = remember(dataPoints) {
                val maxVal = dataPoints.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L
                if (maxVal <= 0L) 100_000L else maxVal
            }

            val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            val incomeColor = SuccessGreen
            val expenseColor = DangerRed
            val density = LocalDensity.current
            val strokeWidthPx = with(density) { 2.5.dp.toPx() }
            val dotRadiusPx = with(density) { 3.5.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height
                    val n = dataPoints.size
                    if (n < 2) return@Canvas

                    val stepX = w / (n - 1)

                    // Garis bantu horizontal tipis
                    val gridCount = 3
                    for (i in 0..gridCount) {
                        val y = h * (i.toFloat() / gridCount)
                        drawLine(
                            color = gridLineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    fun getY(amount: Long): Float {
                        val ratio = (amount.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
                        return h - (ratio * (h - 24f)) - 12f
                    }

                    // 1. Path Pemasukan
                    val incomePath = Path()
                    val incomeAreaPath = Path()

                    val firstIncY = getY(dataPoints[0].income)
                    incomePath.moveTo(0f, firstIncY)
                    incomeAreaPath.moveTo(0f, h)
                    incomeAreaPath.lineTo(0f, firstIncY)

                    for (i in 1 until n) {
                        val prevX = (i - 1) * stepX
                        val prevY = getY(dataPoints[i - 1].income)
                        val currX = i * stepX
                        val currY = getY(dataPoints[i].income)

                        val cX1 = (prevX + currX) / 2f
                        val cY1 = prevY
                        val cX2 = (prevX + currX) / 2f
                        val cY2 = currY

                        incomePath.cubicTo(cX1, cY1, cX2, cY2, currX, currY)
                        incomeAreaPath.cubicTo(cX1, cY1, cX2, cY2, currX, currY)
                    }

                    incomeAreaPath.lineTo((n - 1) * stepX, h)
                    incomeAreaPath.close()

                    drawPath(
                        path = incomeAreaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                incomeColor.copy(alpha = 0.38f),
                                incomeColor.copy(alpha = 0.03f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    drawPath(
                        path = incomePath,
                        color = incomeColor,
                        style = Stroke(width = strokeWidthPx)
                    )

                    // 2. Path Pengeluaran
                    val expensePath = Path()
                    val expenseAreaPath = Path()

                    val firstExpY = getY(dataPoints[0].expense)
                    expensePath.moveTo(0f, firstExpY)
                    expenseAreaPath.moveTo(0f, h)
                    expenseAreaPath.lineTo(0f, firstExpY)

                    for (i in 1 until n) {
                        val prevX = (i - 1) * stepX
                        val prevY = getY(dataPoints[i - 1].expense)
                        val currX = i * stepX
                        val currY = getY(dataPoints[i].expense)

                        val cX1 = (prevX + currX) / 2f
                        val cY1 = prevY
                        val cX2 = (prevX + currX) / 2f
                        val cY2 = currY

                        expensePath.cubicTo(cX1, cY1, cX2, cY2, currX, currY)
                        expenseAreaPath.cubicTo(cX1, cY1, cX2, cY2, currX, currY)
                    }

                    expenseAreaPath.lineTo((n - 1) * stepX, h)
                    expenseAreaPath.close()

                    drawPath(
                        path = expenseAreaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                expenseColor.copy(alpha = 0.30f),
                                expenseColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    drawPath(
                        path = expensePath,
                        color = expenseColor,
                        style = Stroke(width = strokeWidthPx)
                    )

                    // Data dots
                    for (i in 0 until n) {
                        val x = i * stepX
                        val incY = getY(dataPoints[i].income)
                        val expY = getY(dataPoints[i].expense)

                        drawCircle(
                            color = incomeColor,
                            radius = dotRadiusPx,
                            center = androidx.compose.ui.geometry.Offset(x, incY)
                        )
                        drawCircle(
                            color = expenseColor,
                            radius = dotRadiusPx,
                            center = androidx.compose.ui.geometry.Offset(x, expY)
                        )
                    }
                }
            }

            // X-Axis Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEach { pt ->
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
