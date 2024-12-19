package com.dicoding.storyapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.di.Injection
import com.dicoding.storyapp.data.remote.ApiConfig
import com.dicoding.storyapp.ui.loginregister.RegisterViewModel
import com.dicoding.storyapp.view.addstory.AddStoryViewModel
import com.dicoding.storyapp.view.detailStory.StoryDetailViewModel
import com.dicoding.storyapp.view.login.LoginViewModel
import com.dicoding.storyapp.view.maps.MapsViewModel
import com.dicoding.storyapp.view.story.StoryPagingSource
import com.dicoding.storyapp.view.story.StoryViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(Injection.provideRepository(context)) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(Injection.provideRepository(context)) as T
            }
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                val token = getTokenFromSharedPreferences(context)
                val storyRepository = Injection.provideStoryRepository(context)
                val storyPagingSource = StoryPagingSource(storyRepository.apiService, token)
                StoryViewModel(storyRepository, storyPagingSource) as T
            }
            modelClass.isAssignableFrom(StoryDetailViewModel::class.java) -> {
                val apiService = ApiConfig.getApiService()
                StoryDetailViewModel(apiService, Injection.provideRepository(context)) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(
                    storyRepository = Injection.provideStoryRepository(context),
                    userRepository = Injection.provideRepository(context)
                ) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(
                    storyRepository = Injection.provideStoryRepository(context),
                    userRepository = Injection.provideRepository(context)
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // Contoh fungsi untuk mengambil token dari SharedPreferences
    private fun getTokenFromSharedPreferences(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_token", "") ?: ""
    }

    companion object {
        fun getInstance(context: Context): ViewModelFactory {
            return ViewModelFactory(context)
        }
    }
}

