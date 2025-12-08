package com.uoc.whereisitproject.model

data class FollowStatus(
    val exists: Boolean,
    val status: String?, // "pending" | "accepted" | "rejected" | null
    val followId: String? = null
)