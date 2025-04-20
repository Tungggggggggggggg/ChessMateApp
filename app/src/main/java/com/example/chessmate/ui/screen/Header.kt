package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.viewmodel.ChatViewModel

@Composable
fun Header(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    centerContent: @Composable () -> Unit = {
        Text(
            text = "Tìm Bạn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 100.dp)
        )
    }
) {
    val hasUnreadMessages = viewModel.hasUnreadMessages.collectAsState()

    // Gọi loadFriendsWithMessages ngay khi Header được vẽ
    LaunchedEffect(Unit) {
        viewModel.loadFriendsWithMessages()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Box(
            modifier = Modifier
                .clickable { navController.navigate("chat") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.friend),
                contentDescription = "Tin nhắn",
                modifier = Modifier.size(32.dp)
            )
            if (hasUnreadMessages.value) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 6.dp, y = (-6).dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.Red)
                )
            }
        }
        Box(
            modifier = Modifier.weight(1f) // Áp dụng weight cho centerContent
        ) {
            centerContent()
        }
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