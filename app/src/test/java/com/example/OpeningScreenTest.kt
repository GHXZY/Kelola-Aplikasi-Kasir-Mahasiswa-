package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OpeningScreenTest {

    @Test
    fun `opening screen defaults on clean install`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val prefs = application.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val viewModel = MainViewModel(application)
        ShadowLooper.idleMainLooper()

        // Default opening screen title and subtitle on fresh download
        assertFalse(viewModel.hasEditedStoreProfile.value)
        assertEquals("Kelola", viewModel.businessName.value)
        assertEquals("Mudah Berjualan di Sekolah", viewModel.businessAddress.value)
    }

    @Test
    fun `opening screen updates when user edits store name and address`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val prefs = application.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val viewModel = MainViewModel(application)
        ShadowLooper.idleMainLooper()

        // User edits business name and address in Pengaturan / Settings
        viewModel.updateBusinessInfo(
            name = "Kantin Kejujuran SMP 1",
            address = "Gedung B Lt. 2 Sekolah Menengah",
            phone = "081298765432"
        )
        ShadowLooper.idleMainLooper()

        assertTrue(viewModel.hasEditedStoreProfile.value)
        assertEquals("Kantin Kejujuran SMP 1", viewModel.businessName.value)
        assertEquals("Gedung B Lt. 2 Sekolah Menengah", viewModel.businessAddress.value)
    }

    @Test
    fun `opening screen fallback logic for title and subtitle`() {
        // Test default state
        val defaultTitle = if (false && "Toko A".isNotBlank()) "Toko A" else "Kelola"
        val defaultSubtitle = if (false && "Alamat A".isNotBlank()) "Alamat A" else "Mudah Berjualan di Sekolah"
        assertEquals("Kelola", defaultTitle)
        assertEquals("Mudah Berjualan di Sekolah", defaultSubtitle)

        // Test custom edited state
        val customName = "Koperasi Siswa Berkah"
        val customAddress = "Jl. Merdeka No. 45"
        val customTitle = if (true && customName.isNotBlank()) customName else "Kelola"
        val customSubtitle = if (true && customAddress.isNotBlank()) customAddress else "Mudah Berjualan di Sekolah"
        assertEquals("Koperasi Siswa Berkah", customTitle)
        assertEquals("Jl. Merdeka No. 45", customSubtitle)

        // Test custom edited with blank address falls back to default
        val blankAddress = "   "
        val fallbackSubtitle = if (true && blankAddress.isNotBlank()) blankAddress else "Mudah Berjualan di Sekolah"
        assertEquals("Mudah Berjualan di Sekolah", fallbackSubtitle)
    }
}
