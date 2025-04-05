package com.example.chessmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.navigation.NavGraph
import com.example.chessmate.ui.theme.ChessmateTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            ChessmateTheme {
                val navController = rememberNavController()
                // Kiểm tra trạng thái đăng nhập
                val auth = FirebaseAuth.getInstance()
                val startDestination = if (auth.currentUser != null) {
                    "main_screen" // Nếu đã đăng nhập, đi thẳng đến MainScreen
                } else {
                    "home" // Nếu chưa đăng nhập, đi đến HomeScreen
                }
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}