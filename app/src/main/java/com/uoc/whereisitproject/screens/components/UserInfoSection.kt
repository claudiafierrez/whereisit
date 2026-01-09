package com.uoc.whereisitproject.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uoc.whereisitproject.R

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
        Text(text = stringResource(id = R.string.points) + " $points")
    }
}