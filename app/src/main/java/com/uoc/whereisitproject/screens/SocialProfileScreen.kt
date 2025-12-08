package com.uoc.whereisitproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.data.getFollowStatus
import com.uoc.whereisitproject.model.FollowStatus
import com.uoc.whereisitproject.screens.components.UserInfoSection
import com.uoc.whereisitproject.model.UserProfile
import com.uoc.whereisitproject.repository.getCompletedSpotsByPlaceByUser
import com.uoc.whereisitproject.screens.components.AvatarHeader
import com.uoc.whereisitproject.screens.components.FollowChip
import com.uoc.whereisitproject.screens.components.PlaceAchievementsSection
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialProfileScreen(
    userId: String,
    navController: NavHostController
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUid = remember { auth.currentUser!!.uid }

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var status by remember { mutableStateOf<FollowStatus?>(null) }
    var achievements by remember { mutableStateOf<List<PlaceAchievements>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        try {
            loading = true; error = null
            val doc = db.collection("users").document(userId).get().await()
            if (!doc.exists()) {
                error = "Profile not found"
            } else {
                profile = UserProfile(
                    userId = doc.id,
                    username = doc.getString("username")!!,
                    firstName = doc.getString("firstName")!!,
                    lastName  = doc.getString("lastName")!!,
                    email     = doc.getString("email")!!,
                    profileImageUrl = doc.getString("profileImageUrl")!!,
                    points    = (doc.getLong("points") ?: 0L).toInt()
                )
            }
            status = getFollowStatus(db, currentUid, userId)
            achievements = getCompletedSpotsByPlaceByUser(userId, db).filter { it.completedIds.isNotEmpty() }
        } catch (e: Exception) {
            error = e.message ?: "Error loading profile"
        } finally {
            loading = false
        }
    }

    //UI
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Profile", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

            )
        }
    ) {
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center)
                {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Profile", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            profile != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Profile", style = MaterialTheme.typography.headlineLarge)
                    // Avatar + username
                    AvatarHeader(
                        imageUrl = profile!!.profileImageUrl,
                        username = profile!!.username
                    )

                    // Follow status
                    FollowChip(
                        db = db,
                        currentUid = currentUid,
                        otherUid = userId
                    )

                    @Suppress("DEPRECATION")
                    Divider()

                    if(status?.exists == true && status?.status == "accepted") {
                        // Info
                        UserInfoSection(
                            firstName = profile!!.firstName,
                            lastName  = profile!!.lastName,
                            email     = profile!!.email,
                            points    = profile!!.points,
                            showEmail = false
                        )

                        @Suppress("DEPRECATION")
                        Divider()
                        
                        //code for show achievements
                        Text("Achievements", style = MaterialTheme.typography.headlineSmall)
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
    }
}