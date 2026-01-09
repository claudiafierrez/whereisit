package com.uoc.whereisitproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.data.acceptFollow
import com.uoc.whereisitproject.data.getPendingRequestsOnce
import com.uoc.whereisitproject.data.rejectFollow
import com.uoc.whereisitproject.model.FollowRequest
import com.uoc.whereisitproject.model.UserSummary
import com.uoc.whereisitproject.screens.components.NotificationsIcon
import com.uoc.whereisitproject.screens.components.PendingRequestsDialog
import com.uoc.whereisitproject.screens.components.UserSearchItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    onUserClick: (userId: String) -> Unit = {}
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val uid = remember { auth.currentUser!!.uid }

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<UserSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    //  notification states
    var pendingCount by remember { mutableStateOf(0) }
    var pendingRequests by remember { mutableStateOf<List<FollowRequest>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val searchErrorText = stringResource(id = R.string.search_error)

    fun refreshPending() {
        scope.launch {
            try {
                val list = getPendingRequestsOnce(db, uid)
                pendingRequests = list
                pendingCount = list.size
            } catch (_: Exception) {

            }
        }
    }

    LaunchedEffect(Unit) {
        refreshPending()
    }

    fun performSearch(text: String) {
        // Cancel last search
        searchJob?.cancel()
        searchJob = scope.launch {
            loading = true
            error = null
            results = emptyList()

            val q = text.trim().lowercase()
            if (q.isEmpty()) {
                loading = false
                return@launch
            }

            try {
                // Search by prefix in 'username'
                val snap = db.collection("users")
                    .whereGreaterThanOrEqualTo("username", q)
                    .whereLessThan("username", q + "\uf8ff")
                    .limit(20)
                    .get()
                    .await()

                results = snap.documents
                    .filter { it.id != uid } // exclude the logged-in user
                    .mapNotNull { d ->
                        val username = d.getString("username") ?: return@mapNotNull null
                        UserSummary(
                            userId = d.id,
                            username = username,
                            profileImageUrl = d.getString("profileImageUrl")
                        )
                    }
            } catch (e: Exception) {
                error = e.message ?: searchErrorText
            } finally {
                loading = false
            }
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = stringResource(id = R.string.social), style = MaterialTheme.typography.headlineLarge) },
                actions = {
                    NotificationsIcon(
                        pendingCount = pendingCount,
                        onClick = {
                            showDialog = true
                            refreshPending() // refresh on open
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            @Suppress("DEPRECATION")
            SearchBar(
                query = query,
                onQueryChange = { new ->
                    query = new
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        delay(250)
                        performSearch(new)
                    }
                },
                onSearch = { performSearch(query) },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(text = stringResource(id = R.string.search_users_by_username)) },
                trailingIcon = {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (results.isEmpty() && query.isNotBlank() && !loading) {
                    Text(text = stringResource(id = R.string.matching_users) + " “$query”.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(items = results, key = { it.userId }) { user ->
                            UserSearchItem(
                                user = user,
                                onClick = {
                                    active = false
                                    onUserClick(user.userId)
                                }
                            )

                            @Suppress("DEPRECATION")
                            Divider()
                        }
                    }
                }
            }
        }

        // Notifications dialog
        PendingRequestsDialog(
            visible = showDialog,
            requests = pendingRequests,
            onAccept = { followId ->
                scope.launch {
                    try {
                        acceptFollow(db, followId)
                    } catch (_: Exception) { }
                    refreshPending()
                }
            },
            onReject = { followId ->
                scope.launch {
                    try {
                        rejectFollow(db, followId)
                    } catch (_: Exception) { }
                    refreshPending()
                }
            },
            onDismiss = { showDialog = false }
        )
    }
}