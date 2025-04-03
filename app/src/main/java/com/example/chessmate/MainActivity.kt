package com.example.chessmate

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.navigation.NavGraph
import com.example.chessmate.ui.theme.ChessmateTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val database = FirebaseDatabase.getInstance("https://chessmate-2c597-default-rtdb.asia-southeast1.firebasedatabase.app/")
        Log.d("Firebase", "Database URL: ${database.reference.database.reference}")
        setContent {
            ChessmateTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
