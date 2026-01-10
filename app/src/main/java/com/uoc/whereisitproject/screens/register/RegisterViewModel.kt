package com.uoc.whereisitproject.screens.register

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uoc.whereisitproject.repository.AuthRepository
import com.uoc.whereisitproject.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var profileImageUri by mutableStateOf<Uri?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onFirstNameChange(value: String) { firstName = value }
    fun onLastNameChange(value: String) { lastName = value }
    fun onUsernameChange(value: String) { username = value }
    fun onEmailChange(value: String) { email = value }
    fun onPasswordChange(value: String) { password = value }
    fun onImageSelected(uri: Uri?) { profileImageUri = uri }

    fun register(onSuccess: () -> Unit, errorFillFieldsText: String) {
        if (
            firstName.isBlank() ||
            lastName.isBlank() ||
            username.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            profileImageUri == null
        ) {
            errorMessage = errorFillFieldsText
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                val userId = authRepository.register(email, password)
                userRepository.createUserProfile(
                    userId,
                    firstName,
                    lastName,
                    username,
                    email,
                    profileImageUri!!
                )
            }.onSuccess {
                onSuccess()
            }.onFailure {
                errorMessage = it.message
            }

            isLoading = false
        }
    }
}
