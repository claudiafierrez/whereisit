package com.uoc.whereisitproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.UserSummary
import kotlinx.coroutines.tasks.await

class FirebaseSocialRepository(
    private val firestore: FirebaseFirestore
): SocialRepository {
    override suspend fun searchUsersByUsername(
        query: String,
        excludeUserId: String
    ): List<UserSummary> {

        val snap = firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThan("username", query + "\uf8ff")
            .limit(20)
            .get()
            .await()

        return snap.documents
            .filter { it.id != excludeUserId }
            .mapNotNull { d ->
                val username = d.getString("username") ?: return@mapNotNull null
                UserSummary(
                    userId = d.id,
                    username = username,
                    profileImageUrl = d.getString("profileImageUrl")
                )
            }
    }
}