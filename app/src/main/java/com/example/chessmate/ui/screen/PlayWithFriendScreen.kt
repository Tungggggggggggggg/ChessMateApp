package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.model.PieceColor
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.PromotionDialog
import com.example.chessmate.viewmodel.FriendChessViewModel

/**
 * Giao diện header cho màn hình chơi cờ với bạn bè, hiển thị thông tin lượt chơi, thời gian và nút thoát.
 *
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param currentTurn Màu của quân cờ hiện tại (trắng hoặc đen).
 * @param time Thời gian còn lại của người chơi (tính bằng giây).
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun PlayWithFriendHeader(
    onBackClick: () -> Unit,
    currentTurn: PieceColor,
    time: Int,
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(108.dp)
    ) {
        // Nút thoát trận đấu
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = CircleShape)
                .clickable { showExitDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "X",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Thông tin người chơi và lượt hiện tại
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Người chơi 2",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Người chơi 2",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lượt của: ${if (currentTurn == PieceColor.WHITE) "Trắng" else "Đen"}",
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        // Hiển thị thời gian còn lại
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-96).dp)
                .padding(top = 36.dp)
                .width(80.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${time / 60}:${(time % 60).toString().padStart(2, '0')}",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Dialog xác nhận thoát trận đấu
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Thoát trận đấu",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn thoát trận đấu không?",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Hủy", color = Color.White)
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}

/**
 * Giao diện footer cho màn hình chơi cờ với bạn bè, hiển thị thông tin người chơi và lượt hiện tại.
 *
 * @param currentTurn Màu của quân cờ hiện tại (trắng hoặc đen).
 * @param time Thời gian còn lại của người chơi (tính bằng giây).
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun PlayWithFriendFooter(
    currentTurn: PieceColor,
    time: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(108.dp)
    ) {
        // Thông tin người chơi và lượt hiện tại
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Người chơi 1",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Người chơi 1",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lượt của: ${if (currentTurn == PieceColor.WHITE) "Trắng" else "Đen"}",
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        // Hiển thị thời gian còn lại
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 90.dp)
                .padding(top = 20.dp)
                .width(80.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${time / 60}:${(time % 60).toString().padStart(2, '0')}",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Màn hình chính để chơi cờ với bạn bè, hiển thị bàn cờ, header, footer và các dialog liên quan.
 *
 * @param navController Điều hướng để quay lại màn hình trước đó.
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param viewModel ViewModel quản lý logic trò chơi.
 */
@Composable
fun PlayWithFriendScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() },
    viewModel: FriendChessViewModel = viewModel()
) {
    val showGameOverDialog = remember { mutableStateOf(false) }

    // Hiển thị dialog khi trò chơi kết thúc
    if (viewModel.isGameOver.value && !showGameOverDialog.value) {
        showGameOverDialog.value = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.color_c97c5d)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header hiển thị thông tin người chơi và thời gian
            PlayWithFriendHeader(
                onBackClick = onBackClick,
                currentTurn = viewModel.currentTurn.value,
                time = viewModel.blackTime.value
            )
            // Bàn cờ chính
            Chessboard(
                board = viewModel.board.value,
                highlightedSquares = viewModel.highlightedSquares.value,
                onSquareClicked = { row, col -> viewModel.onSquareClicked(row, col) },
                playerColor = viewModel.playerColor.value,
                clickable = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            // Footer hiển thị thông tin người chơi và thời gian
            PlayWithFriendFooter(
                currentTurn = viewModel.currentTurn.value,
                time = viewModel.whiteTime.value
            )
        }
    }

    // Dialog hiển thị khi cần chọn quân để phong cấp
    if (viewModel.isPromoting.value) {
        PromotionDialog(
            playerColor = viewModel.playerColor.value,
            onSelect = { pieceType ->
                viewModel.promotePawn(pieceType)
            },
            onDismiss = {}
        )
    }

    // Dialog thông báo khi trò chơi kết thúc
    if (showGameOverDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            modifier = Modifier.background(colorResource(id = R.color.color_c97c5d)),
            title = {
                Text(
                    text = "Ván đấu kết thúc",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = viewModel.gameResult.value ?: "Game ended.",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGameOverDialog.value = false
                        navController?.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.color_c89f9c)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            containerColor = colorResource(id = R.color.color_c97c5d)
        )
    }
}