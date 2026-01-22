package com.uoc.whereisitproject.navigation

import android.annotation.SuppressLint
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

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user == null) {
        NavHost(navController, startDestination = "login") {
            composable("login") {
                val vm = LoginViewModel(FirebaseAuthRepository(auth))
                LoginScreen(
                    viewModel = vm,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = {}
                )
            }

            composable("register") {
                val vm = RegisterViewModel(
                    authRepository = FirebaseAuthRepository(auth),
                    userRepository = FirebaseUserRepository(
                        FirebaseFirestore.getInstance(),
                        FirebaseStorage.getInstance(),
                        auth = FirebaseAuth.getInstance()
                    )
                )
                RegisterScreen(vm) { navController.popBackStack() }
            }
        }

        return
    }

    // ONLY if user exists
    val uid = user!!.uid

    NavHost(navController, startDestination = "main") {
        composable("main") {
            BottomNavigationScreen(
                currentUserId = uid
            )
        }
    }
}
