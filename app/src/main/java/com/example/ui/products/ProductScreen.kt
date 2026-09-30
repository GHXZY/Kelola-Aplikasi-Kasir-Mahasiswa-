package com.example.ui.products

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.CategoryChipGroup
import com.example.ui.components.EditorialCategoryTag
import com.example.ui.components.EmptyState
import com.example.ui.components.SearchField
import com.example.ui.components.StockBadge
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
fun ProductScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    onOpenAddProduct: () -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onRestockProduct: (ProductEntity) -> Unit,
    onReduceStockProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onNavigateToCashier: () -> Unit = {},
    onOpenAddExpense: () -> Unit = {},
    onOpenPromo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("Semua") }

    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val categoryNames = remember(categories) {
        listOf("Semua") + categories.map { it.name }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategoryName, categoryMap) {
        products.filter { prod ->
            val matchSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    (prod.barcode?.contains(searchQuery.trim(), ignoreCase = true) == true) ||
                    (categoryMap[prod.categoryId]?.name?.contains(searchQuery, ignoreCase = true) == true)

            val matchCategory = selectedCategoryName == "Semua" ||
                    categoryMap[prod.categoryId]?.name.equals(selectedCategoryName, ignoreCase = true)

            matchSearch && matchCategory
        }
    }

    val totalAssetValue = remember(products) {
        products.sumOf { it.costPrice * it.stock }
    }

    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Promo & Bundling
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable {
                                isFabExpanded = false
                                onOpenPromo()
                            }
                        ) {
                            Surface(
                                shape = KelolaRadius.ShapeChip,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Text(
                                    text = "Promo & Bundling",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            FloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onOpenPromo()
                                },
                                shape = CircleShape,
                                containerColor = PrimaryBlueContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("fab_promo")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = "Promo & Bundling",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 2. Pengeluaran (Catat Biaya)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable {
                                isFabExpanded = false
                                onOpenAddExpense()
                            }
                        ) {
                            Surface(
                                shape = KelolaRadius.ShapeChip,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Text(
                                    text = "Catat Biaya / Pengeluaran",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DangerRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            FloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onOpenAddExpense()
                                },
                                shape = CircleShape,
                                containerColor = DangerContainer,
                                contentColor = DangerRed,
                                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("fab_add_expense")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = "Catat Biaya",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 3. Tambah Produk
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable {
                                isFabExpanded = false
                                onOpenAddProduct()
                            }
                        ) {
                            Surface(
                                shape = KelolaRadius.ShapeChip,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Text(
                                    text = "Tambah Produk",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            FloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onOpenAddProduct()
                                },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("fab_sub_add_product")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Produk",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Main Toggle FAB
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("fab_main_toggle")
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isFabExpanded) "Tutup Menu Aksi" else "Menu Aksi Cepat",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = KelolaSpacing.ScreenMargin)
        ) {
            Spacer(modifier = Modifier.height(KelolaSpacing.Space1))

            Column {
                Text(
                    text = "Manajemen Produk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total ${products.size} barang terdaftar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Field
            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Cari nama barang atau kategori...",
                testTag = "inventory_search_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            CategoryChipGroup(
                categories = categoryNames,
                selectedCategory = selectedCategoryName,
                onSelectCategory = { selectedCategoryName = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory2,
                    title = "Belum Ada Produk",
                    description = if (searchQuery.isNotEmpty()) "Tidak ada produk yang sesuai dengan pencarian." else "Tambahkan produk jualanmu agar bisa mulai bertransaksi di Kasir.",
                    buttonText = if (searchQuery.isEmpty()) "+ Tambah Produk Pertama" else null,
                    onButtonClick = if (searchQuery.isEmpty()) onOpenAddProduct else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp)
                                .testTag("product_row_${product.id}"),
                            shape = KelolaRadius.ShapeCard,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Product initials box 44x44
                                        val initials = product.name.take(2).uppercase()
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = KelolaRadius.ShapeInput,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = initials,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            val catName = categoryMap[product.categoryId]?.name ?: "Umum"
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                EditorialCategoryTag(category = catName)

                                                if (product.expirationDate != null) {
                                                    val now = System.currentTimeMillis()
                                                    val isExpired = product.expirationDate < now
                                                    val isExpiringSoon = !isExpired && (product.expirationDate - now) < (7L * 86400000L)
                                                    if (isExpired || isExpiringSoon) {
                                                        Surface(
                                                            shape = KelolaRadius.ShapeSmall,
                                                            color = if (isExpired) DangerContainer else WarningContainer
                                                        ) {
                                                            Text(
                                                                text = if (isExpired) "Kadaluarsa" else "Segera Exp.",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = if (isExpired) DangerRed else WarningAmber,
                                                                fontWeight = FontWeight.SemiBold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = product.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "Modal: ${FormatUtils.formatRupiah(product.costPrice)} • Jual: ${FormatUtils.formatRupiah(product.sellingPrice)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            if (!product.barcode.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Barcode: ${product.barcode}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    StockBadge(
                                        stock = product.stock,
                                        minimumStock = product.minimumStock,
                                        unit = product.unit
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                androidx.compose.material3.HorizontalDivider(
                                    color = BorderLight,
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Bottom Actions Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Restock (+) & Reduce (-) buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            onClick = { onRestockProduct(product) },
                                            shape = KelolaRadius.ShapeInput,
                                            color = SuccessContainer,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("button_restock_${product.id}")
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Tambah Stok",
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = { onReduceStockProduct(product) },
                                            shape = KelolaRadius.ShapeInput,
                                            color = WarningContainer,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("button_reduce_stock_${product.id}")
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(
                                                    Icons.Default.Remove,
                                                    contentDescription = "Kurangi Stok",
                                                    tint = WarningAmber,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Right: Edit & Delete buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            onClick = { onEditProduct(product) },
                                            shape = KelolaRadius.ShapeInput,
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("button_edit_product_${product.id}")
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Produk",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = { onDeleteProduct(product) },
                                            shape = KelolaRadius.ShapeInput,
                                            color = DangerContainer,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("button_delete_product_${product.id}")
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus Produk",
                                                    tint = DangerRed,
                                                    modifier = Modifier.size(18.dp)
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
    }
}
