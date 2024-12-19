package com.dicoding.storyapp.view.maps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.utils.UiState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MapsViewModel(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // MutableLiveData to handle UI state
    val stories: MutableLiveData<UiState<List<ListStoryItem>>> = MutableLiveData()

    init {
        fetchStories()
    }

    private fun fetchStories() {
        // Set initial loading state
        stories.value = UiState.Loading

        viewModelScope.launch {
            try {
                val token = userRepository.getToken().firstOrNull()

                // Check if token is null
                if (token.isNullOrEmpty()) {
                    stories.value = UiState.Error("Token is null or empty")
                    return@launch
                }

                // Fetch stories with location using the token
                storyRepository.getStoryWithLocation(token).observeForever { uiState ->
                    stories.value = uiState
                }
            } catch (e: Exception) {
                // In case of error, set the error state
                stories.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}


