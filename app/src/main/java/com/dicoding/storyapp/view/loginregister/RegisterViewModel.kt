package com.dicoding.storyapp.ui.loginregister

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.data.model.RegisterResponse
import kotlinx.coroutines.flow.Flow

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun register(name: String, email: String, password: String): Flow<Result<RegisterResponse>> {
        return userRepository.register(name, email, password)
    }
}
