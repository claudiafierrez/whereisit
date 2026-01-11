package com.uoc.whereisitproject.screens.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uoc.whereisitproject.model.FollowStatus
import com.uoc.whereisitproject.model.PlaceAchievements
import com.uoc.whereisitproject.model.UserProfile
import com.uoc.whereisitproject.repository.FollowRepository
import com.uoc.whereisitproject.repository.PlaceSpotRepository
import com.uoc.whereisitproject.repository.UserRepository
import kotlinx.coroutines.launch

class SocialProfileViewModel(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val placeSpotRepository: PlaceSpotRepository,
    private val currentUserId: String
) : ViewModel() {

    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var followStatus by mutableStateOf<FollowStatus?>(null)
        private set

    var achievements by mutableStateOf<List<PlaceAchievements>>(emptyList())
        private set

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun load(userId: String, errorText: String) {

        viewModelScope.launch {
            loading = true
            error = null

            runCatching {
                Triple(
                    userRepository.getUserProfile(userId),
                    followRepository.getFollowStatus(currentUserId, userId),
                    placeSpotRepository
                        .getCompletedSpotsByPlaceByUser(userId)
                        .filter { it.completedIds.isNotEmpty() }
                )
            }.onSuccess {
                profile = it.first
                followStatus = it.second
                achievements = it.third
            }.onFailure {
                error = it.message ?: errorText
            }

            loading = false
        }
    }

    fun follow(userId: String) {
        viewModelScope.launch {
            followRepository.followUser(currentUserId, userId)
            followStatus = followRepository.getFollowStatus(currentUserId, userId)
        }
    }

    fun unfollow(userId: String) {
        viewModelScope.launch {
            followRepository.unfollowUser(currentUserId, userId)
            followStatus = followRepository.getFollowStatus(currentUserId, userId)
        }
    }

    fun cancelRequest(userId: String) {
        viewModelScope.launch {
            followRepository.cancelFollowRequest(currentUserId, userId)
            followStatus = followRepository.getFollowStatus(currentUserId, userId)
        }
    }
}