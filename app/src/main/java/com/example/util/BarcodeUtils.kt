package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.EnumMap
import java.util.Locale

object BarcodeUtils {

    private val random = SecureRandom()

    /**
     * Menghitung digit checksum resmi standar GS1 untuk kode EAN-13 (12 digit pertama).
     */
    fun calculateEan13Checksum(first12Digits: String): Int {
        require(first12Digits.length == 12 && first12Digits.all { it.isDigit() }) {
            "Input harus berupa 12 digit angka"
        }
        var sum = 0
        for (i in 0 until 12) {
            val digit = first12Digits[i].digitToInt()
            // Posisi ganjil (indeks 0, 2, 4...) berbobot 1, posisi genap (indeks 1, 3, 5...) berbobot 3
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    /**
     * Memvalidasi apakah string merupakan barcode EAN-13 yang valid (13 digit & checksum benar).
     */
    fun isValidEan13(code: String): Boolean {
        if (code.length != 13 || !code.all { it.isDigit() }) return false
        val expectedChecksum = calculateEan13Checksum(code.take(12))
        return code[12].digitToInt() == expectedChecksum
    }

    /**
     * Membuat kandidat barcode EAN-13 13-digit unik.
     * Menggunakan awalan GS1 Indonesia "899" + 9 digit acak + 1 digit checksum valid.
     */
    fun generateEan13Candidate(prefix: String = "899"): String {
        require(prefix.length == 3 && prefix.all { it.isDigit() }) {
            "Prefix harus 3 digit angka"
        }
        // Generate 9 random digits
        val bodyBuilder = StringBuilder(prefix)
        for (i in 0 until 9) {
            bodyBuilder.append(random.nextInt(10))
        }
        val first12 = bodyBuilder.toString()
        val checksum = calculateEan13Checksum(first12)
        return "$first12$checksum"
    }

    /**
     * Menghasilkan Bitmap barcode beresolusi tinggi, kontras tajam (hitam-putih solid),
     * quiet zone/margin yang memadai, serta teks nomor barcode yang mudah dibaca (human-readable).
     */
    fun createBarcodeBitmap(
        barcode: String,
        productName: String? = null,
        width: Int = 800,
        height: Int = 380,
        includeText: Boolean = true
    ): Bitmap {
        val cleanBarcode = barcode.trim()
        val isEan13 = cleanBarcode.length == 13 && cleanBarcode.all { it.isDigit() } && isValidEan13(cleanBarcode)
        val format = if (isEan13) BarcodeFormat.EAN_13 else BarcodeFormat.CODE_128

        // Encoding hints: Margin 0 agar kita bisa mengontrol quiet zone secara presisi via Canvas
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.MARGIN, 0)
        }

        // Tentukan alokasi vertikal: Header Nama Produk (opsional) + Barcode Bar + Footer Teks Angka
        val hasProductHeader = !productName.isNullOrBlank()
        val topPadding = if (hasProductHeader) 52f else 32f
        val bottomTextPadding = if (includeText) 62f else 24f
        val sideMargin = 40f

        val barcodeDrawWidth = (width - (sideMargin * 2)).toInt().coerceAtLeast(100)
        val barcodeDrawHeight = (height - topPadding - bottomTextPadding).toInt().coerceAtLeast(60)

        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(cleanBarcode, format, barcodeDrawWidth, barcodeDrawHeight, hints)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background putih solid bersih (Quiet Zone)
        canvas.drawColor(Color.WHITE)

        // 2. Gambar Batang Barcode (High Contrast Black)
        val barPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = false // Sharp bar edges without blurring
            style = Paint.Style.FILL
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        // Scaling factor from BitMatrix to target drawing area
        val scaleX = barcodeDrawWidth.toFloat() / matrixWidth.toFloat()
        val scaleY = barcodeDrawHeight.toFloat() / matrixHeight.toFloat()

        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix.get(x, y)) {
                    val left = sideMargin + (x * scaleX)
                    val top = topPadding + (y * scaleY)
                    val right = left + scaleX
                    val bottom = top + scaleY
                    canvas.drawRect(left, top, right, bottom, barPaint)
                }
            }
        }

        // 3. Gambar Nama Produk di Bagian Atas (jika ada)
        if (hasProductHeader) {
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val displayTitle = if (productName!!.length > 38) {
                productName.take(35) + "..."
            } else {
                productName
            }
            canvas.drawText(displayTitle, width / 2f, 34f, titlePaint)
        }

        // 4. Gambar Teks Angka Barcode di Bagian Bawah
        if (includeText) {
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 32f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val formattedText = if (isEan13) {
                // Format EAN-13 standar: X XXXXXX XXXXXX
                "${cleanBarcode[0]}  ${cleanBarcode.substring(1, 7)}  ${cleanBarcode.substring(7)}"
            } else {
                cleanBarcode
            }

            val textY = height - 20f
            canvas.drawText(formattedText, width / 2f, textY, textPaint)
        }

        return bitmap
    }

    /**
     * Menghasilkan nama file yang informatif dan aman dari karakter ilegal.
     * Contoh: barcode-indomie-goreng-8991234567890.png
     */
    fun generateSafeFileName(productName: String?, barcode: String): String {
        val cleanBarcode = barcode.trim().filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        val cleanName = productName
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.replace(Regex("[^a-z0-9]+"), "-")
            ?.trim('-')
            .orEmpty()

        return if (cleanName.isNotBlank()) {
            "barcode-$cleanName-$cleanBarcode.png"
        } else {
            "barcode-$cleanBarcode.png"
        }
    }

    /**
     * Menyimpan Bitmap PNG langsung ke penyimpanan perangkat (Galeri / Pictures / Kelola_Barcodes).
     * Bekerja tanpa permission khusus pada Android 10+ (API 29+) menggunakan MediaStore API.
     */
    fun saveBarcodeToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Result<Uri> {
        return runCatching {
            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kelola_Barcodes")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Gagal membuat entri MediaStore untuk barcode")

                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        throw IllegalStateException("Gagal mengompres barcode menjadi PNG")
                    }
                } ?: throw IllegalStateException("Gagal membuka output stream")

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
                imageUri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val kelolaDir = File(picturesDir, "Kelola_Barcodes")
                if (!kelolaDir.exists()) {
                    kelolaDir.mkdirs()
                }
                val destFile = File(kelolaDir, fileName)
                FileOutputStream(destFile).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        throw IllegalStateException("Gagal mengompres barcode menjadi PNG")
                    }
                }
                var scannedUri: Uri? = null
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("image/png")
                ) { _, uri ->
                    scannedUri = uri
                }
                scannedUri ?: Uri.fromFile(destFile)
            }
        }
    }
}
