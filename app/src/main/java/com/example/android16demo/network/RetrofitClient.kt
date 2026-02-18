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

    private var currentBaseUrl: String = normalizeBaseUrl(LifeAppApi.BASE_URL)
    private var retrofit: Retrofit? = null
    private var api: LifeAppApi? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Synchronized
    fun getApi(baseUrl: String? = null): LifeAppApi {
        val targetBaseUrl = normalizeBaseUrl(baseUrl ?: currentBaseUrl)
        if (api == null || retrofit == null || targetBaseUrl != currentBaseUrl) {
            currentBaseUrl = targetBaseUrl
            retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            api = retrofit!!.create(LifeAppApi::class.java)
        }
        return api!!
    }

    @Synchronized
    fun setBaseUrl(url: String) {
        val normalized = normalizeBaseUrl(url)
        if (normalized != currentBaseUrl) {
            currentBaseUrl = normalized
            retrofit = null
            api = null
        }
    }

    private fun normalizeBaseUrl(url: String): String {
        val trimmed = url.trim().ifEmpty { LifeAppApi.BASE_URL }
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }
}
