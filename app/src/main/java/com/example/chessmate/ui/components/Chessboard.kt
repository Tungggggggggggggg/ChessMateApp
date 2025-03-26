package com.example.chessmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    // Quản lý trạng thái trò chơi
    val gameState = remember { ChessGame() }
    var highlightedSquares by remember { mutableStateOf<List<Position>>(emptyList()) }

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
                            val isHighlighted = highlightedSquares.contains(Position(row, col))

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isHighlighted) Color(0xFF90EE90) else squareColor
                                    )
                                    .clickable {
                                        if (isHighlighted) {
                                            // Di chuyển quân cờ
                                            gameState.movePiece(Position(row, col))
                                            highlightedSquares = emptyList()
                                        } else {
                                            // Chọn quân cờ và hiển thị các ô hợp lệ
                                            highlightedSquares = gameState.getValidMoves(row, col)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val piece = gameState.getPieceAt(row, col)
                                if (piece != null) {
                                    val pieceDrawable = getPieceDrawable(piece)
                                    Image(
                                        painter = painterResource(id = pieceDrawable),
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

// Lấy drawable tương ứng với quân cờ
@Composable
fun getPieceDrawable(piece: ChessPiece): Int {
    return when (piece.type) {
        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) R.drawable.white_pawn else R.drawable.black_pawn
        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) R.drawable.white_rook else R.drawable.black_rook
        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) R.drawable.white_knight else R.drawable.black_knight
        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) R.drawable.white_bishop else R.drawable.black_bishop
        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) R.drawable.white_queen else R.drawable.black_queen
        PieceType.KING -> if (piece.color == PieceColor.WHITE) R.drawable.white_king else R.drawable.black_king
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChessboard() {
    Chessboard()
}