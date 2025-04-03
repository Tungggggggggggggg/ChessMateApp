package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.ButtonItem
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.Logo
import com.example.chessmate.ui.theme.ChessmateTheme

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.color_c97c5d))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Logo ứng dụng
                Logo()
                Spacer(modifier = Modifier.height(20.dp))
                ButtonRow(navController)

                Spacer(modifier = Modifier.height(20.dp))
                // Bàn cờ
                Chessboard()
            }
        }
    }
}

@Composable
fun ButtonRow(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hàng 1: Nút Đăng nhập, Nút Đăng ký
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem(
                text = "Đăng nhập",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("login") }
            )
            Spacer(modifier = Modifier.width(32.dp))
            ButtonItem(
                text = "Đăng ký",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("register") }
            )
        }
        // Hàng 2: Nút Đấu với AI, Nút Đấu với bạn
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
                text = "Đấu với bạn",
                colorId = R.color.color_c89f9c,
                onClick = { navController.navigate("play_with_friend") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    ChessmateTheme {
        HomeScreen(rememberNavController())
    }
}