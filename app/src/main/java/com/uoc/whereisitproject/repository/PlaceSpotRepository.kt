package com.uoc.whereisitproject.repository

import com.uoc.whereisitproject.model.PlaceAchievements

interface PlaceSpotRepository {
    suspend fun getCompletedSpotsByPlaceByUser(
        userId: String
    ): List<PlaceAchievements>
}