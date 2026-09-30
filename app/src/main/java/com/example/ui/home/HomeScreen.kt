package com.example.ui.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChangeRecordEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.DashboardStats
import com.example.ui.components.StockBadge
import com.example.ui.components.SummaryCard
import com.example.ui.theme.AdaptiveContainer
import com.example.ui.theme.BrandDeep
import com.example.ui.theme.BrandSky
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GradientBrand
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.KelolaTheme
import com.example.ui.theme.LocalWindowSizeClass
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

@Composable
fun HomeScreen(
    stats: DashboardStats,
    businessName: String,
    openingCapital: Long = 0L,
    onNavigateToCashier: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToPendingChanges: () -> Unit = {},
    onOpenAddProduct: () -> Unit = {},
    onOpenAddExpense: () -> Unit = {},
    onOpenRestock: (ProductEntity) -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit,
    onMarkChangeGiven: (Long) -> Unit = {},
    onSettleDebt: (DebtEntity) -> Unit = {},
    onEditDebtItems: (DebtEntity) -> Unit = {},
    onOpenNotes: () -> Unit = {},
    onOpenCustomers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeBalance = openingCapital + stats.todaySales - stats.todayExpense
    var isVisible by remember { mutableStateOf(false) }
    val windowSize = LocalWindowSizeClass.current

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AdaptiveContainer(modifier = modifier, maxWidth = 1040.dp) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KelolaSpacing.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
        ) {
            // Spacing di atas Hero Saldo: 12dp untuk visual hierarchy yang pas di mobile
            item {
                Spacer(modifier = Modifier.height(KelolaSpacing.Space3))
            }

            if (windowSize.isCompact) {
                // =========================================================================
                // MOBILE BASELINE (100% UNCHANGED)
                // =========================================================================
                item {
                    HeroSaldoCard(
                        openingCapital = openingCapital,
                        activeBalance = activeBalance,
                        todaySales = stats.todaySales,
                        todayExpense = stats.todayExpense,
                        isVisible = isVisible
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(KelolaSpacing.Space1))
                    Text(
                        text = "Ringkasan Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    TodaySummaryCards(
                        todaySales = stats.todaySales,
                        todayTxCount = stats.todayTxCount,
                        todayExpense = stats.todayExpense,
                        isVisible = isVisible
                    )
                }

                item {
                    PendingChangeSummaryCard(
                        stats = stats,
                        isVisible = isVisible,
                        onClick = onNavigateToPendingChanges
                    )
                }

                item {
                    UnpaidDebtSummaryCard(
                        stats = stats,
                        isVisible = isVisible,
                        onClick = onNavigateToDebts
                    )
                }

                item {
                    NotesShortcutCard(
                        isVisible = isVisible,
                        onClick = onOpenNotes
                    )
                }

                item {
                    CustomersShortcutCard(
                        isVisible = isVisible,
                        onClick = onOpenCustomers
                    )
                }
            } else {
                // =========================================================================
                // TABLET / MEDIUM / EXPANDED ADAPTIVE LAYOUT
                // =========================================================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4),
                        verticalAlignment = Alignment.Top
                    ) {
                        HeroSaldoCard(
                            openingCapital = openingCapital,
                            activeBalance = activeBalance,
                            todaySales = stats.todaySales,
                            todayExpense = stats.todayExpense,
                            isVisible = isVisible,
                            modifier = Modifier.weight(1.15f)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                        ) {
                            Text(
                                text = "Ringkasan Hari Ini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TodaySummaryCards(
                                todaySales = stats.todaySales,
                                todayTxCount = stats.todayTxCount,
                                todayExpense = stats.todayExpense,
                                isVisible = isVisible
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
                    ) {
                        PendingChangeSummaryCard(
                            stats = stats,
                            isVisible = isVisible,
                            onClick = onNavigateToPendingChanges,
                            modifier = Modifier.weight(1f)
                        )
                        UnpaidDebtSummaryCard(
                            stats = stats,
                            isVisible = isVisible,
                            onClick = onNavigateToDebts,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
                    ) {
                        NotesShortcutCard(
                            isVisible = isVisible,
                            onClick = onOpenNotes,
                            modifier = Modifier.weight(1f)
                        )
                        CustomersShortcutCard(
                            isVisible = isVisible,
                            onClick = onOpenCustomers,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

        // Section: Produk Hampir Habis
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produk Hampir Habis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (stats.lowStockProducts.isNotEmpty()) {
                    Surface(
                        shape = KelolaRadius.ShapeSmall,
                        color = WarningContainer
                    ) {
                        Text(
                            text = "${stats.lowStockProducts.size} Perlu Restock",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (stats.lowStockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Semua stok masih aman 🎉",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tidak ada produk yang berada di bawah batas minimum.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(stats.lowStockProducts) { product ->
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
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(KelolaRadius.ShapeSmall)
                                    .background(WarningContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                StockBadge(
                                    stock = product.stock,
                                    minimumStock = product.minimumStock,
                                    unit = product.unit
                                )
                            }
                        }

                        Surface(
                            onClick = { onOpenRestock(product) },
                            shape = KelolaRadius.ShapeSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("button_quick_restock_${product.id}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Stok",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Transaksi Terkini
        item {
            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terkini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onNavigateToCashier,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Buka Kasir",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (stats.recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = KelolaTheme.textTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum ada transaksi hari ini",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Buka kasir untuk melayani transaksi pertamamu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(stats.recentTransactions) { tx ->
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
                            .padding(KelolaSpacing.Space4),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.status == "COMPLETED") SuccessContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = if (tx.status == "COMPLETED") SuccessGreen else KelolaTheme.textTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space3))
                            Column {
                                Text(
                                    text = tx.transactionNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${FormatUtils.formatTime(tx.createdAt)} • ${tx.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Kolom nominal rata kanan dengan lebar tetap agar digit satuan sejajar vertikal
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.width(KelolaSpacing.AmountColumnWidth)
                        ) {
                            Text(
                                text = if (tx.status == "COMPLETED") "+${FormatUtils.formatRupiah(tx.total)}" else FormatUtils.formatRupiah(tx.total),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (tx.status == "COMPLETED") KelolaTheme.positive else KelolaTheme.textTertiary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (tx.status == "CANCELLED") {
                                Text(
                                    text = "Dibatalkan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DangerRed,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }
}

@Composable
private fun HeroSaldoCard(
    openingCapital: Long,
    activeBalance: Long,
    todaySales: Long,
    todayExpense: Long,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 0, easing = FastOutSlowInEasing)) +
                slideInVertically(animationSpec = tween(300, delayMillis = 0, easing = FastOutSlowInEasing)) { it / 6 },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .kelolaSoftShadow(shape = KelolaRadius.ShapeHero, elevation = 4.dp)
                .testTag("hero_saldo_card"),
            shape = KelolaRadius.ShapeHero,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GradientBrand)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399))
                            )
                            Text(
                                text = "Saldo Kas Toko Saat Ini",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.90f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (openingCapital > 0L) {
                            Surface(
                                shape = KelolaRadius.ShapeSmall,
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Modal: ${FormatUtils.formatRupiah(openingCapital)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedContent(
                        targetState = FormatUtils.formatRupiah(activeBalance),
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 4 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                    slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it / 4 }
                                )
                        },
                        label = "HeroSaldoValueAnim"
                    ) { animatedBalance ->
                        Text(
                            text = animatedBalance,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Penjualan Hari Ini",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 11.sp
                            )
                            AnimatedContent(
                                targetState = "+${FormatUtils.formatRupiah(todaySales)}",
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                        slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 4 })
                                        .togetherWith(
                                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                            slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it / 4 }
                                        )
                                },
                                label = "HeroSalesAnim"
                            ) { animSales ->
                                Text(
                                    text = animSales,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pengeluaran",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 11.sp
                            )
                            AnimatedContent(
                                targetState = "-${FormatUtils.formatRupiah(todayExpense)}",
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                        slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 4 })
                                        .togetherWith(
                                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                            slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it / 4 }
                                        )
                                },
                                label = "HeroExpenseAnim"
                            ) { animExpense ->
                                Text(
                                    text = animExpense,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCards(
    todaySales: Long,
    todayTxCount: Int,
    todayExpense: Long,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 60, easing = FastOutSlowInEasing)) +
                slideInVertically(animationSpec = tween(300, delayMillis = 60, easing = FastOutSlowInEasing)) { it / 6 },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
        ) {
            SummaryCard(
                title = "Omset Kasir",
                value = FormatUtils.formatRupiah(todaySales),
                subtitle = "$todayTxCount transaksi selesai",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                testTag = "summary_sales"
            )

            SummaryCard(
                title = "Pengeluaran",
                value = FormatUtils.formatRupiah(todayExpense),
                subtitle = "Hari ini",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                contentColor = KelolaTheme.negative,
                modifier = Modifier.weight(1f),
                testTag = "summary_expense"
            )
        }
    }
}

@Composable
private fun PendingChangeSummaryCard(
    stats: DashboardStats,
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPending = stats.pendingChangeList.isNotEmpty()
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 120, easing = FastOutSlowInEasing)) +
                slideInVertically(animationSpec = tween(300, delayMillis = 120, easing = FastOutSlowInEasing)) { it / 6 },
        modifier = modifier
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp)
                .testTag("card_pending_change_summary"),
            shape = KelolaRadius.ShapeCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hasPending) WarningContainer else SuccessContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = if (hasPending) WarningAmber else SuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Kembalian Belum Diberikan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = FormatUtils.formatRupiah(stats.pendingChangeTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (hasPending) KelolaTheme.negative else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (hasPending) {
                                "${stats.pendingChangeList.size} Transaksi Tertunda"
                            } else {
                                "Semua kembalian beres diberikan 👏"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buka Kembalian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnpaidDebtSummaryCard(
    stats: DashboardStats,
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasDebts = stats.unpaidDebtCount > 0
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 180, easing = FastOutSlowInEasing)) +
                slideInVertically(animationSpec = tween(300, delayMillis = 180, easing = FastOutSlowInEasing)) { it / 6 },
        modifier = modifier
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp)
                .testTag("card_unpaid_debts_summary"),
            shape = KelolaRadius.ShapeCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hasDebts) DangerContainer else SuccessContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (hasDebts) DangerRed else SuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Orang Belum Bayar (Kasbon)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = FormatUtils.formatRupiah(stats.unpaidDebtTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDebts) KelolaTheme.negative else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (hasDebts) {
                                "${stats.unpaidDebtCount} Orang Belum Lunas"
                            } else {
                                "Semua kasbon lunas 👏"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buka Kasbon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesShortcutCard(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 240, easing = FastOutSlowInEasing)) +
                slideInVertically(animationSpec = tween(300, delayMillis = 240, easing = FastOutSlowInEasing)) { it / 6 },
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            shape = KelolaRadius.ShapeMedium,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = modifier
                .fillMaxWidth()
                .kelolaSoftShadow(KelolaRadius.ShapeMedium, 2.dp)
                .testTag("button_open_notes_home")
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Catatan & Riwayat Toko",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pengingat stok, catatan transaksi & aktivitas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Buka Catatan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomersShortcutCard(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = 260, easing = FastOutSlowInEasing)) +
                slideInVertically(animationSpec = tween(300, delayMillis = 260, easing = FastOutSlowInEasing)) { it / 6 },
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            shape = KelolaRadius.ShapeMedium,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = modifier
                .fillMaxWidth()
                .kelolaSoftShadow(KelolaRadius.ShapeMedium, 2.dp)
                .testTag("button_open_customers_home")
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "List Pelanggan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kelola pelanggan, kasbon & kembalian tertunda",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Buka List Pelanggan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
