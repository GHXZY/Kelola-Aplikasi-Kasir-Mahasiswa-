package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

object FormatUtils {

    private val rupiahSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val decimalFormat = DecimalFormat("#,##0", rupiahSymbols)

    fun formatRupiah(amount: Long): String {
        return if (amount < 0) {
            "-Rp" + decimalFormat.format(-amount)
        } else {
            "Rp" + decimalFormat.format(amount)
        }
    }

    fun formatNumberWithDots(amount: Long): String {
        return decimalFormat.format(amount)
    }

    fun formatRupiahInput(input: String): String {
        val clean = input.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val amount = clean.toLongOrNull() ?: 0L
        return decimalFormat.format(amount)
    }

    fun parseRupiahInput(input: String): Long {
        val clean = input.filter { it.isDigit() }
        return clean.toLongOrNull() ?: 0L
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun generateTransactionNumber(): String {
        val datePart = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        val randomDigits = String.format(Locale.US, "%04d", Random().nextInt(10000))
        return "TRX-$datePart-$randomDigits"
    }

    fun isToday(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(timestamp)) == sdf.format(Date())
    }

    fun isYesterday(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return sdf.format(Date(timestamp)) == sdf.format(Date(yesterday))
    }

    fun isWithinDays(timestamp: Long, days: Int): Boolean {
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
        return timestamp >= cutoff
    }

    fun isThisMonth(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        return sdf.format(Date(timestamp)) == sdf.format(Date())
    }
}
