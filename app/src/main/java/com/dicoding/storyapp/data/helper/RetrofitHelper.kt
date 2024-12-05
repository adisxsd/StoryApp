package com.dicoding.storyapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    private const val BASE_URL = "https://story-api.dicoding.dev/v1/"  // Replace with your API base URL

    // Retrofit instance
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // To handle JSON conversion
            .client(OkHttpClient()) // Optional: If you want to add OkHttp logging or interceptors
            .build()
    }

    // Method to get the ApiService instance
    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
