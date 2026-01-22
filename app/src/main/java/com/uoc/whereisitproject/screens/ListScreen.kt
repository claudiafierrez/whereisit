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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.R
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
    val uid = remember { FirebaseAuth.getInstance().currentUser!!.uid }

    var completedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var placeId by remember { mutableStateOf<String?>(null) }

    val permissionNotGrantedText = stringResource(id = R.string.no_permission)
    val unknownText = stringResource(id = R.string.unknown)
    val locationNotAvailableText = stringResource(id = R.string.location_not_available)
    val noPlaceFoundText = stringResource(id = R.string.no_place_found)
    val failLoadSpotsText = stringResource(id = R.string.fail_load_spots)
    val failLoadPlaceText = stringResource(id = R.string.fail_load_place)
    val dashText = stringResource(id = R.string.dash)

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            error = permissionNotGrantedText
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
                        cityName = addresses?.firstOrNull()?.locality ?: unknownText
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
                                    cityName = addresses?.firstOrNull()?.locality ?: unknownText
                                }
                                isLocating = false
                            }
                        } else {
                            error = locationNotAvailableText
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

    LaunchedEffect(placeId, uid) {
        completedIds = emptySet()
        val pid = placeId ?: return@LaunchedEffect

        db.collection("users").document(uid)
            .collection("completedSpots")
            .whereEqualTo("placeId", pid)
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                completedIds = snap?.documents
                    ?.mapNotNull { it.getString("spotId") }
                    ?.toSet()
                    ?: emptySet()
            }
    }

    // Once we have cityName, we search for the Place and its 5 Spots.
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
                    error = "$noPlaceFoundText '$city'."
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
                                Spot.fromSnapshot(d)
                            }
                            isLoadingSpots = false
                        }
                        .addOnFailureListener {
                            error = failLoadSpotsText + " ${it.message}"
                            isLoadingSpots = false
                        }
                }
            }
            .addOnFailureListener {
                error = failLoadPlaceText + " ${it.message}"
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.discover),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = cityName ?: dashText,
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
                            Text(text = stringResource(id = R.string.no_spots_for_city))
                        }
                    }
                    else -> {
                        items(spots.size) { index ->
                            val spot = spots[index]
                            val isCompleted = completedIds.contains(spot.spotId)
                            SpotCard(
                                spot = spot,
                                apiKey = apiKey,
                                isCompleted = isCompleted,
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