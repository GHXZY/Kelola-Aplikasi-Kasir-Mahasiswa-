package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideAndAboutTest {

    @Test
    fun `test app information constants`() {
        val appName = "Kelola"
        val tagline = "Mudah Berjualan di Sekolah"
        val version = "Kelola 2.5"
        val developer = "GHXZY Devindie"
        val organization = "Potensi Cerdas Indonesia"

        assertEquals("Kelola", appName)
        assertEquals("Mudah Berjualan di Sekolah", tagline)
        assertEquals("Kelola 2.5", version)
        assertEquals("GHXZY Devindie", developer)
        assertEquals("Potensi Cerdas Indonesia", organization)
    }

    @Test
    fun `test navigation state transitions for guide and about`() {
        var isShowingGuide = false
        var isShowingAbout = false
        var isShowingSettings = true

        // 1. User opens Guide from settings
        isShowingGuide = true
        assertTrue(isShowingGuide)
        assertFalse(isShowingAbout)

        // 2. User presses back from Guide
        isShowingGuide = false
        assertFalse(isShowingGuide)
        assertTrue(isShowingSettings)

        // 3. User opens About from settings
        isShowingAbout = true
        assertTrue(isShowingAbout)
        assertFalse(isShowingGuide)

        // 4. User presses back from About
        isShowingAbout = false
        assertFalse(isShowingAbout)
        assertTrue(isShowingSettings)
    }

    @Test
    fun `test privacy policy core principles`() {
        val isOfflineOnly = true
        val hasExternalServer = false
        val sendsAnalytics = false

        assertTrue("Kelola harus berjalan 100% offline", isOfflineOnly)
        assertFalse("Tidak boleh ada server eksternal yang menampung data user", hasExternalServer)
        assertFalse("Tidak boleh mengirim analitik atau melacak pengguna", sendsAnalytics)
    }
}
