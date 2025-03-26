package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.components.ButtonItem
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme

// Màn hình chơi cờ vua với thời gian và bàn cờ
@Composable
fun PlayScreen(
    navController: NavController? = null,
    onMessageClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.color_c97c5d)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Thanh tiêu đề
            // Kiểm tra navController để tránh lỗi trong preview
            if (navController != null) {
                MainHeader(navController = navController, onMessageClick = onMessageClick)
            } else {
                MainHeader(navController = rememberNavController(), onMessageClick = onMessageClick)
            }
            // Logo ứng dụng
            Logo()
            Spacer(modifier = Modifier.height(20.dp))
            // Thời gian chơi
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonItem(
                    text = "10 phút",
                    colorId = R.color.color_c89f9c,
                    onClick = { navController?.navigate("loading") } 
                )
                Spacer(modifier = Modifier.width(32.dp))
                ButtonItem(
                    text = "3 phút",
                    colorId = R.color.color_c89f9c,
                    onClick = { navController?.navigate("loading") }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Bàn cờ
            Chessboard()
        }
    }
}

// Xem trước giao diện màn hình chơi cờ
@Preview(showBackground = true)
@Composable
fun PlayScreenPreview() {
    ChessmateTheme {
        PlayScreen()
    }
}