package com.dicoding.storyapp.view.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch


class StoryViewModel(
    private val repository: StoryRepository,
    private val storyPagingSource: StoryPagingSource
) : ViewModel() {

    private val _pagingData = MutableLiveData<PagingData<ListStoryItem>>()
    val pagingData: LiveData<PagingData<ListStoryItem>> get() = _pagingData

    fun getStories(token: String) {
        viewModelScope.launch {
            try {
                repository.getStories(token)
                    .cachedIn(viewModelScope)
                    .collect { pagingData ->
                        _pagingData.postValue(pagingData)
                    }
            } catch (e: Exception) {
                Log.e("StoryViewModel", "Error fetching stories: ${e.message}")
                _pagingData.postValue(PagingData.empty())
            }
        }
    }


    fun reloadData() {
        storyPagingSource.invalidate()
    }

}


