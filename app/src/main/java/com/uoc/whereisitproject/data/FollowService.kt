package com.uoc.whereisitproject.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.FollowRequest
import com.uoc.whereisitproject.model.FollowStatus
import kotlinx.coroutines.tasks.await

//Check if there is a follow relationship between currentUid (follower) and otherUid (following)
suspend fun getFollowStatus(
    db: FirebaseFirestore,
    currentUid: String,
    otherUid: String
): FollowStatus {
    val q = db.collection("follows")
        .whereEqualTo("followerId", currentUid)
        .whereEqualTo("followingId", otherUid)
        .limit(1)
        .get()
        .await()

    val doc = q.documents.firstOrNull() ?: return FollowStatus(false, null, null)
    val status = doc.getString("status")
    return FollowStatus(true, status, doc.id)
}

suspend fun followUser(
    db: FirebaseFirestore,
    followerUid: String,
    followingUid: String
) {
    val existing = db.collection("follows")
        .whereEqualTo("followerId", followerUid)
        .whereEqualTo("followingId", followingUid)
        .limit(1)
        .get()
        .await()
        .documents
        .firstOrNull()

    if (existing != null) {
        val currentStatus = existing.getString("status")
        if (currentStatus != "accepted") {
            existing.reference.update(
                mapOf(
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        return
    }

    // We read follower data for receiver notifications.
    val followerSnap = db.collection("users").document(followerUid).get().await()
    val followerUsername = followerSnap.getString("username")
    val followerProfileImageUrl = followerSnap.getString("profileImageUrl")

    val docRef = db.collection("follows").document() // auto-id
    val data = mapOf(
        "followId" to docRef.id,
        "followerId" to followerUid,
        "followingId" to followingUid,
        "status" to "pending",
        "createdAt" to FieldValue.serverTimestamp(),
        "followerUsername" to followerUsername,
        "followerProfileImageUrl" to followerProfileImageUrl
    )
    docRef.set(data).await()
}

suspend fun acceptFollow(
    db: FirebaseFirestore,
    followId: String
) {
    db.collection("follows").document(followId)
        .update("status", "accepted")
        .await()
}

suspend fun rejectFollow(
    db: FirebaseFirestore,
    followId: String
) {
    db.collection("follows").document(followId)
        .update("status", "rejected")
        .await()
}

suspend fun unfollowUser(
    db: FirebaseFirestore,
    followerUid: String,
    followingUid: String
) {
    val existing = db.collection("follows")
        .whereEqualTo("followerId", followerUid)
        .whereEqualTo("followingId", followingUid)
        .limit(1)
        .get()
        .await()
        .documents
        .firstOrNull()

    existing?.reference?.delete()?.await()
}

// List of pending requests where the recipient is the current user (followingId = currentUid).
// Used in notifications.
suspend fun getPendingRequestsOnce(
    db: FirebaseFirestore,
    currentUid: String
): List<FollowRequest> {
    val snap = db.collection("follows")
        .whereEqualTo("followingId", currentUid)
        .whereEqualTo("status", "pending")
        .get()
        .await()

    return snap.documents.map { d ->
        FollowRequest(
            followId = d.id,
            followerId = d.getString("followerId") ?: "",
            followerUsername = d.getString("followerUsername"),
            followerProfileImageUrl = d.getString("followerProfileImageUrl"),
            status = d.getString("status") ?: "pending",
            createdAt = d.getTimestamp("createdAt")
        )
    }
}

