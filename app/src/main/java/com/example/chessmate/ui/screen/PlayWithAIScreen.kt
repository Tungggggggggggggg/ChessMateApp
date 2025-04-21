package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.chessmate.viewmodel.ChessViewModel

/**
 * Giao diện header cho màn hình chơi cờ với AI, hiển thị thông tin lượt chơi và nút thoát.
 *
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param currentTurn Màu của quân cờ hiện tại (trắng hoặc đen).
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun PlayWithAIHeader(
    onBackClick: () -> Unit,
    currentTurn: PieceColor,
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
        // Thông tin AI và lượt hiện tại
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "AI",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AI",
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
 * Giao diện footer cho màn hình chơi cờ với AI, hiển thị thông tin người chơi và lượt hiện tại.
 *
 * @param currentTurn Màu của quân cờ hiện tại (trắng hoặc đen).
 * @param modifier Modifier tùy chỉnh giao diện.
 */
@Composable
fun PlayWithAIFooter(
    currentTurn: PieceColor,
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
                contentDescription = "Bạn",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BẠN",
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
    }
}

/**
 * Màn hình chính để chơi cờ với AI, hiển thị bàn cờ, header, footer và các dialog liên quan.
 *
 * @param navController Điều hướng để quay lại màn hình trước đó.
 * @param onBackClick Hàm xử lý khi người dùng nhấn nút quay lại.
 * @param viewModel ViewModel quản lý logic trò chơi.
 */
@Composable
fun PlayWithAIScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() },
    viewModel: ChessViewModel = viewModel()
) {
    val showGameOverDialog = remember { mutableStateOf(viewModel.isGameOver.value) }

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
            // Header hiển thị thông tin AI và lượt hiện tại
            PlayWithAIHeader(
                onBackClick = onBackClick,
                currentTurn = viewModel.currentTurn.value
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
            // Footer hiển thị thông tin người chơi và lượt hiện tại
            PlayWithAIFooter(currentTurn = viewModel.currentTurn.value)
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