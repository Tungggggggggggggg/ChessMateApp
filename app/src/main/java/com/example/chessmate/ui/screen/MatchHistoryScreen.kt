package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.DatabaseHelper
import com.example.chessmate.R
import com.example.chessmate.ui.theme.ChessmateTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.tooling.preview.Preview


// Thanh tiêu đề với nút quay lại và tiêu đề "Lịch sử trận đấu"
@Composable
fun MatchHistoryHeader(
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
            text = "Lịch sử trận đấu",
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

// Dữ liệu mẫu cho một trận đấu
data class Match(
    val result: String,
    val date: String,
    val moves: Int,
    val opponent: String
)

// Hàng hiển thị thông tin một trận đấu
@Composable
fun MatchHistoryRow(match: Match) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Biểu tượng người chơi (hình tròn)
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Người chơi",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp)
            )

            // Cột thông tin chính (tên đối thủ, số bước, ngày giờ)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Dòng 1: Tên đối thủ
                Text(
                    text = match.opponent,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dòng 2: Số bước và ngày giờ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Số bước đi
                    Text(
                        text = "Số bước: ${match.moves}",
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    // Ngày giờ
                    Text(
                        text = match.date,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            // Kết quả trận đấu (bên phải)
            Text(
                text = match.result,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (match.result) {
                    "Thắng" -> Color(0xFF29BF12)
                    "Thua" -> Color(0xFFF21B3F)
                    else -> Color.Black
                },
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Đường gạch ngang kéo dài toàn bộ chiều rộng màn hình
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black)
        )
    }
}

// Nội dung chính của màn hình lịch sử trận đấu
@Composable
fun MatchHistoryContent(
    matches: List<Match>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        items(matches) { match ->
            MatchHistoryRow(match = match)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Màn hình chính để hiển thị lịch sử trận đấu
@Composable
fun MatchHistoryScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }

    // Lấy lịch sử trận đấu từ Realtime Database
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            DatabaseHelper.getMatchHistory(userId) { matchList ->
                matches = matchList
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MatchHistoryHeader(onBackClick = onBackClick)
        MatchHistoryContent(
            matches = matches,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

// Xem trước giao diện màn hình lịch sử trận đấu
@Preview(showBackground = true)
@Composable
fun MatchHistoryScreenPreview() {
    ChessmateTheme {
        MatchHistoryScreen()
    }
}