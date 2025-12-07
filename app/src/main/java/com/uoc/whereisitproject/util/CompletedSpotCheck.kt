package com.uoc.whereisitproject.util

import android.location.Location
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Distance in metres between two lat/lng
fun distanceMeters(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double
): Float {
    val res = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, res)
    return res[0]
}

// Check if a spot has already been completed
suspend fun isSpotCompleted(
    db: FirebaseFirestore,
    uid: String,
    placeId: String,
    spotId: String
): Boolean {
    val completedId = "${placeId}_${spotId}"
    val doc = db.collection("users").document(uid)
        .collection("completedSpots").document(completedId)
        .get().await()
    return doc.exists()
}

// Mark as completed only once
// Returns true if created for the first time; false if it already existed (or error).
suspend fun markSpotCompleted(
    db: FirebaseFirestore,
    uid: String,
    placeId: String,
    spotId: String,
    difficulty: Int
): Boolean {
    val completedId = "${placeId}_${spotId}"
    val ref = db.collection("users").document(uid)
        .collection("completedSpots").document(completedId)
    val userRef = db.collection("users").document(uid)

    return try {
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            if (snap.exists()) {
                false // already completed
            } else {
                val data = mapOf("spotId" to spotId, "placeId" to placeId)
                tx.set(ref, data)
                tx.update(userRef, mapOf("points" to FieldValue.increment(difficulty.toLong())))
                true
            }
        }.await()
    } catch (e: Exception) {
        false
    }
}