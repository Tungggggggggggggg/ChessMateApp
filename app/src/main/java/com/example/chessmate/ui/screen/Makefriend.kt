package com.example.chessmate.ui.screen

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle

@Composable
fun FindFriendsScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFCCCC)) // Màu header
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.message), contentDescription = "Messages")
            Text("Tìm Bạn", fontSize = 20.sp, fontWeight = FontWeight.Bold )
            Icon(painter = painterResource(id = R.drawable.profile), contentDescription = "Profile")
        }

        Column( // Phần còn lại của nội dung
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Chiếm phần không gian còn lại
                .background(Color(0xFFFF9966)) // Màu cam nhạt
                .padding(16.dp),
        ) {
            // Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Xử lý sự kiện back */ }) {
                    Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Logo and Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Chess Logo")
                /*Text("CHESSMATE", fontSize = 32.sp, textAlign = TextAlign.Center, color = Color.White)*/
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFFFCCCC), shape = RoundedCornerShape(25.dp)), // Bo góc và màu
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Thêm padding cho text field
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black), // Màu chữ đen
                    cursorBrush = SolidColor(Color.Black), // Màu con trỏ đen
                    decorationBox = { innerTextField ->
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text = "Nhập tên người chơi ...", // Placeholder
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        innerTextField()
                    }
                )

                Icon(painter = painterResource(id = R.drawable.search), contentDescription = "Search")

            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Results
            if (searchQuery.text.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Kết quả tìm kiếm:")
                    // TODO: Thêm danh sách bạn bè nếu có
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindFriendsScreenPreview() {
    FindFriendsScreen()
}