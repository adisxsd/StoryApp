package com.dicoding.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.storyapp.data.local.UserPreference
import com.dicoding.storyapp.data.model.AddStoryResponse
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.remote.ApiService
import com.dicoding.storyapp.utils.UiState
import com.dicoding.storyapp.view.story.StoryPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(
    val apiService: ApiService,
    private val userPreference: UserPreference
) {

    fun getStories(token: String): Flow<PagingData<ListStoryItem>> {
        return userPreference.getToken().map { token ->
            Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = {
                    if (!token.isNullOrEmpty()) {
                        StoryPagingSource(apiService, token)
                    } else {
                        throw IllegalStateException("Token tidak valid atau kosong")
                    }
                }
            ).flow
        }.flattenMerge(1)
    }



    // Upload story with additional data (photo, lat, lon)
    suspend fun uploadStory(
        description: String,
        photo: File,
        lat: Double?,
        lon: Double?,
        authHeader: String
    ): AddStoryResponse {

        val photoRequestBody = photo.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photo.name, photoRequestBody)
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lonRequestBody = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.addStory(authHeader, descriptionRequestBody, photoPart, latRequestBody, lonRequestBody).body()!!
    }

    // Get stories with location (not using Paging)
    suspend fun getStoryWithLocation(token: String): LiveData<UiState<List<ListStoryItem>>> {
        val result = MutableLiveData<UiState<List<ListStoryItem>>>()

        result.value = UiState.Loading

        try {
            val response = apiService.getStoriesWithLocation("Bearer $token", location = 1)

            val stories = response.listStory?.filterNotNull()?.distinctBy { it.id } ?: emptyList()

            result.value = UiState.Success(stories)
        } catch (e: Exception) {
            // Emit Error state in case of failure
            result.value = UiState.Error(e.message ?: "Unknown error")
            e.printStackTrace() // Log the error
        }

        return result
    }
}
