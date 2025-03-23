package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.theme.ChessmateTheme

// Thanh tiêu đề với nút quay lại và tiêu đề "Thông tin cá nhân"
@Composable
fun ProfileHeader(
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
        Text(
            text = "Thông tin cá nhân",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.width(44.dp))
    }
}

// Phần nội dung chính của màn hình hồ sơ
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c97c5d)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Ảnh đại diện (hình tròn)
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Ảnh đại diện",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Các nút
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* TODO: Xử lý sửa thông tin */ },
                modifier = Modifier
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Sửa thông tin",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { /* TODO: Xử lý đổi ảnh */ },
                modifier = Modifier
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.color_c89f9c)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Đổi ảnh",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Thông tin hồ sơ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .border(1.dp, Color.Black)
                .background(colorResource(id = R.color.color_eee2df))
                .padding(8.dp)
        ) {
            ProfileInfoRow(label = "Tên:", value = "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "ID:", value = "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Ngày tạo:", value = "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Xếp hạng:", value = "")
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            ProfileInfoRow(label = "Mô tả:", value = "")
        }
    }
}

// Hàng thông tin hồ sơ (label và value)
@Composable
fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

// Màn hình chính để hiển thị hồ sơ
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHeader(onBackClick = onBackClick)
        ProfileContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

// Xem trước giao diện màn hình hồ sơ
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ChessmateTheme {
        ProfileScreen()
    }
}