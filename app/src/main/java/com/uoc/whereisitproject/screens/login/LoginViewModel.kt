package com.uoc.whereisitproject.screens.login

import android.util.Patterns
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uoc.whereisitproject.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(email: String, invalidEmailText: String) {
        this.email = email
        emailError =
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                invalidEmailText
            else null
    }

    fun onPasswordChange(password: String) {
        this.password = password
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                authRepository.login(email, password)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                errorMessage = it.message
            }

            isLoading = false
        }
    }
}