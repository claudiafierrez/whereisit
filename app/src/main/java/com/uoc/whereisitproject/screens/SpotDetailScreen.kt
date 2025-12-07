@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uoc.whereisitproject.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.util.readMapsApiKey
import com.uoc.whereisitproject.util.streetViewUrl
import com.uoc.whereisitproject.R

@Composable
fun SpotDetailScreen(
    spotId: String,
    placeId: String,
    navController: NavHostController
) {
    val db = remember { FirebaseFirestore.getInstance() }
    var spot by remember { mutableStateOf<Spot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(spotId, placeId) {
        db.collection("places").document(placeId)
            .collection("spots").document(spotId)
            .get()
            .addOnSuccessListener { doc ->
                try {
                    spot = Spot(
                        spotId = doc.id,
                        name = doc.getString("name")!!,
                        description = doc.getString("description")!!,
                        location = doc.getGeoPoint("location")!!,
                        streetViewHeading = doc.getLong("streetViewHeading")!!.toInt(),
                        streetViewPitch = doc.getLong("streetViewPitch")!!.toInt(),
                        difficulty = doc.getLong("difficulty")!!.toInt()
                    )
                    loading = false
                } catch (e: Exception) {
                    error = "Invalid spot data: ${e.message}"
                    loading = false
                }
            }
            .addOnFailureListener {
                error = it.message
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Text(text = error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val s = spot!!
                val context = LocalContext.current
                val apiKey = remember { readMapsApiKey(context) }
                val imageUrl = remember(s, apiKey) {
                    streetViewUrl(
                        lat = s.location.latitude,
                        lng = s.location.longitude,
                        apiKey = apiKey,
                        width = 600, height = 400, scale = 2,
                        heading = s.streetViewHeading,
                        pitch = s.streetViewPitch
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = s.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = s.description,
                        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Justify)
                    )
                    Text(
                        text = "Difficulty: ${s.difficulty}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.image_error)
                            .build(),
                        contentDescription = "Street View ${s.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}