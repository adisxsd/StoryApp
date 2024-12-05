package com.dicoding.storyapp.data.repository

import com.dicoding.storyapp.data.model.AddStoryResponse
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.model.StoryResponse
import com.dicoding.storyapp.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(private val apiService: ApiService) {


    suspend fun getStories(token: String): List<ListStoryItem?> {
        val storyResponse: StoryResponse = apiService.getStories("Bearer $token")
        return storyResponse.listStory ?: emptyList()
    }
    suspend fun uploadStory(description: String, photo: File, authHeader: String): AddStoryResponse {
        val photoRequestBody = photo.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photo.name, photoRequestBody)
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.addStory(authHeader, descriptionRequestBody, photoPart).body()!!
    }





}
