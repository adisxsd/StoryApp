package com.dicoding.storyapp.data.di

import android.content.Context
import com.dicoding.storyapp.data.local.UserPreference
import com.dicoding.storyapp.data.remote.ApiConfig
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.repository.UserRepository

object Injection {

    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference(context)
        val apiService = ApiConfig.getApiService()
        return UserRepository(apiService, pref)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val userPreference = UserPreference(context)  // Use UserPreference directly
        val apiService = ApiConfig.getApiService()
        return StoryRepository(apiService, userPreference)  // Provide both apiService and userPreference
    }
}


