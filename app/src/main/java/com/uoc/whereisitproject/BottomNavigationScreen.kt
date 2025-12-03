package com.uoc.whereisitproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun BottomNavigationScreen() {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("list") { ListScreen() }
            composable("profile") { ProfileScreen() }
            composable("achievements") { AchievementsScreen() }
            composable("social") { SocialScreen() }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "List") },
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
