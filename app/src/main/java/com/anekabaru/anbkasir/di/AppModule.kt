package com.anekabaru.anbkasir.di

import android.app.Application
import androidx.room.Room
import com.anekabaru.anbkasir.data.AppDatabase
import com.anekabaru.anbkasir.data.PosDao
import com.anekabaru.anbkasir.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "pos-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePosDao(db: AppDatabase): PosDao = db.posDao()

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        // REPLACE WITH YOUR REAL VERCEL URL
        val baseUrl = "https://anbkasir-backend.vercel.app/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}