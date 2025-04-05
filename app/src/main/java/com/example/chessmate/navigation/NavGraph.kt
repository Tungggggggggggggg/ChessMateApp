package com.example.chessmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chessmate.ui.screen.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("find_friends") { FindFriendsScreen(navController) }
        composable("loading") { LoadingScreen() }
        composable("reset_password") { ResetPasswordScreen(navController) }
        composable("play") { PlayScreen(navController) }
        composable("main_screen") { MainScreen(navController) }
        composable("match_history") { MatchHistoryScreen(navController) }
    }
}