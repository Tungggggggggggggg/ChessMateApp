package com.example.chessmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessmate.ui.theme.ChessmateTheme
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessmateTheme {
                ChessmateScreen()
            }
        }
    }
}

@Composable
fun ChessmateScreen() {
    // Tạo giao diện chính với nền và các thành phần chính
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
                Logo() // Hiển thị logo và tiêu đề
                Spacer(modifier = Modifier.height(20.dp))
                ButtonRow() // Hiển thị các nút và text cho các tùy chọn
                Spacer(modifier = Modifier.height(20.dp))
                Chessboard() // Hiển thị bàn cờ
            }
        }
    }
}

@Composable
fun ButtonRow() {
    // Tạo hai hàng chứa các nút và text cho các tùy chọn
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hàng 1: Nút Đăng nhập, Text Online, Nút Đăng ký
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem("Đăng nhập", R.color.color_c89f9c)
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(45.dp),
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
            ButtonItem("Đăng ký", R.color.color_c89f9c)
        }
        // Hàng 2: Nút Đấu với AI, Text Offline, Nút Đấu với bạn
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonItem("Đấu với AI", R.color.color_c89f9c)
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(45.dp),
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
            ButtonItem("Đấu với bạn", R.color.color_c89f9c)
        }
    }
}

@Composable
fun ButtonItem(text: String, colorId: Int) {
    // Tạo nút với văn bản và màu nền tùy chỉnh
    Button(
        onClick = { },
        modifier = Modifier
            .width(120.dp)
            .height(45.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = colorId))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() {
    ChessmateTheme {
        ChessmateScreen()
    }
}