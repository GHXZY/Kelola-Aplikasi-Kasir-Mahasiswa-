package com.example

import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun testFormatRupiahInput() {
        assertEquals("10.000", FormatUtils.formatRupiahInput("10000"))
        assertEquals("1.000.000", FormatUtils.formatRupiahInput("1000000"))
        assertEquals("500", FormatUtils.formatRupiahInput("500"))
        assertEquals("0", FormatUtils.formatRupiahInput("0"))
        assertEquals("", FormatUtils.formatRupiahInput(""))
        assertEquals("10.000", FormatUtils.formatRupiahInput("10.000"))
        assertEquals("25.000", FormatUtils.formatRupiahInput("Rp 25.000"))
    }

    @Test
    fun testParseRupiahInput() {
        assertEquals(10000L, FormatUtils.parseRupiahInput("10.000"))
        assertEquals(1000000L, FormatUtils.parseRupiahInput("1.000.000"))
        assertEquals(500L, FormatUtils.parseRupiahInput("500"))
        assertEquals(0L, FormatUtils.parseRupiahInput("0"))
        assertEquals(0L, FormatUtils.parseRupiahInput(""))
        assertEquals(25000L, FormatUtils.parseRupiahInput("Rp 25.000"))
    }

    @Test
    fun testFormatNumberWithDots() {
        assertEquals("10.000", FormatUtils.formatNumberWithDots(10000L))
        assertEquals("1.000.000", FormatUtils.formatNumberWithDots(1000000L))
        assertEquals("0", FormatUtils.formatNumberWithDots(0L))
        assertEquals("50", FormatUtils.formatNumberWithDots(50L))
    }
}
