package com.dicoding.storyapp.data.remote
import com.dicoding.storyapp.data.model.AddStoryResponse
import com.dicoding.storyapp.data.model.DetailResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import com.dicoding.storyapp.data.model.RegisterResponse
import com.dicoding.storyapp.data.model.LoginResponse
import com.dicoding.storyapp.data.model.StoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String
    ): StoryResponse


    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Header("Authorization") token: String,
        @Field("name") name: String,
        @Field("description") description: String,
        @Field("photoUrl") photoUrl: String?
    ): Response<AddStoryResponse>

    @GET("stories/{id}")
    suspend fun getStoryDetail(@Path("id") storyId: String, @Header("Authorization") token: String): retrofit2.Response<DetailResponse>
    abstract fun addStory(token: String, name: String, description: String): Response<AddStoryResponse>

}
