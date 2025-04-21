package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessmate.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * ViewModel quản lý logic trò chơi cờ vua khi đấu với AI.
 */
class ChessViewModel : ViewModel() {
    private val game = ChessGame()

    // Trạng thái bàn cờ
    val board = mutableStateOf(game.getBoard())
    // Lượt đi hiện tại
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    // Các ô được đánh dấu (nước đi hợp lệ)
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    // Trạng thái trò chơi kết thúc
    val isGameOver = mutableStateOf(game.isGameOver())
    // Kết quả trò chơi
    val gameResult = mutableStateOf<String?>(null)
    // Trạng thái đang phong cấp
    val isPromoting = mutableStateOf(false)
    // Màu của người chơi, mặc định là trắng
    val playerColor = mutableStateOf(PieceColor.WHITE)

    /**
     * Xử lý sự kiện nhấn vào một ô trên bàn cờ.
     *
     * @param row Hàng của ô.
     * @param col Cột của ô.
     */
    fun onSquareClicked(row: Int, col: Int) {
        val position = Position(row, col)
        if (highlightedSquares.value.any { it.position == position }) {
            if (game.movePiece(position)) {
                updateGameState()
                highlightedSquares.value = emptyList()
                if (game.getPendingPromotion() != null) {
                    isPromoting.value = true
                } else if (!isGameOver.value && currentTurn.value == PieceColor.BLACK) {
                    triggerAIMove()
                }
            }
        } else {
            highlightedSquares.value = game.selectPiece(row, col)
        }
    }

    /**
     * Phong cấp Tốt thành một loại quân cờ.
     *
     * @param toType Loại quân cờ để phong cấp.
     */
    fun promotePawn(toType: PieceType) {
        game.promotePawn(toType)
        isPromoting.value = false
        updateGameState()
        if (!isGameOver.value && currentTurn.value == PieceColor.BLACK) {
            triggerAIMove()
        }
    }

    /**
     * Cập nhật trạng thái trò chơi (bàn cờ, lượt đi, kết quả).
     */
    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()
    }

    /**
     * Kích hoạt nước đi của AI khi đến lượt đen.
     */
    private fun triggerAIMove() {
        viewModelScope.launch {
            val bestMove = findBestMove()
            if (bestMove != null) {
                val (from, to) = bestMove
                game.selectPiece(from.row, from.col)
                game.movePiece(to)
                updateGameState()
                if (game.getPendingPromotion() != null) {
                    game.promotePawn(PieceType.QUEEN)
                    updateGameState()
                }
            }
        }
    }

    /**
     * Tìm nước đi tốt nhất cho AI (ưu tiên bắt quân có giá trị cao nhất).
     *
     * @return Cặp vị trí (từ, đến) hoặc null nếu không có nước đi.
     */
    private fun findBestMove(): Pair<Position, Position>? {
        val allMoves = getAllMoves(PieceColor.BLACK)
        if (allMoves.isEmpty()) return null

        var bestMove: Pair<Position, Position>? = null
        var bestCaptureValue = -1

        for (move in allMoves) {
            val (from, to) = move
            val capturedPiece = game.getPieceAt(to.row, to.col)
            if (capturedPiece != null && capturedPiece.color == PieceColor.WHITE) {
                val captureValue = when (capturedPiece.type) {
                    PieceType.PAWN -> 1
                    PieceType.KNIGHT -> 3
                    PieceType.BISHOP -> 3
                    PieceType.ROOK -> 5
                    PieceType.QUEEN -> 9
                    PieceType.KING -> 100
                }
                if (captureValue > bestCaptureValue) {
                    bestCaptureValue = captureValue
                    bestMove = move
                }
            }
        }

        return bestMove ?: allMoves.random()
    }

    /**
     * Lấy tất cả nước đi hợp lệ của một màu quân.
     *
     * @param color Màu của quân cờ.
     * @return Danh sách cặp vị trí (từ, đến).
     */
    private fun getAllMoves(color: PieceColor): List<Pair<Position, Position>> {
        val allMoves = mutableListOf<Pair<Position, Position>>()
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = game.getPieceAt(row, col)
                if (piece != null && piece.color == color) {
                    val moves = game.selectPiece(row, col)
                    moves.forEach { move ->
                        allMoves.add(Pair(Position(row, col), move.position))
                    }
                }
            }
        }
        return allMoves
    }

    /**
     * Lấy vị trí của Tốt đang chờ phong cấp.
     *
     * @return Vị trí của Tốt hoặc null nếu không có.
     */
    fun getPendingPromotion(): Position? = game.getPendingPromotion()
}

/**
 * ViewModel quản lý logic trò chơi cờ vua khi đấu với bạn bè.
 */
class FriendChessViewModel : ViewModel() {
    private val game = ChessGame()

    // Trạng thái bàn cờ
    val board = mutableStateOf(game.getBoard())
    // Lượt đi hiện tại
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    // Các ô được đánh dấu (nước đi hợp lệ)
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    // Trạng thái trò chơi kết thúc
    val isGameOver = mutableStateOf(game.isGameOver())
    // Kết quả trò chơi
    val gameResult = mutableStateOf<String?>(null)
    // Thời gian còn lại của người chơi trắng
    val whiteTime = mutableStateOf(600)
    // Thời gian còn lại của người chơi đen
    val blackTime = mutableStateOf(600)
    // Trạng thái đang phong cấp
    val isPromoting = mutableStateOf(false)
    // Màu của người chơi, mặc định là trắng
    val playerColor = mutableStateOf(PieceColor.WHITE)
    private var timerJob: Job? = null

    init {
        startTimer()
    }

    /**
     * Khởi động bộ đếm thời gian cho trận đấu.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!isGameOver.value && !isPromoting.value) {
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

    /**
     * Xử lý sự kiện nhấn vào một ô trên bàn cờ.
     *
     * @param row Hàng của ô.
     * @param col Cột của ô.
     */
    fun onSquareClicked(row: Int, col: Int) {
        if (isPromoting.value) return

        val position = Position(row, col)
        if (highlightedSquares.value.any { it.position == position }) {
            if (game.movePiece(position)) {
                updateGameState()
                highlightedSquares.value = emptyList()
                if (game.getPendingPromotion() != null) {
                    isPromoting.value = true
                    timerJob?.cancel()
                } else {
                    startTimer()
                }
            }
        } else {
            highlightedSquares.value = game.selectPiece(row, col)
        }
    }

    /**
     * Phong cấp Tốt thành một loại quân cờ.
     *
     * @param toType Loại quân cờ để phong cấp.
     */
    fun promotePawn(toType: PieceType) {
        game.promotePawn(toType)
        isPromoting.value = false
        updateGameState()
        startTimer()
    }

    /**
     * Cập nhật trạng thái trò chơi (bàn cờ, lượt đi, kết quả).
     */
    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()
    }

    /**
     * Lấy vị trí của Tốt đang chờ phong cấp.
     *
     * @return Vị trí của Tốt hoặc null nếu không có.
     */
    fun getPendingPromotion(): Position? = game.getPendingPromotion()

    /**
     * Dọn dẹp tài nguyên khi ViewModel bị hủy.
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
