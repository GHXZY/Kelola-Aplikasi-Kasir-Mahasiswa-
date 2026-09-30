package com.example

import com.example.util.BarcodeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeUtilsTest {

    @Test
    fun testCalculateEan13Checksum() {
        // Stabilo Boss example: 400638133393 -> 1
        assertEquals(1, BarcodeUtils.calculateEan13Checksum("400638133393"))

        // Known GS1 test code: 899123456789 -> 1
        assertEquals(1, BarcodeUtils.calculateEan13Checksum("899123456789"))

        // All zeroes: 000000000000 -> 0
        assertEquals(0, BarcodeUtils.calculateEan13Checksum("000000000000"))
    }

    @Test
    fun testIsValidEan13() {
        assertTrue(BarcodeUtils.isValidEan13("4006381333931"))
        assertTrue(BarcodeUtils.isValidEan13("8991234567891"))
        assertTrue(BarcodeUtils.isValidEan13("0000000000000"))

        // Wrong checksum
        assertFalse(BarcodeUtils.isValidEan13("8991234567890"))
        // Wrong length
        assertFalse(BarcodeUtils.isValidEan13("89912345678"))
        assertFalse(BarcodeUtils.isValidEan13("89912345678901"))
        // Non-digits
        assertFalse(BarcodeUtils.isValidEan13("899123456789A"))
    }

    @Test
    fun testGenerateEan13Candidate() {
        for (i in 0 until 50) {
            val candidate = BarcodeUtils.generateEan13Candidate("899")
            assertEquals(13, candidate.length)
            assertTrue(candidate.startsWith("899"))
            assertTrue(candidate.all { it.isDigit() })
            assertTrue("Generated candidate $candidate must have valid checksum", BarcodeUtils.isValidEan13(candidate))
        }

        // Test uniqueness of sequential generations
        val set = mutableSetOf<String>()
        for (i in 0 until 100) {
            val candidate = BarcodeUtils.generateEan13Candidate("899")
            set.add(candidate)
        }
        // Collisions in 100 samples with 10^9 space should be virtually 0
        assertEquals(100, set.size)
    }

    @Test
    fun testGenerateSafeFileName() {
        assertEquals(
            "barcode-indomie-goreng-8991234567890.png",
            BarcodeUtils.generateSafeFileName("Indomie Goreng", "8991234567890")
        )

        assertEquals(
            "barcode-kopi-susu-aren-8991234567890.png",
            BarcodeUtils.generateSafeFileName("  Kopi / Susu # Aren!!  ", "8991234567890")
        )

        assertEquals(
            "barcode-8991234567890.png",
            BarcodeUtils.generateSafeFileName("", "8991234567890")
        )

        assertEquals(
            "barcode-8991234567890.png",
            BarcodeUtils.generateSafeFileName(null, "8991234567890")
        )
    }
}
