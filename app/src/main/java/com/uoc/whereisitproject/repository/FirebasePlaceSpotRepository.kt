package com.uoc.whereisitproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.PlaceAchievements
import com.uoc.whereisitproject.model.Spot
import kotlinx.coroutines.tasks.await

class FirebasePlaceSpotRepository(
    private val firestore: FirebaseFirestore
) : PlaceSpotRepository {
    override suspend fun getCompletedSpotsByPlaceByUser(
        userId: String
    ): List<PlaceAchievements> {
        val placesSnap = firestore.collection("places").get().await()
        val results = mutableListOf<PlaceAchievements>()

        for (placeDoc in placesSnap.documents) {
            val placeId = placeDoc.id
            val placeName = placeDoc.getString("name") ?: "(Without name)"

            //Spots of place
            val spotsSnap = firestore
                .collection("places")
                .document(placeId)
                .collection("spots")
                .get()
                .await()

            val spots = spotsSnap.documents.mapNotNull {
                runCatching { Spot.fromSnapshot(it) }.getOrNull()
            }

            // Completed spots by the user for that place
            val completedSnap = firestore
                .collection("users")
                .document(userId)
                .collection("completedSpots")
                .whereEqualTo("placeId", placeId)
                .get()
                .await()

            val completedIds = completedSnap.documents
                .mapNotNull { it.getString("spotId") }
                .toSet()

            results += PlaceAchievements(
                placeId = placeId,
                placeName = placeName,
                spots = spots,
                completedIds = completedIds
            )
        }

        return results
    }
}