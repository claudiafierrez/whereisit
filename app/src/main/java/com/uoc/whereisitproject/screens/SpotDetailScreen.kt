package com.uoc.whereisitproject.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uoc.whereisitproject.model.Spot
import com.uoc.whereisitproject.util.readMapsApiKey
import com.uoc.whereisitproject.util.streetViewUrl
import com.uoc.whereisitproject.R
import com.uoc.whereisitproject.repository.distanceMeters
import com.uoc.whereisitproject.repository.isSpotCompleted
import com.uoc.whereisitproject.repository.markSpotCompleted
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    spotId: String,
    placeId: String,
    navController: NavHostController,
    radiusMeters: Float = 20f
) {
    val db = remember { FirebaseFirestore.getInstance() }
    var spot by remember { mutableStateOf<Spot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }


    // Completion status and proximity
    var isCompleted by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var promptShown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val apiKey = remember { readMapsApiKey(context) }
    val uid = remember { FirebaseAuth.getInstance().currentUser!!.uid }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    val invalidSpotDataText = stringResource(id = R.string.invalid_spot_data)

    LaunchedEffect(spotId, placeId) {
        try {
            val doc = db.collection("places").document(placeId)
                .collection("spots").document(spotId)
                .get().await()

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
            error = invalidSpotDataText + " ${e.message}"
            loading = false
        }
    }

    //Check if completed
    LaunchedEffect(placeId, spotId, uid) {
        try {
            isCompleted = isSpotCompleted(db, uid, placeId, spotId)
        } catch (_: Exception) { /* ignores */ }
    }

    // Listen to location ONLY if it is not completed
    LaunchedEffect(spot, isCompleted) {
        val s = spot ?: return@LaunchedEffect
        if (isCompleted) return@LaunchedEffect

        // Defensive verification (BottomNavigation already guarantees permission, but we check anyway)
        val granted = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return@LaunchedEffect

        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateDistanceMeters(3f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val d = distanceMeters(
                    lat1 = loc.latitude, lng1 = loc.longitude,
                    lat2 = s.location.latitude, lng2 = s.location.longitude
                )
                if (d <= radiusMeters && !promptShown) {
                    promptShown = true
                    showDialog = true
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(req, callback, context.mainLooper)
            // Suspend until cancellation (e.g., isCompleted = true)
            awaitCancellation()
        } finally {
            // Always runs when cancelling the effect → we clean up
            fusedLocationClient.removeLocationUpdates(callback)
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
                    Text(text = error ?: stringResource(id = R.string.error), color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val s = spot!!
                val context = LocalContext.current
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
                        text = stringResource(id = R.string.difficulty) + " ${s.difficulty}",
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

                    if (isCompleted) {
                        AssistChip(
                            onClick = {},
                            label = { Text(text = stringResource(id = R.string.completed)) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },

                        )
                    }

                    if (showDialog && !isCompleted) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text(text = stringResource(id = R.string.spot_got_it)) },
                            confirmButton = {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Gray, // background
                                        contentColor = Color.White   // text
                                    ),
                                    enabled = !isCompleted,
                                    onClick = {
                                        scope.launch {
                                            val ok = markSpotCompleted(db,
                                                uid, placeId, spotId, spot!!.difficulty)
                                            if (ok) {
                                                isCompleted = true // close location
                                            }
                                            showDialog = false
                                        }
                                    }
                                ) { Text(text = stringResource(id = R.string.completed)) }
                            }
                        )
                    }
                }
            }
        }
    }
}