package com.dicoding.storyapp.di

import android.content.Context
import com.dicoding.storyapp.data.local.UserPreference
import com.dicoding.storyapp.data.remote.ApiConfig
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.repository.UserRepository

object Injection {

    // Provide UserRepository
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference(context)

        val apiService = ApiConfig.getApiService()

        return UserRepository(apiService, pref)  // Return UserRepository with ApiService and UserPreference
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val userRepository = provideRepository(context)  // Get UserRepository for token
        val apiService = ApiConfig.getApiService()  // API service without token here
        return StoryRepository(apiService)  // Return StoryRepository
    }

}
