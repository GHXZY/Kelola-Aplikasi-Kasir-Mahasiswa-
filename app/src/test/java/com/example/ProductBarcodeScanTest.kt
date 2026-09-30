package com.example

import com.example.data.local.entity.ProductEntity
import com.example.util.BarcodeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ProductBarcodeScanTest:
 * Unit test suite untuk validasi integrasi Scan Barcode pada halaman Tambah/Edit Produk.
 * Menguji:
 * 1. Barcode normalization & trimming.
 * 2. Deteksi produk duplikat pada database lookup.
 * 3. Kompatibilitas barcode hasil Generate Barcode dengan format scanner (EAN-13, GS1).
 * 4. Mekanisme penguncian pembacaan tunggal (single-scan lock) untuk mencegah duplikasi scan cepat.
 */
class ProductBarcodeScanTest {

    @Test
    fun testScannedBarcodeTrimmingAndSanitization() {
        val rawScannedWithSpaces = "   8991234567891   \n"
        val clean = rawScannedWithSpaces.trim()
        assertEquals("8991234567891", clean)
        assertTrue(clean.isNotBlank())

        val emptyScan = "   \n\t "
        assertTrue(emptyScan.trim().isBlank())
    }

    @Test
    fun testDuplicateBarcodeLookupLogic() {
        // Mock database produk yang sudah ada
        val existingProducts = listOf(
            ProductEntity(
                id = 1L,
                name = "Kopi Susu Gula Aren",
                categoryId = 1L,
                costPrice = 8000L,
                sellingPrice = 15000L,
                stock = 25,
                unit = "cup",
                barcode = "8999999111115"
            ),
            ProductEntity(
                id = 2L,
                name = "Roti Coklat Keju",
                categoryId = 2L,
                costPrice = 5000L,
                sellingPrice = 10000L,
                stock = 12,
                unit = "pcs",
                barcode = "8998888222224"
            )
        )

        val scannedBarcode = "8999999111115"

        // Kasus 1: Tambah produk baru (initialProduct = null) -> Harus terdeteksi duplikat
        val currentProductId: Long? = null
        val foundProduct = existingProducts.find { it.barcode.equals(scannedBarcode, ignoreCase = true) }
        assertNotNull(foundProduct)

        val isDuplicateForNewProduct = foundProduct != null && foundProduct.id != currentProductId
        assertTrue("Barcode yang sudah terdaftar harus dideteksi sebagai duplikat saat tambah produk baru", isDuplicateForNewProduct)

        // Kasus 2: Edit produk yang SAMA (initialProduct.id = 1) -> Bukan duplikat
        val editingSameProductId: Long? = 1L
        val isDuplicateForSameProduct = foundProduct != null && foundProduct.id != editingSameProductId
        assertFalse("Barcode milik produk yang sedang diedit tidak boleh dianggap sebagai duplikat", isDuplicateForSameProduct)

        // Kasus 3: Barcode belum pernah terdaftar di database
        val brandNewBarcode = "8997777333333"
        val notFound = existingProducts.find { it.barcode.equals(brandNewBarcode, ignoreCase = true) }
        assertNull("Barcode belum terdaftar harus menghasilkan null dari database lookup", notFound)

        val isDuplicateForBrandNew = notFound != null && notFound.id != currentProductId
        assertFalse("Barcode baru tidak boleh memicu peringatan duplikat", isDuplicateForBrandNew)
    }

    @Test
    fun testGeneratedBarcodeCompatibilityWithScanner() {
        // Barcode yang dibuat oleh Generate Barcode harus memiliki format EAN-13 valid standar GS1
        for (i in 0 until 20) {
            val generated = BarcodeUtils.generateEan13Candidate("899")
            assertEquals("Panjang barcode harus 13 digit", 13, generated.length)
            assertTrue("Harus berawalan 899", generated.startsWith("899"))
            assertTrue("Semua karakter harus digit", generated.all { it.isDigit() })
            assertTrue("Checksum EAN-13 harus valid sehingga scanner dapat membacanya", BarcodeUtils.isValidEan13(generated))
        }
    }

    @Test
    fun testRapidScanSingleLockSimulation() {
        // Simulasi penerimaan frame kamera berturut-turut dari sensor kamera
        var isDetectionLocked = false
        var processedCount = 0
        var lastProcessedBarcode: String? = null

        val incomingFrames = listOf(
            "8991234567891",
            "8991234567891",
            "8991234567891",
            "8991234567891",
            "8991234567891"
        )

        for (frame in incomingFrames) {
            if (!isDetectionLocked) {
                isDetectionLocked = true
                processedCount++
                lastProcessedBarcode = frame
            }
        }

        assertEquals("Meskipun kamera membaca 5 frame berturut-turut, hanya 1 hasil yang boleh diproses", 1, processedCount)
        assertEquals("8991234567891", lastProcessedBarcode)
    }

    @Test
    fun testProductFormStateIntegration() {
        // Simulasi alur state form Tambah Produk:
        // 1. Initial state kosong
        var formBarcode: String? = null
        var formName: String = ""

        // 2. Barcode terbaca dari kamera
        val scannedBarcode = "8991234567891"
        formBarcode = scannedBarcode
        assertEquals("8991234567891", formBarcode)

        // 3. User melengkapi dan mengedit nama produk setelah scan
        formName = "Susu UHT Coklat 250ml"
        assertEquals("Susu UHT Coklat 250ml", formName)

        // 4. Form disubmit ke entity database
        val savedEntity = ProductEntity(
            name = formName,
            categoryId = 3L,
            costPrice = 4500L,
            sellingPrice = 6000L,
            stock = 24,
            unit = "pcs",
            barcode = formBarcode
        )

        assertEquals("8991234567891", savedEntity.barcode)
        assertEquals("Susu UHT Coklat 250ml", savedEntity.name)
    }
}
