package com.uoc.whereisitproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.uoc.whereisitproject.repository.FirebaseAuthRepository
import com.uoc.whereisitproject.repository.FirebaseUserRepository
import com.uoc.whereisitproject.screens.BottomNavigationScreen
import com.uoc.whereisitproject.screens.login.LoginScreen
import com.uoc.whereisitproject.screens.register.RegisterScreen
import com.uoc.whereisitproject.screens.login.LoginViewModel
import com.uoc.whereisitproject.screens.register.RegisterViewModel
/*fun AppNavHost(
    startDestination: String
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("login") {
            val loginViewModel = LoginViewModel(
                authRepository = FirebaseAuthRepository(
                    FirebaseAuth.getInstance()
                )
            )
            LoginScreen(
                viewModel = loginViewModel,
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
            val registerViewModel = RegisterViewModel(
                authRepository = FirebaseAuthRepository(
                    FirebaseAuth.getInstance()
                ),
                userRepository = FirebaseUserRepository(
                    firestore = FirebaseFirestore.getInstance(),
                    storage = FirebaseStorage.getInstance()
                )
            )
            RegisterScreen(
                viewModel = registerViewModel,
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
}*/
@Composable
fun AppNavHost() {
    val rootNavController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }
        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val startDestination = if (user == null) "login" else "main"

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("login") {
            val loginViewModel = LoginViewModel(
                authRepository = FirebaseAuthRepository(
                    FirebaseAuth.getInstance()
                )
            )
            LoginScreen(
                viewModel = loginViewModel,
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
            val registerViewModel = RegisterViewModel(
                authRepository = FirebaseAuthRepository(
                    FirebaseAuth.getInstance()
                ),
                userRepository = FirebaseUserRepository(
                    firestore = FirebaseFirestore.getInstance(),
                    storage = FirebaseStorage.getInstance()
                )
            )
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
        composable("main") {
            BottomNavigationScreen(
                currentUserId = user!!.uid,
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
