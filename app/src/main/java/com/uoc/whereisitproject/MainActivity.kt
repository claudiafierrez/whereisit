package com.uoc.whereisitproject

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.uoc.whereisitproject.ui.theme.WhereIsItProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        //Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Splash screen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isLoading by mutableStateOf(true)

        splashScreen.setKeepOnScreenCondition { isLoading }

        // Charge simulation (2 sec)
        android.os.Handler(mainLooper).postDelayed({
            isLoading = false
        }, 2000)

        //val meta = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        //Log.d("TFG", "MAPS KEY: ${meta.getString("com.google.android.geo.API_KEY")}")

        setContent {
            WhereIsItProjectTheme {
                if (isLoading) {
                    // API shows splash automatically
                } else {
                    LoginScreen()
                }
            }
        }
    }
}
