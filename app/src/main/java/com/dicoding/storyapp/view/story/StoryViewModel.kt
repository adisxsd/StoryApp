package com.dicoding.storyapp.view.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.utils.Result

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun getStories(token: String) = liveData<Result<List<ListStoryItem?>>>(context = viewModelScope.coroutineContext) {
        emit(Result.Loading)
        try {
            val response = storyRepository.getStories(token)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
