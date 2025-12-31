package com.anekabaru.anbkasir.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class // Pastikan ini ada karena di Entities.kt sudah ditambahkan
    ],
    version = 6, // Versi dinaikkan
    exportSchema = false
)
// Gunakan 'Converters' sesuai nama class di file Entities.kt yang saya berikan sebelumnya
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anb_kasir_db"
                )
                    // PENTING: Opsi ini akan menghapus database lama & membuat baru jika versi berubah.
                    // Sangat disarankan saat Development/Perubahan struktur tabel yang kompleks.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}