package com.example.android16demo.network

import com.example.android16demo.network.api.LifeAppApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client configuration for Life App API
 */
object RetrofitClient {
    
    private var baseUrl: String = LifeAppApi.BASE_URL
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api: LifeAppApi by lazy {
        retrofit.create(LifeAppApi::class.java)
    }
    
    /**
     * Update base URL (useful for testing or configuration)
     */
    fun setBaseUrl(url: String) {
        baseUrl = url
    }
}
