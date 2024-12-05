package com.dicoding.storyapp.view.addstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.model.AddStoryResponse
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _addStoryResponse = MutableLiveData<Result<AddStoryResponse>>()
    val addStoryResponse: LiveData<Result<AddStoryResponse>> get() = _addStoryResponse

    fun uploadStory(description: String, photo: File) {
        viewModelScope.launch {
            _addStoryResponse.postValue(Result.Loading)

            try {
                val token = userRepository.getToken().first()

                // Ensure token is not null or empty
                if (token.isNullOrEmpty()) {
                    throw IllegalArgumentException("Token is missing")
                }

                val response = storyRepository.uploadStory(description, photo, "Bearer $token")

                _addStoryResponse.postValue(Result.Success(response))

            } catch (e: Exception) {
                _addStoryResponse.postValue(Result.Error(e))
            }
        }
    }
}


