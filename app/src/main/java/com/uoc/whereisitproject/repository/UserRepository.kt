package com.uoc.whereisitproject.repository

import android.net.Uri
import com.uoc.whereisitproject.model.UserProfile

interface UserRepository {
    suspend fun createUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        profileImageUri: Uri
    )
    suspend fun getUserProfile(userId: String): UserProfile
}