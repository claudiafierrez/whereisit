package com.uoc.whereisitproject.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@Composable
fun UserInfoSection(
    firstName: String,
    lastName: String,
    email: String,
    points: Int,
    showEmail: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(firstName)
        Text(lastName)
        if (showEmail) Text(email)
        Text("Points: $points")
    }
}