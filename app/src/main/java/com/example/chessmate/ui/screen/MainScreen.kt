package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.model.PieceColor
import com.example.chessmate.ui.components.ButtonItem
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.viewmodel.ChatViewModel
import com.example.chessmate.viewmodel.ChessViewModel
import com.example.chessmate.viewmodel.FindFriendsViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Hiển thị hàng nút chính trên màn hình chính.
 *
 * @param navController Điều hướng đến các màn hình khác.
 * @param hasPendingRequests True nếu có lời mời kết bạn đang chờ.
 */
@Composable
fun MainButtonRow(
    navController: NavController,
    hasPendingRequests: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem(
                text = "Chơi",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("loading") }
            )
            Spacer(modifier = Modifier.width(32.dp))
            Box {
                ButtonItem(
                    text = "Tìm bạn",
                    colorId = R.color.color_c89f9c,
                    onClick = { navController.navigate("find_friends") }
                )
                if (hasPendingRequests) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = (-16).dp, y = 8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem(
                text = "Đấu với AI",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("play_with_ai") }
            )
            Spacer(modifier = Modifier.width(32.dp))
            ButtonItem(
                text = "Chơi với bạn",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("play_with_friend") }
            )
        }
    }
}

/**
 * Màn hình chính của ứng dụng, hiển thị các tùy chọn chơi và bàn cờ mẫu.
 *
 * @param navController Điều hướng đến các màn hình khác.
 * @param viewModel ViewModel quản lý logic bàn cờ.
 * @param friendViewModel ViewModel quản lý danh sách bạn bè.
 * @param chatViewModel ViewModel quản lý tin nhắn.
 */
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: ChessViewModel,
    friendViewModel: FindFriendsViewModel,
    chatViewModel: ChatViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val receivedRequests = friendViewModel.receivedRequests.collectAsState()

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        friendViewModel.loadReceivedRequests()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.color_c97c5d))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Header(
                navController = navController,
                viewModel = chatViewModel,
                centerContent = { Spacer(modifier = Modifier) }
            )
            Logo()
            Spacer(modifier = Modifier.height(20.dp))
            MainButtonRow(
                navController = navController,
                hasPendingRequests = receivedRequests.value.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Chessboard(
                board = viewModel.board.value,
                highlightedSquares = viewModel.highlightedSquares.value,
                onSquareClicked = { _, _ -> },
                playerColor = PieceColor.WHITE,
                clickable = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}