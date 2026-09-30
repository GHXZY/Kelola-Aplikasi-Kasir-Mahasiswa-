package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.kelolaSoftShadow

/**
 * GuideScreen:
 * Laman khusus panduan penggunaan aplikasi Kelola.
 * Menyajikan petunjuk lengkap langkah demi langkah untuk setiap modul aplikasi
 * dengan bahasa yang ramah, ringkas, dan mudah dipahami oleh pelajar.
 */
@Composable
fun GuideScreen(
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
                            .testTag("button_back_guide")
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
                            text = "Panduan Penggunaan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Petunjuk lengkap & mudah menggunakan Kelola",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- 2. Konten Panduan ---
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
            // Banner Pembuka
            item {
                Card(
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mulai Berjualan dalam 3 Langkah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Tambah Produk di menu Produk.\n2. Pilih barang & masukkan ke Keranjang di menu Kasir.\n3. Pilih metode pembayaran & cetak/bagikan struk.",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section 1: Kasir & Transaksi
            item {
                GuideAccordionItem(
                    title = "1. Kasir & Transaksi Penjualan",
                    subtitle = "Scan barcode, atur keranjang, diskon & pembayaran",
                    icon = Icons.Default.PointOfSale,
                    initiallyExpanded = true,
                    points = listOf(
                        GuidePoint(
                            headline = "Memilih & Memasukkan Produk",
                            description = "Ketuk produk pada etalase untuk menambahkannya ke keranjang belanja. Anda juga bisa mencari produk lewat kolom pencarian atau filter kategori."
                        ),
                        GuidePoint(
                            headline = "Pindai Barcode Cepat (Kamera)",
                            description = "Ketuk ikon Scan Barcode pada kolom pencarian kasir untuk langsung memindai barcode fisik pada kemasan produk tanpa mengetik."
                        ),
                        GuidePoint(
                            headline = "Mengatur Keranjang Belanja",
                            description = "Buka lembar keranjang di bagian bawah untuk menaikkan/menurunkan jumlah barang (+ / -), menghapus item, atau mengosongkan keranjang jika pembeli batal."
                        ),
                        GuidePoint(
                            headline = "Menerapkan Diskon / Promo",
                            description = "Gunakan tombol Diskon di keranjang untuk memberikan potongan harga khusus (persen atau rupiah langsung)."
                        ),
                        GuidePoint(
                            headline = "Pilihan Metode Pembayaran",
                            description = "• Tunai: Masukkan uang yang diterima, aplikasi otomatis menghitung kembalian pas.\n• QRIS: Menampilkan QRIS toko Anda agar pembeli tinggal scan.\n• Transfer Bank: Menampilkan nomor rekening toko yang dapat disalin.\n• E-Wallet: Pilihan untuk dompet digital (GoPay, OVO, Dana, ShopeePay)."
                        ),
                        GuidePoint(
                            headline = "Struk Belanja & Pembatalan / Retur",
                            description = "Setelah transaksi berhasil, Anda dapat mencetak struk via printer Bluetooth atau membagikannya ke WhatsApp pembeli. Jika ada kesalahan, buka Laporan > pilih transaksi > Batalkan & Retur."
                        )
                    )
                )
            }

            // Section 2: Produk, Stok & Barcode
            item {
                GuideAccordionItem(
                    title = "2. Kelola Produk, Stok & Barcode",
                    subtitle = "Tambah produk, barcode otomatis, restock & kerugian",
                    icon = Icons.Default.Inventory2,
                    initiallyExpanded = false,
                    points = listOf(
                        GuidePoint(
                            headline = "Menambah Produk Baru",
                            description = "Buka menu Produk > tekan tombol [+ Tambah Produk]. Isi nama barang, harga modal (kulakan), harga jual, kategori, dan stok awal barang."
                        ),
                        GuidePoint(
                            headline = "Scan Barcode Kemasan atau Generate Otomatis",
                            description = "• Scan Barcode: Untuk produk pabrik berkemasan (seperti snack & minuman), tekan tombol Scan Barcode di kanan atas form.\n• Generate Barcode: Untuk produk buatan sendiri (es teh, gorengan, roti), tekan tombol Generate Barcode untuk membuat kode EAN-13 otomatis."
                        ),
                        GuidePoint(
                            headline = "Peringatan Kedaluwarsa",
                            description = "Anda dapat mengisi tanggal kedaluwarsa produk. Aplikasi akan memberi tanda peringatan kuning/merah jika barang mendekati batas kedaluwarsa."
                        ),
                        GuidePoint(
                            headline = "Restock Cepat & Kurangi Stok",
                            description = "Tekan tombol Restock pada kartu produk untuk menambah stok saat Anda baru selesai kulakan tanpa perlu membuka form edit."
                        ),
                        GuidePoint(
                            headline = "Catat & Batalkan Kerugian",
                            description = "Jika ada barang basi, rusak, atau tumpah, tekan 'Kurangi Stok' dan pilih alasan kerugian. Jika terjadi kekeliruan, kerugian dapat dibatalkan di menu Laporan agar dana kembali ke kas."
                        )
                    )
                )
            }

            // Section 3: Kasbon & Piutang
            item {
                GuideAccordionItem(
                    title = "3. Kasbon & Piutang Siswa/Pelanggan",
                    subtitle = "Catat hutang, tambah pesanan berjalan & pelunasan",
                    icon = Icons.Default.HourglassTop,
                    initiallyExpanded = false,
                    points = listOf(
                        GuidePoint(
                            headline = "Mencatat Kasbon Baru",
                            description = "Saat di kasir, pilih opsi 'Simpan Kasbon'. Masukkan nama siswa/pelanggan dan tenggat waktu pembayaran. Transaksi akan tercatat rapi di menu Kasbon."
                        ),
                        GuidePoint(
                            headline = "Menambah Item pada Kasbon Berjalan",
                            description = "Jika pelanggan mengambil barang tambahan sebelum membayar, Anda dapat langsung menambahkan barang baru ke kasbon yang sudah ada."
                        ),
                        GuidePoint(
                            headline = "Pelunasan Bertahap atau Penuh",
                            description = "Pelanggan bisa membayar cicilan sebagian atau melunasi seluruhnya menggunakan metode Tunai, QRIS, maupun Transfer."
                        )
                    )
                )
            }

            // Section 4: Beranda & Kas Harian
            item {
                GuideAccordionItem(
                    title = "4. Beranda & Manajemen Kas Harian",
                    subtitle = "Modal awal, ringkasan kas laci & catatan cepat",
                    icon = Icons.Default.Home,
                    initiallyExpanded = false,
                    points = listOf(
                        GuidePoint(
                            headline = "Input Modal Awal",
                            description = "Masukkan uang modal kas / uang kembalian setiap pagi hari di Beranda agar pembukuan kas di sore hari pas dan transparan."
                        ),
                        GuidePoint(
                            headline = "Statistik Penjualan Harian",
                            description = "Pantau omzet hari ini, perkiraan laba bersih, kas di tangan (laci kasir), serta total kasbon yang belum dilunasi."
                        ),
                        GuidePoint(
                            headline = "Catatan Cepat Toko (Quick Notes)",
                            description = "Tulis pengingat kilat seperti pesanan titipan guru, barang titipan teman, atau daftar belanjaan yang harus dibeli saat kulakan."
                        )
                    )
                )
            }

            // Section 5: Laporan & Keuangan
            item {
                GuideAccordionItem(
                    title = "5. Laporan & Keuangan Usaha",
                    subtitle = "Grafik laba, catat biaya operasional & ekspor",
                    icon = Icons.Default.BarChart,
                    initiallyExpanded = false,
                    points = listOf(
                        GuidePoint(
                            headline = "Pilihan Periode Laporan",
                            description = "Lihat performa penjualan berdasarkan Hari Ini, 7 Hari Terakhir, Bulan Ini, atau pilih Rentang Tanggal kustom sesuai kebutuhan."
                        ),
                        GuidePoint(
                            headline = "Laba Kotor vs Laba Bersih",
                            description = "Aplikasi menghitung laba kotor dari selisih harga jual dan harga modal, lalu mengurangi biaya operasional untuk menghasilkan laba bersih riil."
                        ),
                        GuidePoint(
                            headline = "Catat Pengeluaran Operasional Toko",
                            description = "Tekan tombol '+ Pengeluaran' untuk mencatat biaya seperti pembelian kantong kresek, es batu, sedotan, atau bensin saat kulakan."
                        ),
                        GuidePoint(
                            headline = "Pembatalan Kerugian",
                            description = "Jika ada pencatatan kerugian yang keliru, Anda dapat membatalkannya di daftar kerugian. Dana kerugian otomatis kembali ke kas."
                        )
                    )
                )
            }

            // Section 6: Pengaturan & Kustomisasi
            item {
                GuideAccordionItem(
                    title = "6. Pengaturan, Tema & Cadangkan Data",
                    subtitle = "Profil toko, rekening bank, QRIS, tema & backup",
                    icon = Icons.Default.Settings,
                    initiallyExpanded = false,
                    points = listOf(
                        GuidePoint(
                            headline = "Identitas Usaha & Opening Screen",
                            description = "Ubah Nama Usaha dan Alamat Toko. Nama toko Anda akan otomatis muncul sebagai judul utama pada Layar Pembuka (Opening Screen) saat aplikasi dibuka!"
                        ),
                        GuidePoint(
                            headline = "Kelola Rekening Bank Toko",
                            description = "Simpan hingga 5 rekening bank agar memudahkan pembeli saat memilih metode pembayaran Transfer."
                        ),
                        GuidePoint(
                            headline = "Kustomisasi Gambar QRIS Toko",
                            description = "Unggah foto QRIS toko Anda dan sesuaikan potongannya (crop) agar pas ditampilkan saat pembeli membayar via QRIS."
                        ),
                        GuidePoint(
                            headline = "Tema Tampilan & Mode Gelap",
                            description = "Pilih tema warna favorit Anda (Biru Bawaan, Pink, Coklat, Orange) dan aktifkan Mode Gelap (Dark Mode) untuk kenyamanan mata di malam hari."
                        ),
                        GuidePoint(
                            headline = "Cadangkan Data (JSON) — Sangat Penting!",
                            description = "Gunakan fitur 'Cadangkan Data (JSON)' secara berkala. Simpan file cadangan ke Google Drive atau WhatsApp agar data Anda aman jika ganti ponsel."
                        )
                    )
                )
            }

            // Tips Tambahan
            item {
                Card(
                    shape = KelolaRadius.ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tips Terbaik Penggunaan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "• Selalu masukkan modal awal sebelum memulai transaksi pertama.\n• Gunakan kamera pemindai barcode untuk mempercepat antrean pembeli.\n• Lakukan ekspor cadangan data minimal satu kali seminggu.",
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class GuidePoint(
    val headline: String,
    val description: String
)

@Composable
private fun GuideAccordionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = false,
    points: List<GuidePoint>
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotateArrow by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "RotateAccordionArrow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .kelolaSoftShadow(shape = KelolaRadius.ShapeCard, elevation = 2.dp),
        shape = KelolaRadius.ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Tutup" else "Buka",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotateArrow)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    points.forEach { point ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "▸ ${point.headline}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = point.description,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
