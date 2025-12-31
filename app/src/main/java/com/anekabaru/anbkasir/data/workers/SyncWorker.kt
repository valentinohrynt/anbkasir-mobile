package com.anekabaru.anbkasir.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anekabaru.anbkasir.data.PosRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun getRepository(): PosRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val appContext = applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                SyncWorkerEntryPoint::class.java
            )
            val repository = entryPoint.getRepository()

            // Menjalankan sinkronisasi data secara agresif (Push & Pull)
            repository.syncData()

            Log.d("SyncWorker", "Sinkronisasi otomatis berhasil dijalankan")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sinkronisasi otomatis gagal: ${e.message}")
            // Menginstruksikan WorkManager untuk mencoba lagi berdasarkan kriteria backoff
            Result.retry()
        }
    }
}