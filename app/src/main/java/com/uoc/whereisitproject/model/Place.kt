package com.uoc.whereisitproject.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Data model for a Place (document in /places/{placeId}).
 * Every Place contains a subcollection of de Spots.
 */
data class Place(
    val placeId: String,
    val name: String
) {
    companion object {
        fun fromSnapshot(d: DocumentSnapshot): Place {
            return Place(
                placeId = d.id,
                name = d.getString("name")!!
            )
        }
    }
}