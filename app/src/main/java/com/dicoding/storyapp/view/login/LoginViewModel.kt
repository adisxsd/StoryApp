package com.dicoding.storyapp.view.login

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.data.model.LoginResponse
import kotlinx.coroutines.flow.Flow

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun login(email: String, password: String): Flow<Result<LoginResponse>> {
        return userRepository.login(email, password)
    }

    suspend fun saveToken(token: String) {
        userRepository.saveToken(token)
    }
}
