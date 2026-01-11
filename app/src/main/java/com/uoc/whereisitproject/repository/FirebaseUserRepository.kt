package com.uoc.whereisitproject.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.uoc.whereisitproject.model.UserProfile
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {
    override suspend fun createUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        profileImageUri: Uri
    ) {
        val imageRef = storage.reference.child("profileImages/$userId.jpg")
        val imageUrl = imageRef.putFile(profileImageUri)
            .continueWithTask { imageRef.downloadUrl }
            .await()
            .toString()

        val userData = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username.lowercase(),
            "email" to email,
            "profileImageUrl" to imageUrl,
            "points" to 0,
            "createdAt" to Timestamp.now()
        )

        firestore.collection("users")
            .document(userId)
            .set(userData)
            .await()
    }

    override suspend fun getUserProfile(userId: String): UserProfile {
        val doc = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        if (!doc.exists()) {
            throw Exception("User not found")
        }

        return UserProfile(
            userId = doc.id,
            username = doc.getString("username")!!,
            firstName = doc.getString("firstName")!!,
            lastName = doc.getString("lastName")!!,
            email = doc.getString("email")!!,
            profileImageUrl = doc.getString("profileImageUrl")!!,
            points = (doc.getLong("points") ?: 0L).toInt()
        )
    }
}