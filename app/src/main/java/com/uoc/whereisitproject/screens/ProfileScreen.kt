package com.uoc.whereisitproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.screens.components.EditProfileDialog
import com.uoc.whereisitproject.screens.components.UserInfoSection
import com.uoc.whereisitproject.model.UserProfile
import com.uoc.whereisitproject.screens.components.AvatarHeader
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit = {}
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }
    val uid = remember { auth.currentUser!!.uid }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var showEdit by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val errorProfileFoundText = stringResource(id = R.string.profile_not_found)
    val errorProfileLoadingText = stringResource(id = R.string.profile_error_loading)

    // Load profile
    LaunchedEffect(uid) {
        try {
            loading = true; error = null
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                error = errorProfileFoundText
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
        } catch (e: Exception) {
            error = e.message ?: errorProfileLoadingText
        } finally {
            loading = false
        }
    }

    fun refreshProfile() {
        scope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
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
            } catch (_: Exception) {
            }
        }
    }

    // UI
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
                Text(text = stringResource(id = R.string.profile), style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
        profile != null -> {
            val p = profile!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = stringResource(id = R.string.profile), style = MaterialTheme.typography.headlineLarge)

                // Avatar + username
                AvatarHeader(
                    imageUrl = p.profileImageUrl,
                    username = p.username
                )

                @Suppress("DEPRECATION")
                Divider()

                // Info
                UserInfoSection(
                    firstName = p.firstName,
                    lastName  = p.lastName,
                    email     = p.email,
                    points    = p.points,
                    showEmail = true
                )

                Spacer(Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    Button(
                        onClick = { showEdit = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray, // background
                            contentColor = Color.White   // text
                        )
                    ) {
                        Text(text = stringResource(id = R.string.edit_profile))
                    }
                    OutlinedButton(onClick = {
                        auth.signOut()
                        onLoggedOut()
                    }) {
                        Text(text = stringResource(id = R.string.logout))
                    }
                }
            }

            if (profile != null && showEdit) {
                val p = profile!!
                EditProfileDialog(
                    initialFirstName = p.firstName,
                    initialLastName  = p.lastName,
                    currentImageUrl  = p.profileImageUrl,
                    db = db,
                    auth = auth,
                    storage = storage,
                    onDismiss = { showEdit = false },
                    onSaved = {
                        refreshProfile()
                        showEdit = false
                    }
                )
            }
        }
    }
}