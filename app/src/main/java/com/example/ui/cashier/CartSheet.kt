package com.example.ui.cashier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.CartSummary
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.QuantityStepper
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(
    cart: CartSummary,
    sheetState: SheetState,
    onIncreaseQty: (ProductEntity) -> Unit,
    onDecreaseQty: (ProductEntity) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onApplyDiscount: (Long) -> Unit,
    onClearCart: () -> Unit,
    onProceedToPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDiscountInput by remember { mutableStateOf(false) }
    var discountPercent by remember { mutableIntStateOf(0) }
    var customDiscountAmount by remember { mutableStateOf("") }
    var showCancelOrderConfirm by remember { mutableStateOf(false) }

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
                // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Keranjang",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${cart.totalItemCount} item)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("button_cart_header_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Keranjang",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cart.items.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ShoppingBag,
                    title = "Keranjang Kosong",
                    description = "Pilih produk dari daftar kasir untuk menambahkan ke keranjang.",
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                // Cart Items List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cart.items, key = { it.product.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KelolaRadius.ShapeCard,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                    Text(
                                        text = item.product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${FormatUtils.formatRupiah(item.product.sellingPrice)} / ${item.product.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Subtotal: ${FormatUtils.formatRupiah(item.subtotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QuantityStepper(
                                        quantity = item.quantity,
                                        onDecrease = { onDecreaseQty(item.product) },
                                        onIncrease = { onIncreaseQty(item.product) },
                                        maxLimit = item.product.stock
                                    )

                                    IconButton(
                                        onClick = { onRemoveItem(item.product.id) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .testTag("cart_remove_item_${item.product.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Hapus item ${item.product.name}",
                                            tint = DangerRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Discount Accordion / Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDiscountInput = !showDiscountInput }
                    ) {
                        Icon(
                            Icons.Default.Discount,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cart.discount > 0) "Diskon: ${FormatUtils.formatRupiah(cart.discount)}" else "+ Beri Diskon",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (cart.discount > 0) {
                        TextButton(
                            onClick = {
                                discountPercent = 0
                                customDiscountAmount = ""
                                onApplyDiscount(0L)
                            }
                        ) {
                            Text("Hapus Diskon", color = DangerRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (showDiscountInput) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KelolaRadius.ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Pilih Diskon Cepat",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 15, 20).forEach { pct ->
                                    val isSelected = discountPercent == pct
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            discountPercent = pct
                                            val disc = (cart.subtotal * pct / 100)
                                            onApplyDiscount(disc)
                                        },
                                        label = { Text("$pct%") },
                                        shape = KelolaRadius.ShapeSmall,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = null
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = customDiscountAmount,
                                onValueChange = { input ->
                                    val formatted = FormatUtils.formatRupiahInput(input)
                                    customDiscountAmount = formatted
                                    val disc = FormatUtils.parseRupiahInput(formatted)
                                    discountPercent = 0
                                    onApplyDiscount(disc.coerceAtMost(cart.subtotal))
                                },
                                label = { Text("Atau Nominal Diskon (Rp)") },
                                prefix = { Text("Rp ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                                placeholder = { Text("10.000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = KelolaRadius.ShapeInput,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Breakdown inside a neat quiet card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatRupiah(cart.subtotal), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }

                        if (cart.appliedPromos.isNotEmpty()) {
                            cart.appliedPromos.forEach { promoInfo ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = KelolaRadius.ShapeSmall,
                                            color = SuccessContainer
                                        ) {
                                            Text(
                                                text = "PROMO",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = promoInfo.promoName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = if (promoInfo.discountAmount > 0) "-${FormatUtils.formatRupiah(promoInfo.discountAmount)}" else "Belum diklaim",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (promoInfo.discountAmount > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (promoInfo.discountType == "FREE_PRODUCT" && promoInfo.freeQuantityInCart < promoInfo.freeQuantityEligible) {
                                    val remaining = promoInfo.freeQuantityEligible - promoInfo.freeQuantityInCart
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = KelolaRadius.ShapeSmall,
                                        color = SuccessContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🎁 Klaim Hadiah: Masukkan $remaining pcs '${promoInfo.freeProductName}' ke keranjang (otomatis Rp 0).",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (cart.manualDiscount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Diskon Tambahan", color = KelolaTheme.negative)
                                Text("-${FormatUtils.formatRupiah(cart.manualDiscount)}", fontWeight = FontWeight.SemiBold, color = KelolaTheme.negative)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = FormatUtils.formatRupiah(cart.total),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CTA Bayar: Gradient Brand, Radius 12px, Teks Putih
                Surface(
                    onClick = onProceedToPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(KelolaSpacing.ButtonHeightCta)
                        .clip(KelolaRadius.ShapeInput)
                        .testTag("button_checkout_pay"),
                    shape = KelolaRadius.ShapeInput,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GradientBrand),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bayar ${FormatUtils.formatRupiah(cart.total)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tombol Batalkan Pesanan (Soft Danger, Radius 12px, Tanpa Border)
                Surface(
                    onClick = { showCancelOrderConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(KelolaSpacing.ButtonHeightSecondary)
                        .testTag("button_cart_cancel_order_bottom"),
                    shape = KelolaRadius.ShapeInput,
                    color = DangerContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Batalkan Pesanan",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = DangerRed,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        }

        if (showCancelOrderConfirm) {
            ConfirmationDialog(
                title = "Batalkan Pembelian?",
                message = "Apakah Anda yakin pembeli tidak jadi beli? Seluruh ${cart.totalItemCount} item dalam keranjang senilai ${FormatUtils.formatRupiah(cart.total)} akan dikosongkan.",
                confirmText = "Ya, Batalkan",
                dismissText = "Kembali",
                isDestructive = true,
                onConfirm = {
                    showCancelOrderConfirm = false
                    onClearCart()
                    onDismiss()
                },
                onDismiss = { showCancelOrderConfirm = false }
            )
        }
    }
}
