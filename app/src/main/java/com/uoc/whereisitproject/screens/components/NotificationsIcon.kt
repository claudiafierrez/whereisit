package com.uoc.whereisitproject.screens.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable

@Composable
fun NotificationsIcon(
    pendingCount: Int,
    onClick: () -> Unit
) {
    BadgedBox(
        badge = {
            if (pendingCount > 0) {
                Badge {
                    Text(
                        text = if (pendingCount > 99) "99+" else "$pendingCount"
                    )
                }
            }
        }
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications"
            )
        }
    }
}