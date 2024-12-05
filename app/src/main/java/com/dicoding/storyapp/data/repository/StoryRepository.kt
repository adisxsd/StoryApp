package com.dicoding.storyapp.data.repository

import com.dicoding.storyapp.data.model.AddStoryResponse
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.model.StoryResponse
import com.dicoding.storyapp.data.remote.ApiService
import okhttp3.MultipartBody
import retrofit2.Response

class StoryRepository(private val apiService: ApiService) {


    suspend fun getStories(token: String): List<ListStoryItem?> {
        val storyResponse: StoryResponse = apiService.getStories("Bearer $token")


        return storyResponse.listStory ?: emptyList()
    }
    suspend fun addStory(
        token: String,
        description: String, // Make sure description is a String here
        photo: MultipartBody.Part
    ): Response<AddStoryResponse> {
        return apiService.addStory("Bearer $token", description, photo.toString())
    }

}