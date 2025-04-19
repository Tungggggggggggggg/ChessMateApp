package com.example.chessmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.chessmate.ui.screen.*
import java.net.URLDecoder

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
        composable("loading") { LoadingScreen(navController = navController) }
        composable("reset_password") { ResetPasswordScreen(navController) }
        composable("main_screen") { MainScreen(navController) }
        composable(
            route = "match_history/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MatchHistoryScreen(
                navController = navController,
                userId = userId
            )
        }
        composable("chat") { ChatScreen(navController) }
        composable(
            route = "chat_detail/{friendId}/{friendName}",
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("friendName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendName = backStackEntry.arguments?.getString("friendName")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            ChatDetailScreen(
                navController = navController,
                friendId = friendId,
                friendName = friendName
            )
        }
        composable("play_with_ai") { PlayWithAIScreen(navController) }
        composable("play_with_friend") { PlayWithFriendScreen(navController) }
        composable(
            route = "play_with_opponent/{matchId}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "chessmate://play_with_opponent/{matchId}" }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            PlayWithOpponentScreen(
                navController = navController,
                matchId = matchId
            )
        }
        composable(
            route = "competitor_profile/{opponentId}",
            arguments = listOf(navArgument("opponentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val opponentId = backStackEntry.arguments?.getString("opponentId") ?: ""
            CompetitorProfileScreen(
                navController = navController,
                opponentId = opponentId
            )
        }
    }
}