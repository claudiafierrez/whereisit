package com.uoc.whereisitproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uoc.whereisitproject.screens.BottomNavigationScreen
import com.uoc.whereisitproject.screens.LoginScreen
import com.uoc.whereisitproject.screens.RegisterScreen

@Composable
fun AppNavHost(
    startDestination: String
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    rootNavController.navigate("register")
                },
                onLoginSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
        composable("main") {
            BottomNavigationScreen(
                onLoggedOutNavigateToLogin = {
                    rootNavController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}