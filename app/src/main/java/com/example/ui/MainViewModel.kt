package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ChangeRecordEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.LossRecordEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PromoEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionItemEntity
import com.example.data.repository.PosRepository
import com.example.ui.promo.AppliedPromoInfo
import com.example.ui.promo.PromoEngine
import com.example.util.FormatUtils
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val subtotal: Long,
    val profit: Long
)

data class CartSummary(
    val items: List<CartItem> = emptyList(),
    val totalItemCount: Int = 0,
    val subtotal: Long = 0L,
    val manualDiscount: Long = 0L,
    val promoDiscount: Long = 0L,
    val appliedPromos: List<AppliedPromoInfo> = emptyList(),
    val discount: Long = 0L,
    val isDiscountPercentage: Boolean = false,
    val discountValue: Long = 0L,
    val total: Long = 0L
)

data class TopProductSummary(
    val productId: Long,
    val name: String,
    val quantitySold: Int,
    val totalRevenue: Long,
    val unit: String
)

data class DailySalesStat(
    val dayLabel: String,
    val amount: Long
)

data class BankAccount(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bankName: String,
    val accountName: String,
    val accountNumber: String
)

data class DashboardStats(
    val todaySales: Long = 0L,
    val todayExpense: Long = 0L,
    val todayProfit: Long = 0L,
    val todayTxCount: Int = 0,
    val todayItemsSold: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val unpaidDebtTotal: Long = 0L,
    val unpaidDebtCount: Int = 0,
    val unpaidDebts: List<DebtEntity> = emptyList(),
    val pendingChangeList: List<ChangeRecordEntity> = emptyList(),
    val pendingChangeTotal: Long = 0L,
    val expiringProducts: List<ProductEntity> = emptyList(),
    val totalLoss: Long = 0L
)

