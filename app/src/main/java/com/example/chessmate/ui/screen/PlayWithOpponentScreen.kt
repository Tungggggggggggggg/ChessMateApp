package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.components.ChessGame
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.PieceColor
import com.example.chessmate.ui.theme.ChessmateTheme

@Composable
fun CustomDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) { /* Ngăn tương tác với màn hình chính */ }
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.8f)
                .background(
                    color = colorResource(id = R.color.color_c97c5d),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp)
                            .background(
                                color = colorResource(id = R.color.color_eed7c5),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Có",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp)
                            .background(
                                color = Color.LightGray,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayWithOpponentHeader(
    onBackClick: () -> Unit,
    onExitConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(120.dp)
    ) {
        // Nút thoát (biểu tượng X) - Đặt ở góc trái trên
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
        // Biểu tượng người chơi và văn bản "Đối thủ" - Căn giữa
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Đối thủ",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Đối thủ",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Điểm và thời gian - Đặt bên trái của Column
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-106).dp, y = 10.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = "Điểm: 230",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .offset(x = 8.dp)
                    .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "10:00",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Nút "Kết bạn" - Đặt bên phải của Column
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 106.dp)
                .padding(top = 24.dp)
                .width(94.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { /* TODO: Xử lý kết bạn */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Kết bạn",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Dialog khi nhấn nút thoát
    if (showExitDialog) {
        CustomDialog(
            title = "Bạn có chắc chắn muốn thoát trận đấu không?",
            onConfirm = {
                showExitDialog = false
                onExitConfirm()
            },
            onDismiss = { showExitDialog = false }
        )
    }
}

@Composable
fun PlayWithOpponentFooter(
    onOfferDraw: () -> Unit,
    onSurrender: () -> Unit,
    onChatClick: () -> Unit = { /* TODO: Xử lý mở chat */ },
    modifier: Modifier = Modifier
) {
    var showDrawDialog by remember { mutableStateOf(false) }
    var showSurrenderDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_c89f9c))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(108.dp)
    ) {
        // Biểu tượng người chơi và văn bản "BẠN" - Căn trái
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Người chơi",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "BẠN",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Điểm: 250",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Nút "Cầu hòa" - Đặt ở giữa, lệch trái
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-14).dp, y = 10.dp)
                .padding(top = 20.dp)
                .width(80.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { showDrawDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cầu hòa",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Nút "Đầu hàng" - Đặt ở giữa, lệch phải
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 86.dp, y = 10.dp)
                .padding(top = 20.dp)
                .width(90.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { showSurrenderDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Đầu hàng",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Nút chat - Đặt trước box thời gian
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-30).dp, y = (-36).dp)
                .padding(top = 20.dp)
                .width(38.dp)
                .height(38.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp))
                .clickable { onChatClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.message),
                contentDescription = "Message",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
        }
        // Box thời gian - Đặt bên phải, cùng hàng với "BẠN"
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 24.dp)
                .offset(x = (-56).dp, y = (-36).dp)
                .width(82.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "10:00",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Dialog khi nhấn "Câu hòa"
    if (showDrawDialog) {
        CustomDialog(
            title = "Bạn có chắc chắn muốn cầu hòa trận đấu không?",
            onConfirm = {
                showDrawDialog = false
                onOfferDraw()
            },
            onDismiss = { showDrawDialog = false }
        )
    }

    // Dialog khi nhấn "Đầu hàng"
    if (showSurrenderDialog) {
        CustomDialog(
            title = "Bạn có chắc chắn muốn đầu hàng trận đấu không?",
            onConfirm = {
                showSurrenderDialog = false
                onSurrender()
            },
            onDismiss = { showSurrenderDialog = false }
        )
    }
}

@Composable
fun PlayWithOpponentScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    val gameState = remember { ChessGame() }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var gameResult by remember { mutableStateOf<String?>(null) }

    // Kiểm tra trạng thái trò chơi sau mỗi nước đi
    LaunchedEffect(gameState.getCurrentTurn()) {
        if (gameState.isGameOver()) {
            gameResult = gameState.getGameResult()
            showGameOverDialog = true
        }
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
            // Thanh tiêu đề
            PlayWithOpponentHeader(
                onBackClick = onBackClick,
                onExitConfirm = onBackClick
            )
            // Bàn cờ
            Chessboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            // Footer
            PlayWithOpponentFooter(
                onOfferDraw = {
                    gameResult = "Game is a draw by agreement."
                    showGameOverDialog = true
                },
                onSurrender = {
                    gameResult = if (gameState.getCurrentTurn() == PieceColor.WHITE) {
                        "Black wins by surrender!"
                    } else {
                        "White wins by surrender!"
                    }
                    showGameOverDialog = true
                }
            )
        }
    }

    // Dialog khi trò chơi kết thúc
    if (showGameOverDialog) {
        CustomDialog(
            title = gameResult ?: "Game ended.",
            onConfirm = {
                showGameOverDialog = false
                navController?.popBackStack()
            },
            onDismiss = {
                showGameOverDialog = false
                navController?.popBackStack()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayWithOpponentScreenPreview() {
    ChessmateTheme {
        PlayWithOpponentScreen()
    }
}