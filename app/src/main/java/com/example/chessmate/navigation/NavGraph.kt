package com.example.chessmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chessmate.ui.screen.HomeScreen
import com.example.chessmate.ui.screen.LoginScreen
import com.example.chessmate.ui.screen.RegisterScreen
import com.example.chessmate.ui.screen.ProfileScreen
import com.example.chessmate.ui.screen.FindFriendsScreen
import com.example.chessmate.ui.screen.LoadingScreen
import com.example.chessmate.ui.screen.MainScreen
import com.example.chessmate.ui.screen.ResetPasswordScreen
import com.example.chessmate.ui.screen.PlayScreen
import com.example.chessmate.ui.screen.MatchHistoryScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
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
