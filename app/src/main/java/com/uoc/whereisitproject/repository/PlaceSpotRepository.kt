package com.uoc.whereisitproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.screens.PlaceAchievements
import kotlinx.coroutines.tasks.await

suspend fun getCompletedSpotsByPlaceByUser(
    uid: String,
    db: FirebaseFirestore
): List<PlaceAchievements> {

    val placesSnap = db.collection("places").get().await()
    val placeDocs = placesSnap.documents
    val results = mutableListOf<PlaceAchievements>()

    for (placeDoc in placeDocs) {
        val placeId = placeDoc.id
        val placeName = placeDoc.getString("name") ?: "(Without name)"

        // Spots of place
        val spotsSnap = db.collection("places").document(placeId)
            .collection("spots")
            .get().await()

        val spots = spotsSnap.documents.mapNotNull { d ->
            try {
                Spot(
                    spotId = d.id,
                    name = d.getString("name")!!,
                    description = d.getString("description") ?: "",
                    location = d.getGeoPoint("location")!!,
                    streetViewHeading = d.getLong("streetViewHeading")!!.toInt(),
                    streetViewPitch = d.getLong("streetViewPitch")!!.toInt(),
                    difficulty = d.getLong("difficulty")!!.toInt()
                )
            } catch (_: Exception) {
                null
            }
        }

        // Completed spots by the user for that place
        val completedSnap = db.collection("users").document(uid)
            .collection("completedSpots")
            .whereEqualTo("placeId", placeId)
            .get().await()

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