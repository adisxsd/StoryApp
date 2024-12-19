package com.dicoding.storyapp.view.story

import androidx.paging.PagingData
import com.dicoding.storyapp.data.model.ListStoryItem

object DataDummy {
    const val DUMMY_STORY_SIZE = 5

    fun generateDummyPagingData(): PagingData<ListStoryItem> {
        val stories = (1..DUMMY_STORY_SIZE).map {
            ListStoryItem(
                id = it.toString(),
                name = "Story $it",
                description = "Description $it",
                photoUrl = "https://example.com/story$it.jpg",
            )
        }
        return PagingData.from(stories)
    }
}