data class ReportStats(
    val totalSales: Long = 0L,
    val totalExpense: Long = 0L,
    val grossProfit: Long = 0L,
    val netCashflow: Long = 0L,
    val transactionCount: Int = 0,
    val itemsSold: Int = 0,
    val averageTxValue: Long = 0L,
    val topProducts: List<TopProductSummary> = emptyList(),
    val dailySales: List<DailySalesStat> = emptyList(),
    val totalLoss: Long = 0L,
    val lossRecords: List<LossRecordEntity> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PosRepository
    private val prefs = application.getSharedPreferences("kelola_settings", Context.MODE_PRIVATE)

    // --- Donation Dialog ---
    private val _showDonationDialog = MutableStateFlow(false)
    val showDonationDialog: StateFlow<Boolean> = _showDonationDialog.asStateFlow()

    fun checkAndTriggerDonationDialog() {
        val lastShown = prefs.getLong("last_donation_popup_shown_time", 0L)
        val now = System.currentTimeMillis()
        val fiveHoursMs = 5 * 60 * 60 * 1000L // 5 Jam
        if (lastShown == 0L || (now - lastShown) >= fiveHoursMs) {
            _showDonationDialog.value = true
            prefs.edit().putLong("last_donation_popup_shown_time", now).apply()
        }
    }

    fun dismissDonationDialog() {
        _showDonationDialog.value = false
    }

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PosRepository(db.posDao())
        viewModelScope.launch {
            repository.processExpiredProducts()
        }
        viewModelScope.launch {
            checkAndTriggerDonationDialog()
            while (isActive) {
                delay(15 * 60 * 1000L) // Periksa berkala tiap 15 menit
                checkAndTriggerDonationDialog()
            }
        }
    }

    // --- Preferences / Business Info ---
    private val _businessName = MutableStateFlow(prefs.getString("business_name", "Kantin Mahasiswa") ?: "Kantin Mahasiswa")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _businessAddress = MutableStateFlow(prefs.getString("business_address", "Gedung Utama Kampus Lt. 1") ?: "Gedung Utama Kampus Lt. 1")
    val businessAddress: StateFlow<String> = _businessAddress.asStateFlow()

    private val _businessPhone = MutableStateFlow(prefs.getString("business_phone", "0812-3456-7890") ?: "0812-3456-7890")
    val businessPhone: StateFlow<String> = _businessPhone.asStateFlow()

    private val _receiptFooter = MutableStateFlow(prefs.getString("receipt_footer", "Terima kasih atas kunjungan Anda!") ?: "Terima kasih atas kunjungan Anda!")
    val receiptFooter: StateFlow<String> = _receiptFooter.asStateFlow()

    private val _qrisMerchantName = MutableStateFlow(prefs.getString("qris_merchant_name", "Kantin Mahasiswa") ?: "Kantin Mahasiswa")
    val qrisMerchantName: StateFlow<String> = _qrisMerchantName.asStateFlow()

    private val _qrisImagePath = MutableStateFlow(prefs.getString("qris_image_path", "") ?: "")
    val qrisImagePath: StateFlow<String> = _qrisImagePath.asStateFlow()

    private val _expWarningDays = MutableStateFlow(prefs.getInt("exp_warning_days", 7))
    val expWarningDays: StateFlow<Int> = _expWarningDays.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM") // SYSTEM, LIGHT, DARK
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _colorTheme = MutableStateFlow(prefs.getString("color_theme", "DEFAULT") ?: "DEFAULT") // DEFAULT, PINK, COKLAT, ORANGE
    val colorTheme: StateFlow<String> = _colorTheme.asStateFlow()

    private val _viewportWidth = MutableStateFlow(prefs.getString("viewport_width", "412dp") ?: "412dp")
    val viewportWidth: StateFlow<String> = _viewportWidth.asStateFlow()

    private val _defaultPaymentMethod = MutableStateFlow(
        (prefs.getString("default_payment_method", "Tunai") ?: "Tunai").let {
            if (it.equals("Emoney", ignoreCase = true)) "E-Wallet" else it
        }
    )
    val defaultPaymentMethod: StateFlow<String> = _defaultPaymentMethod.asStateFlow()

    private val _openingCapital = MutableStateFlow(prefs.getLong("opening_capital", 0L))
    val openingCapital: StateFlow<Long> = _openingCapital.asStateFlow()

    private val _previousSales = MutableStateFlow(prefs.getLong("previous_sales", 0L))
    val previousSales: StateFlow<Long> = _previousSales.asStateFlow()

    private val _previousSalesDate = MutableStateFlow(prefs.getString("previous_sales_date", "") ?: "")
    val previousSalesDate: StateFlow<String> = _previousSalesDate.asStateFlow()

    private val _previousSalesNote = MutableStateFlow(prefs.getString("previous_sales_note", "") ?: "")
    val previousSalesNote: StateFlow<String> = _previousSalesNote.asStateFlow()

    fun updateBusinessInfo(name: String, address: String, phone: String) {
        val trimmedName = name.trim().ifEmpty { "Kantin Mahasiswa" }
        val trimmedAddress = address.trim()
        val trimmedPhone = phone.trim()
        _businessName.value = trimmedName
        _businessAddress.value = trimmedAddress
        _businessPhone.value = trimmedPhone
        prefs.edit()
            .putString("business_name", trimmedName)
            .putString("business_address", trimmedAddress)
            .putString("business_phone", trimmedPhone)
            .apply()
    }

    fun updateDefaultPaymentMethod(method: String) {
        val clean = when {
            method.equals("QRIS", ignoreCase = true) -> "QRIS"
            method.equals("Transfer", ignoreCase = true) -> "Transfer"
            method.equals("E-Wallet", ignoreCase = true) || method.equals("Emoney", ignoreCase = true) -> "E-Wallet"
            else -> "Tunai"
        }
        _defaultPaymentMethod.value = clean
        prefs.edit().putString("default_payment_method", clean).apply()
    }

    private fun loadBankAccounts(): List<BankAccount> {
        val jsonStr = prefs.getString("bank_accounts_json", "[]") ?: "[]"
        val list = mutableListOf<BankAccount>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    BankAccount(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        bankName = obj.optString("bankName", ""),
                        accountName = obj.optString("accountName", ""),
                        accountNumber = obj.optString("accountNumber", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private val _bankAccounts = MutableStateFlow<List<BankAccount>>(loadBankAccounts())
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    fun addBankAccount(bankName: String, accountName: String, accountNumber: String): Boolean {
        val current = _bankAccounts.value.toMutableList()
        if (current.size >= 5) {
            viewModelScope.launch {
                _userMessage.emit("Maksimal 5 akun rekening transfer yang dapat didaftarkan.")
            }
            return false
        }
        val trimmedBank = bankName.trim()
        val trimmedAccName = accountName.trim()
        val trimmedAccNum = accountNumber.trim()
        if (trimmedBank.isBlank() || trimmedAccNum.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Nama Bank dan Nomor Rekening wajib diisi.")
            }
            return false
        }

        val newAcc = BankAccount(
            bankName = trimmedBank,
            accountName = trimmedAccName,
            accountNumber = trimmedAccNum
        )
        current.add(newAcc)
        saveBankAccountsInternal(current)
        viewModelScope.launch {
            _userMessage.emit("Rekening $trimmedBank berhasil ditambahkan.")
        }
        return true
    }

    fun removeBankAccount(id: String) {
        val current = _bankAccounts.value.filter { it.id != id }
        saveBankAccountsInternal(current)
        viewModelScope.launch {
            _userMessage.emit("Rekening berhasil dihapus.")
        }
    }

    private fun saveBankAccountsInternal(list: List<BankAccount>) {
        val array = JSONArray()
        list.take(5).forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("bankName", it.bankName)
            obj.put("accountName", it.accountName)
            obj.put("accountNumber", it.accountNumber)
            array.put(obj)
        }
        prefs.edit().putString("bank_accounts_json", array.toString()).apply()
        _bankAccounts.value = list.take(5)
    }

    fun updateOpeningCapital(amount: Long) {
        val clean = amount.coerceAtLeast(0L)
        _openingCapital.value = clean
        prefs.edit().putLong("opening_capital", clean).apply()
        viewModelScope.launch {
            _userMessage.emit("Modal awal berhasil disimpan.")
        }
    }

    fun updatePreviousSales(amount: Long, date: String, note: String) {
        val cleanAmount = amount.coerceAtLeast(0L)
        val cleanDate = date.trim()
        val cleanNote = note.trim()
        _previousSales.value = cleanAmount
        _previousSalesDate.value = cleanDate
        _previousSalesNote.value = cleanNote
        prefs.edit()
            .putLong("previous_sales", cleanAmount)
            .putString("previous_sales_date", cleanDate)
            .putString("previous_sales_note", cleanNote)
            .apply()
        viewModelScope.launch {
            _userMessage.emit("Data penjualan sebelumnya berhasil disimpan.")
        }
    }

    fun saveQrisImage(context: Context, uri: android.net.Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri)
                if (mimeType != null && !mimeType.startsWith("image/")) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("File yang dipilih bukan format gambar.")
                    }
                    return@launch
                }

                val inputStream = contentResolver.openInputStream(uri)
                    ?: throw java.io.IOException("Tidak dapat membuka file gambar.")
                val bytes = inputStream.use { it.readBytes() }
                if (bytes.isEmpty()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("File gambar kosong.")
                    }
                    return@launch
                }
                if (bytes.size > 10 * 1024 * 1024) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("Ukuran gambar terlalu besar (maksimal 10MB).")
                    }
                    return@launch
                }

                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap == null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("Format gambar tidak valid atau rusak.")
                    }
                    return@launch
                }

                val destFile = java.io.File(context.filesDir, "qris_merchant.png")
                java.io.FileOutputStream(destFile).use { out ->
                    out.write(bytes)
                    out.flush()
                }

                val savedPath = destFile.absolutePath
                _qrisImagePath.value = savedPath
                prefs.edit().putString("qris_image_path", savedPath).apply()

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _userMessage.emit("Gambar QRIS berhasil disimpan.")
                    onSuccess()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Gagal menyimpan QRIS: ${e.localizedMessage ?: "Terjadi kesalahan"}")
                }
            }
        }
    }

    fun saveCroppedQrisBitmap(
        context: Context,
        bitmap: Bitmap,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val destFile = java.io.File(context.filesDir, "qris_merchant.png")
                java.io.FileOutputStream(destFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                    out.flush()
                }

                val savedPath = destFile.absolutePath
                _qrisImagePath.value = savedPath
                prefs.edit().putString("qris_image_path", savedPath).apply()

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _userMessage.emit("Gambar QRIS berhasil dipotong kotak & disimpan.")
                    onSuccess()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Gagal menyimpan QRIS: ${e.localizedMessage ?: "Terjadi kesalahan"}")
                }
            }
        }
    }

    fun deleteQrisImage(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val oldPath = _qrisImagePath.value
                if (oldPath.isNotBlank()) {
                    val oldFile = java.io.File(oldPath)
                    if (oldFile.exists()) oldFile.delete()
                }
                val internalFile = java.io.File(context.filesDir, "qris_merchant.png")
                if (internalFile.exists()) internalFile.delete()
            } catch (_: Exception) {}
            _qrisImagePath.value = ""
            prefs.edit().putString("qris_image_path", "").apply()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _userMessage.emit("Gambar QRIS berhasil dihapus.")
                onComplete()
            }
        }
    }

    fun resetSettingsOnly(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val internalFile = java.io.File(context.filesDir, "qris_merchant.png")
                if (internalFile.exists()) internalFile.delete()
            } catch (_: Exception) {}
            prefs.edit().clear().apply()
            _businessName.value = "Kantin Mahasiswa"
            _businessAddress.value = "Gedung Utama Kampus Lt. 1"
            _businessPhone.value = "0812-3456-7890"
            _receiptFooter.value = "Terima kasih atas kunjungan Anda!"
            _qrisMerchantName.value = "Kantin Mahasiswa"
            _qrisImagePath.value = ""
            _defaultPaymentMethod.value = "Tunai"
            _openingCapital.value = 0L
            _previousSales.value = 0L
            _previousSalesDate.value = ""
            _previousSalesNote.value = ""
            _themeMode.value = "SYSTEM"
            _colorTheme.value = "DEFAULT"
            _expWarningDays.value = 7
            _bankAccounts.value = emptyList()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _userMessage.emit("Pengaturan berhasil direset ke bawaan.")
                onComplete()
            }
        }
    }

    fun updateQrisInfo(merchantName: String, imagePath: String) {
        _qrisMerchantName.value = merchantName.trim()
        _qrisImagePath.value = imagePath.trim()
        prefs.edit()
            .putString("qris_merchant_name", merchantName.trim())
            .putString("qris_image_path", imagePath.trim())
            .apply()
    }

    fun updateReceiptSettings(footer: String) {
        val trimmed = footer.trim()
        _receiptFooter.value = trimmed
        prefs.edit().putString("receipt_footer", trimmed).apply()
    }

    fun updateViewportWidth(width: String) {
        _viewportWidth.value = width
        prefs.edit().putString("viewport_width", width).apply()
    }

    fun updateStockSettings(expDays: Int) {
        val days = expDays.coerceAtLeast(1)
        _expWarningDays.value = days
        prefs.edit().putInt("exp_warning_days", days).apply()
    }

    fun updateBusinessName(newName: String) {
        val trimmed = newName.trim().ifEmpty { "Kantin Mahasiswa" }
        _businessName.value = trimmed
        prefs.edit().putString("business_name", trimmed).apply()
    }

    fun updateThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun updateColorTheme(themeKey: String) {
        val validKey = when (themeKey.uppercase()) {
            "PINK" -> "PINK"
            "COKLAT" -> "COKLAT"
            "ORANGE" -> "ORANGE"
            else -> "DEFAULT"
        }
        _colorTheme.value = validKey
        prefs.edit().putString("color_theme", validKey).apply()
    }

    // --- Database Streams ---
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements: StateFlow<List<StockMovementEntity>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionItems: StateFlow<List<TransactionItemEntity>> = repository.allTransactionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val changeRecords: StateFlow<List<ChangeRecordEntity>> = repository.allChangeRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingChangeRecords: StateFlow<List<ChangeRecordEntity>> = repository.pendingChangeRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lossRecords: StateFlow<List<LossRecordEntity>> = repository.allLossRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promos: StateFlow<List<PromoEntity>> = repository.allPromos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- POS & Cart State ---
    private val _cartMap = MutableStateFlow<Map<Long, Int>>(emptyMap()) // productId -> qty
    private val _cartProducts = MutableStateFlow<Map<Long, ProductEntity>>(emptyMap())
    private val _discountValue = MutableStateFlow(0L)
    private val _isDiscountPercentage = MutableStateFlow(false)

    val cart: StateFlow<CartSummary> = combine(
        listOf(
            products,
            _cartMap,
            _cartProducts,
            _discountValue,
            _isDiscountPercentage,
            promos
        )
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val productList = args[0] as List<ProductEntity>
        @Suppress("UNCHECKED_CAST")
        val cartMap = args[1] as Map<Long, Int>
        @Suppress("UNCHECKED_CAST")
        val cachedProducts = args[2] as Map<Long, ProductEntity>
        val discVal = args[3] as Long
        val isPercentage = args[4] as Boolean
        @Suppress("UNCHECKED_CAST")
        val promoList = args[5] as List<PromoEntity>

        val productMap = productList.associateBy { it.id }
        val items = cartMap.mapNotNull { (prodId, qty) ->
            val prod = productMap[prodId] ?: cachedProducts[prodId] ?: return@mapNotNull null
            val sub = prod.sellingPrice * qty
            val prof = (prod.sellingPrice - prod.costPrice) * qty
            CartItem(product = prod, quantity = qty, subtotal = sub, profit = prof)
        }

        val totalItems = items.sumOf { it.quantity }
        val subtotal = items.sumOf { it.subtotal }

        // Manual discount calculation
        val manualDiscountAmount = if (isPercentage) {
            (subtotal * discVal) / 100
        } else {
            discVal
        }.coerceAtMost(subtotal)

        // Automated bundling promo calculation
        val allPrices = productMap.mapValues { it.value.sellingPrice }
        val allNames = productMap.mapValues { it.value.name }
        val promoResult = PromoEngine.evaluatePromos(
            cartItems = cartMap,
            productPrices = allPrices,
            activePromos = promoList.filter { it.isActive },
            productNames = allNames
        )

        val totalDiscount = (manualDiscountAmount + promoResult.totalDiscount).coerceAtMost(subtotal)
        val total = (subtotal - totalDiscount).coerceAtLeast(0L)

        CartSummary(
            items = items,
            totalItemCount = totalItems,
            subtotal = subtotal,
            manualDiscount = manualDiscountAmount,
            promoDiscount = promoResult.totalDiscount,
            appliedPromos = promoResult.appliedPromos,
            discount = totalDiscount,
            isDiscountPercentage = isPercentage,
            discountValue = discVal,
            total = total
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CartSummary())

    // Notification / Toast / Snackbar events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Last completed transaction for receipt / success modal
    private val _lastCompletedTx = MutableStateFlow<TransactionEntity?>(null)
    val lastCompletedTx: StateFlow<TransactionEntity?> = _lastCompletedTx.asStateFlow()

    fun clearLastCompletedTx() {
        _lastCompletedTx.value = null
    }

    fun addToCart(product: ProductEntity) {
        val currentQty = _cartMap.value[product.id] ?: 0
        if (currentQty >= product.stock) {
            viewModelScope.launch {
                _userMessage.emit("Stok ${product.name} tidak cukup. Tersisa ${product.stock}.")
            }
            return
        }
        _cartProducts.value = _cartProducts.value + (product.id to product)
        _cartMap.value = _cartMap.value + (product.id to (currentQty + 1))
    }

    fun decreaseCart(product: ProductEntity) {
        val currentQty = _cartMap.value[product.id] ?: 0
        if (currentQty <= 1) {
            _cartMap.value = _cartMap.value - product.id
            _cartProducts.value = _cartProducts.value - product.id
        } else {
            _cartMap.value = _cartMap.value + (product.id to (currentQty - 1))
        }
    }

    fun updateCartQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            val product = _cartProducts.value[productId]
                ?: products.value.find { it.id == productId }
                ?: return
            if (quantity > product.stock) {
                viewModelScope.launch {
                    _userMessage.emit("Stok ${product.name} tidak cukup. Tersisa ${product.stock}.")
                }
                return
            }
            _cartProducts.value = _cartProducts.value + (productId to product)
            _cartMap.value = _cartMap.value + (productId to quantity)
        }
    }

    fun removeFromCart(productId: Long) {
        _cartMap.value = _cartMap.value - productId
        _cartProducts.value = _cartProducts.value - productId
    }

    fun clearCart() {
        _cartMap.value = emptyMap()
        _cartProducts.value = emptyMap()
        _discountValue.value = 0L
        _isDiscountPercentage.value = false
    }

    fun setDiscount(value: Long, isPercentage: Boolean) {
        _discountValue.value = value
        _isDiscountPercentage.value = isPercentage
    }

    // --- Double click protection ---
    private val _isProcessingSale = MutableStateFlow(false)
    val isProcessingSale: StateFlow<Boolean> = _isProcessingSale.asStateFlow()

    // --- Complete Sale Flow ---
    fun processSale(
        paymentMethod: String,
        cashReceived: Long,
        customerName: String = "",
        customerPhone: String = "",
        debtNote: String = "",
        isChangePending: Boolean = false,
        buyerNameForChange: String = "",
        changeNote: String = "",
        onSuccess: (TransactionEntity) -> Unit
    ) {
        if (_isProcessingSale.value) return
        _isProcessingSale.value = true

        viewModelScope.launch {
            try {
                val currentCart = cart.value
                if (currentCart.items.isEmpty()) {
                    _userMessage.emit("Keranjang masih kosong!")
                    return@launch
                }

                val isBayarNanti = paymentMethod == "Bayar Nanti"

                if (isBayarNanti && customerName.isBlank()) {
                    _userMessage.emit("Nama penghutang wajib diisi!")
                    return@launch
                }

                if (paymentMethod == "Tunai" && cashReceived < currentCart.total) {
                    _userMessage.emit("Uang diterima kurang dari total belanja!")
                    return@launch
                }

                val change = if (paymentMethod == "Tunai") {
                    (cashReceived - currentCart.total).coerceAtLeast(0L)
                } else {
                    0L
                }

                if (paymentMethod == "Tunai" && change > 0 && isChangePending && buyerNameForChange.isBlank()) {
                    _userMessage.emit("Nama pembeli untuk kembalian belum diberikan wajib diisi!")
                    return@launch
                }

                val primaryPromo = currentCart.appliedPromos.firstOrNull()
                val promoNamesSummary = currentCart.appliedPromos.joinToString(", ") { it.promoName }

                val txEntity = TransactionEntity(
                    transactionNumber = FormatUtils.generateTransactionNumber(),
                    subtotal = currentCart.subtotal,
                    discount = currentCart.discount,
                    promoId = primaryPromo?.promoId,
                    promoName = if (promoNamesSummary.isNotBlank()) promoNamesSummary else null,
                    promoDiscount = currentCart.promoDiscount,
                    total = currentCart.total,
                    paymentMethod = paymentMethod,
                    customerName = customerName.trim(),
                    cashReceived = if (paymentMethod == "Tunai") cashReceived else if (isBayarNanti) 0L else currentCart.total,
                    change = change,
                    status = if (isBayarNanti) "UNPAID" else "COMPLETED"
                )

                val items = currentCart.items.map { item ->
                    TransactionItemEntity(
                        transactionId = 0L,
                        productId = item.product.id,
                        productNameSnapshot = item.product.name,
                        costPriceSnapshot = item.product.costPrice,
                        sellingPriceSnapshot = item.product.sellingPrice,
                        quantity = item.quantity,
                        subtotal = item.subtotal,
                        profit = item.profit
                    )
                }

                val debtEntity = if (isBayarNanti) {
                    DebtEntity(
                        transactionId = 0L,
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        amount = currentCart.total,
                        remainingAmount = currentCart.total,
                        status = "UNPAID",
                        note = debtNote.trim()
                    )
                } else null

                val changeRecord = if (paymentMethod == "Tunai" && change > 0 && isChangePending) {
                    ChangeRecordEntity(
                        transactionId = null,
                        buyerName = buyerNameForChange.trim(),
                        amount = change,
                        status = "PENDING",
                        note = changeNote.trim()
                    )
                } else null

                val txId = repository.completeSale(txEntity, items, debtEntity, changeRecord)
                val completed = txEntity.copy(id = txId)
                if (isBayarNanti) {
                    _userMessage.emit("Hutang atas nama ${customerName.trim()} berhasil dicatat!")
                } else {
                    _lastCompletedTx.value = completed
                    if (changeRecord != null) {
                        _userMessage.emit("Transaksi berhasil! Catatan kembalian ${FormatUtils.formatRupiah(change)} untuk ${buyerNameForChange.trim()} tersimpan.")
                    } else {
                        _userMessage.emit("Transaksi #${completed.transactionNumber} berhasil!")
                    }
                }
                clearCart()
                onSuccess(completed)
            } catch (e: Exception) {
                _userMessage.emit("Gagal menyimpan transaksi: ${e.localizedMessage}")
            } finally {
                _isProcessingSale.value = false
            }
        }
    }

    // --- Catatan Kembalian Actions ---
    fun markChangeAsPaid(id: Long) {
        viewModelScope.launch {
            try {
                repository.markChangeAsPaid(id)
                _userMessage.emit("Kembalian berhasil ditandai sudah diberikan!")
            } catch (e: Exception) {
                _userMessage.emit("Gagal memperbarui catatan kembalian: ${e.localizedMessage}")
            }
        }
    }

    fun deleteChangeRecord(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteChangeRecord(id)
                _userMessage.emit("Catatan kembalian berhasil dihapus.")
            } catch (e: Exception) {
                _userMessage.emit("Gagal menghapus catatan kembalian: ${e.localizedMessage}")
            }
        }
    }

    // --- Debts / Bayar Nanti Operations ---
    fun settleDebt(
        debtId: Long,
        amount: Long,
        paymentMethod: String,
        note: String = "",
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (amount <= 0L) {
                _userMessage.emit("Nominal pembayaran harus lebih dari 0!")
                return@launch
            }
            try {
                val success = repository.settleDebt(debtId, amount, paymentMethod, note)
                if (success) {
                    _userMessage.emit("Pelunasan hutang berhasil dicatat!")
                    onSuccess?.invoke()
                } else {
                    _userMessage.emit("Data hutang tidak ditemukan.")
                }
            } catch (e: Exception) {
                _userMessage.emit("Gagal memproses pelunasan: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteDebt(debtId)
                _userMessage.emit("Data hutang berhasil dihapus.")
            } catch (e: Exception) {
                _userMessage.emit("Gagal menghapus hutang: ${e.localizedMessage}")
            }
        }
    }

    fun updateDebtItems(
        debtId: Long,
        updatedItems: List<com.example.data.local.dao.UpdatedDebtItem>,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val success = repository.updateDebtItems(debtId, updatedItems)
                if (success) {
                    _userMessage.emit("Belanjaan kasbon berhasil diperbarui & stok disesuaikan!")
                    onSuccess?.invoke()
                } else {
                    _userMessage.emit("Gagal memperbarui daftar belanjaan hutang.")
                }
            } catch (e: Exception) {
                _userMessage.emit("Terjadi kesalahan: ${e.localizedMessage}")
            }
        }
    }

    // --- Dashboard Stats Calculation ---
    val dashboardStats: StateFlow<DashboardStats> = combine(
        listOf(
            transactions,
            allTransactionItems,
            expenses,
            products,
            debts,
            pendingChangeRecords,
            lossRecords,
            _expWarningDays
        )
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val txList = args[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val itemList = args[1] as List<TransactionItemEntity>
        @Suppress("UNCHECKED_CAST")
        val expenseList = args[2] as List<ExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val productList = args[3] as List<ProductEntity>
        @Suppress("UNCHECKED_CAST")
        val debtList = args[4] as List<DebtEntity>
        @Suppress("UNCHECKED_CAST")
        val pendingChanges = args[5] as List<ChangeRecordEntity>
        @Suppress("UNCHECKED_CAST")
        val lossList = args[6] as List<LossRecordEntity>
        val warningDays = args[7] as Int
        val now = System.currentTimeMillis()
        val todayTx = txList.filter { it.status == "COMPLETED" && FormatUtils.isToday(it.createdAt) }
        val todayTxIds = todayTx.map { it.id }.toSet()
        val todayItems = itemList.filter { it.transactionId in todayTxIds }

        val sales = todayTx.sumOf { it.total }
        val expensesToday = expenseList.filter { FormatUtils.isToday(it.date) }.sumOf { it.amount }
        val totalDiscountsToday = todayTx.sumOf { it.discount }
        val profit = (todayItems.sumOf { it.profit } - totalDiscountsToday).coerceAtLeast(0L)
        val itemsSold = todayItems.sumOf { it.quantity }

        val lowStock = productList.filter { it.stock <= it.minimumStock }
        val recent = txList.take(5)

        val unpaidDebts = debtList.filter { it.status != "PAID" }
        val unpaidTotal = unpaidDebts.sumOf { it.remainingAmount }
        val unpaidCount = unpaidDebts.size

        val pendingChangeTotal = pendingChanges.sumOf { it.amount }
        val warningMs = warningDays.toLong() * 24 * 60 * 60 * 1000L
        val expiring = productList.filter {
            val exp = it.expirationDate
            exp != null && exp > now && exp <= (now + warningMs) && it.stock > 0
        }
        val totalLoss = lossList.sumOf { it.totalLoss }

        DashboardStats(
            todaySales = sales,
            todayExpense = expensesToday,
            todayProfit = profit,
            todayTxCount = todayTx.size,
            todayItemsSold = itemsSold,
            lowStockProducts = lowStock,
            recentTransactions = recent,
            unpaidDebtTotal = unpaidTotal,
            unpaidDebtCount = unpaidCount,
            unpaidDebts = unpaidDebts,
            pendingChangeList = pendingChanges,
            pendingChangeTotal = pendingChangeTotal,
            expiringProducts = expiring,
            totalLoss = totalLoss
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // --- Report Period Filter & Stats ---
    private val _reportPeriod = MutableStateFlow("Hari Ini") // "Hari Ini", "Kemarin", "7 Hari", "Bulan Ini", "Semua"
    val reportPeriod: StateFlow<String> = _reportPeriod.asStateFlow()

    fun setReportPeriod(period: String) {
        _reportPeriod.value = period
    }

    val reportStats: StateFlow<ReportStats> = combine(
        listOf(
            transactions,
            allTransactionItems,
            expenses,
            incomes,
            lossRecords,
            _reportPeriod
        )
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val txList = args[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val itemList = args[1] as List<TransactionItemEntity>
        @Suppress("UNCHECKED_CAST")
        val expenseList = args[2] as List<ExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val incomeList = args[3] as List<IncomeEntity>
        @Suppress("UNCHECKED_CAST")
        val lossList = args[4] as List<LossRecordEntity>
        val period = args[5] as String
        val isMatch: (Long) -> Boolean = when (period) {
            "Hari Ini" -> { ts -> FormatUtils.isToday(ts) }
            "Kemarin" -> { ts -> FormatUtils.isYesterday(ts) }
            "7 Hari", "7 Hari Terakhir" -> { ts -> FormatUtils.isWithinDays(ts, 7) }
            "Bulan Ini" -> { ts -> FormatUtils.isThisMonth(ts) }
            else -> { _ -> true }
        }

        val filteredTx = txList.filter { it.status == "COMPLETED" && isMatch(it.createdAt) }
        val filteredTxIds = filteredTx.map { it.id }.toSet()
        val filteredItems = itemList.filter { it.transactionId in filteredTxIds }

        val filteredExpenses = expenseList.filter { isMatch(it.date) }
        val filteredIncomes = incomeList.filter { isMatch(it.date) }
        val filteredLosses = lossList.filter { isMatch(it.date) }

        val totalSales = filteredTx.sumOf { it.total }
        val totalExpense = filteredExpenses.sumOf { it.amount }
        val totalIncome = filteredIncomes.sumOf { it.amount }
        val totalDiscount = filteredTx.sumOf { it.discount }
        val grossProfit = (filteredItems.sumOf { it.profit } - totalDiscount).coerceAtLeast(0L)
        val netCashflow = totalIncome - totalExpense
        val txCount = filteredTx.size
        val itemsSold = filteredItems.sumOf { it.quantity }
        val avgTx = if (txCount > 0) totalSales / txCount else 0L
        val totalLoss = filteredLosses.sumOf { it.totalLoss }

        // Top Products
        val topProds = filteredItems.groupBy { it.productId }
            .map { (prodId, items) ->
                val name = items.firstOrNull()?.productNameSnapshot ?: "Produk"
                val qty = items.sumOf { it.quantity }
                val rev = items.sumOf { it.subtotal }
                TopProductSummary(
                    productId = prodId,
                    name = name,
                    quantitySold = qty,
                    totalRevenue = rev,
                    unit = "item"
                )
            }
            .sortedByDescending { it.quantitySold }
            .take(5)

        // Daily sales breakdown for simple chart (last 7 days or matching days)
        val dailyMap = mutableMapOf<String, Long>()
        for (i in 6 downTo 0) {
            val dayTs = System.currentTimeMillis() - (i.toLong() * 24 * 60 * 60 * 1000)
            val label = FormatUtils.formatDayName(dayTs)
            dailyMap[label] = 0L
        }
        for (tx in filteredTx) {
            val label = FormatUtils.formatDayName(tx.createdAt)
            dailyMap[label] = (dailyMap[label] ?: 0L) + tx.total
        }
        val dailySalesList = dailyMap.map { DailySalesStat(it.key, it.value) }

        ReportStats(
            totalSales = totalSales,
            totalExpense = totalExpense,
            grossProfit = grossProfit,
            netCashflow = netCashflow,
            transactionCount = txCount,
            itemsSold = itemsSold,
            averageTxValue = avgTx,
            topProducts = topProds,
            dailySales = dailySalesList,
            totalLoss = totalLoss,
            lossRecords = filteredLosses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportStats())

    // --- Product Actions ---
    fun addProduct(
        name: String,
        categoryId: Long,
        costPrice: Long,
        sellingPrice: Long,
        stock: Int,
        minStock: Int,
        unit: String,
        expirationDate: Long? = null
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessage.emit("Nama produk tidak boleh kosong")
                return@launch
            }
            if (sellingPrice <= 0) {
                _userMessage.emit("Harga jual harus lebih besar dari 0")
                return@launch
            }

            val product = ProductEntity(
                name = name.trim(),
                categoryId = categoryId,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stock = stock.coerceAtLeast(0),
                minimumStock = minStock.coerceAtLeast(0),
                unit = unit.ifBlank { "pcs" },
                expirationDate = expirationDate
            )
            repository.insertProduct(product)
            _userMessage.emit("Produk '${product.name}' berhasil disimpan!")
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _userMessage.emit("Perubahan produk '${product.name}' berhasil disimpan!")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product.id)
            removeFromCart(product.id)
            _userMessage.emit("Produk '${product.name}' dihapus.")
        }
    }

    fun restockProduct(
        productId: Long,
        addQty: Int,
        note: String,
        recordExpense: Boolean,
        totalCost: Long
    ) {
        viewModelScope.launch {
            if (addQty <= 0) return@launch
            repository.adjustStock(
                productId = productId,
                qtyChange = addQty,
                type = "RESTOCK",
                note = note.ifBlank { "Restock stok baru" },
                recordExpense = recordExpense,
                expenseAmount = totalCost
            )
            _userMessage.emit("Stok berhasil ditambahkan (+$addQty)!")
        }
    }

    fun reduceStock(productId: Long, reduceQty: Int, reason: String) {
        viewModelScope.launch {
            if (reduceQty <= 0) return@launch
            val isLoss = reason.contains("Kadaluarsa", ignoreCase = true) || reason.contains("Rusak", ignoreCase = true)
            if (isLoss) {
                val success = repository.recordLoss(productId, reduceQty, reason, note = "Pengurangan stok manual")
                if (success) {
                    _userMessage.emit("Kerugian stok (-$reduceQty: $reason) berhasil dicatat!")
                } else {
                    _userMessage.emit("Gagal mengurangi stok.")
                }
            } else {
                repository.adjustStock(
                    productId = productId,
                    qtyChange = -reduceQty,
                    type = "MANUAL_REMOVE",
                    note = reason.ifBlank { "Penyesuaian stok" }
                )
                _userMessage.emit("Stok berhasil dikurangi (-$reduceQty)!")
            }
        }
    }

    fun recordLoss(productId: Long, quantity: Int, reason: String, note: String = "") {
        viewModelScope.launch {
            if (quantity <= 0) return@launch
            val success = repository.recordLoss(productId, quantity, reason, note)
            if (success) {
                _userMessage.emit("Kerugian stok ($quantity item) berhasil dicatat!")
            } else {
                _userMessage.emit("Gagal mencatat kerugian stok.")
            }
        }
    }

    fun cancelLoss(lossId: Long, restoreStock: Boolean = true, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.cancelLoss(lossId, restoreStock)
            if (success) {
                _userMessage.emit("Kerugian berhasil dibatalkan. Dana otomatis masuk ke kas.")
            } else {
                _userMessage.emit("Gagal membatalkan kerugian.")
            }
            onComplete()
        }
    }

    fun processExpiredProductsManually() {
        viewModelScope.launch {
            val count = repository.processExpiredProducts()
            if (count > 0) {
                _userMessage.emit("$count produk kadaluarsa berhasil dialihkan ke catatan kerugian!")
            } else {
                _userMessage.emit("Tidak ada produk yang kadaluarsa.")
            }
        }
    }

    // --- Category Actions ---
    fun addCategory(name: String, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val newId = repository.insertCategory(name.trim())
            _userMessage.emit("Kategori '$name' berhasil ditambahkan!")
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onComplete?.invoke(newId)
            }
        }
    }

    // --- Expense Actions ---
    fun addExpense(category: String, amount: Long, note: String) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("Nominal pengeluaran harus lebih besar dari 0")
                return@launch
            }
            repository.insertExpense(
                category = category.ifBlank { "Lainnya" },
                amount = amount,
                note = note
            )
            _userMessage.emit("Pengeluaran ${FormatUtils.formatRupiah(amount)} berhasil dicatat!")
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            _userMessage.emit("Pengeluaran dihapus.")
        }
    }

    fun updateExpense(id: Long, category: String, amount: Long, note: String) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("Nominal pengeluaran harus lebih besar dari 0")
                return@launch
            }
            repository.updateExpense(id, category, amount, note)
            _userMessage.emit("Pengeluaran berhasil diperbarui!")
        }
    }

    // --- Transaction Management ---
    fun cancelTransaction(txId: Long) {
        viewModelScope.launch {
            repository.cancelTransaction(txId)
            _userMessage.emit("Transaksi berhasil dibatalkan dan stok dikembalikan!")
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
            _userMessage.emit("Transaksi berhasil dihapus dan stok dikembalikan!")
        }
    }

    // Fetch items for specific transaction detail
    suspend fun getTransactionItemsForDetail(txId: Long): List<TransactionItemEntity> {
        return repository.getTransactionItemsSync(txId)
    }

    // Reset database
    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetAllData()
            clearCart()
            _userMessage.emit("Semua data berhasil direset.")
            onComplete()
        }
    }

    // --- Backup & Restore JSON ---
    fun exportDataAsJson(onComplete: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val root = JSONObject()
                root.put("app", "Kantin Mahasiswa POS")
                root.put("version", 1)
                root.put("exportedAt", System.currentTimeMillis())

                val settingsObj = JSONObject().apply {
                    put("businessName", _businessName.value)
                    put("businessAddress", _businessAddress.value)
                    put("businessPhone", _businessPhone.value)
                    put("receiptFooter", _receiptFooter.value)
                    put("qrisMerchantName", _qrisMerchantName.value)
                    put("qrisImagePath", _qrisImagePath.value)
                    put("defaultPaymentMethod", _defaultPaymentMethod.value)
                    put("openingCapital", _openingCapital.value)
                    put("previousSales", _previousSales.value)
                    put("previousSalesDate", _previousSalesDate.value)
                    put("previousSalesNote", _previousSalesNote.value)
                    put("themeMode", _themeMode.value)
                    put("colorTheme", _colorTheme.value)
                    put("expWarningDays", _expWarningDays.value)
                }
                root.put("settings", settingsObj)

                val catArray = JSONArray()
                for (cat in repository.allCategoriesSync()) {
                    catArray.put(JSONObject().apply {
                        put("id", cat.id)
                        put("name", cat.name)
                    })
                }
                root.put("categories", catArray)

                val prodArray = JSONArray()
                for (prod in repository.allProductsSync()) {
                    prodArray.put(JSONObject().apply {
                        put("id", prod.id)
                        put("name", prod.name)
                        put("categoryId", prod.categoryId)
                        put("costPrice", prod.costPrice)
                        put("sellingPrice", prod.sellingPrice)
                        put("stock", prod.stock)
                        put("minimumStock", prod.minimumStock)
                        put("unit", prod.unit)
                        if (prod.expirationDate != null) {
                            put("expirationDate", prod.expirationDate)
                        }
                    })
                }
                root.put("products", prodArray)

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(root.toString(2))
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _userMessage.emit("Gagal backup data: ${e.localizedMessage}")
                }
            }
        }
    }

    // --- PROMO MANAGEMENT ---
    fun savePromo(promo: PromoEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (promo.id == 0L) {
                repository.insertPromo(promo)
                _userMessage.emit("Promo '${promo.name}' berhasil dibuat")
            } else {
                repository.updatePromo(promo)
                _userMessage.emit("Promo '${promo.name}' berhasil diperbarui")
            }
            onComplete()
        }
    }

    fun togglePromoActive(promoId: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.updatePromoActive(promoId, isActive)
            _userMessage.emit(if (isActive) "Promo diaktifkan" else "Promo dinonaktifkan")
        }
    }

    fun deletePromo(promoId: Long) {
        viewModelScope.launch {
            repository.deletePromo(promoId)
            _userMessage.emit("Promo berhasil dihapus")
        }
    }

    // --- NOTES MANAGEMENT ---
    fun saveNote(note: NoteEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
                _userMessage.emit("Catatan tersimpan")
            } else {
                repository.updateNote(note)
                _userMessage.emit("Catatan diperbarui")
            }
            onComplete()
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _userMessage.emit("Catatan dihapus")
        }
    }
}

