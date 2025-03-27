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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.theme.ChessmateTheme

// Phần Header của màn hình Chat
@Composable
fun ChatHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = "Nhắn tin",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(20.dp)) // Để căn chỉnh cho đẹp
    }
}

// Nội dung chính của màn hình Chat
@Composable
fun ChatContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        UserListItem(userName = "Người dùng 1")
        HorizontalDivider(color = Color.Black, thickness = 1.dp) // Thêm Divider
        UserListItem(userName = "Người dùng 2")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.color_c97c5d))
        )
    }
}

// Hiển thị thông tin người dùng trong danh sách
@Composable
fun UserListItem(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = userName, fontSize = 18.sp)
    }
}

// Màn hình chính để hiển thị Chat
@Composable
fun ChatScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() } // Thêm tham số onBackClick
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChatHeader(onBackClick = onBackClick) // Truyền hàm onBackClick
        ChatContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}


// Xem trước giao diện màn hình Chat
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChessmateTheme {
        ChatScreen()
    }
}