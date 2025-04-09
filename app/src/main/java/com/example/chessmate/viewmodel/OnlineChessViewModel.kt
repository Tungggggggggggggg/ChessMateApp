package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessmate.model.ChessGame
import com.example.chessmate.model.ChessPiece
import com.example.chessmate.model.Move
import com.example.chessmate.model.PieceColor
import com.example.chessmate.model.PieceType
import com.example.chessmate.model.Position
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Thêm import
import java.util.concurrent.atomic.AtomicBoolean

class OnlineChessViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val game = ChessGame()

    val board = mutableStateOf(game.getBoard())
    val currentTurn = mutableStateOf(game.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(game.isGameOver())
    val gameResult = mutableStateOf<String?>(null)
    val whiteTime = mutableStateOf(600)
    val blackTime = mutableStateOf(600)
    val isPromoting = mutableStateOf(false)
    val matchId = mutableStateOf<String?>(null)
    val playerColor = mutableStateOf<PieceColor?>(null)
    val drawRequest = mutableStateOf<String?>(null)
    private val matchData = mutableStateOf<Map<String, Any>?>(null)
    // Thêm state để thông báo lỗi hoặc timeout
    val matchmakingError = mutableStateOf<String?>(null)

    private var timerJob: Job? = null
    private var matchListener: ListenerRegistration? = null
    private var matchmakingJob: Job? = null
    private val isMatchmaking = AtomicBoolean(false)

    init {
        startMatchmaking()
    }

    private fun startMatchmaking() {
        val userId = auth.currentUser?.uid ?: return
        if (isMatchmaking.get()) return // Tránh gọi nhiều lần

        isMatchmaking.set(true)
        val queueData = hashMapOf(
            "userId" to userId,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "waiting"
        )

        // Thêm người chơi vào hàng đợi
        db.collection("matchmaking_queue")
            .document(userId)
            .set(queueData)
            .addOnSuccessListener {
                tryMatchmaking(userId)
            }
            .addOnFailureListener {
                matchmakingError.value = "Không thể tham gia hàng đợi. Vui lòng thử lại."
                isMatchmaking.set(false)
            }
    }

    private fun tryMatchmaking(userId: String) {
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            val timeoutSeconds = 30L
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
                val matched = tryMatchWithOpponent(userId)
                if (matched) {
                    isMatchmaking.set(false)
                    return@launch
                }
                delay(1000L) // Kiểm tra mỗi giây
            }

            // Timeout: Xóa khỏi hàng đợi và thông báo
            db.collection("matchmaking_queue").document(userId).delete()
            matchmakingError.value = "Không tìm thấy đối thủ trong $timeoutSeconds giây. Vui lòng thử lại."
            isMatchmaking.set(false)
        }
    }

    private suspend fun tryMatchWithOpponent(userId: String): Boolean {
        return try {
            var opponentId: String? = null
            val success = db.runTransaction { transaction ->
                // Tìm tất cả người chơi đang chờ, ngoại trừ chính mình
                val snapshot = db.collection("matchmaking_queue")
                    .whereEqualTo("status", "waiting")
                    .get()
                    .result

                val waitingPlayers = snapshot.documents
                    .filter { it.getString("userId") != userId }
                    .sortedBy { it.getTimestamp("timestamp")?.toDate()?.time }

                if (waitingPlayers.isEmpty()) {
                    return@runTransaction false
                }

                // Lấy người chơi đầu tiên trong hàng đợi
                val opponentDoc = waitingPlayers.first()
                opponentId = opponentDoc.getString("userId")

                if (opponentId != null) {
                    // Cập nhật trạng thái của cả hai người chơi
                    transaction.update(
                        db.collection("matchmaking_queue").document(userId),
                        "status",
                        "matched"
                    )
                    transaction.update(
                        db.collection("matchmaking_queue").document(opponentId!!),
                        "status",
                        "matched"
                    )
                    true
                } else {
                    false
                }
            }.addOnSuccessListener { success ->
                if (success && opponentId != null) {
                    createMatch(userId, opponentId!!)
                }
            }.addOnFailureListener {
                matchmakingError.value = "Lỗi khi ghép cặp. Vui lòng thử lại."
            }.await()
            success
        } catch (e: Exception) {
            matchmakingError.value = "Lỗi khi ghép cặp: ${e.message}"
            false
        }
    }

    private fun createMatch(player1Id: String, player2Id: String) {
        val matchId = db.collection("matches").document().id
        this.matchId.value = matchId

        val matchData = hashMapOf(
            "matchId" to matchId,
            "player1" to player1Id, // Trắng
            "player2" to player2Id, // Đen
            "board" to boardToMap(game.getBoard()),
            "currentTurn" to currentTurn.value.toString(),
            "whiteTime" to 600,
            "blackTime" to 600,
            "status" to "ongoing",
            "winner" to null,
            "drawRequest" to null,
            "lastMove" to null
        )

        db.collection("matches")
            .document(matchId)
            .set(matchData)
            .addOnSuccessListener {
                // Xóa người chơi khỏi hàng đợi
                db.collection("matchmaking_queue").document(player1Id).delete()
                db.collection("matchmaking_queue").document(player2Id).delete()

                // Xác định màu của người chơi
                playerColor.value = if (auth.currentUser?.uid == player1Id) PieceColor.WHITE else PieceColor.BLACK
                listenToMatchUpdates()
                startTimer()
            }
            .addOnFailureListener {
                matchmakingError.value = "Lỗi khi tạo trận đấu. Vui lòng thử lại."
            }
    }

    fun listenToMatchUpdates() {
        matchId.value?.let { id ->
            matchListener = db.collection("matches")
                .document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    // Lưu matchData vào state
                    val matchDataValue = snapshot.data ?: return@addSnapshotListener
                    matchData.value = matchDataValue

                    val boardData = matchDataValue["board"] as? List<List<Map<String, Any?>>>
                    val currentTurnStr = matchDataValue["currentTurn"] as? String
                    val whiteTimeData = matchDataValue["whiteTime"] as? Long
                    val blackTimeData = matchDataValue["blackTime"] as? Long
                    val status = matchDataValue["status"] as? String
                    val winner = matchDataValue["winner"] as? String
                    val drawRequestData = matchDataValue["drawRequest"] as? String

                    // Cập nhật trạng thái bàn cờ
                    if (boardData != null) {
                        board.value = mapToBoard(boardData)
                    }

                    // Cập nhật lượt đi
                    currentTurn.value = PieceColor.valueOf(currentTurnStr ?: "WHITE")

                    // Cập nhật thời gian
                    whiteTime.value = (whiteTimeData ?: 600).toInt()
                    blackTime.value = (blackTimeData ?: 600).toInt()

                    // Cập nhật yêu cầu cầu hòa
                    drawRequest.value = drawRequestData

                    // Kiểm tra trạng thái trận đấu
                    if (status != "ongoing") {
                        isGameOver.value = true
                        gameResult.value = when (status) {
                            "draw" -> "Game is a draw by agreement."
                            "surrendered" -> if (winner == auth.currentUser?.uid) {
                                "You win by opponent's surrender!"
                            } else {
                                "Opponent wins by your surrender!"
                            }
                            "checkmate" -> if (winner == auth.currentUser?.uid) {
                                "You win by checkmate!"
                            } else {
                                "Opponent wins by checkmate!"
                            }
                            else -> "Game ended."
                        }
                        timerJob?.cancel()
                    }
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!isGameOver.value && !isPromoting.value) {
                delay(1000L)
                if (currentTurn.value == PieceColor.WHITE) {
                    whiteTime.value = (whiteTime.value - 1).coerceAtLeast(0)
                    updateTime("whiteTime", whiteTime.value)
                    if (whiteTime.value == 0) {
                        endMatch(status = "checkmate", winner = matchData.value?.get("player2") as? String)
                        break
                    }
                } else {
                    blackTime.value = (blackTime.value - 1).coerceAtLeast(0)
                    updateTime("blackTime", blackTime.value)
                    if (blackTime.value == 0) {
                        endMatch(status = "checkmate", winner = matchData.value?.get("player1") as? String)
                        break
                    }
                }
            }
        }
    }

    private fun updateTime(field: String, time: Int) {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(field, time)
        }
    }

    fun onSquareClicked(row: Int, col: Int) {
        if (isPromoting.value || playerColor.value != currentTurn.value) return

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

    fun promotePawn(toType: PieceType) {
        game.promotePawn(toType)
        isPromoting.value = false
        updateGameState()
        startTimer()
    }

    fun requestDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update("drawRequest", auth.currentUser?.uid)
        }
    }

    fun acceptDraw() {
        endMatch(status = "draw", winner = null)
    }

    fun declineDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update("drawRequest", null)
        }
    }

    fun surrender() {
        val opponentId = if (playerColor.value == PieceColor.WHITE) {
            matchData.value?.get("player2") as? String
        } else {
            matchData.value?.get("player1") as? String
        }
        endMatch(status = "surrendered", winner = opponentId)
    }

    private fun endMatch(status: String, winner: String?) {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(
                    mapOf(
                        "status" to status,
                        "winner" to winner
                    )
                )
        }
    }

    private fun updateGameState() {
        board.value = game.getBoard()
        currentTurn.value = game.getCurrentTurn()
        isGameOver.value = game.isGameOver()
        gameResult.value = game.getGameResult()

        // Đồng bộ trạng thái với Firestore
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(
                    mapOf(
                        "board" to boardToMap(board.value),
                        "currentTurn" to currentTurn.value.toString(),
                        "lastMove" to game.getLastMove()?.let { lastMove ->
                            mapOf(
                                "from" to mapOf("row" to lastMove.first.row, "col" to lastMove.first.col),
                                "to" to mapOf("row" to lastMove.second.row, "col" to lastMove.second.col)
                            )
                        }
                    )
                )
        }
    }

    private fun boardToMap(board: Array<Array<ChessPiece?>>): List<List<Map<String, Any?>>> {
        return board.map { row ->
            row.map { piece ->
                if (piece == null) {
                    mapOf("type" to null, "color" to null, "position" to null)
                } else {
                    mapOf(
                        "type" to piece.type.toString(),
                        "color" to piece.color.toString(),
                        "position" to mapOf("row" to piece.position.row, "col" to piece.position.col)
                    )
                }
            }
        }
    }

    private fun mapToBoard(boardData: List<List<Map<String, Any?>>>): Array<Array<ChessPiece?>> {
        return Array(8) { row ->
            Array(8) { col ->
                val pieceData = boardData[row][col]
                val type = pieceData["type"]?.toString()
                val color = pieceData["color"]?.toString()
                val positionData = pieceData["position"] as? Map<String, Long>
                if (type != null && color != null && positionData != null) {
                    ChessPiece(
                        type = PieceType.valueOf(type),
                        color = PieceColor.valueOf(color),
                        position = Position(positionData["row"]!!.toInt(), positionData["col"]!!.toInt())
                    )
                } else {
                    null
                }
            }
        }
    }

    fun getPendingPromotion(): Position? = game.getPendingPromotion()

    // Thêm phương thức để hủy matchmaking khi thoát
    fun cancelMatchmaking() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("matchmaking_queue").document(userId).delete()
        }
        matchmakingJob?.cancel()
        isMatchmaking.set(false)
        matchmakingError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchListener?.remove()
        matchId.value?.let { id ->
            db.collection("matches").document(id).delete()
        }
        cancelMatchmaking()
    }
}