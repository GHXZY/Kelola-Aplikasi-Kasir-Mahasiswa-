package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.kelolaSoftShadow

/**
 * AboutScreen:
 * Laman khusus tentang aplikasi Kelola.
 * Memuat informasi pengembang (GHXZY Devindie di bawah Potensi Cerdas Indonesia),
 * deskripsi aplikasi kasir pelajar (local, offline, 100% aman), versi terbaru Kelola 2.5,
 * serta ringkasan kebijakan privasi data pengguna.
 */
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. Top Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .testTag("button_back_about")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Tentang Aplikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Informasi aplikasi, pengembang & privasi",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- 2. Konten Informasi ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = KelolaSpacing.ScreenMargin,
                end = KelolaSpacing.ScreenMargin,
                top = KelolaSpacing.Space4,
                bottom = KelolaSpacing.Space6
            ),
            verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
        ) {
            // Card Hero: Logo & Versi
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Logo Kelola Resmi
                        Image(
                            painter = painterResource(id = R.drawable.logo_kelola_1),
                            contentDescription = "Logo Kelola",
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Kelola",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Mudah Berjualan di Sekolah",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Badge Versi Terbaru: Kelola 2.5
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Versi Terbaru: Kelola 2.5",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Feature Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgePill(text = "Local")
                            BadgePill(text = "Offline")
                            BadgePill(text = "100% Aman")
                        }
                    }
                }
            }

            // Card Deskripsi Aplikasi
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "DESKRIPSI APLIKASI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Text(
                            text = "Kelola adalah aplikasi kasir yang memudahkan pelajar untuk berjualan, local, offline, dan 100% aman.",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Aplikasi ini dirancang khusus untuk mendukung kegiatan wirausaha pelajar, kantin sekolah, dan koperasi siswa. Tanpa perlu koneksi internet, tanpa biaya langganan bulanan, dan tanpa iklan yang mengganggu — seluruh proses kasir berjalan lancar, cepat, dan mandiri langsung di saku Anda.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card Pengembang
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "PENGEMBANG & KEPEMILIKAN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Dibuat oleh GHXZY Devindie",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "di bawah naungan Potensi Cerdas Indonesia",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = KelolaRadius.ShapeChip,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Copyright © 2026 | Potensi Cerdas Indonesia\nSeluruh hak cipta dilindungi undang-undang.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Card Pilar Utama: Local, Offline & 100% Aman
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "KEUNGGULAN UTAMA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        FeatureItem(
                            icon = Icons.Default.Smartphone,
                            title = "Local (Penyimpanan Lokal)",
                            description = "Semua database produk, penjualan, dan keuangan tersimpan langsung di memori HP Anda tanpa server pihak ketiga."
                        )

                        FeatureItem(
                            icon = Icons.Default.WifiOff,
                            title = "Offline (Tanpa Internet)",
                            description = "Berjalan 100% tanpa sambungan internet atau kuota data. Anda dapat berjualan di mana saja dan kapan saja."
                        )

                        FeatureItem(
                            icon = Icons.Default.Lock,
                            title = "100% Aman & Terlindungi",
                            description = "Data bisnis Anda adalah privasi mutlak Anda. Tidak ada pihak luar yang dapat mengakses atau melihat omzet Anda."
                        )
                    }
                }
            }

            // Card Kebijakan Privasi
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(KelolaSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(KelolaSpacing.Space3)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(KelolaSpacing.Space2))
                            Text(
                                text = "KEBIJAKAN PRIVASI SINGKAT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        PrivacyPolicyPoint(
                            number = "1",
                            title = "Tidak Ada Pengumpulan Data Pribadi",
                            description = "Aplikasi Kelola sama sekali tidak mengumpulkan, tidak merekam, dan tidak mengirim data pengguna atau transaksi ke server eksternal mana pun."
                        )

                        PrivacyPolicyPoint(
                            number = "2",
                            title = "Penyimpanan Sepenuhnya Offline & Terisolasi",
                            description = "Semua data disimpan di database SQLite lokal perangkat dengan sistem keamanan sandbox bawaan Android."
                        )

                        PrivacyPolicyPoint(
                            number = "3",
                            title = "Penggunaan Izin Kamera yang Transparan",
                            description = "Izin kamera hanya digunakan secara lokal ketika Anda memindai barcode fisik barang atau memotong (crop) foto QRIS toko. Tidak ada rekaman gambar yang disimpan atau ditransmisikan keluar."
                        )

                        PrivacyPolicyPoint(
                            number = "4",
                            title = "Izin Penyimpanan & Berbagi",
                            description = "Digunakan murni atas inisiatif Anda sendiri untuk mencadangkan data (ekspor JSON) atau membagikan struk transaksi ke pelanggan."
                        )

                        PrivacyPolicyPoint(
                            number = "5",
                            title = "Hak & Kendali Data 100% Milik Anda",
                            description = "Anda memiliki kebebasan mutlak untuk mencadangkan, memulihkan, maupun menghapus seluruh data kapan saja lewat menu Pengaturan."
                        )
                    }
                }
            }

            // Footer Penutup
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Kelola | Aplikasi Kasir Pelajar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Versi 2.5 • Potensi Cerdas Indonesia",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    Surface(
        shape = KelolaRadius.ShapeSmall,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrivacyPolicyPoint(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
