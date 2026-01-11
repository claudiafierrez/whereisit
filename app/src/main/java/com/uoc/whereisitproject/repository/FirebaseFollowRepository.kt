package com.uoc.whereisitproject.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.FollowRequest
import com.uoc.whereisitproject.model.FollowStatus
import kotlinx.coroutines.tasks.await

class FirebaseFollowRepository(
    private val firestore: FirebaseFirestore
) : FollowRepository {

    override suspend fun getFollowStatus(
        currentUid: String,
        otherUid: String
    ): FollowStatus {

        val q = firestore.collection("follows")
            .whereEqualTo("followerId", currentUid)
            .whereEqualTo("followingId", otherUid)
            .limit(1)
            .get()
            .await()

        val doc = q.documents.firstOrNull()
            ?: return FollowStatus(false, null, null)

        return FollowStatus(
            exists = true,
            status = doc.getString("status"),
            followId = doc.id
        )
    }

    override suspend fun followUser(
        currentUid: String,
        otherUid: String
    ) {
        val existing = firestore.collection("follows")
            .whereEqualTo("followerId", currentUid)
            .whereEqualTo("followingId", otherUid)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        if (existing != null) {
            val status = existing.getString("status")
            if (status != "accepted") {
                existing.reference.update(
                    "status", "pending",
                    "createdAt", FieldValue.serverTimestamp()
                ).await()
            }
            return
        }

        val followerSnap =
            firestore.collection("users").document(currentUid).get().await()

        val data = mapOf(
            "followerId" to currentUid,
            "followingId" to otherUid,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "followerUsername" to followerSnap.getString("username"),
            "followerProfileImageUrl" to followerSnap.getString("profileImageUrl")
        )

        firestore.collection("follows").add(data).await()
    }

    override suspend fun unfollowUser(
        currentUid: String,
        otherUid: String
    ) {
        val doc = firestore.collection("follows")
            .whereEqualTo("followerId", currentUid)
            .whereEqualTo("followingId", otherUid)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        doc?.reference?.delete()?.await()
    }

    override suspend fun acceptFollow(followId: String) {
        firestore.collection("follows")
            .document(followId)
            .update("status", "accepted")
            .await()
    }

    override suspend fun rejectFollow(followId: String) {
        firestore.collection("follows")
            .document(followId)
            .update("status", "rejected")
            .await()
    }

    override suspend fun cancelFollowRequest(currentUserId: String, otherUserId: String) {
        val snap = firestore.collection("follows")
            .whereEqualTo("followerId", currentUserId)
            .whereEqualTo("followingId", otherUserId)
            .whereEqualTo("status", "pending")
            .limit(1)
            .get()
            .await()

        snap.documents.firstOrNull()?.reference?.delete()?.await()
    }

    override suspend fun getPendingRequests(
        currentUid: String
    ): List<FollowRequest> {

        val snap = firestore.collection("follows")
            .whereEqualTo("followingId", currentUid)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        return snap.documents.map {
            FollowRequest(
                followId = it.id,
                followerId = it.getString("followerId")!!,
                followerUsername = it.getString("followerUsername"),
                followerProfileImageUrl = it.getString("followerProfileImageUrl"),
                status = it.getString("status")!!,
                createdAt = it.getTimestamp("createdAt")
            )
        }
    }
}