package com.uoc.whereisitproject.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

/**
 * Data model for a Spot stored in Firestore in:
 * /places/{placeId}/spots/{spotId}
 */

data class Spot (
    val spotId: String,
    val name: String,
    val description: String,
    val location: GeoPoint,
    val streetViewHeading: Int,
    val streetViewPitch: Int,
    val difficulty: Int
) {
    companion object {
        fun fromSnapshot(d: DocumentSnapshot): Spot {
            return Spot(
                spotId = d.id,
                name = d.getString("name")!!,
                description = d.getString("description")!!,
                location = d.getGeoPoint("location")!!,
                streetViewHeading = d.getLong("streetViewHeading")!!.toInt(),
                streetViewPitch = d.getLong("streetViewPitch")!!.toInt(),
                difficulty = d.getLong("difficulty")!!.toInt()
            )
        }
    }
}