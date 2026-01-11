package com.uoc.whereisitproject.repository

import com.uoc.whereisitproject.model.FollowRequest
import com.uoc.whereisitproject.model.FollowStatus

interface FollowRepository {
    suspend fun getFollowStatus(currentUid: String, otherUid: String): FollowStatus
    suspend fun followUser(currentUid: String, otherUid: String)
    suspend fun unfollowUser(currentUid: String, otherUid: String)
    suspend fun acceptFollow(followId: String)
    suspend fun rejectFollow(followId: String)
    suspend fun cancelFollowRequest(currentUserId: String, otherUserId: String)
    suspend fun getPendingRequests(currentUid: String): List<FollowRequest>
}