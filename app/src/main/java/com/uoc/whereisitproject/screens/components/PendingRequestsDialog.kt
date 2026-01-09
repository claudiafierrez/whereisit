package com.uoc.whereisitproject.screens.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.model.FollowRequest

@Composable
fun PendingRequestsDialog(
    visible: Boolean,
    requests: List<FollowRequest>,
    onAccept: (followId: String) -> Unit,
    onReject: (followId: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.followup_requests)) },
        text = {
            if (requests.isEmpty()) {
                Text(text = stringResource(id = R.string.no_pending_requests))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = requests, key = { it.followId }) { req ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = req.followerProfileImageUrl,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.Black, CircleShape)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Text(
                                    text = req.followerUsername ?: req.followerId,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = stringResource(id = R.string.wants_follow),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = 8.dp,
                                        alignment = Alignment.CenterHorizontally
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { onAccept(req.followId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Gray, // background
                                            contentColor = Color.White   // text
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = stringResource(id = R.string.accept))
                                    }
                                    OutlinedButton(
                                        onClick = { onReject(req.followId) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = stringResource(id = R.string.reject))
                                    }
                                }
                            }
                        }
                    }

                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(id = R.string.close), color = Color.Black) }
        }
    )
}