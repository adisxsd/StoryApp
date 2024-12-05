package com.dicoding.storyapp.view.detailStory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.model.DetailResponse
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.data.remote.ApiService
import com.dicoding.storyapp.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response

class StoryDetailViewModel(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _storyDetail = MutableLiveData<Story?>()
    val storyDetail: LiveData<Story?> get() = _storyDetail

    fun loadStoryDetail(storyId: String) {
        viewModelScope.launch {
            try {
                val token = userRepository.getToken().first()
                if (!token.isNullOrEmpty()) {
                    val response: Response<DetailResponse> = apiService.getStoryDetail(storyId, "Bearer $token")
                    if (response.isSuccessful) {
                        val story = response.body()?.story
                        _storyDetail.postValue(story)
                    } else {
                        _storyDetail.postValue(null)
                    }
                } else {
                    _storyDetail.postValue(null)
                }
            } catch (e: Exception) {
                _storyDetail.postValue(null)
                Log.e("StoryDetailViewModel", "Error fetching story detail", e)
            }
        }
    }
}
