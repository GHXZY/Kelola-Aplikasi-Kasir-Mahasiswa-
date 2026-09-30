package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.PosDao
import com.example.data.local.entity.CustomerEntity
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
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        StockMovementEntity::class,
        DebtEntity::class,
        ChangeRecordEntity::class,
        LossRecordEntity::class,
        PromoEntity::class,
        NoteEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun safeAddColumn(db: SupportSQLiteDatabase, table: String, column: String, definition: String) {
            var cursor: android.database.Cursor? = null
            try {
                cursor = db.query("PRAGMA table_info(`$table`)")
                var exists = false
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex != -1 && cursor.getString(nameIndex).equals(column, ignoreCase = true)) {
                        exists = true
                        break
                    }
                }
                if (!exists) {
                    db.execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $definition")
                }
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
        }

        private fun migrateV3ToV4(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `promos` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    `discountType` TEXT NOT NULL,
                    `discountValue` INTEGER NOT NULL,
                    `maxUsage` INTEGER NOT NULL,
                    `requiredItemsJson` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_promos_isActive` ON `promos` (`isActive`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_promos_createdAt` ON `promos` (`createdAt`)")
            safeAddColumn(db, "transactions", "promoId", "INTEGER DEFAULT NULL")
            safeAddColumn(db, "transactions", "promoName", "TEXT DEFAULT NULL")
            safeAddColumn(db, "transactions", "promoDiscount", "INTEGER NOT NULL DEFAULT 0")
        }

        private fun migrateV4ToV5(db: SupportSQLiteDatabase) {
            safeAddColumn(db, "promos", "freeProductId", "INTEGER DEFAULT NULL")
            safeAddColumn(db, "promos", "freeQuantity", "INTEGER NOT NULL DEFAULT 1")
        }

        private fun migrateV5ToV6(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `relatedEntityId` INTEGER DEFAULT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_createdAt` ON `notes` (`createdAt`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_category` ON `notes` (`category`)")
        }

        private fun migrateV6ToV7(db: SupportSQLiteDatabase) {
            safeAddColumn(db, "products", "barcode", "TEXT DEFAULT NULL")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_products_barcode` ON `products` (`barcode`)")
        }

        private fun migrateV7ToV8(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `customers` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `phone` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_name` ON `customers` (`name`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_createdAt` ON `customers` (`createdAt`)")
            safeAddColumn(db, "transactions", "customerId", "INTEGER DEFAULT NULL")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_customerId` ON `transactions` (`customerId`)")
            safeAddColumn(db, "debts", "customerId", "INTEGER DEFAULT NULL")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_debts_customerId` ON `debts` (`customerId`)")
            safeAddColumn(db, "change_records", "customerId", "INTEGER DEFAULT NULL")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_change_records_customerId` ON `change_records` (`customerId`)")
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateV3ToV4(db)
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateV4ToV5(db)
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateV5ToV6(db)
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateV6ToV7(db)
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateV7ToV8(db)
        }

        val MIGRATION_1_7 = object : Migration(1, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV3ToV4(db)
                migrateV4ToV5(db)
                migrateV5ToV6(db)
                migrateV6ToV7(db)
            }
        }

        val MIGRATION_2_7 = object : Migration(2, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV3ToV4(db)
                migrateV4ToV5(db)
                migrateV5ToV6(db)
                migrateV6ToV7(db)
            }
        }

        val MIGRATION_3_7 = object : Migration(3, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV3ToV4(db)
                migrateV4ToV5(db)
                migrateV5ToV6(db)
                migrateV6ToV7(db)
            }
        }

        val MIGRATION_4_7 = object : Migration(4, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV4ToV5(db)
                migrateV5ToV6(db)
                migrateV6ToV7(db)
            }
        }

        val MIGRATION_5_7 = object : Migration(5, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV5ToV6(db)
                migrateV6ToV7(db)
            }
        }

        val MIGRATION_6_8 = object : Migration(6, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV6ToV7(db)
                migrateV7ToV8(db)
            }
        }

        val MIGRATION_1_8 = object : Migration(1, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateV3ToV4(db)
                migrateV4ToV5(db)
                migrateV5ToV6(db)
                migrateV6ToV7(db)
                migrateV7ToV8(db)
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kelola_pos.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_1_7,
                        MIGRATION_2_7,
                        MIGRATION_3_7,
                        MIGRATION_4_7,
                        MIGRATION_5_7,
                        MIGRATION_6_8,
                        MIGRATION_1_8
                    )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.posDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: PosDao) {
            // Seed Categories
            val catMakanan = dao.insertCategory(CategoryEntity(name = "Makanan"))
            val catMinuman = dao.insertCategory(CategoryEntity(name = "Minuman"))
            val catSnack = dao.insertCategory(CategoryEntity(name = "Snack"))
            val catLainnya = dao.insertCategory(CategoryEntity(name = "Lainnya"))

            // Seed Sample Products as requested in product requirements
            val products = listOf(
                ProductEntity(
                    name = "Es Teh",
                    categoryId = catMinuman,
                    costPrice = 2000L,
                    sellingPrice = 5000L,
                    stock = 20,
                    minimumStock = 5,
                    unit = "gelas"
                ),
                ProductEntity(
                    name = "Roti Cokelat",
                    categoryId = catSnack,
                    costPrice = 3000L,
                    sellingPrice = 5000L,
                    stock = 15,
                    minimumStock = 5,
                    unit = "bungkus"
                ),
                ProductEntity(
                    name = "Mie Goreng",
                    categoryId = catMakanan,
                    costPrice = 6000L,
                    sellingPrice = 10000L,
                    stock = 10,
                    minimumStock = 3,
                    unit = "porsi"
                ),
                ProductEntity(
                    name = "Air Mineral",
                    categoryId = catMinuman,
                    costPrice = 2500L,
                    sellingPrice = 4000L,
                    stock = 20,
                    minimumStock = 5,
                    unit = "botol"
                ),
                ProductEntity(
                    name = "Kripik Pedas",
                    categoryId = catSnack,
                    costPrice = 3500L,
                    sellingPrice = 6000L,
                    stock = 12,
                    minimumStock = 4,
                    unit = "bungkus"
                )
            )
            dao.insertProducts(products)
        }
    }
}
