package com.anekabaru.anbkasir.data.workers

import android.content.Context
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

    // 1. Define an EntryPoint to access Hilt dependencies
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun getRepository(): PosRepository
    }

    override suspend fun doWork(): Result {
        return try {
            // 2. Use EntryPointAccessors to get the injected Repository
            // This automatically handles the Database and API connections for us
            val appContext = applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                SyncWorkerEntryPoint::class.java
            )
            val repository = entryPoint.getRepository()

            // 3. Run the sync
            repository.syncData()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}