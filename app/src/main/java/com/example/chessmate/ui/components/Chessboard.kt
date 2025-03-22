package com.example.chessmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessmate.R


@Composable
fun Chessboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(5.dp, colorResource(id = R.color.board_border))
            .padding(5.dp)
    ) {
        // Thanh viền trên bàn cờ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(colorResource(id = R.color.board_border))
        ) {}

        // Hàng chứa bàn cờ và nhãn số bên phải
        Row {
            // Nhãn số (1-8) bên trái
            Column {
                for (row in 7 downTo 0) {
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 40.dp)
                            .background(colorResource(id = R.color.board_border)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${row + 1}",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Tạo bàn cờ 8x8
            Column {
                for (row in 7 downTo 0) {
                    Row {
                        for (col in 0 until 8) {
                            val isWhiteSquare = (row + col) % 2 == 1
                            val squareColor = if (isWhiteSquare) {
                                Color.White
                            } else {
                                colorResource(id = R.color.dark_square)
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(squareColor),
                                contentAlignment = Alignment.Center
                            ) {
                                val pieceRes = getPieceAtPosition(row, col)
                                pieceRes?.let {
                                    Image(
                                        painter = painterResource(id = it),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Nhãn số (1-8) bên phải bàn cờ
            Column {
                for (row in 7 downTo 0) {
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 40.dp)
                            .background(colorResource(id = R.color.board_border)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${row + 1}",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Hàng chứa nhãn chữ (A-H) bên dưới bàn cờ
        Row {
            // Ô trống ở góc dưới bên trái
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 20.dp)
                    .background(colorResource(id = R.color.board_border))
            )

            // Nhãn chữ A-H
            for (col in 0 until 8) {
                val letter = ('A' + col).toString()
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp)
                        .background(colorResource(id = R.color.board_border)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            // Ô trống ở góc dưới bên phải
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 20.dp)
                    .background(colorResource(id = R.color.board_border))
            )
        }
    }
}

@Composable
fun getPieceAtPosition(row: Int, col: Int): Int? {
    return when {
        row == 0 && (col == 0 || col == 7) -> R.drawable.white_rook
        row == 0 && (col == 1 || col == 6) -> R.drawable.white_knight
        row == 0 && (col == 2 || col == 5) -> R.drawable.white_bishop
        row == 0 && col == 3 -> R.drawable.white_queen
        row == 0 && col == 4 -> R.drawable.white_king
        row == 1 -> R.drawable.white_pawn
        row == 7 && (col == 0 || col == 7) -> R.drawable.black_rook
        row == 7 && (col == 1 || col == 6) -> R.drawable.black_knight
        row == 7 && (col == 2 || col == 5) -> R.drawable.black_bishop
        row == 7 && col == 3 -> R.drawable.black_queen
        row == 7 && col == 4 -> R.drawable.black_king
        row == 6 -> R.drawable.black_pawn
        else -> null
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewChessboard() {
    Chessboard()
}