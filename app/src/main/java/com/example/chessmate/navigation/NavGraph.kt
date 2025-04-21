package com.example.chessmate.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.chessmate.ui.screen.*
import com.example.chessmate.viewmodel.ChatViewModel
import java.net.URLDecoder

/**
 * Hàm Composable định nghĩa biểu đồ điều hướng cho ứng dụng, quản lý các màn hình và chuyển đổi giữa chúng.
 *
 * @param navController Bộ điều khiển điều hướng để quản lý các chuyển đổi màn hình.
 * @param startDestination Điểm bắt đầu của điều hướng (mặc định là "home").
 * @param chatViewModel ViewModel để quản lý logic trò chuyện, được truyền từ MainActivity.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "home",
    chatViewModel: ChatViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Màn hình chính (Home)
        composable("home") { HomeScreen(navController) }
        // Màn hình đăng nhập
        composable("login") { LoginScreen(navController) }
        // Màn hình đăng ký
        composable("register") { RegisterScreen(navController) }
        // Màn hình hồ sơ người dùng
        composable("profile") { ProfileScreen(navController) }
        // Màn hình tìm kiếm bạn bè
        composable("find_friends") {
            FindFriendsScreen(
                navController = navController,
                viewModel = viewModel(),
                chatViewModel = chatViewModel
            )
        }
        // Màn hình tải (loading)
        composable("loading") { LoadingScreen(navController = navController) }
        // Màn hình đặt lại mật khẩu
        composable("reset_password") { ResetPasswordScreen(navController) }
        // Màn hình chính sau khi đăng nhập
        composable("main_screen") {
            MainScreen(
                navController = navController,
                viewModel = viewModel(),
                friendViewModel = viewModel(),
                chatViewModel = chatViewModel
            )
        }
        // Màn hình lịch sử trận đấu của người dùng
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
        // Màn hình danh sách trò chuyện
        composable("chat") {
            ChatScreen(
                navController = navController,
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        // Màn hình chi tiết trò chuyện với một người bạn
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
                friendName = friendName,
                viewModel = chatViewModel
            )
        }
        // Màn hình chơi cờ với AI
        composable("play_with_ai") { PlayWithAIScreen(navController) }
        // Màn hình chơi cờ với bạn bè
        composable("play_with_friend") { PlayWithFriendScreen(navController) }
        // Màn hình chơi cờ với đối thủ (hỗ trợ deep link)
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
        // Màn hình hồ sơ của đối thủ
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