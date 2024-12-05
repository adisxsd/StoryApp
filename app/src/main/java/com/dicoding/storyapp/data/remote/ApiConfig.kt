package com.dicoding.storyapp.data.remote

import com.dicoding.storyapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private const val BASE_URL = BuildConfig.BASE_URL

    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY  // Log full request/response
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)  // Add logging interceptor to OkHttp
            .build()
    }

    // Retrofit instance builder
    private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)  // Pass the OkHttpClient
            .build()
    }

    fun getApiService(token: String? = null): ApiService {
        val okHttpClient = provideOkHttpClient()

        // If token is provided, add Authorization header to the request
        val client = if (token != null) {
            okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")  // Add Bearer token if available
                        .build()
                    chain.proceed(request)
                }
                .build()
        } else {
            okHttpClient
        }

        val retrofit = provideRetrofit(client)
        return retrofit.create(ApiService::class.java)  // Create the ApiService instance
    }
}
