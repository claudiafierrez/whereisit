package com.uoc.whereisitproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.uoc.whereisitproject.navigation.AppNavHost
import com.uoc.whereisitproject.ui.theme.WhereIsItProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        //Initialize Firebase
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()

        setContent {
            WhereIsItProjectTheme {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val start = if (currentUser != null) "main" else "login"
                AppNavHost(
                    startDestination = start
                )
            }
        }
    }
}
