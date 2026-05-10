package com.example.fallguard.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ---------------------------------------------------------
// Central Retrofit builder.
// Emulator uses 10.0.2.2 to reach localhost on your machine.
// ---------------------------------------------------------

object ApiProvider {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: BackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }
}