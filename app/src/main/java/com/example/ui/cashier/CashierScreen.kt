package com.example.ui.cashier

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.CartSummary
import com.example.ui.components.CategoryChipGroup
import com.example.ui.components.EmptyState
import com.example.ui.components.KelolaSecondaryButton
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
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.kelolaSoftShadow
import com.example.util.FormatUtils

@Composable
fun CashierScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    cart: CartSummary,
    onAddToCart: (ProductEntity) -> Unit,
    onUpdateQuantity: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit = {},
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("Semua") }
    var showCancelOrderDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val categoryMap = remember(categories) {
        categories.associateBy { it.id }
    }

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

    // Map of product in cart quantities for instant visual feedback
    val cartQtyMap = remember(cart) {
        cart.items.associate { it.product.id to it.quantity }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KelolaSpacing.ScreenMargin)
        ) {
            Spacer(modifier = Modifier.height(KelolaSpacing.Space1))

            // Search Bar & Barcode Scanner Integration
            SearchField(
                query = searchQuery,
                onQueryChange = { newQuery ->
                    val trimmed = newQuery.trim()
                    // Deteksi instan jika input adalah hasil pemindaian scanner barcode (cocok tepat dengan barcode produk)
                    val exactScannedProduct = products.find { it.barcode != null && it.barcode.equals(trimmed, ignoreCase = true) }
                    if (exactScannedProduct != null && exactScannedProduct.stock > 0 && trimmed.length >= 8) {
                        onAddToCart(exactScannedProduct)
                        searchQuery = "" // Reset query agar kasir langsung siap untuk scan berikutnya
                    } else {
                        searchQuery = newQuery
                    }
                },
                placeholder = "Cari produk, kategori, atau barcode...",
                testTag = "cashier_search_input"
            )

            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

            // Category Chips (Tombol kecil, chip kategori: radius 10px, tanpa border)
            CategoryChipGroup(
                categories = categoryNames,
                selectedCategory = selectedCategoryName,
                onSelectCategory = { selectedCategoryName = it }
            )

            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

            // Tombol [ Scan Barcode ] (Search -> Kategori -> [ Scan Barcode ] -> Daftar Produk)
            KelolaSecondaryButton(
                text = "Scan Barcode",
                icon = Icons.Default.QrCodeScanner,
                onClick = { showBarcodeScanner = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_scan_barcode_cashier")
            )

            Spacer(modifier = Modifier.height(KelolaSpacing.Space3))

            if (filteredProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory2,
                    title = "Produk Tidak Ditemukan",
                    description = if (searchQuery.isNotEmpty()) "Tidak ada produk yang sesuai dengan '$searchQuery'." else "Belum ada produk di kategori ini.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3),
                    verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3),
                    contentPadding = PaddingValues(bottom = if (cart.totalItemCount > 0) 180.dp else 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val qtyInCart = cartQtyMap[product.id] ?: 0
                        val isOutOfStock = product.stock <= 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KelolaRadius.ShapeCard)
                                .clickable(enabled = !isOutOfStock && qtyInCart == 0) { onAddToCart(product) }
                                .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 3.dp)
                                .testTag("product_pos_card_${product.id}"),
                            shape = KelolaRadius.ShapeCard,
                            colors = CardDefaults.cardColors(
                                containerColor = if (qtyInCart > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Product Header / Category + Tombol Batalkan Pembelian
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val catName = categoryMap[product.categoryId]?.name ?: "Umum"
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if (qtyInCart > 0) {
                                        Surface(
                                            onClick = { onRemoveFromCart(product.id) },
                                            modifier = Modifier
                                                .height(32.dp)
                                                .testTag("button_cancel_product_${product.id}"),
                                            color = DangerContainer,
                                            shape = KelolaRadius.ShapeSmall
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Batalkan pembelian ${product.name}",
                                                    tint = DangerRed,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Batal",
                                                    color = DangerRed,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Product visual thumbnail container
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .clip(KelolaRadius.ShapeInput)
                                        .background(
                                            if (qtyInCart > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = product.name.take(2).uppercase()
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Product Name
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Price
                                Text(
                                    text = FormatUtils.formatRupiah(product.sellingPrice),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Stock info
                                StockBadge(
                                    stock = product.stock,
                                    minimumStock = product.minimumStock,
                                    unit = product.unit
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Cart control action
                                if (qtyInCart > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            onClick = { onUpdateQuantity(product.id, qtyInCart - 1) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("button_decrease_cart_${product.id}"),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Kurang",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "$qtyInCart",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Surface(
                                            onClick = {
                                                if (qtyInCart < product.stock) {
                                                    onUpdateQuantity(product.id, qtyInCart + 1)
                                                }
                                            },
                                            enabled = qtyInCart < product.stock,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("button_increase_cart_${product.id}"),
                                            shape = CircleShape,
                                            color = if (qtyInCart < product.stock) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Tambah",
                                                    tint = if (qtyInCart < product.stock) MaterialTheme.colorScheme.primary else KelolaTheme.textTertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        onClick = { onAddToCart(product) },
                                        enabled = !isOutOfStock,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .testTag("button_add_to_cart_${product.id}"),
                                        shape = KelolaRadius.ShapeSmall,
                                        color = if (!isOutOfStock) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = if (!isOutOfStock) MaterialTheme.colorScheme.primary else KelolaTheme.textTertiary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isOutOfStock) "Habis" else "Pilih",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (!isOutOfStock) MaterialTheme.colorScheme.primary else KelolaTheme.textTertiary,
                                                maxLines = 1,
                                                softWrap = false
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

        // Sticky Bottom Cart Bar (Radius 24px, Soft shadow, Border subtle)
        if (cart.totalItemCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .kelolaSoftShadow(shape = KelolaRadius.ShapeSheet, elevation = 12.dp),
                shape = KelolaRadius.ShapeSheet,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Total Pesanan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${cart.totalItemCount} barang dipilih",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                            if (cart.appliedPromos.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = KelolaRadius.ShapeSmall,
                                    color = SuccessContainer
                                ) {
                                    Text(
                                        text = "Promo Hemat: -${FormatUtils.formatRupiah(cart.promoDiscount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (cart.appliedPromos.isNotEmpty()) {
                                Text(
                                    text = FormatUtils.formatRupiah(cart.subtotal),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = FormatUtils.formatRupiah(cart.total),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(KelolaSpacing.Space3))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Tidak Jadi Beli (Tombol Sekunder: 44px, Soft Danger)
                        Surface(
                            onClick = { showCancelOrderDialog = true },
                            shape = KelolaRadius.ShapeInput,
                            color = DangerContainer,
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(KelolaSpacing.ButtonHeightCta)
                                .testTag("button_cancel_order_bar")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = KelolaSpacing.Space3)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(KelolaSpacing.Space1))
                                Text(
                                    text = "Batal Beli",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DangerRed,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Tombol Bayar / Buka Keranjang (Tombol Utama CTA: 48px, Solid Primary)
                        Surface(
                            onClick = onOpenCart,
                            shape = KelolaRadius.ShapeInput,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(KelolaSpacing.ButtonHeightCta)
                                .testTag("button_open_cart_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = KelolaSpacing.Space4),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Bayar",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog Konfirmasi Tidak Jadi Beli (Modal Radius 32px, Tanpa Border)
        if (showCancelOrderDialog) {
            AlertDialog(
                onDismissRequest = { showCancelOrderDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DangerContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Batalkan Pembelian?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "Keranjang belanja akan dikosongkan (${cart.totalItemCount} barang senilai ${FormatUtils.formatRupiah(cart.total)}). Apakah pembeli tidak jadi beli?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Surface(
                        onClick = {
                            onClearCart()
                            showCancelOrderDialog = false
                        },
                        color = DangerRed,
                        shape = KelolaRadius.ShapeInput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("button_confirm_cancel_cart")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Ya, Batalkan Pesanan", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCancelOrderDialog = false },
                        shape = KelolaRadius.ShapeInput,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lanjut Belanja", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                shape = KelolaRadius.ShapeCard,
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // Dialog Scanner Barcode Kamera (CameraX + ML Kit)
        if (showBarcodeScanner) {
            BarcodeScannerDialog(
                products = products,
                cart = cart,
                onProductScanned = { scannedProduct ->
                    onAddToCart(scannedProduct)
                },
                onDismissRequest = {
                    showBarcodeScanner = false
                }
            )
        }
    }
}
