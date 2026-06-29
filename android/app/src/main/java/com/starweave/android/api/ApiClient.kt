package com.starweave.android.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Change this to your server IP/domain
    var BASE_URL = "http://192.168.110.224:8080/api/"

    /** 当前 JWT token，由 AuthViewModel 在登录/注册成功后设置 */
    @Volatile
    var token: String? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            // 添加 JWT Authorization 头（如果已登录）
            token?.let { t ->
                builder.addHeader("Authorization", "Bearer $t")
            }
            chain.proceed(builder.build())
        }
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    fun getService(): ApiService {
        if (apiService == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService!!
    }

    fun setBaseUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        BASE_URL = normalized
        retrofit = null
        apiService = null
    }
}
