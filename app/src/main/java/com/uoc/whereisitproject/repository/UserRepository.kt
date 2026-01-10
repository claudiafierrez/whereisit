package com.uoc.whereisitproject.repository

import android.net.Uri

interface UserRepository {
    suspend fun createUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        profileImageUri: Uri
    )
}