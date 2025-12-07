package com.uoc.whereisitproject.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.util.streetViewUrl
import com.uoc.whereisitproject.R

@Composable
fun SpotCard(
    spot: Spot,
    apiKey: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val gp = spot.location
    val url = remember(gp, spot.streetViewHeading, spot.streetViewPitch, apiKey) {
        streetViewUrl(
            lat = gp.latitude,
            lng = gp.longitude,
            apiKey = apiKey,
            width = 600,
            height = 400,
            scale = 2,
            heading = spot.streetViewHeading,
            pitch = spot.streetViewPitch
        )
    }

    val borderColor = if (isCompleted) Color(0xFF2E7D32) else Color.Black
    val borderWidth = if (isCompleted) 2.dp else 1.dp
    val containerColor = if (isCompleted)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.image_error)
                    .build(),
                contentDescription = "Street View ${spot.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF2E7D32)
                    )
                }
            }

        }
    }
}