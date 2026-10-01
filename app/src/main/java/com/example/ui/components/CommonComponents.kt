package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandDeep
import com.example.ui.theme.BrandSky
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

/**
 * Kelola Primary CTA Button (Oceanic Modernity)
 * Height 48-52px, Rounded 8px (ShapeInput), solid primary deep blue (#006199), Inter font.
 */
@Composable
fun KelolaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .height(KelolaSpacing.ButtonHeightCta)
            .clip(KelolaRadius.ShapeInput),
        shape = KelolaRadius.ShapeInput,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = KelolaSpacing.Space4),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Kelola Secondary Button (Oceanic Modernity)
 * Height 48px, Surface #FFFFFF, 1px solid #006199 border, text #006199, Rounded 12px (ShapeInput).
 */
@Composable
fun KelolaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .height(KelolaSpacing.ButtonHeightSecondary),
        shape = KelolaRadius.ShapeInput,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = KelolaSpacing.Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * SummaryCard:
 * Kartu standar (transaksi, item info). Radius 20px, TANPA border, bayangan lembut.
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 4.dp),
        shape = KelolaRadius.ShapeCard,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(KelolaSpacing.Space1))
                Box(
                    modifier = Modifier
                        .size(KelolaSpacing.IconContainerSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 3 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                            slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it / 3 }
                        )
                },
                label = "SummaryCardValueAnim"
            ) { animatedVal ->
                Text(
                    text = animatedVal,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * SearchField:
 * Input field radius 8px (ShapeInput), 1px subtle border, pure surface container.
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Cari produk...",
    modifier: Modifier = Modifier,
    testTag: String = "search_field"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = KelolaTheme.textTertiary, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Cari",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Hapus pencarian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        shape = KelolaRadius.ShapeInput,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = BorderLight,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

/**
 * CategoryChipGroup:
 * Tombol kecil, chip kategori: radius 8px (ShapeSmall).
 * Surface container dengan 1px border (#E2E8F0 atau #006199 saat aktif).
 */
@Composable
fun CategoryChipGroup(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                shape = KelolaRadius.ShapeSmall,
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

/**
 * CategoryIconGrid:
 * Sesuai panduan resmi Kelola Layout System (Mobile):
 * Grid ikon kategori: 4 kolom, gap 16px (space-4),
 * setiap sel persegi dengan rasio 1:1 supaya label di bawah ikon tidak berdesakan.
 * Tanpa border, kontras warna latar, sudut radius ShapeInput (14px).
 */
@Composable
fun CategoryIconGrid(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chunked = categories.chunked(4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4) // 16px
    ) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space4) // 16px
            ) {
                for (i in 0 until 4) {
                    if (i < rowItems.size) {
                        val category = rowItems[i]
                        val isSelected = category == selectedCategory
                        Surface(
                            onClick = { onSelectCategory(category) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .testTag("category_cell_$category"),
                            shape = KelolaRadius.ShapeInput,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(KelolaSpacing.Space2), // 8px
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (category.equals("Semua", ignoreCase = true)) Icons.Default.GridView else Icons.Default.Category,
                                    contentDescription = category,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(KelolaSpacing.Space1)) // 4px
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Empty placeholder to maintain 4-column 1:1 grid
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * QuantityStepper:
 * Tombol ikon (tap target): 44x44px (minimum Apple HIG).
 * Ikon di dalamnya 18-20px (maks 24px), sisanya padding.
 * Jarak antar elemen interaktif >= space-3 (12px).
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    maxLimit: Int = Int.MAX_VALUE,
    minLimit: Int = 0
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3) // 12px gap
    ) {
        Surface(
            onClick = onDecrease,
            enabled = quantity > minLimit,
            modifier = Modifier
                .size(KelolaSpacing.MinTouchTarget) // 44px
                .testTag("stepper_decrease"),
            shape = CircleShape,
            color = if (quantity > minLimit) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Kurangi",
                    tint = if (quantity > minLimit) MaterialTheme.colorScheme.onSurface else KelolaTheme.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = KelolaSpacing.Space1)
                .testTag("stepper_value"),
            textAlign = TextAlign.Center
        )

        Surface(
            onClick = onIncrease,
            enabled = quantity < maxLimit,
            modifier = Modifier
                .size(KelolaSpacing.MinTouchTarget) // 44px
                .testTag("stepper_increase"),
            shape = CircleShape,
            color = if (quantity < maxLimit) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = if (quantity < maxLimit) MaterialTheme.colorScheme.primary else KelolaTheme.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * StockBadge:
 * Chip kecil radius 10px, tanpa border.
 */
@Composable
fun StockBadge(
    stock: Int,
    minimumStock: Int = 5,
    unit: String = "pcs",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        stock <= 0 -> Triple(DangerContainer, DangerRed, "Habis")
        stock <= minimumStock -> Triple(WarningContainer, WarningAmber, "Sisa $stock $unit")
        else -> Triple(SuccessContainer, SuccessGreen, "$stock $unit")
    }

    Surface(
        modifier = modifier,
        shape = KelolaRadius.ShapeSmall,
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * EditorialCategoryTag:
 * Design System "Oceanic Modernity":
 * Background rgba(138, 207, 248, 0.2), text #002B47, uppercase label-sm (Inter 11px, 600, tracking 0.04em).
 */
@Composable
fun EditorialCategoryTag(
    category: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = KelolaRadius.ShapeSmall,
        color = BrandSky.copy(alpha = 0.2f)
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF002B47),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.44.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/**
 * EmptyState:
 * Tenang, rapi, dengan CTA elegan.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    buttonIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            KelolaPrimaryButton(
                text = buttonText,
                icon = buttonIcon,
                onClick = onButtonClick
            )
        }
    }
}

/**
 * ConfirmationDialog:
 * Modal / dialog radius 32px (Apple HIG sheet/modal), tanpa border.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Hapus",
    dismissText: String = "Batal",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = KelolaRadius.ShapeInput,
                color = if (isDestructive) DangerRed else MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(KelolaSpacing.ButtonHeightCompact)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmText,
                        color = if (isDestructive) Color.White else MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = KelolaRadius.ShapeInput,
                modifier = Modifier.height(KelolaSpacing.ButtonHeightCompact)
            ) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = KelolaRadius.ShapeLarge,
        containerColor = MaterialTheme.colorScheme.surface
    )
}
