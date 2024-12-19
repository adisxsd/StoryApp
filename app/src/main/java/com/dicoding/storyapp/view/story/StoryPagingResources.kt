package com.dicoding.storyapp.view.story

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.remote.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, ListStoryItem>() {

    companion object {
        private const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val formattedToken = "Bearer $token"

            val response = apiService.getStories(
                token = formattedToken,
                page = position,
                size = params.loadSize
            )

            val filteredData = response.listStory?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = filteredData,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (filteredData.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }


    fun invalidateAndReload() {
        invalidate()
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
