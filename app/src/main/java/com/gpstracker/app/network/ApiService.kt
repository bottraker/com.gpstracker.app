package com.gpstracker.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // ¡¡¡CAMBIA ESTA URL POR LA DE TU SERVIDOR!!!
    private const val BASE_URL = "https://tu-servidor.com/api/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    fun create(): ApiService = retrofit.create(ApiService::class.java)
}
