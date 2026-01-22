package com.uoc.whereisitproject.screens.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uoc.whereisitproject.model.UserProfile
import com.uoc.whereisitproject.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val currentUserId: String
) : ViewModel() {

    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var showEditDialog by mutableStateOf(false)
        private set

    fun load(errorText: String) {
        viewModelScope.launch {
            loading = true
            error = null

            runCatching {
                userRepository.getUserProfile(currentUserId)
            }.onSuccess {
                profile = it
            }.onFailure {
                error = it.message ?: errorText
            }

            loading = false
        }
    }

    fun openEdit() { showEditDialog = true }
    fun closeEdit() { showEditDialog = false }

    fun saveProfile(
        firstName: String,
        lastName: String,
        newImage: Uri?,
        currentPwd: String?,
        newPwd: String?,
        onError: (String) -> Unit,
        onSuccess: () -> Unit,
        errorLoadingText: String,
        errorSavingText: String
    ) {
        viewModelScope.launch {
            try {
                if (newImage != null) {
                    userRepository.updateProfileImage(currentUserId, newImage)
                }

                userRepository.updateUserNames(currentUserId, firstName, lastName)

                if (!currentPwd.isNullOrBlank() && !newPwd.isNullOrBlank()) {
                    userRepository.changePassword(currentPwd, newPwd)
                }

                load(errorLoadingText)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: errorSavingText)
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }
}
