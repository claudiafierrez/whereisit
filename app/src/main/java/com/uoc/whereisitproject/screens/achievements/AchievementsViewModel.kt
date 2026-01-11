package com.uoc.whereisitproject.screens.achievements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uoc.whereisitproject.model.PlaceAchievements
import com.uoc.whereisitproject.repository.PlaceSpotRepository
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val repository: PlaceSpotRepository,
    private val currentUserId: String
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var achievements by mutableStateOf<List<PlaceAchievements>>(emptyList())
        private set

    fun loadAchievements(errorText: String) {
        viewModelScope.launch {
            loading = true
            error = null

            runCatching {
                repository
                    .getCompletedSpotsByPlaceByUser(currentUserId)
                    .filter { it.completedIds.isNotEmpty() }
            }.onSuccess {
                achievements = it
            }.onFailure {
                error = it.message ?: errorText
            }

            loading = false
        }
    }
}