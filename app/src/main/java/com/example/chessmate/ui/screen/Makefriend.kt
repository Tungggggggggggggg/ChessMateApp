package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.SolidColor
import androidx.navigation.NavController
import com.example.chessmate.ui.components.Logo

// Thanh tiêu đề với các biểu tượng tin nhắn và hồ sơ
@Composable
fun Header(
    navController: NavController, // Thêm navController để điều hướng
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
        // Biểu tượng tin nhắn với chấm đỏ thông báo
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
        Text(
            text = "Tìm Bạn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        // Biểu tượng hồ sơ
        IconButton(
            onClick = { navController.navigate("profile") }, // Điều hướng đến ProfileScreen
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

// Nút quay lại
@Composable
fun BackButton(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Thanh tìm kiếm với ô nhập tên người chơi và biểu tượng kính lúp
@Composable
fun SearchBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp)
            .background(
                colorResource(id = R.color.color_c89f9c),
                shape = RoundedCornerShape(25.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                Text(
                    text = "Nhập tên người chơi ...",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                innerTextField()
            }
        )
        Image(
            painter = painterResource(id = R.drawable.search),
            contentDescription = "Tìm kiếm",
            modifier = Modifier
                .size(36.dp)
                .padding(end = 8.dp)
        )
    }
}

// Màn hình chính để tìm bạn
@Composable
fun FindFriendsScreen(
    navController: NavController // Thêm navController để điều hướng
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(navController = navController)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colorResource(id = R.color.color_c97c5d))
                .padding(16.dp),
        ) {
            BackButton(onBackClick = { navController.popBackStack() })
            Logo(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            Spacer(modifier = Modifier.height(20.dp))
            SearchBar()
        }
    }
}

// Xem trước giao diện màn hình tìm bạn
@Preview(showBackground = true)
@Composable
fun FindFriendsScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    FindFriendsScreen(navController)
}