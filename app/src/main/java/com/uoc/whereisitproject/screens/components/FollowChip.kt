package com.uoc.whereisitproject.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.data.getFollowStatus
import com.uoc.whereisitproject.data.followUser
import com.uoc.whereisitproject.data.unfollowUser
import com.uoc.whereisitproject.model.FollowStatus

@Composable
fun FollowChip(
    db: FirebaseFirestore,
    currentUid: String,
    otherUid: String
) {
    var status by remember { mutableStateOf<FollowStatus?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(otherUid) {
        loading = true
        try {
            status = getFollowStatus(db, currentUid, otherUid)
        } catch (_: Exception) {
            status = null
        } finally {
            loading = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier, strokeWidth = 2.dp)

            // Accepted -> shows "Following" and allows UNFOLLOW
            status?.exists == true && status?.status == "accepted" -> {
                AssistChip(
                    onClick = {
                        scope.launch {
                            try {
                                unfollowUser(db, currentUid, otherUid)
                                status = getFollowStatus(db, currentUid, otherUid)
                            } catch (_: Exception) { }
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.following)) }
                )
            }

            // Pending -> deactivated
            status?.exists == true && status?.status == "pending" -> {
                AssistChip(
                    onClick = { /* not activated */ },
                    enabled = false,
                    label = { Text(text = stringResource(id = R.string.pending)) }
                )
            }

            // Rejected -> allows request again (Follow)
            status?.exists == true && status?.status == "rejected" -> {
                AssistChip(
                    onClick = {
                        scope.launch {
                            try {
                                followUser(db, currentUid, otherUid)
                                status = getFollowStatus(db, currentUid, otherUid)
                            } catch (_: Exception) { }
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.follow)) }
                )
            }

            // Does not exist relationship -> Follow
            else -> {
                AssistChip(
                    onClick = {
                        scope.launch {
                            try {
                                followUser(db, currentUid, otherUid)
                                status = getFollowStatus(db, currentUid, otherUid)
                            } catch (_: Exception) { }
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.follow)) }
                )
            }
        }
    }
}