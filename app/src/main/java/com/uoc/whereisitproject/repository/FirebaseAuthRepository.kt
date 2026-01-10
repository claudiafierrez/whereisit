package com.uoc.whereisitproject.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository (
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid
            ?: throw IllegalStateException("UserId not found after registration")
    }
}