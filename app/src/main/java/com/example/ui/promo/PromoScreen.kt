package com.example.ui.promo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import com.example.ui.theme.AdaptiveContainer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.QuantityStepper
import com.example.ui.components.SearchField
import com.example.ui.components.SummaryCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandSky
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GradientBrand
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
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

/**
 * PromoScreen:
 * Laman mandiri manajemen Promo & Bundling kasir.
 * Mengikuti Kelola Design & Layout System:
 * - Bebas border, kontras lewat bayangan lembut dan warna latar.
 * - Radius ramah Apple (ShapeCard 20px, ShapeInput 14px, ShapeSmall 10px).
 * - Spacing konsisten berbasis kelipatan 4px (KelolaSpacing).
 * - Anti-dua baris pada tombol dan chip filter.
 */
@Composable
fun PromoScreen(
    promos: List<PromoEntity>,
    products: List<ProductEntity>,
    onSavePromo: (PromoEntity) -> Unit,
    onTogglePromoActive: (Long, Boolean) -> Unit,
    onDeletePromo: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCreatingOrEditing by remember { mutableStateOf(false) }
    var promoToEdit by remember { mutableStateOf<PromoEntity?>(null) }
    var promoToDelete by remember { mutableStateOf<PromoEntity?>(null) }

    // Tangani tombol back di Android
    BackHandler(enabled = isCreatingOrEditing) {
        isCreatingOrEditing = false
        promoToEdit = null
    }

    BackHandler(enabled = !isCreatingOrEditing) {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PromoScreenTopBar(
                isEditing = isCreatingOrEditing,
                title = if (isCreatingOrEditing) {
                    if (promoToEdit == null) "Buat Promo Baru" else "Edit Promo"
                } else {
                    "Promo & Bundling"
                },
                subtitle = if (isCreatingOrEditing) {
                    "Atur syarat beli bundling dan diskon / hadiah"
                } else {
                    "Diskon paket hemat dan hadiah kasir"
                },
                onBack = {
                    if (isCreatingOrEditing) {
                        isCreatingOrEditing = false
                        promoToEdit = null
                    } else {
                        onNavigateBack()
                    }
                },
                onOpenCreate = {
                    promoToEdit = null
                    isCreatingOrEditing = true
                }
            )
        }
    ) { innerPadding ->
        AdaptiveContainer(maxWidth = 960.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isCreatingOrEditing) {
                    CreateEditPromoScreenContent(
                        promoToEdit = promoToEdit,
                        products = products,
                        onSave = { promo ->
                            onSavePromo(promo)
                            isCreatingOrEditing = false
                            promoToEdit = null
                        },
                        onCancel = {
                            isCreatingOrEditing = false
                            promoToEdit = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PromoListScreenContent(
                        promos = promos,
                        products = products,
                        onAddNew = {
                            promoToEdit = null
                            isCreatingOrEditing = true
                        },
                        onEditPromo = { promo ->
                            promoToEdit = promo
                            isCreatingOrEditing = true
                        },
                        onToggleActive = onTogglePromoActive,
                        onDeleteRequest = { promo ->
                            promoToDelete = promo
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus Promo
    if (promoToDelete != null) {
        AlertDialog(
            onDismissRequest = { promoToDelete = null },
            title = {
                Text(
                    text = "Hapus Promo?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Promo '${promoToDelete?.name}' akan dihapus permanen dan tidak akan diterapkan lagi di kasir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = promoToDelete?.id
                        if (id != null) {
                            onDeletePromo(id)
                        }
                        promoToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_promo")
                ) {
                    Text("Hapus", color = DangerRed, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            },
            dismissButton = {
                TextButton(onClick = { promoToDelete = null }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = KelolaRadius.ShapeCard
        )
    }
}

/**
 * TopBar resmi untuk PromoScreen:
 * - 44x44px circular Back Button (sesuai Apple HIG & Kelola Spacing).
 * - Judul & Subtitle rata kiri, sejajar dengan margin halaman 20px.
 * - Tombol Add bulat 44x44px di sisi kanan saat mode daftar.
 */
@Composable
private fun PromoScreenTopBar(
    isEditing: Boolean,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onOpenCreate: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 960.dp)
                    .statusBarsPadding()
                    .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = KelolaSpacing.Space3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(KelolaSpacing.MinTouchTarget)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_back_promo_screen")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!isEditing) {
                    Surface(
                        onClick = onOpenCreate,
                        shape = KelolaRadius.ShapeSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("button_open_create_promo")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Promo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tampilan Daftar Promo:
 * - 2 SummaryCard statistik terpadu (Promo Aktif & Total Promo).
 * - SearchField & Horizontal FilterChip.
 * - EmptyState jika belum ada promo.
 * - Kartu promo yang bersih, tanpa border, dengan bayangan lembut.
 */
@Composable
private fun PromoListScreenContent(
    promos: List<PromoEntity>,
    products: List<ProductEntity>,
    onAddNew: () -> Unit,
    onEditPromo: (PromoEntity) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDeleteRequest: (PromoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val activeCount = remember(promos) { promos.count { it.isActive } }
    val freeProductCount = remember(promos) { promos.count { it.discountType == "FREE_PRODUCT" } }

    val filteredPromos = remember(promos, searchQuery, selectedFilter) {
        promos.filter { promo ->
            val matchSearch = searchQuery.isBlank() || promo.name.contains(searchQuery, ignoreCase = true)
            val matchFilter = when (selectedFilter) {
                "ACTIVE" -> promo.isActive
                "INACTIVE" -> !promo.isActive
                "FREE_PRODUCT" -> promo.discountType == "FREE_PRODUCT"
                "NOMINAL" -> promo.discountType == "NOMINAL"
                "PERCENTAGE" -> promo.discountType == "PERCENTAGE"
                else -> true
            }
            matchSearch && matchFilter
        }
    }

    val productMap = remember(products) { products.associateBy { it.id } }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = KelolaSpacing.ScreenMargin,
            end = KelolaSpacing.ScreenMargin,
            top = KelolaSpacing.Space4,
            bottom = KelolaSpacing.Space6
        ),
        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
    ) {
        // 1. Ringkasan Statistik Promo (2 Kartu Berdampingan Sesuai Grid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
            ) {
                SummaryCard(
                    title = "Promo Aktif",
                    value = "$activeCount promo",
                    subtitle = "Otomatis di kasir",
                    icon = Icons.Default.LocalOffer,
                    contentColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "summary_active_promo"
                )

                SummaryCard(
                    title = "Total Promo",
                    value = "${promos.size} promo",
                    subtitle = if (freeProductCount > 0) "$freeProductCount gratis produk" else "Semua paket diskon",
                    icon = Icons.Default.CardGiftcard,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    testTag = "summary_total_promo"
                )
            }
        }

        // 2. Kolom Pencarian & Horizontal Filter Chip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space2)) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Cari nama promo...",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "search_promo_input"
                )

                val filterOptions = listOf(
                    "ALL" to "Semua",
                    "ACTIVE" to "Aktif",
                    "INACTIVE" to "Nonaktif",
                    "FREE_PRODUCT" to "🎁 Gratis Produk",
                    "NOMINAL" to "🏷️ Potongan Rp",
                    "PERCENTAGE" to "📊 Persentase"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            shape = KelolaRadius.ShapeSmall,
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderLight)
                        )
                    }
                }
            }
        }

        // 3. Daftar Kartu Promo atau Empty State
        if (filteredPromos.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.LocalOffer,
                    title = if (promos.isEmpty()) "Belum Ada Promo" else "Promo Tidak Ditemukan",
                    description = if (promos.isEmpty()) {
                        "Buat promo paket bundling hemat atau beli paket gratis produk untuk memikat pelanggan kasir."
                    } else {
                        "Tidak ada promo yang sesuai dengan kata kunci atau filter saat ini."
                    },
                    buttonText = if (promos.isEmpty()) "Tambah Promo" else null,
                    buttonIcon = if (promos.isEmpty()) Icons.Default.Add else null,
                    onButtonClick = if (promos.isEmpty()) onAddNew else null,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            items(filteredPromos, key = { it.id }) { promo ->
                PromoCardItem(
                    promo = promo,
                    productMap = productMap,
                    onEdit = { onEditPromo(promo) },
                    onToggleActive = { active -> onToggleActive(promo.id, active) },
                    onDelete = { onDeleteRequest(promo) }
                )
            }
        }
    }
}

/**
 * Kartu Item Promo:
 * - Menggunakan elevasi kelolaSoftShadow tanpa garis border kaku.
 * - Desain badge jenis diskon / hadiah yang kontras dan informatif.
 * - Area rincian syarat dan hadiah di wadah berlatar lembut.
 */
@Composable
private fun PromoCardItem(
    promo: PromoEntity,
    productMap: Map<Long, ProductEntity>,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isFreeProduct = promo.discountType.uppercase() == "FREE_PRODUCT"
    val isPercentage = promo.discountType.uppercase() == "PERCENTAGE"

    val freeProduct = if (isFreeProduct && promo.freeProductId != null) {
        productMap[promo.freeProductId]
    } else null

    val requirements = remember(promo.requiredItemsJson) {
        PromoEngine.parseRequirements(promo.requiredItemsJson)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp)
            .testTag("card_promo_${promo.id}"),
        shape = KelolaRadius.ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(KelolaSpacing.Space4)) {
            // Baris Atas: Badge Jenis Promo & Switch Status Aktif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Jenis Diskon / Hadiah
                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = when {
                        !promo.isActive -> MaterialTheme.colorScheme.surfaceVariant
                        isFreeProduct -> SuccessContainer
                        isPercentage -> WarningContainer
                        else -> PrimaryBlueContainer
                    }
                ) {
                    Text(
                        text = when {
                            isFreeProduct -> "🎁 GRATIS PRODUK"
                            isPercentage -> "DISKON ${promo.discountValue}%"
                            else -> "HEMAT ${FormatUtils.formatRupiah(promo.discountValue)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            !promo.isActive -> MaterialTheme.colorScheme.onSurfaceVariant
                            isFreeProduct -> SuccessGreen
                            isPercentage -> WarningAmber
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Switch Aktif/Nonaktif
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (promo.isActive) "Aktif" else "Nonaktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (promo.isActive) SuccessGreen else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = promo.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("switch_promo_${promo.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

            // Nama Promo
            Text(
                text = promo.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (promo.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

            // Rincian Syarat Beli Bundling
            Surface(
                shape = KelolaRadius.ShapeSmall,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Syarat Bundling Keranjang:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (requirements.isEmpty()) {
                        Text(
                            text = "Semua produk (berlaku tanpa syarat paket)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        requirements.forEach { req ->
                            val prod = productMap[req.productId]
                            val pName = prod?.name ?: "Produk #${req.productId}"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${req.quantity}x $pName",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (prod != null) {
                                    Text(
                                        text = FormatUtils.formatRupiah(prod.sellingPrice * req.quantity),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

            // Kotak Hadiah / Diskon yang Didapat
            if (isFreeProduct) {
                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = SuccessContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎁",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            val freeName = freeProduct?.name ?: "Produk #${promo.freeProductId ?: ""}"
                            Text(
                                text = "Hadiah Gratis: ${promo.freeQuantity}x $freeName",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                            if (freeProduct != null) {
                                Text(
                                    text = "Nilai: ${FormatUtils.formatRupiah(freeProduct.sellingPrice * promo.freeQuantity)} • Otomatis Rp 0 di kasir",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = KelolaRadius.ShapeSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            val discountText = if (isPercentage) {
                                "Diskon Persentase: ${promo.discountValue}%"
                            } else {
                                "Potongan Harga: ${FormatUtils.formatRupiah(promo.discountValue)}"
                            }
                            Text(
                                text = discountText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Diproses otomatis saat syarat bundling terpenuhi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Info Batas Penggunaan
            Spacer(modifier = Modifier.height(KelolaSpacing.Space2))
            Text(
                text = if (promo.maxUsage > 0) {
                    "Batas Penggunaan: Maksimal ${promo.maxUsage}x per transaksi"
                } else {
                    "Batas Penggunaan: Berlaku kelipatan tanpa batas"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            // Tombol Aksi Edit & Hapus
            Spacer(modifier = Modifier.height(KelolaSpacing.Space3))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onEdit,
                    shape = KelolaRadius.ShapeSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("button_edit_promo_${promo.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(KelolaSpacing.Space2))

                Surface(
                    onClick = onDelete,
                    shape = KelolaRadius.ShapeSmall,
                    color = DangerContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("button_delete_promo_${promo.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hapus",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DangerRed,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formulir Buat / Edit Promo:
 * - Single column dengan padding konsisten 20px.
 * - Kontrol segmented 3 jenis diskon (Potongan Rp, Persen %, Hadiah Gratis).
 * - Pemilih produk hadiah gratis dan stepper kuantitas.
 * - Tombol aksi Simpan & Batal dengan tinggi 52px (ButtonHeightCta) di akhir alur.
 */
@Composable
private fun CreateEditPromoScreenContent(
    promoToEdit: PromoEntity?,
    products: List<ProductEntity>,
    onSave: (PromoEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(promoToEdit?.name ?: "") }
    var discountType by remember { mutableStateOf(promoToEdit?.discountType ?: "NOMINAL") }

    var discountValueText by remember {
        val initial = if (promoToEdit != null && promoToEdit.discountValue > 0) {
            if (promoToEdit.discountType == "PERCENTAGE") {
                promoToEdit.discountValue.toString()
            } else {
                FormatUtils.formatNumberWithDots(promoToEdit.discountValue)
            }
        } else ""
        mutableStateOf(initial)
    }

    var selectedFreeProductId by remember { mutableStateOf<Long?>(promoToEdit?.freeProductId) }
    var freeQuantity by remember { mutableStateOf(promoToEdit?.freeQuantity ?: 1) }

    var maxUsageChoice by remember {
        mutableStateOf(
            if (promoToEdit == null || promoToEdit.maxUsage == 0) "UNLIMITED"
            else if (promoToEdit.maxUsage == 1) "ONCE"
            else "CUSTOM"
        )
    }
    var customMaxUsageText by remember {
        mutableStateOf(if (promoToEdit != null && promoToEdit.maxUsage > 1) promoToEdit.maxUsage.toString() else "2")
    }
    var isActive by remember { mutableStateOf(promoToEdit?.isActive ?: true) }

    var requirements by remember {
        val initial = if (promoToEdit != null) {
            PromoEngine.parseRequirements(promoToEdit.requiredItemsJson)
        } else {
            emptyList()
        }
        mutableStateOf(initial)
    }

    var showRequiredProductPicker by remember { mutableStateOf(false) }
    var showFreeProductPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val productMap = remember(products) { products.associateBy { it.id } }

    val cleanInputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = KelolaSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4)
    ) {
        // Banner Pesan Kesalahan Validasi
        if (errorMessage != null) {
            Surface(
                color = DangerContainer,
                shape = KelolaRadius.ShapeInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(KelolaSpacing.Space3),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DangerRed
                    )
                }
            }
        }

        // 1. Bagian Nama Promo
        Column {
            Text(
                text = "Nama Promo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(KelolaSpacing.Space1))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                placeholder = { Text("Misal: Beli 2 Kopi Gratis 1 Donat", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = KelolaRadius.ShapeInput,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_promo_name"),
                colors = cleanInputColors
            )
        }

        // 2. Bagian Produk Syarat Bundling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
            shape = KelolaRadius.ShapeCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(KelolaSpacing.Space4)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Produk Syarat Bundling",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Wajib ada di keranjang kasir",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(KelolaSpacing.Space2))

                    Surface(
                        onClick = { showRequiredProductPicker = true },
                        shape = KelolaRadius.ShapeSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("button_add_required_product")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Produk",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(KelolaSpacing.Space3))

                if (requirements.isEmpty()) {
                    Surface(
                        shape = KelolaRadius.ShapeInput,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRequiredProductPicker = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Belum ada produk syarat. Klik tombol '+' untuk menambahkan barang.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah Produk",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space2)) {
                        requirements.forEachIndexed { index, req ->
                            val prod = productMap[req.productId]
                            val prodName = prod?.name ?: "Produk #${req.productId}"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = KelolaRadius.ShapeInput
                                    )
                                    .padding(horizontal = KelolaSpacing.Space3, vertical = KelolaSpacing.Space2),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prodName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (prod != null) {
                                        Text(
                                            text = "Harga: ${FormatUtils.formatRupiah(prod.sellingPrice)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QuantityStepper(
                                        quantity = req.quantity,
                                        onDecrease = {
                                            if (req.quantity > 1) {
                                                val updated = requirements.toMutableList()
                                                updated[index] = req.copy(quantity = req.quantity - 1)
                                                requirements = updated
                                            }
                                        },
                                        onIncrease = {
                                            if (req.quantity < 999) {
                                                val updated = requirements.toMutableList()
                                                updated[index] = req.copy(quantity = req.quantity + 1)
                                                requirements = updated
                                            }
                                        },
                                        minLimit = 1,
                                        maxLimit = 999
                                    )

                                    Spacer(modifier = Modifier.width(KelolaSpacing.Space2))

                                    IconButton(
                                        onClick = {
                                            val updated = requirements.toMutableList()
                                            updated.removeAt(index)
                                            requirements = updated
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Hapus",
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

        // 3. Bagian Jenis Diskon / Hadiah
        Column {
            Text(
                text = "Jenis Diskon / Hadiah",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(KelolaSpacing.Space1))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = KelolaRadius.ShapeInput)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Potongan Rp
                val isNominal = discountType == "NOMINAL"
                Surface(
                    onClick = {
                        discountType = "NOMINAL"
                        discountValueText = FormatUtils.formatRupiahInput(discountValueText)
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = KelolaRadius.ShapeSmall,
                    color = if (isNominal) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isNominal) 1.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Potongan (Rp)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isNominal) FontWeight.Bold else FontWeight.Medium,
                            color = if (isNominal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            fontSize = 12.sp
                        )
                    }
                }

                // Tab 2: Persentase %
                val isPercentage = discountType == "PERCENTAGE"
                Surface(
                    onClick = {
                        discountType = "PERCENTAGE"
                        val digits = discountValueText.filter { it.isDigit() }
                        val num = digits.toIntOrNull() ?: 0
                        discountValueText = if (num > 100) "100" else digits
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = KelolaRadius.ShapeSmall,
                    color = if (isPercentage) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isPercentage) 1.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Persentase (%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPercentage) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPercentage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            fontSize = 12.sp
                        )
                    }
                }

                // Tab 3: Gratis Produk 🎁
                val isFreeProduct = discountType == "FREE_PRODUCT"
                Surface(
                    onClick = {
                        discountType = "FREE_PRODUCT"
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = KelolaRadius.ShapeSmall,
                    color = if (isFreeProduct) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isFreeProduct) 1.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🎁 Gratis Produk",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isFreeProduct) FontWeight.Bold else FontWeight.Medium,
                            color = if (isFreeProduct) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 4. Konfigurasi Khusus Berdasarkan Jenis Diskon
        if (discountType == "FREE_PRODUCT") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                shape = KelolaRadius.ShapeCard,
                colors = CardDefaults.cardColors(containerColor = SuccessContainer.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(KelolaSpacing.Space4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Produk Hadiah yang Digratiskan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "Otomatis dipotong 100% (Rp 0) di kasir",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(KelolaSpacing.Space2))

                        Surface(
                            onClick = { showFreeProductPicker = true },
                            shape = KelolaRadius.ShapeSmall,
                            color = SuccessGreen,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("button_select_free_product")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (selectedFreeProductId == null) Icons.Default.Add else Icons.Default.Check,
                                    contentDescription = "Pilih Hadiah",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(KelolaSpacing.Space3))

                    val selectedProd = selectedFreeProductId?.let { productMap[it] }

                    if (selectedProd == null) {
                        Surface(
                            shape = KelolaRadius.ShapeInput,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFreeProductPicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Belum ada produk gratis yang dipilih. Klik tombol '+' untuk memilih hadiah.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = SuccessGreen,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Pilih Hadiah",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = KelolaRadius.ShapeInput,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(KelolaSpacing.Space3),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedProd.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Harga: ${FormatUtils.formatRupiah(selectedProd.sellingPrice)} • Stok: ${selectedProd.stock} ${selectedProd.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Qty:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    QuantityStepper(
                                        quantity = freeQuantity,
                                        onDecrease = {
                                            if (freeQuantity > 1) freeQuantity--
                                        },
                                        onIncrease = {
                                            if (freeQuantity < 99) freeQuantity++
                                        },
                                        minLimit = 1,
                                        maxLimit = 99
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

                        val totalSyaratCount = requirements.sumOf { it.quantity }
                        Surface(
                            shape = KelolaRadius.ShapeSmall,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Di kasir: Pelanggan yang membeli $totalSyaratCount produk syarat otomatis berhak mendapatkan gratis $freeQuantity ${selectedProd.unit} '${selectedProd.name}' (Hemat ${FormatUtils.formatRupiah(selectedProd.sellingPrice * freeQuantity)}).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Column {
                Text(
                    text = if (discountType == "PERCENTAGE") "Besar Diskon Persen" else "Besar Potongan Diskon",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(KelolaSpacing.Space1))
                OutlinedTextField(
                    value = discountValueText,
                    onValueChange = { input ->
                        if (discountType == "PERCENTAGE") {
                            val digits = input.filter { it.isDigit() }
                            val num = digits.toIntOrNull() ?: 0
                            discountValueText = if (num > 100) "100" else digits
                        } else {
                            discountValueText = FormatUtils.formatRupiahInput(input)
                        }
                        errorMessage = null
                    },
                    prefix = if (discountType == "NOMINAL") {
                        { Text("Rp ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) }
                    } else null,
                    suffix = if (discountType == "PERCENTAGE") {
                        { Text("%", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary) }
                    } else null,
                    placeholder = {
                        Text(
                            if (discountType == "PERCENTAGE") "10" else "10.000",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = KelolaRadius.ShapeInput,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_promo_discount_value"),
                    colors = cleanInputColors
                )
            }
        }

        // 5. Bagian Batas Penggunaan per Transaksi
        Column {
            Text(
                text = "Batas Penggunaan per Transaksi",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(KelolaSpacing.Space1))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = KelolaRadius.ShapeInput)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "UNLIMITED" to "Tanpa Batas",
                    "ONCE" to "Maks 1x",
                    "CUSTOM" to "Kustom"
                ).forEach { (choice, label) ->
                    val isSelected = maxUsageChoice == choice
                    Surface(
                        onClick = { maxUsageChoice = choice },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = KelolaRadius.ShapeSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isSelected) 1.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            if (maxUsageChoice == "CUSTOM") {
                Spacer(modifier = Modifier.height(KelolaSpacing.Space2))
                OutlinedTextField(
                    value = customMaxUsageText,
                    onValueChange = { customMaxUsageText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Contoh: 2 (Maksimal 2x per transaksi)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = KelolaRadius.ShapeInput,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_promo_custom_max_usage"),
                    colors = cleanInputColors
                )
            }
        }

        // 6. Baris Status Promo Aktif
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = KelolaRadius.ShapeInput,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = KelolaSpacing.Space3, vertical = KelolaSpacing.Space2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktifkan Promo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Promo aktif akan otomatis dihitung saat kasir memproses pesanan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

        // 7. Tombol Aksi Simpan & Batal (Anti-Dua Baris, Sesuai Kelola Layout System)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
        ) {
            Surface(
                onClick = onCancel,
                shape = KelolaRadius.ShapeInput,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .weight(1f)
                    .height(KelolaSpacing.ButtonHeightCta)
                    .testTag("button_cancel_promo")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Surface(
                onClick = {
                    if (name.trim().isBlank()) {
                        errorMessage = "Nama promo tidak boleh kosong!"
                        return@Surface
                    }
                    if (requirements.isEmpty()) {
                        errorMessage = "Pilih minimal 1 produk syarat bundling!"
                        return@Surface
                    }

                    var finalDiscountValue = 0L
                    var finalFreeProductId: Long? = null
                    var finalFreeQuantity = 1

                    if (discountType == "FREE_PRODUCT") {
                        if (selectedFreeProductId == null) {
                            errorMessage = "Silakan pilih produk yang digratiskan!"
                            return@Surface
                        }
                        finalFreeProductId = selectedFreeProductId
                        finalFreeQuantity = freeQuantity.coerceAtLeast(1)
                    } else {
                        val parsedVal = if (discountType == "PERCENTAGE") {
                            discountValueText.toLongOrNull() ?: 0L
                        } else {
                            FormatUtils.parseRupiahInput(discountValueText)
                        }

                        if (parsedVal <= 0L) {
                            errorMessage = "Besar diskon harus lebih dari 0!"
                            return@Surface
                        }
                        if (discountType == "PERCENTAGE" && parsedVal > 100L) {
                            errorMessage = "Diskon persentase maksimal 100%!"
                            return@Surface
                        }
                        finalDiscountValue = parsedVal
                    }

                    val finalMaxUsage = when (maxUsageChoice) {
                        "ONCE" -> 1
                        "CUSTOM" -> (customMaxUsageText.toIntOrNull() ?: 2).coerceAtLeast(1)
                        else -> 0
                    }

                    val promo = PromoEntity(
                        id = promoToEdit?.id ?: 0L,
                        name = name.trim(),
                        isActive = isActive,
                        discountType = discountType,
                        discountValue = finalDiscountValue,
                        maxUsage = finalMaxUsage,
                        requiredItemsJson = PromoEngine.serializeRequirements(requirements),
                        freeProductId = finalFreeProductId,
                        freeQuantity = finalFreeQuantity,
                        createdAt = promoToEdit?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    onSave(promo)
                },
                shape = KelolaRadius.ShapeInput,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1.5f)
                    .height(KelolaSpacing.ButtonHeightCta)
                    .testTag("button_save_promo")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = KelolaSpacing.Space4),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (promoToEdit == null) "Buat Promo" else "Simpan Promo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(KelolaSpacing.Space6))
    }

    // Dialog Pemilih Produk Syarat
    if (showRequiredProductPicker) {
        ProductSelectorDialog(
            title = "Pilih Produk Syarat Bundling",
            subtitle = "Produk yang harus dibeli oleh pelanggan",
            products = products,
            onSelect = { selectedProduct ->
                val existing = requirements.find { it.productId == selectedProduct.id }
                if (existing == null) {
                    requirements = requirements + PromoRequirement(productId = selectedProduct.id, quantity = 1)
                }
                showRequiredProductPicker = false
            },
            onDismiss = { showRequiredProductPicker = false }
        )
    }

    // Dialog Pemilih Produk Hadiah Gratis
    if (showFreeProductPicker) {
        ProductSelectorDialog(
            title = "Pilih Produk Hadiah Gratis",
            subtitle = "Produk ini akan bernilai Rp 0 (Diskon 100%) di kasir",
            products = products,
            onSelect = { selectedProduct ->
                selectedFreeProductId = selectedProduct.id
                showFreeProductPicker = false
            },
            onDismiss = { showFreeProductPicker = false }
        )
    }
}

/**
 * Dialog Pemilih Produk dari Katalog:
 * - Bentuk kartu ber-radius Apple (ShapeCard 20px).
 * - Fitur pencarian instan.
 * - Tombol aksi rapi dengan area tap yang nyaman.
 */
@Composable
private fun ProductSelectorDialog(
    title: String,
    subtitle: String,
    products: List<ProductEntity>,
    onSelect: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(products, search) {
        if (search.isBlank()) products
        else products.filter { it.name.contains(search, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Cari nama produk...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = KelolaRadius.ShapeInput,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(KelolaSpacing.Space2))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = KelolaSpacing.Space4),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (products.isEmpty()) "Katalog produk masih kosong. Tambahkan produk di menu Produk terlebih dahulu." else "Produk tidak ditemukan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(filtered, key = { it.id }) { prod ->
                            Surface(
                                onClick = { onSelect(prod) },
                                shape = KelolaRadius.ShapeInput,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${FormatUtils.formatRupiah(prod.sellingPrice)} • Stok ${prod.stock} ${prod.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Pilih",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = MaterialTheme.colorScheme.primary, maxLines = 1, softWrap = false)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = KelolaRadius.ShapeCard
    )
}
