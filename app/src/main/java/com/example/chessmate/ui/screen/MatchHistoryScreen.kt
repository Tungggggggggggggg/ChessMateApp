package com.example.chessmate.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.ui.theme.ChessmateTheme
import com.example.chessmate.viewmodel.MatchHistoryViewModel
import com.example.chessmate.model.Match
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Giao diện header cho màn hình lịch sử trận đấu, hiển thị tiêu đề và nút quay lại.
 *
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param modifier Modifier tùy chỉnh giao diện.
 */
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
        // Nút quay lại
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
        // Tiêu đề lịch sử trận đấu
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

/**
 * Giao diện hiển thị một hàng trong danh sách lịch sử trận đấu, bao gồm thông tin đối thủ, kết quả và thời gian.
 *
 * @param match Thông tin trận đấu cần hiển thị.
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun MatchHistoryRow(
    match: Match,
    modifier: Modifier = Modifier
) {
    // Chuyển đổi định dạng ngày giờ từ "yyyy-MM-dd HH:mm" sang "HH:mm dd/MM/yyyy"
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val formattedDate = try {
        val date = inputFormat.parse(match.date)
        outputFormat.format(date)
    } catch (e: Exception) {
        match.date
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                when (match.result) {
                    "Thắng" -> colorResource(id = R.color.win_background)
                    "Thua" -> colorResource(id = R.color.lose_background)
                    else -> colorResource(id = R.color.draw_background)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hiển thị ký hiệu kết quả (W, L, D)
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .background(
                    when (match.result) {
                        "Thắng" -> colorResource(id = R.color.win_indicator)
                        "Thua" -> colorResource(id = R.color.lose_indicator)
                        else -> colorResource(id = R.color.draw_indicator)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (match.result) {
                    "Thắng" -> "W"
                    "Thua" -> "L"
                    else -> "D"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Ảnh đại diện đối thủ
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Ảnh đại diện đối thủ",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        // Thông tin trận đấu
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tên đối thủ
            Text(
                text = match.opponent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            // Số bước và thời gian
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${match.moves} bước",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Giao diện nội dung chính của màn hình lịch sử trận đấu, hiển thị danh sách các trận đấu hoặc trạng thái tải.
 *
 * @param modifier Modifier tùy chỉnh giao diện.
 * @param matches Danh sách các trận đấu cần hiển thị.
 * @param isLoading Trạng thái đang tải dữ liệu.
 * @param error Thông báo lỗi nếu có.
 */
@Composable
fun MatchHistoryContent(
    modifier: Modifier = Modifier,
    matches: List<Match>,
    isLoading: Boolean,
    error: String?
) {
    val context = LocalContext.current

    // Hiển thị thông báo lỗi nếu có
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c97c5d))
    ) {
        if (isLoading) {
            // Hiển thị vòng tròn tải khi đang lấy dữ liệu
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (matches.isEmpty()) {
            // Hiển thị thông báo khi không có lịch sử trận đấu
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có lịch sử trận đấu.",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        } else {
            // Hiển thị danh sách các trận đấu
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matches) { match ->
                    MatchHistoryRow(match = match)
                }
            }
        }
    }
}

/**
 * Màn hình chính hiển thị lịch sử các trận đấu của người dùng.
 *
 * @param navController Điều hướng để quay lại màn hình trước đó.
 * @param userId ID của người dùng để lấy lịch sử trận đấu.
 * @param viewModel ViewModel quản lý logic lịch sử trận đấu.
 */
@Composable
fun MatchHistoryScreen(
    navController: NavController? = null,
    userId: String,
    viewModel: MatchHistoryViewModel = viewModel()
) {
    // Tải lịch sử trận đấu khi userId thay đổi
    LaunchedEffect(userId) {
        viewModel.loadMatchHistory(userId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header hiển thị tiêu đề và nút quay lại
        MatchHistoryHeader(
            onBackClick = { navController?.popBackStack() }
        )
        // Nội dung danh sách lịch sử trận đấu
        MatchHistoryContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            matches = viewModel.matches.value,
            isLoading = viewModel.isLoading.value,
            error = viewModel.error.value
        )
    }
}