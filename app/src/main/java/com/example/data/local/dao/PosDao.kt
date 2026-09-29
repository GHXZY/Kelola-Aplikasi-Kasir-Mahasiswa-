package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

data class TransactionWithItems(
    val transaction: TransactionEntity,
    val items: List<TransactionItemEntity>
)

data class UpdatedDebtItem(
    val productId: Long,
    val productNameSnapshot: String,
    val costPriceSnapshot: Long,
    val sellingPriceSnapshot: Long,
    val quantity: Int
)

@Dao
interface PosDao {

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryRaw(id: Long)

    @Query("UPDATE products SET categoryId = :fallbackCategoryId WHERE categoryId = :deletedCategoryId")
    suspend fun reassignProductCategory(deletedCategoryId: Long, fallbackCategoryId: Long)

    @Transaction
    suspend fun deleteCategoryAndReassign(deletedCategoryId: Long, fallbackCategoryId: Long) {
        reassignProductCategory(deletedCategoryId, fallbackCategoryId)
        deleteCategoryRaw(deletedCategoryId)
    }

    // --- PRODUCTS ---
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isDeleted = 0")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteProduct(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = :newStock, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProductStock(id: Long, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    // --- TRANSACTIONS ---
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTransactionStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionRaw(id: Long)

