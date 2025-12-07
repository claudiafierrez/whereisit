
package com.uoc.whereisitproject.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.screens.components.SpotCard
import com.uoc.whereisitproject.util.readMapsApiKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ListScreen(
    onSpotClick: (spotId: String, placeId: String) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val coroutineScope = rememberCoroutineScope()

    var cityName by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Firestore
    val db = remember { FirebaseFirestore.getInstance() }
    var isLoadingSpots by remember { mutableStateOf(true) }
    var spots by remember { mutableStateOf<List<Spot>>(emptyList()) }

    val apiKey = remember { readMapsApiKey(context) }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            error = "Permission not granted"
            isLocating = false
            return@LaunchedEffect
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        cityName = addresses?.firstOrNull()?.locality ?: "Unknown"
                    }
                    isLocating = false
                }
            } else {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000
                ).setMaxUpdates(1).build()

                fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val newLocation = result.lastLocation
                        fusedLocationClient.removeLocationUpdates(this)
                        if (newLocation != null) {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    @Suppress("DEPRECATION")
                                    val addresses = geocoder.getFromLocation(newLocation.latitude, newLocation.longitude, 1)
                                    cityName = addresses?.firstOrNull()?.locality ?: "Unknown"
                                }
                                isLocating = false
                            }
                        } else {
                            error = "Location not available"
                            isLocating = false
                        }
                    }
                }, context.mainLooper)
            }
        }.addOnFailureListener {
            error = it.message
            isLocating = false
        }
    }


    // Once we have cityName, we search for the Place and its 5 Spots.
    var placeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cityName) {
        val city = cityName ?: return@LaunchedEffect
        if (city.isBlank() || city == "Unknown") return@LaunchedEffect

        //isLoadingSpots = true
        error = null
        spots = emptyList()

        // Search Place
        db.collection("places")
            .whereEqualTo("name", city)
            .limit(1)
            .get()
            .addOnSuccessListener { placeSnap ->
                if (placeSnap.isEmpty) {
                    error = "No place found for '$city'."
                    isLoadingSpots = false
                } else {
                    placeId = placeSnap.documents.first().id
                    // Search Spots
                    db.collection("places").document(placeId!!)
                        .collection("spots")
                        .limit(5)
                        .get()
                        .addOnSuccessListener { spotsSnap ->
                            spots = spotsSnap.documents.map { d ->
                                Spot(
                                    spotId = d.id,
                                    name = d.getString("name")!!,
                                    description = d.getString("description")!!,
                                    location = d.getGeoPoint("location")!!,
                                    streetViewHeading = d.getLong("streetViewHeading")!!.toInt(),
                                    streetViewPitch = d.getLong("streetViewPitch")!!.toInt(),
                                    difficulty = d.getLong("difficulty")!!.toInt()
                                )
                            }
                            isLoadingSpots = false
                        }
                        .addOnFailureListener {
                            error = "Failed to load spots: ${it.message}"
                            isLoadingSpots = false
                        }
                }
            }
            .addOnFailureListener {
                error = "Failed to load place: ${it.message}"
                isLoadingSpots = false
            }
    }

    // UI
    if (isLocating) {
        // Centered Spinner, without texts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            //verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "To discover in...",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = cityName ?: "—",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(12.dp))

            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                when {
                    isLoadingSpots -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    error != null -> {
                        item {
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    spots.isEmpty() -> {
                        item {
                            Text("No spots for this city yet.")
                        }
                    }
                    else -> {
                        items(spots.size) { index ->
                            val spot = spots[index]
                            SpotCard(
                                spot = spot,
                                apiKey = apiKey,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val pid = placeId ?: return@clickable
                                        onSpotClick(spot.spotId, pid)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}