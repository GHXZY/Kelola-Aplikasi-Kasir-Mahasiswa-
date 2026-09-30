package com.example.data.repository

import com.example.data.local.dao.PosDao
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PosRepository(private val posDao: PosDao) {

    // --- Flows ---
    val allCategories: Flow<List<CategoryEntity>> = posDao.getAllCategories()
    val activeProducts: Flow<List<ProductEntity>> = posDao.getActiveProducts()
    val allTransactions: Flow<List<TransactionEntity>> = posDao.getAllTransactions()
    val allExpenses: Flow<List<ExpenseEntity>> = posDao.getAllExpenses()
    val allNotes: Flow<List<NoteEntity>> = posDao.getAllNotes()
    val allIncomes: Flow<List<IncomeEntity>> = posDao.getAllIncomes()
    val allStockMovements: Flow<List<StockMovementEntity>> = posDao.getAllStockMovements()
    val allTransactionItems: Flow<List<TransactionItemEntity>> = posDao.getAllTransactionItems()
    val allDebts: Flow<List<DebtEntity>> = posDao.getAllDebts()
    val allChangeRecords: Flow<List<ChangeRecordEntity>> = posDao.getAllChangeRecords()
    val pendingChangeRecords: Flow<List<ChangeRecordEntity>> = posDao.getPendingChangeRecords()
    val allLossRecords: Flow<List<LossRecordEntity>> = posDao.getAllLossRecords()
    val allPromos: Flow<List<PromoEntity>> = posDao.getAllPromos()
    val activePromos: Flow<List<PromoEntity>> = posDao.getActivePromos()

    fun getTransactionItems(transactionId: Long): Flow<List<TransactionItemEntity>> {
        return posDao.getTransactionItems(transactionId)
    }

    suspend fun getTransactionItemsSync(transactionId: Long): List<TransactionItemEntity> {
        return withContext(Dispatchers.IO) {
            posDao.getTransactionItemsSync(transactionId)
        }
    }

    suspend fun getProductById(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        posDao.getProductById(id)
    }

    suspend fun allCategoriesSync(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        posDao.getAllCategoriesSync()
    }

    suspend fun allProductsSync(): List<ProductEntity> = withContext(Dispatchers.IO) {
        posDao.getAllProductsSync()
    }

    // --- Product Mutations ---
    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val id = posDao.insertProduct(product)
        // Record initial stock movement
        if (product.stock > 0) {
            posDao.insertStockMovement(
                StockMovementEntity(
                    productId = id,
                    type = "MANUAL_ADD",
                    quantity = product.stock,
                    previousStock = 0,
                    newStock = product.stock,
                    note = "Stok awal produk baru"
                )
            )
        }
        id
    }

    suspend fun getProductByBarcode(barcode: String): ProductEntity? = withContext(Dispatchers.IO) {
        posDao.getProductByBarcode(barcode)
    }

    suspend fun isBarcodeUnique(barcode: String, excludeProductId: Long? = null): Boolean = withContext(Dispatchers.IO) {
        posDao.countProductsWithBarcode(barcode, excludeProductId) == 0
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        posDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        posDao.softDeleteProduct(id)
    }

    // --- Stock Adjustments ---
    suspend fun adjustStock(
        productId: Long,
        qtyChange: Int,
        type: String,
        note: String,
        recordExpense: Boolean = false,
        expenseAmount: Long = 0L
    ) = withContext(Dispatchers.IO) {
        posDao.adjustStockAtomic(productId, qtyChange, type, note, recordExpense, expenseAmount)
    }

    // --- Category Mutations ---
    suspend fun insertCategory(name: String): Long = withContext(Dispatchers.IO) {
        posDao.insertCategory(CategoryEntity(name = name))
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        posDao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: Long, fallbackCategoryId: Long) = withContext(Dispatchers.IO) {
        posDao.deleteCategoryAndReassign(categoryId, fallbackCategoryId)
    }

    // --- Complete Sale ---
    suspend fun completeSale(
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        debt: DebtEntity? = null,
        changeRecord: ChangeRecordEntity? = null
    ): Long = withContext(Dispatchers.IO) {
        posDao.completeSaleAtomic(transaction, items, debt, changeRecord)
    }

    // --- Catatan Kembalian ---
    suspend fun insertChangeRecord(record: ChangeRecordEntity): Long = withContext(Dispatchers.IO) {
        posDao.insertChangeRecord(record)
    }

    suspend fun markChangeAsPaid(id: Long) = withContext(Dispatchers.IO) {
        posDao.markChangeAsPaid(id, System.currentTimeMillis())
    }

    suspend fun deleteChangeRecord(id: Long) = withContext(Dispatchers.IO) {
        posDao.deleteChangeRecord(id)
    }

    // --- Laporan Kerugian ---
    suspend fun recordLoss(
        productId: Long,
        quantity: Int,
        reason: String,
        note: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        posDao.recordLossAtomic(productId, quantity, reason, note)
    }

    suspend fun cancelLoss(
        lossId: Long,
        restoreStock: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        posDao.cancelLossAtomic(lossId, restoreStock)
    }

    // Check & process expired products (IDEMPOTENT)
    suspend fun processExpiredProducts(): Int = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val allProducts = posDao.getAllProductsSync()
        var processedCount = 0
        for (product in allProducts) {
            val exp = product.expirationDate
            if (exp != null && exp < now && product.stock > 0 && !product.isDeleted) {
                val stockToLoss = product.stock
                val success = posDao.recordLossAtomic(
                    productId = product.id,
                    quantity = stockToLoss,
                    reason = "Kadaluarsa",
                    note = "Otomatis kadaluarsa sistem (HPP: Rp${product.costPrice})"
                )
                if (success) {
                    processedCount++
                }
            }
        }
        processedCount
    }

    // --- Debts / Bayar Nanti Operations ---
    suspend fun settleDebt(
        debtId: Long,
        paymentAmount: Long,
        paymentMethod: String,
        note: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        posDao.settleDebtAtomic(debtId, paymentAmount, paymentMethod, note)
    }

    suspend fun deleteDebt(debtId: Long) = withContext(Dispatchers.IO) {
        posDao.deleteDebt(debtId)
    }

    suspend fun getDebtById(debtId: Long): DebtEntity? = withContext(Dispatchers.IO) {
        posDao.getDebtById(debtId)
    }

    suspend fun updateDebtItems(
        debtId: Long,
        updatedItems: List<com.example.data.local.dao.UpdatedDebtItem>
    ): Boolean = withContext(Dispatchers.IO) {
        posDao.updateDebtItemsAtomic(debtId, updatedItems)
    }

    // --- Cancel / Delete Sale ---
    suspend fun cancelTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        posDao.cancelTransactionAtomic(transactionId)
    }

    suspend fun deleteTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        posDao.deleteTransactionAtomic(transactionId)
    }

    // --- Expenses & Incomes ---
    suspend fun insertExpense(
        category: String,
        amount: Long,
        note: String,
        date: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        posDao.insertExpense(
            ExpenseEntity(
                category = category,
                amount = amount,
                note = note,
                date = date
            )
        )
    }

    suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        posDao.deleteExpense(id)
    }

    suspend fun updateExpense(
        id: Long,
        category: String,
        amount: Long,
        note: String
    ) = withContext(Dispatchers.IO) {
        posDao.insertExpense(
            ExpenseEntity(
                id = id,
                category = category,
                amount = amount,
                note = note,
                date = System.currentTimeMillis()
            )
        )
    }

    suspend fun insertManualIncome(
        source: String,
        amount: Long,
        note: String,
        date: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        posDao.insertIncome(
            IncomeEntity(
                source = source,
                amount = amount,
                note = note,
                date = date
            )
        )
    }

    // --- Promos Operations ---
    suspend fun getPromoById(id: Long): PromoEntity? = withContext(Dispatchers.IO) {
        posDao.getPromoById(id)
    }

    suspend fun insertPromo(promo: PromoEntity): Long = withContext(Dispatchers.IO) {
        posDao.insertPromo(promo)
    }

    suspend fun updatePromo(promo: PromoEntity) = withContext(Dispatchers.IO) {
        posDao.updatePromo(promo.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updatePromoActive(id: Long, isActive: Boolean) = withContext(Dispatchers.IO) {
        posDao.updatePromoActive(id, isActive)
    }

    suspend fun deletePromo(id: Long) = withContext(Dispatchers.IO) {
        posDao.deletePromo(id)
    }

    // --- Catatan Toko (Notes) ---
    suspend fun getNoteById(id: Long): NoteEntity? = withContext(Dispatchers.IO) {
        posDao.getNoteById(id)
    }

    suspend fun insertNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        posDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        posDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        posDao.deleteNote(id)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        posDao.resetAllDataAtomic()
        posDao.insertCategories(
            listOf(
                CategoryEntity(name = "Makanan"),
                CategoryEntity(name = "Minuman"),
                CategoryEntity(name = "Snack"),
                CategoryEntity(name = "Lainnya")
            )
        )
    }
}
