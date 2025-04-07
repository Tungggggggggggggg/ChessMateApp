package com.example.chessmate.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessmate.R
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.PieceType
import com.example.chessmate.ui.components.Chessboard
import com.example.chessmate.viewmodel.FriendChessViewModel

@Composable
fun PromotionDialog(
    onSelect: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn quân để phong cấp") },
        text = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { onSelect(PieceType.QUEEN) }) { Text("Hậu") }
                Button(onClick = { onSelect(PieceType.ROOK) }) { Text("Xe") }
                Button(onClick = { onSelect(PieceType.BISHOP) }) { Text("Tượng") }
                Button(onClick = { onSelect(PieceType.KNIGHT) }) { Text("Mã") }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun PlayWithFriendHeader(
    onBackClick: () -> Unit,
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
}

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

@Composable
fun PlayWithFriendScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() },
    viewModel: FriendChessViewModel = viewModel()
) {
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
            PlayWithFriendHeader(
                onBackClick = onBackClick,
                currentTurn = viewModel.currentTurn.value,
                time = viewModel.blackTime.value
            )
            Chessboard(
                board = viewModel.board.value,
                highlightedSquares = viewModel.highlightedSquares.value,
                onSquareClicked = { row, col -> viewModel.onSquareClicked(row, col) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            PlayWithFriendFooter(
                currentTurn = viewModel.currentTurn.value,
                time = viewModel.whiteTime.value
            )
        }
    }

    if (viewModel.getPendingPromotion() != null) {
        PromotionDialog(
            onSelect = { pieceType ->
                viewModel.promotePawn(pieceType)
            },
            onDismiss = {}
        )
    }

    if (viewModel.isGameOver.value) {
        AlertDialog(
            onDismissRequest = { navController?.popBackStack() },
            title = { Text("Game Over") },
            text = { Text(viewModel.gameResult.value ?: "Game ended.") },
            confirmButton = {
                Button(onClick = { navController?.popBackStack() }) {
                    Text("OK")
                }
            }
        )
    }
}