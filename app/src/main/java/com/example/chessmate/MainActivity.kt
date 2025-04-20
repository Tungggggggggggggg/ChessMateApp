package com.example.chessmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.navigation.NavGraph
import com.example.chessmate.ui.theme.ChessmateTheme
import com.example.chessmate.viewmodel.ChatViewModel
import com.example.chessmate.viewmodel.ChatViewModelFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    // Khởi tạo ChatViewModel trong MainActivity
    private val chatViewModel: ChatViewModel by viewModels { ChatViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Gọi loadFriendsWithMessages() một lần duy nhất khi ứng dụng khởi động
        chatViewModel.loadFriendsWithMessages()

        setContent {
            ChessmateTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val startDestination = if (auth.currentUser != null) {
                    "main_screen"
                } else {
                    "home"
                }
                // Truyền chatViewModel vào NavGraph
                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    chatViewModel = chatViewModel
                )
            }
        }
    }
}