package com.uoc.whereisitproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.repository.getCompletedSpotsByPlaceByUser
import com.uoc.whereisitproject.screens.components.PlaceAchievementsSection

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

    val errorLoadAchievementsText = stringResource(id = R.string.error_load_achievements)

    LaunchedEffect(uid) {
        try {
            loading = true
            error = null

            achievements = getCompletedSpotsByPlaceByUser(uid, db).filter { it.completedIds.isNotEmpty() }

        } catch (e: Exception) {
            error = e.message ?: errorLoadAchievementsText
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
                Text(text = stringResource(id = R.string.achievements), style = MaterialTheme.typography.headlineLarge)
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
                Text(text = stringResource(id = R.string.achievements), style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))

                if (achievements.isEmpty()) {
                    Text(text = stringResource(id = R.string.no_achievements_yet))
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