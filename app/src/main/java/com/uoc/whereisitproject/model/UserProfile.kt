package com.uoc.whereisitproject.model

data class UserProfile(
    val userId: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: String,
    val points: Int
)
