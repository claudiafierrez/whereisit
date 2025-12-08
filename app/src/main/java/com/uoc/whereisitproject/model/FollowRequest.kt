package com.uoc.whereisitproject.model

import com.google.firebase.Timestamp

data class FollowRequest(
    val followId: String,
    val followerId: String,
    val followerUsername: String?,
    val followerProfileImageUrl: String?,
    val status: String, // pending / accepted / rejected
    val createdAt: Timestamp?
)