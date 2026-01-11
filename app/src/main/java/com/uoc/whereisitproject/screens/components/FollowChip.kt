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
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.model.FollowStatus

@Composable
fun FollowChip(
    status: FollowStatus?,
    loading: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onPendingClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        when {
            loading -> CircularProgressIndicator(strokeWidth = 2.dp)

            status?.exists == true && status.status == "accepted" -> {
                AssistChip(
                    onClick = onUnfollow,
                    label = { Text(stringResource(R.string.following)) }
                )
            }

            status?.exists == true && status.status == "pending" -> {
                AssistChip(
                    onClick = onPendingClick,
                    label = { Text(stringResource(R.string.pending)) }
                )
            }

            else -> {
                AssistChip(
                    onClick = onFollow,
                    label = { Text(stringResource(R.string.follow)) }
                )
            }
        }
    }
}