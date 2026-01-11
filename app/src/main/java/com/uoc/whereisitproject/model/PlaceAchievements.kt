package com.uoc.whereisitproject.model

data class PlaceAchievements(
    val placeId: String,
    val placeName: String,
    val spots: List<Spot>,
    val completedIds: Set<String>
) {
    val total: Int get() = spots.size
}