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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessmate.ui.components.ChessGame
import com.example.chessmate.ui.components.ChessPiece
import com.example.chessmate.ui.components.Position
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.ui.components.PieceColor
import com.example.chessmate.ui.theme.ChessmateTheme
import kotlinx.coroutines.delay

@Composable
fun PlayWithAIHeader(
    onBackClick: () -> Unit,
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
        // Nút thoát (biểu tượng X) - Đặt ở góc trái trên
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "X",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        // Biểu tượng người chơi và văn bản "AI" - Căn giữa
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Người chơi",
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
            // Hiển thị lượt đi hiện tại
            Text(
                text = "Lượt của: ${if (currentTurn == PieceColor.WHITE) "Trắng" else "Đen"}",
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )
        }
        // Box thời gian - Đặt bên trái của Column
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-80).dp)
                .padding(top = 36.dp)
                .width(80.dp)
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
        // Box chế độ - Đặt bên phải của Column
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 98.dp)
                .padding(top = 36.dp)
                .width(120.dp)
                .height(32.dp)
                .background(colorResource(id = R.color.color_eed7c5), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chế độ: Khó",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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
        // Biểu tượng người chơi và văn bản "BẠN" - Căn giữa
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Người chơi",
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
            // Hiển thị lượt đi hiện tại
            Text(
                text = "Lượt của: ${if (currentTurn == PieceColor.WHITE) "Trắng" else "Đen"}",
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )
        }
        // Box thời gian - Đặt bên phải của Column
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 80.dp)
                .padding(top = 20.dp)
                .width(80.dp)
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
}

@Composable
fun PlayWithAIScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    val gameState = remember { ChessGame() }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var gameResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gameState.getCurrentTurn()) {
        if (gameState.getCurrentTurn() == PieceColor.BLACK && !gameState.isGameOver()) {
            delay(1000L) // Đợi 1 giây để mô phỏng AI suy nghĩ
            val allMoves = mutableListOf<Pair<ChessPiece, Position>>()
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val piece = gameState.getPieceAt(row, col)
                    if (piece != null && piece.color == PieceColor.BLACK) {
                        val moves = gameState.getValidMoves(row, col)
                        moves.forEach { move ->
                            allMoves.add(Pair(piece, move))
                        }
                    }
                }
            }
            if (allMoves.isNotEmpty()) {
                val randomMove = allMoves.random()
                gameState.movePiece(randomMove.second)
            }
        }
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
            PlayWithAIHeader(
                onBackClick = onBackClick,
                currentTurn = gameState.getCurrentTurn()
            )
            // Bàn cờ
            Chessboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            // Footer
            PlayWithAIFooter(currentTurn = gameState.getCurrentTurn())
        }
    }

    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { showGameOverDialog = false },
            title = { Text("Game Over") },
            text = { Text(gameResult ?: "Game ended.") },
            confirmButton = {
                Button(onClick = {
                    showGameOverDialog = false
                    navController?.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayWithAIScreenPreview() {
    ChessmateTheme {
        PlayWithAIScreen()
    }
}