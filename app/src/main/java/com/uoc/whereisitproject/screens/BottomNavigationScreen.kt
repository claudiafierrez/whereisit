package com.uoc.whereisitproject.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun BottomNavigationScreen(
    onLoggedOutNavigateToLogin : () -> Unit
) {
    val bottomNavController = rememberNavController()
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) {
            // user denies location, show popup to open settings
            showPermissionDialog = true
        }
    }

    // check permission on entry
    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            permissionGranted = true
        }
    }

    // Check changes on back from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                permissionGranted = isGranted
                if (isGranted) {
                    showPermissionDialog = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!permissionGranted) {
        // Screen blocked and show popup
        Box(modifier = Modifier.fillMaxSize()) {
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { /* Close not available */ },
                    title = { Text("Location Required") },
                    text = {
                        Text("You must enable location permission from Settings to use the app.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", activity.packageName, null)
                                }
                                activity.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray, // background
                                contentColor = Color.White   // text
                            )
                        ) {
                            Text("Open Settings")
                        }
                    }
                )
            }
        }
    } else {
        // Show content only if the permission is grant
        Scaffold(
            bottomBar = { BottomNavigationBar(bottomNavController) }
        ) { innerPadding ->
            NavHost(
                navController = bottomNavController,
                startDestination = "list",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("list") {
                    ListScreen(
                        onSpotClick = { spotId, placeId ->
                            bottomNavController.navigate("spotDetail/$spotId/$placeId")
                        }
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        onLoggedOut = onLoggedOutNavigateToLogin
                    )
                }
                composable("achievements") { AchievementsScreen() }
                composable("social") { SocialScreen() }
                composable("spotDetail/{spotId}/{placeId}") { backStackEntry ->
                    val spotId = backStackEntry.arguments?.getString("spotId") ?: ""
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
                    SpotDetailScreen(
                        spotId = spotId,
                        placeId = placeId,
                        navController = bottomNavController
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List") },
            selected = currentRoute == "list",
            onClick = { navController.navigate("list") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements") },
            selected = currentRoute == "achievements",
            onClick = { navController.navigate("achievements") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.People, contentDescription = "Social") },
            selected = currentRoute == "social",
            onClick = { navController.navigate("social") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") }
        )
    }
}
