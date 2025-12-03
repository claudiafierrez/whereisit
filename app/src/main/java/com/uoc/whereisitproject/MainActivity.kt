package com.uoc.whereisitproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.uoc.whereisitproject.ui.theme.WhereIsItProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        //Initialize Firebase
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()


        //val meta = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        //Log.d("TFG", "MAPS KEY: ${meta.getString("com.google.android.geo.API_KEY")}")

        setContent {
            WhereIsItProjectTheme {

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("main") {
                        BottomNavigationScreen()
                    }
                }

            }
        }
    }
}
