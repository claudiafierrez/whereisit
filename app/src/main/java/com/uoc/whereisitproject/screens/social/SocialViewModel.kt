package com.uoc.whereisitproject.screens.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uoc.whereisitproject.model.FollowRequest
import com.uoc.whereisitproject.model.UserSummary
import com.uoc.whereisitproject.repository.FollowRepository
import com.uoc.whereisitproject.repository.SocialRepository
import kotlinx.coroutines.launch

class SocialViewModel(
    private val socialRepository: SocialRepository,
    private val followRepository: FollowRepository,
    private val currentUserId: String
) : ViewModel() {

    init {
        loadPending()
    }

    var query by mutableStateOf("")
        private set

    var results by mutableStateOf<List<UserSummary>>(emptyList())
        private set

    var pendingRequests by mutableStateOf<List<FollowRequest>>(emptyList())
        private set

    var isSearchActive by mutableStateOf(false)
        private set

    var isDialogOpen by mutableStateOf(false)
        private set

    var pendingCount by mutableStateOf(0)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun onQueryChange(text: String, errorText: String) {
        query = text
        search(errorText)
    }

    fun search(errorText: String) {
        val q = query.trim().lowercase()

        if (q.isBlank()) {
            results = emptyList()
            error = null
            loading = false
            return
        }

        viewModelScope.launch {
            loading = true
            error = null
            runCatching {
                socialRepository.searchUsersByUsername(q, currentUserId)
            }.onSuccess {
                results = it
            }.onFailure {
                error = it.message ?: errorText
            }
            loading = false
        }
    }

    fun loadPending() {
        viewModelScope.launch {
            val list = followRepository.getPendingRequests(currentUserId)
            pendingRequests = list
            pendingCount = list.size
        }
    }

    fun acceptFollow(id: String) {
        viewModelScope.launch {
            followRepository.acceptFollow(id)
            loadPending()
        }
    }

    fun rejectFollow(id: String) {
        viewModelScope.launch {
            followRepository.rejectFollow(id)
            loadPending()
        }
    }

    fun onSearchActiveChange(active: Boolean) {
        isSearchActive = active
    }

    fun openRequestsDialog() {
        isDialogOpen = true
        loadPending()
    }

    fun closeRequestsDialog() {
        isDialogOpen = false
    }
}