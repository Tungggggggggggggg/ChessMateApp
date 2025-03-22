package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chessmate.R
import com.example.chessmate.ui.components.ButtonItem
import com.example.chessmate.ui.theme.ChessmateTheme
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.Logo



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
                Logo()
                Spacer(modifier = Modifier.height(20.dp))
                ButtonRow(navController)
                Spacer(modifier = Modifier.height(20.dp))
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
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem("Đăng nhập", R.color.color_c89f9c) {
                navController.navigate("login")
            }
            Box(
                modifier = Modifier.width(120.dp).height(45.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Online",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            ButtonItem("Đăng ký", R.color.color_c89f9c) {
                navController.navigate("register")
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem("Đấu với AI", R.color.color_c89f9c) {
                navController.navigate("ai_game")
            }
            Box(
                modifier = Modifier.width(120.dp).height(45.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Offline",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            ButtonItem("Đấu với bạn", R.color.color_c89f9c) {
                navController.navigate("friend_game")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    val navController = rememberNavController()
    ChessmateTheme {
        HomeScreen(navController)
    }
}
