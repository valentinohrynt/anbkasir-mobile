package com.anekabaru.anbkasir.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // REPLACE THIS with your actual Vercel Domain
    // Example: "https://anb-kasir-app.vercel.app/"
    private const val BASE_URL = "https://anbkasir-backend.vercel.app/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}