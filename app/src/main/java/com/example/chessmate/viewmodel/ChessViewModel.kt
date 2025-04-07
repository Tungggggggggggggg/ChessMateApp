package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessmate.model.ChessGame
import com.example.chessmate.model.Move
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.PieceType
import com.example.chessmate.model.Position
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class ChessViewModel : ViewModel() {
    private val game = ChessGame()

    val board = mutableStateOf(game.getBoard())
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(game.isGameOver())
    val gameResult = mutableStateOf<String?>(null)

    fun onSquareClicked(row: Int, col: Int) {
        val position = Position(row, col)
        if (highlightedSquares.value.any { it.position == position }) {
            if (game.movePiece(position)) {
                updateGameState()
                highlightedSquares.value = emptyList()
                if (currentTurn.value == PieceColor.BLACK) {
                    triggerAIMove()
                }
            }
        } else {
            highlightedSquares.value = game.selectPiece(row, col)
        }
    }

    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()
    }

    private fun triggerAIMove() {
        viewModelScope.launch {
            delay(1000L)
            val allMoves = mutableListOf<Pair<Position, Position>>()
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val piece = game.getPieceAt(row, col)
                    if (piece != null && piece.color == PieceColor.BLACK) {
                        val moves = game.selectPiece(row, col)
                        moves.forEach { move ->
                            allMoves.add(Pair(Position(row, col), move.position))
                        }
                    }
                }
            }
            if (allMoves.isNotEmpty()) {
                val (from, to) = allMoves.random()
                game.selectPiece(from.row, from.col)
                game.movePiece(to)
                updateGameState()
            }
        }
    }
}

class FriendChessViewModel : ViewModel() {
    private val game = ChessGame()

    val board = mutableStateOf(game.getBoard())
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(game.isGameOver())
    val gameResult = mutableStateOf<String?>(null)
    val whiteTime = mutableStateOf(600) // 10 phút = 600 giây
    val blackTime = mutableStateOf(600)
    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!isGameOver.value && game.getPendingPromotion() == null) {
                delay(1000L)
                if (currentTurn.value == PieceColor.WHITE) {
                    whiteTime.value = (whiteTime.value - 1).coerceAtLeast(0)
                    if (whiteTime.value == 0) {
                        isGameOver.value = true
                        gameResult.value = "Black wins by timeout!"
                        break
                    }
                } else {
                    blackTime.value = (blackTime.value - 1).coerceAtLeast(0)
                    if (blackTime.value == 0) {
                        isGameOver.value = true
                        gameResult.value = "White wins by timeout!"
                        break
                    }
                }
            }
        }
    }

    fun onSquareClicked(row: Int, col: Int) {
        val position = Position(row, col)
        if (highlightedSquares.value.any { it.position == position }) {
            if (game.movePiece(position)) {
                updateGameState()
                highlightedSquares.value = emptyList()
                if (game.getPendingPromotion() == null) {
                    startTimer()
                }
            }
        } else {
            highlightedSquares.value = game.selectPiece(row, col)
        }
    }

    fun promotePawn(toType: PieceType) {
        game.promotePawn(toType)
        updateGameState()
        startTimer()
    }

    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()
    }

    fun getPendingPromotion(): Position? = game.getPendingPromotion()

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}