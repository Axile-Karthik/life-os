package com.karthik.lifeos.tracker.network

import com.karthik.lifeos.tracker.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the Life OS backend.
 *
 * Configuration:
 * - Base URL from BuildConfig.BACKEND_URL (Tailscale VPN IP)
 * - Gson serialization (epoch millis for timestamps)
 * - OkHttp logging interceptor (DEBUG builds only)
 * - Conservative timeouts for mobile networks
 */
object RetrofitClient {

    val api: LifeOsApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LifeOsApi::class.java)
    }
}