    // --- TRANSACTION ITEMS ---
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun getTransactionItems(transactionId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItemsSync(transactionId: Long): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items")
    fun getAllTransactionItems(): Flow<List<TransactionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteTransactionItemsByTxId(transactionId: Long)

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    // --- INCOMES ---
    @Query("SELECT * FROM incomes ORDER BY date DESC, id DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Query("DELETE FROM incomes WHERE transactionId = :transactionId")
    suspend fun deleteIncomeByTransactionId(transactionId: Long)

    // --- STOCK MOVEMENTS ---
    @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC")
    fun getAllStockMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity): Long

    // --- ATOMIC TRANSACTION: COMPLETE SALE ---
    @Transaction
    suspend fun completeSaleAtomic(
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        debt: DebtEntity? = null,
        changeRecord: ChangeRecordEntity? = null
    ): Long {
        val txId = insertTransaction(transaction)

        val itemsWithTxId = items.map { it.copy(transactionId = txId) }
        insertTransactionItems(itemsWithTxId)

        // Deduct stock for each item & record movement
        for (item in itemsWithTxId) {
            val product = getProductById(item.productId)
            if (product != null) {
                val previousStock = product.stock
                val newStock = (previousStock - item.quantity).coerceAtLeast(0)
                updateProductStock(product.id, newStock, System.currentTimeMillis())

                insertStockMovement(
                    StockMovementEntity(
                        productId = product.id,
                        type = "SALE",
                        quantity = -item.quantity,
                        previousStock = previousStock,
                        newStock = newStock,
                        transactionId = txId,
                        note = "Penjualan #${transaction.transactionNumber}"
                    )
                )
            }
        }

        if (debt != null) {
            // Save debt record
            insertDebt(
                debt.copy(
                    transactionId = txId,
                    transactionNumber = transaction.transactionNumber
                )
            )
        } else {
            // Add to Income if payment received
            insertIncome(
                IncomeEntity(
                    source = "Penjualan",
                    amount = transaction.total,
                    note = "Transaksi #${transaction.transactionNumber}",
                    transactionId = txId,
                    date = transaction.createdAt,
                    createdAt = transaction.createdAt
                )
            )
        }

        if (changeRecord != null) {
            insertChangeRecord(
                changeRecord.copy(
                    transactionId = txId,
                    transactionNumber = transaction.transactionNumber
                )
            )
        }

        return txId
    }

    // --- DEBTS / BAYAR NANTI ---
    @Query("SELECT * FROM debts ORDER BY CASE WHEN status = 'UNPAID' THEN 0 WHEN status = 'PARTIALLY_PAID' THEN 1 ELSE 2 END, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE transactionId = :txId LIMIT 1")
    suspend fun getDebtByTransactionId(txId: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: Long)

    @Query("DELETE FROM debts WHERE transactionId = :txId")
    suspend fun deleteDebtByTransactionId(txId: Long)

    @Transaction
    suspend fun settleDebtAtomic(
        debtId: Long,
        paymentAmount: Long,
        paymentMethod: String,
        note: String = ""
    ): Boolean {
        val debt = getDebtById(debtId) ?: return false
        val newRemaining = (debt.remainingAmount - paymentAmount).coerceAtLeast(0L)
        val newStatus = if (newRemaining == 0L) "PAID" else "PARTIALLY_PAID"

        val updatedDebt = debt.copy(
            remainingAmount = newRemaining,
            status = newStatus,
            paidAt = if (newRemaining == 0L) System.currentTimeMillis() else debt.paidAt,
            settlementPaymentMethod = paymentMethod
        )
        updateDebt(updatedDebt)

        // Insert Income record for this settlement into cashflow
        insertIncome(
            IncomeEntity(
                source = "Pelunasan Hutang",
                amount = paymentAmount,
                note = "Pelunasan hutang oleh ${debt.customerName} ($paymentMethod)${if (note.isNotBlank()) " - $note" else ""}",
                transactionId = debt.transactionId,
                date = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
        )

        if (newStatus == "PAID") {
            val tx = getTransactionById(debt.transactionId)
            if (tx != null) {
                updateTransaction(
                    tx.copy(
                        status = "COMPLETED",
                        cashReceived = tx.total,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        return true
    }

    // --- ATOMIC TRANSACTION: EDIT ITEMS IN UNPAID DEBT SALE ---
    @Transaction
    suspend fun updateDebtItemsAtomic(
        debtId: Long,
        updatedItems: List<UpdatedDebtItem>
    ): Boolean {
        val debt = getDebtById(debtId) ?: return false
        val tx = getTransactionById(debt.transactionId) ?: return false
        val existingItems = getTransactionItemsSync(tx.id)

        // 1. Calculate stock difference for all affected products
        val oldQtyMap = mutableMapOf<Long, Int>()
        for (item in existingItems) {
            oldQtyMap[item.productId] = (oldQtyMap[item.productId] ?: 0) + item.quantity
        }

        val newQtyMap = mutableMapOf<Long, Int>()
        for (item in updatedItems) {
            if (item.quantity > 0) {
                newQtyMap[item.productId] = (newQtyMap[item.productId] ?: 0) + item.quantity
            }
        }

        val allProductIds = (oldQtyMap.keys + newQtyMap.keys).toSet()

        for (pId in allProductIds) {
            val oldQ = oldQtyMap[pId] ?: 0
            val newQ = newQtyMap[pId] ?: 0
            val delta = newQ - oldQ // if delta > 0: customer bought more, stock decreases
            if (delta != 0) {
                val product = getProductById(pId)
                if (product != null) {
                    val prevStock = product.stock
                    val adjustedStock = (prevStock - delta).coerceAtLeast(0)
                    updateProductStock(product.id, adjustedStock, System.currentTimeMillis())

                    insertStockMovement(
                        StockMovementEntity(
                            productId = product.id,
                            type = if (delta > 0) "SALE" else "CANCELLED_SALE",
                            quantity = -delta,
                            previousStock = prevStock,
                            newStock = adjustedStock,
                            transactionId = tx.id,
                            note = if (delta > 0)
                                "Penambahan belanjaan kasbon #${tx.transactionNumber}"
                            else
                                "Pengurangan belanjaan kasbon #${tx.transactionNumber}"
                        )
                    )
                }
            }
        }

        // 2. Replace transaction items
        deleteTransactionItemsByTxId(tx.id)
        val validNewItems = updatedItems.filter { it.quantity > 0 }
        val newEntities = validNewItems.map {
            val sub = it.sellingPriceSnapshot * it.quantity
            val profit = (it.sellingPriceSnapshot - it.costPriceSnapshot) * it.quantity
            TransactionItemEntity(
                transactionId = tx.id,
                productId = it.productId,
                productNameSnapshot = it.productNameSnapshot,
                costPriceSnapshot = it.costPriceSnapshot,
                sellingPriceSnapshot = it.sellingPriceSnapshot,
                quantity = it.quantity,
                subtotal = sub,
                profit = profit
            )
        }
        insertTransactionItems(newEntities)

        // 3. Recalculate Transaction Total
        val newSubtotal = newEntities.sumOf { it.subtotal }
        val newTotal = (newSubtotal - tx.discount).coerceAtLeast(0L)

        updateTransaction(
            tx.copy(
                subtotal = newSubtotal,
                total = newTotal,
                updatedAt = System.currentTimeMillis()
            )
        )

        // 4. Recalculate Debt amounts
        val paidSoFar = (debt.amount - debt.remainingAmount).coerceAtLeast(0L)
        val newRemaining = (newTotal - paidSoFar).coerceAtLeast(0L)
        val newStatus = when {
            newRemaining == 0L -> "PAID"
            paidSoFar > 0L -> "PARTIALLY_PAID"
            else -> "UNPAID"
        }

        updateDebt(
            debt.copy(
                amount = newTotal,
                remainingAmount = newRemaining,
                status = newStatus,
                paidAt = if (newRemaining == 0L) System.currentTimeMillis() else debt.paidAt
            )
        )

        if (newStatus == "PAID") {
            updateTransaction(
                tx.copy(
                    subtotal = newSubtotal,
                    total = newTotal,
                    status = "COMPLETED",
                    cashReceived = newTotal,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        return true
    }

    // --- ATOMIC TRANSACTION: CANCEL / RESTORE SALE ---
    @Transaction
    suspend fun cancelTransactionAtomic(transactionId: Long) {
        val tx = getTransactionById(transactionId) ?: return
        if (tx.status == "CANCELLED") return // already cancelled

        // Mark transaction as CANCELLED
        updateTransactionStatus(transactionId, "CANCELLED")

        // Restore stocks
        val items = getTransactionItemsSync(transactionId)
        for (item in items) {
            val product = getProductById(item.productId)
            if (product != null) {
                val previousStock = product.stock
                val newStock = previousStock + item.quantity
                updateProductStock(product.id, newStock, System.currentTimeMillis())

                insertStockMovement(
                    StockMovementEntity(
                        productId = product.id,
                        type = "CANCELLED_SALE",
                        quantity = item.quantity,
                        previousStock = previousStock,
                        newStock = newStock,
                        transactionId = transactionId,
                        note = "Pembatalan transaksi #${tx.transactionNumber}"
                    )
                )
            }
        }

        // Delete associated income, debt, and change record
        deleteIncomeByTransactionId(transactionId)
        deleteDebtByTransactionId(transactionId)
        deleteChangeRecordByTransactionId(transactionId)
    }

    // --- ATOMIC TRANSACTION: DELETE SALE COMPLETELY ---
    @Transaction
    suspend fun deleteTransactionAtomic(transactionId: Long) {
        val tx = getTransactionById(transactionId) ?: return
        // If not already cancelled, restore stock first
        if (tx.status != "CANCELLED") {
            val items = getTransactionItemsSync(transactionId)
            for (item in items) {
                val product = getProductById(item.productId)
                if (product != null) {
                    val previousStock = product.stock
                    val newStock = previousStock + item.quantity
                    updateProductStock(product.id, newStock, System.currentTimeMillis())

                    insertStockMovement(
                        StockMovementEntity(
                            productId = product.id,
                            type = "CANCELLED_SALE",
                            quantity = item.quantity,
                            previousStock = previousStock,
                            newStock = newStock,
                            transactionId = transactionId,
                            note = "Hapus transaksi #${tx.transactionNumber}"
                        )
                    )
                }
            }
        }

        deleteIncomeByTransactionId(transactionId)
        deleteDebtByTransactionId(transactionId)
        deleteChangeRecordByTransactionId(transactionId)
        deleteTransactionItemsByTxId(transactionId)
        deleteTransactionRaw(transactionId)
    }

    // --- ATOMIC STOCK ADJUSTMENT ---
    @Transaction
    suspend fun adjustStockAtomic(
        productId: Long,
        qtyChange: Int, // positive for add, negative for reduce
        type: String, // RESTOCK, MANUAL_ADD, MANUAL_REMOVE, ADJUSTMENT
        note: String,
        recordExpense: Boolean,
        expenseAmount: Long
    ) {
        val product = getProductById(productId) ?: return
        val previousStock = product.stock
        val newStock = (previousStock + qtyChange).coerceAtLeast(0)
        updateProductStock(productId, newStock, System.currentTimeMillis())

        insertStockMovement(
            StockMovementEntity(
                productId = productId,
                type = type,
                quantity = qtyChange,
                previousStock = previousStock,
                newStock = newStock,
                note = note
            )
        )

        if (recordExpense && expenseAmount > 0) {
            insertExpense(
                ExpenseEntity(
                    category = "Belanja Stok",
                    amount = expenseAmount,
                    note = if (note.isNotBlank()) "Restock: ${product.name} ($note)" else "Restock: ${product.name} (+$qtyChange ${product.unit})"
                )
            )
        }
    }

    // --- CATATAN KEMBALIAN (CHANGE RECORDS) ---
    @Query("SELECT * FROM change_records ORDER BY CASE WHEN status = 'PENDING' THEN 0 ELSE 1 END, createdAt DESC")
    fun getAllChangeRecords(): Flow<List<ChangeRecordEntity>>

    @Query("SELECT * FROM change_records WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingChangeRecords(): Flow<List<ChangeRecordEntity>>

    @Query("SELECT * FROM change_records WHERE id = :id")
    suspend fun getChangeRecordById(id: Long): ChangeRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeRecord(record: ChangeRecordEntity): Long

    @Update
    suspend fun updateChangeRecord(record: ChangeRecordEntity)

    @Query("UPDATE change_records SET status = 'PAID', paidAt = :paidAt WHERE id = :id")
    suspend fun markChangeAsPaid(id: Long, paidAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM change_records WHERE id = :id")
    suspend fun deleteChangeRecord(id: Long)

    @Query("DELETE FROM change_records WHERE transactionId = :txId")
    suspend fun deleteChangeRecordByTransactionId(txId: Long)

    @Query("DELETE FROM change_records")
    suspend fun clearChangeRecords()

    // --- LAPORAN KERUGIAN (LOSS RECORDS) ---
    @Query("SELECT * FROM loss_records ORDER BY createdAt DESC")
    fun getAllLossRecords(): Flow<List<LossRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLossRecord(record: LossRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLossRecords(records: List<LossRecordEntity>)

    @Query("DELETE FROM loss_records")
    suspend fun clearLossRecords()

    @Transaction
    suspend fun recordLossAtomic(
        productId: Long,
        quantity: Int,
        reason: String,
        note: String = ""
    ): Boolean {
        val product = getProductById(productId) ?: return false
        val costPrice = product.costPrice
        val totalLoss = quantity.toLong() * costPrice
        val previousStock = product.stock
        val newStock = (previousStock - quantity).coerceAtLeast(0)

        updateProductStock(productId, newStock, System.currentTimeMillis())

        insertStockMovement(
            StockMovementEntity(
                productId = productId,
                type = if (reason.contains("Kadaluarsa", ignoreCase = true)) "EXPIRED" else "DAMAGE",
                quantity = -quantity,
                previousStock = previousStock,
                newStock = newStock,
                note = if (note.isNotBlank()) "$reason ($note)" else reason
            )
        )

        insertLossRecord(
            LossRecordEntity(
                productId = productId,
                productName = product.name,
                quantity = quantity,
                costPrice = costPrice,
                totalLoss = totalLoss,
                reason = reason,
                date = System.currentTimeMillis()
            )
        )
        return true
    }

    @Query("SELECT * FROM loss_records WHERE id = :id LIMIT 1")
    suspend fun getLossRecordById(id: Long): LossRecordEntity?

    @Query("DELETE FROM loss_records WHERE id = :id")
    suspend fun deleteLossRecord(id: Long)

    @Transaction
    suspend fun cancelLossAtomic(
        lossId: Long,
        restoreStock: Boolean = true
    ): Boolean {
        val loss = getLossRecordById(lossId) ?: return false

        // 1. Kembalikan stok barang jika diminta dan produk masih tersedia di database
        if (restoreStock) {
            val product = getProductById(loss.productId)
            if (product != null) {
                val previousStock = product.stock
                val newStock = previousStock + loss.quantity
                updateProductStock(product.id, newStock, System.currentTimeMillis())

                insertStockMovement(
                    StockMovementEntity(
                        productId = product.id,
                        type = "RESTORE_LOSS",
                        quantity = loss.quantity,
                        previousStock = previousStock,
                        newStock = newStock,
                        note = "Pembatalan kerugian (${loss.reason})"
                    )
                )
            }
        }

        // 2. Masukkan dana kerugian kembali ke catatan kas (pemasukan)
        insertIncome(
            IncomeEntity(
                source = "Pembatalan Kerugian",
                amount = loss.totalLoss,
                note = "Pengembalian dana rugi ${loss.productName} (${loss.quantity} pcs)",
                date = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
        )

        // 3. Hapus catatan kerugian
        deleteLossRecord(lossId)
        return true
    }

    // --- PROMOS ---
    @Query("SELECT * FROM promos ORDER BY createdAt DESC")
    fun getAllPromos(): Flow<List<PromoEntity>>

    @Query("SELECT * FROM promos WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActivePromos(): Flow<List<PromoEntity>>

    @Query("SELECT * FROM promos WHERE id = :id LIMIT 1")
    suspend fun getPromoById(id: Long): PromoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromo(promo: PromoEntity): Long

    @Update
    suspend fun updatePromo(promo: PromoEntity)

    @Query("UPDATE promos SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePromoActive(id: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM promos WHERE id = :id")
    suspend fun deletePromo(id: Long)

    @Query("DELETE FROM promos")
    suspend fun clearPromos()

    // --- RESET ALL DATA (FOR SETTINGS OPTION) ---
    @Query("DELETE FROM transaction_items")
    suspend fun clearTransactionItems()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM stock_movements")
    suspend fun clearStockMovements()

    @Query("DELETE FROM incomes")
    suspend fun clearIncomes()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM debts")
    suspend fun clearDebts()

    // --- CATATAN TOKO (NOTES) ---
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    @Transaction
    suspend fun resetAllDataAtomic() {
        clearTransactionItems()
        clearTransactions()
        clearDebts()
        clearChangeRecords()
        clearLossRecords()
        clearStockMovements()
        clearIncomes()
        clearExpenses()
        clearProducts()
        clearCategories()
        clearPromos()
        clearNotes()
    }
}
