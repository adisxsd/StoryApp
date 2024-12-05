package com.dicoding.storyapp.view.addstory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.remote.RetrofitHelper
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.utils.Result
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AddStoryViewModel(application: StoryRepository, s: String) : AndroidViewModel(Application()) {

    val apiService = RetrofitHelper.getApiService()
    private val storyRepository = StoryRepository(apiService)

    val addStoryResponse = MutableLiveData<Result<String>>()

    fun addStory(token: String, description: String, photo: MultipartBody.Part) {
        addStoryResponse.postValue(Result.Loading)

        viewModelScope.launch {
            try {
                val response = storyRepository.addStory(token, description, photo)
                if (response.isSuccessful) {
                    addStoryResponse.postValue(Result.Success("success"))
                } else {
                    addStoryResponse.postValue(Result.Error(Exception("Failed to upload story")))
                }
            } catch (e: Exception) {
                addStoryResponse.postValue(Result.Error(e))
            }
        }
    }
}
