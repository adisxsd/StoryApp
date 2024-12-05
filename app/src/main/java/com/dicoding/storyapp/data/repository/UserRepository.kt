package com.dicoding.storyapp.data.repository
import com.dicoding.storyapp.data.local.UserPreference
import com.dicoding.storyapp.data.model.LoginResponse
import com.dicoding.storyapp.data.model.RegisterResponse
import com.dicoding.storyapp.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UserRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference) {


    fun register(name: String, email: String, password: String): Flow<Result<RegisterResponse>> = flow {
        val response = apiService.register(name, email, password)
        emit(Result.success(response))
    }.catch { e ->
        emit(Result.failure(e))
    }

    fun login(email: String, password: String): Flow<Result<LoginResponse>> = flow {
        val response = apiService.login(email, password)
        emit(Result.success(response))
    }.catch { e ->
        emit(Result.failure(e))
    }

    suspend fun saveToken(token: String) {
        userPreference.saveToken(token)
    }

    fun getToken(): Flow<String?> {
        return userPreference.getToken()
    }

    suspend fun clearToken() {
        userPreference.clearToken()
    }
}