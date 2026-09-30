package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

/**
 * OpeningScreen:
 * Tampilan pembuka (Splash / Opening Screen) saat pengguna membuka aplikasi Kelola.
 *
 * Desain dan layout 1:1 mengikuti referensi resmi mockup:
 * - Latar belakang pastel iridescent / holographic lembut.
 * - Logo lingkaran Kelola resmi (berdasarkan "Logo Kelola 1.svg" / R.drawable.logo_kelola_1).
 * - Teks Utama: Default "Kelola", berubah menjadi nama toko setelah diedit di Pengaturan.
 * - Subtitle: Default "Mudah Berjualan di Sekolah", berubah menjadi alamat toko setelah diedit di Pengaturan.
 * - Bagian Bawah:
 *     "Kelola | Aplikasi Kasir Pelajar"
 *     "Copyright © 2026 | Potensi Cerdas Indonesia" (statis, tidak berubah).
 */
@Composable
fun OpeningScreen(
    storeName: String,
    storeAddress: String,
    hasCustomProfile: Boolean = false,
    durationMillis: Long = 2300L,
    onTimeoutOrDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Teks Utama: "Kelola" (default baru download) atau nama toko setelah user mengedit profil toko
    val displayTitle = if (hasCustomProfile && storeName.isNotBlank()) {
        storeName
    } else {
        "Kelola"
    }

    // Teks Subtitle: "Mudah Berjualan di Sekolah" (default) atau alamat toko setelah user mengedit
    val displaySubtitle = if (hasCustomProfile && storeAddress.isNotBlank()) {
        storeAddress
    } else {
        "Mudah Berjualan di Sekolah"
    }

    // Animasi kemunculan lembut (fade in & gentle scale)
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isStarted = true
        delay(durationMillis)
        onTimeoutOrDismiss()
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "OpeningContentAlpha"
    )

    val contentScale by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0.92f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "OpeningContentScale"
    )

    // Base fallback fluid gradient
    val baseGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE5F8FD), // Pale cyan top
            Color(0xFFEDF2FB), // Soft pearlescent
            Color(0xFFFDEAF2), // Soft blush iridescent
            Color(0xFFD6E8FA), // Pastel lavender sky
            Color(0xFFB9DBF7)  // Soft sky blue bottom
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Sentuhan di mana saja mempercepat peralihan ke aplikasi utama
                onTimeoutOrDismiss()
            }
            .testTag("opening_screen_container")
    ) {
        // Lapisan Latar Belakang Gambar Gradien Halus dari Mockup
        Image(
            painter = painterResource(id = R.drawable.bg_opening_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Konten Tengah: Logo, Teks Nama, dan Slogan/Alamat
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .padding(bottom = 56.dp) // Mengangkat sedikit di atas titik tengah vertikal sesuai mockup
                .scale(contentScale)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Resmi Kelola (Lingkaran Teal dengan Monogram K Putih & Garis Transaksi)
            Image(
                painter = painterResource(id = R.drawable.logo_kelola_1),
                contentDescription = "Logo Kelola",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(134.dp)
                    .testTag("opening_screen_logo")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Teks Nama ("Kelola" atau Nama Toko yang telah diedit)
            Text(
                text = displayTitle,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("opening_screen_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Teks Slogan ("Mudah Berjualan di Sekolah" atau Alamat Toko yang telah diedit)
            Text(
                text = displaySubtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp,
                lineHeight = 22.sp,
                modifier = Modifier.testTag("opening_screen_subtitle")
            )
        }

        // Konten Bawah: Copyright & Identitas Aplikasi Kasir Pelajar (Statis / Tidak Berubah)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp, start = 20.dp, end = 20.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val footerTitle = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))) {
                    append("Kelola")
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color(0xFF1E293B))) {
                    append(" | Aplikasi Kasir Pelajar")
                }
            }

            Text(
                text = footerTitle,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("opening_screen_footer_line1")
            )

            Text(
                text = "Copyright © 2026 | Potensi Cerdas Indonesia",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("opening_screen_footer_line2")
            )
        }
    }
}
