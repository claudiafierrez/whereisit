package com.uoc.whereisitproject.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.screens.components.PlaceAchievementsSection
import kotlinx.coroutines.tasks.await

data class PlaceAchievements(
    val placeId: String,
    val placeName: String,
    val spots: List<Spot>,
    val completedIds: Set<String>
) {
    val total: Int get() = spots.size
}

@Composable
fun AchievementsScreen(){

    val db = remember { FirebaseFirestore.getInstance() }
    val uid = remember { FirebaseAuth.getInstance().currentUser!!.uid }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var achievements by remember { mutableStateOf<List<PlaceAchievements>>(emptyList()) }

    LaunchedEffect(uid) {
        try {
            loading = true
            error = null

            val placesSnap = db.collection("places").get().await()
            val placeDocs = placesSnap.documents
            val results = mutableListOf<PlaceAchievements>()

            for (placeDoc in placeDocs) {
                val placeId = placeDoc.id
                val placeName = placeDoc.getString("name") ?: "(Sin nombre)"

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

            achievements = results
        } catch (e: Exception) {
            error = e.message ?: "Failed to load achievements"
        } finally {
            loading = false
        }
    }

    // UI
    when {
        loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Achievements", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Achievements", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))

                if (achievements.isEmpty()) {
                    Text("You have no achievements yet.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(achievements, key = { it.placeId }) { pa ->
                            PlaceAchievementsSection(pa)
                            @Suppress("DEPRECATION")
                            Divider(Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}