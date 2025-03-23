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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessmate.R
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Chessboard(modifier: Modifier = Modifier) {
    // Tạo bàn cờ với các nhãn số và chữ
    Column(
        modifier = modifier
            .wrapContentSize(Alignment.Center)
            .border(5.dp, colorResource(id = R.color.color_c89f9c))
            .padding(5.dp)
    ) {
        // Khoảng trống phía trên bàn cờ
        Row(
            modifier = Modifier
                .size(width = 360.dp, height = 20.dp)
                .background(colorResource(id = R.color.color_c89f9c))
        ) {}

        // Hàng chứa bàn cờ và nhãn số bên phải
        Row {
            // Khoảng trống bên trái bàn cờ
            Column(
                modifier = Modifier
                    .size(width = 20.dp, height = 320.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            ) {}

            // Tạo bàn cờ 8x8
            Column {
                for (row in 7 downTo 0) {
                    Row {
                        for (col in 0 until 8) {
                            val isWhiteSquare = (row + col) % 2 == 1
                            val squareColor = if (isWhiteSquare) {
                                Color.White
                            } else {
                                colorResource(id = R.color.color_b36a5e)
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(squareColor),
                                contentAlignment = Alignment.Center
                            ) {
                                val piece = getPieceAtPosition(row, col)
                                if (piece != null) {
                                    Image(
                                        painter = painterResource(id = piece),
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
                            .background(colorResource(id = R.color.color_c89f9c)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${row + 1}",
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
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
                    .background(colorResource(id = R.color.color_c89f9c))
            )

            // Nhãn chữ A-H
            for (col in 0 until 8) {
                val letter = ('A' + col).toString()
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp)
                        .background(colorResource(id = R.color.color_c89f9c)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Ô trống ở góc dưới bên phải
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 20.dp)
                    .background(colorResource(id = R.color.color_c89f9c))
            )
        }
    }
}

@Composable
fun getPieceAtPosition(row: Int, col: Int): Int? {
    // Xác định vị trí và trả về quân cờ tương ứng
    val adjustedCol = 7 - col

    return when {
        // Quân trắng (hàng 1 và 2)
        row == 0 && (adjustedCol == 0 || adjustedCol == 7) -> R.drawable.white_rook
        row == 0 && (adjustedCol == 1 || adjustedCol == 6) -> R.drawable.white_knight
        row == 0 && (adjustedCol == 2 || adjustedCol == 5) -> R.drawable.white_bishop
        row == 0 && adjustedCol == 3 -> R.drawable.white_king
        row == 0 && adjustedCol == 4 -> R.drawable.white_queen
        row == 1 -> R.drawable.white_pawn
        // Quân đen (hàng 7 và 8)
        row == 7 && (adjustedCol == 0 || adjustedCol == 7) -> R.drawable.black_rook
        row == 7 && (adjustedCol == 1 || adjustedCol == 6) -> R.drawable.black_knight
        row == 7 && (adjustedCol == 2 || adjustedCol == 5) -> R.drawable.black_bishop
        row == 7 && adjustedCol == 3 -> R.drawable.black_king
        row == 7 && adjustedCol == 4 -> R.drawable.black_queen
        row == 6 -> R.drawable.black_pawn
        else -> null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChessboard() {
    Chessboard()
}