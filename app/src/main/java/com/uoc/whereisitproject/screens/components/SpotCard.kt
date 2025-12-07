package com.uoc.whereisitproject.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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

            Text(
                text = spot.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}