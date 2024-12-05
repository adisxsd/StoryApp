package com.dicoding.storyapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.remote.ApiConfig
import com.dicoding.storyapp.data.di.Injection
import com.dicoding.storyapp.view.login.LoginViewModel
import com.dicoding.storyapp.ui.loginregister.RegisterViewModel
import com.dicoding.storyapp.view.addstory.AddStoryViewModel
import com.dicoding.storyapp.view.story.StoryViewModel
import com.dicoding.storyapp.view.detailStory.StoryDetailViewModel

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
                StoryViewModel(Injection.provideStoryRepository(context)) as T
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

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        fun getInstance(context: Context): ViewModelFactory {
            return ViewModelFactory(context)
        }
    }
}
