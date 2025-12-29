package com.anekabaru.anbkasir.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, TransactionEntity::class, TransactionItemEntity::class, SupplierEntity::class, PurchaseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao
}