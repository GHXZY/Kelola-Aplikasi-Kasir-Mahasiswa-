package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["name"]),
        Index(value = ["barcode"], unique = true)
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val costPrice: Long, // HPP (Harga Modal) in IDR
    val sellingPrice: Long, // Harga Jual in IDR
    val stock: Int,
    val minimumStock: Int = 5,
    val unit: String = "pcs", // pcs, botol, bungkus, gelas, porsi
    val imageUri: String? = null,
    val barcode: String? = null, // Barcode EAN-13 / Code 128 (unik per produk aktif)
    val expirationDate: Long? = null, // Tanggal Kadaluarsa (timestamp ms), null jika tidak ada
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false // Soft delete to preserve transaction history
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["transactionNumber"], unique = true), Index(value = ["createdAt"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionNumber: String,
    val subtotal: Long,
    val discount: Long = 0,
    val promoId: Long? = null,
    val promoName: String? = null,
    val promoDiscount: Long = 0,
    val total: Long,
    val paymentMethod: String = "Tunai", // Tunai, QRIS, Transfer, E-Wallet, Bayar Nanti, Lainnya
    val customerName: String = "",
    val cashReceived: Long = 0,
    val change: Long = 0,
    val status: String = "COMPLETED", // COMPLETED, CANCELLED, UNPAID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transactionId"]), Index(value = ["productId"])]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val costPriceSnapshot: Long,
    val sellingPriceSnapshot: Long,
    val quantity: Int,
    val subtotal: Long,
    val profit: Long // (sellingPriceSnapshot - costPriceSnapshot) * quantity
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["date"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Belanja Stok, Transportasi, Kemasan, Listrik, Sewa, Makan, Lainnya
    val amount: Long,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "incomes",
    indices = [Index(value = ["date"]), Index(value = ["transactionId"])]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String, // Penjualan, Tambahan Modal, Pendapatan Lain
    val amount: Long,
    val note: String = "",
    val transactionId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_movements",
    indices = [Index(value = ["productId"]), Index(value = ["createdAt"])]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: String, // SALE, RESTOCK, MANUAL_ADD, MANUAL_REMOVE, ADJUSTMENT, CANCELLED_SALE
    val quantity: Int, // + or -
    val previousStock: Int,
    val newStock: Int,
    val transactionId: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["customerName"]),
        Index(value = ["status"]),
        Index(value = ["transactionId"]),
        Index(value = ["createdAt"])
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val transactionNumber: String = "",
    val customerName: String,
    val customerPhone: String = "",
    val amount: Long,              // Total hutang awal
    val remainingAmount: Long,     // Sisa hutang belum dibayar
    val status: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID
    val note: String = "",
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val settlementPaymentMethod: String? = null
)

@Entity(
    tableName = "change_records",
    indices = [
        Index(value = ["status"]),
        Index(value = ["buyerName"]),
        Index(value = ["transactionId"]),
        Index(value = ["createdAt"])
    ]
)
data class ChangeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long? = null,
    val transactionNumber: String = "",
    val buyerName: String,
    val amount: Long,
    val status: String = "PENDING", // PENDING, PAID
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
)

@Entity(
    tableName = "loss_records",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["reason"]),
        Index(value = ["date"]),
        Index(value = ["createdAt"])
    ]
)
data class LossRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val costPrice: Long, // HPP modal!
    val totalLoss: Long, // quantity * costPrice
    val reason: String = "Kadaluarsa",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "promos",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["createdAt"])
    ]
)
data class PromoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = true,
    val discountType: String = "NOMINAL", // "NOMINAL", "PERCENTAGE", "FREE_PRODUCT"
    val discountValue: Long = 0L,
    val maxUsage: Int = 0, // 0 = unlimited / kelipatan tanpa batas
    val requiredItemsJson: String = "[]", // JSON array of [{"productId":1,"quantity":1}]
    val freeProductId: Long? = null, // ID produk gratis jika discountType == "FREE_PRODUCT"
    val freeQuantity: Int = 1, // Kuantitas produk gratis per paket promo
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["category"])
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Umum", // Umum, Transaksi, Stok, Kembalian, Keuangan
    val relatedEntityId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

