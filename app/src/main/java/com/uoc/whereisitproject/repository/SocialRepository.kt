package com.uoc.whereisitproject.repository

import com.uoc.whereisitproject.model.UserSummary

interface SocialRepository {
    suspend fun searchUsersByUsername(
        query: String,
        excludeUserId: String
    ): List<UserSummary>
}