package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.ButtonItem
import com.example.chessmate.ui.theme.ChessmateTheme
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.Logo

// Thanh tiêu đề với các biểu tượng tin nhắn và hồ sơ
@Composable
fun MainHeader(
    navController: NavController,
    modifier: Modifier = Modifier,
    onMessageClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        // Biểu tượng tin nhắn với chấm đỏ thông báo
        IconButton(
            onClick = onMessageClick,
            modifier = Modifier.size(40.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.message),
                    contentDescription = "Tin nhắn",
                    modifier = Modifier.size(32.dp)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        // Biểu tượng hồ sơ
        IconButton(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.size(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Hồ sơ",
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
    }
}

// Các hàng nút điều hướng
@Composable
fun MainButtonRow(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hàng 1: Nút Chơi, Nút Tìm bạn
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem(
                text = "Chơi",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("play") } // Điều hướng đến PlayScreen
            )
            Spacer(modifier = Modifier.width(32.dp))
            ButtonItem(
                text = "Tìm bạn",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("find_friends") }
            )
        }
        // Hàng 2: Nút Đấu với AI, Nút Chơi với bạn
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem(
                text = "Đấu với AI",
                colorId = R.color.color_c89f9c
            )
            Spacer(modifier = Modifier.width(32.dp))
            ButtonItem(
                text = "Chơi với bạn",
                colorId = R.color.color_c89f9c
            )
        }
    }
}

// Màn hình chính với header, logo, các nút và bàn cờ
@Composable
fun MainScreen(navController: NavController) {
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
            MainHeader(navController = navController)
            // Logo ứng dụng
            Logo()
            Spacer(modifier = Modifier.height(20.dp))
            MainButtonRow(navController)
            Spacer(modifier = Modifier.height(20.dp))
            // Bàn cờ
            Chessboard()
        }
    }
}

// Xem trước giao diện màn hình chính
@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val navController = rememberNavController()
    ChessmateTheme {
        MainScreen(navController)
    }
}