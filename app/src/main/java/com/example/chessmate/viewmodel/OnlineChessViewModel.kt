package com.example.chessmate.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicBoolean

class OnlineChessViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val chessGame = ChessGame()

    val board = mutableStateOf(chessGame.getBoard())
    val currentTurn = mutableStateOf(chessGame.getCurrentTurn())
    val highlightedSquares = mutableStateOf<List<Move>>(emptyList())
    val isGameOver = mutableStateOf(chessGame.isGameOver())
    val gameResult = mutableStateOf<String?>(chessGame.getGameResult())
    val whiteTime = mutableStateOf(600)
    val blackTime = mutableStateOf(600)
    val isPromoting = mutableStateOf(false)
    val matchId = mutableStateOf<String?>(null)
    val playerColor = mutableStateOf<PieceColor?>(null)
    val drawRequest = mutableStateOf<String?>(null)
    val moveHistory = mutableStateListOf<String>()
    private val matchData = mutableStateOf<Map<String, Any>?>(null)
    val matchmakingError = mutableStateOf<String?>(null)

    private var timerJob: Job? = null
    private var matchListener: ListenerRegistration? = null
    private var matchmakingListener: ListenerRegistration? = null
    private var matchmakingJob: Job? = null
    private val isMatchmaking = AtomicBoolean(false)
    private var lastMoveByThisDevice = false

    sealed class MatchStatus {
        object Ongoing : MatchStatus()
        object Draw : MatchStatus()
        object Surrendered : MatchStatus()
        object Checkmate : MatchStatus()

        override fun toString(): String {
            return when (this) {
                is Ongoing -> "ongoing"
                is Draw -> "draw"
                is Surrendered -> "surrendered"
                is Checkmate -> "checkmate"
            }
        }

        companion object {
            fun fromString(status: String?): MatchStatus {
                return when (status) {
                    "draw" -> Draw
                    "surrendered" -> Surrendered
                    "checkmate" -> Checkmate
                    "ongoing" -> Ongoing
                    else -> Ongoing
                }
            }
        }
    }

    init {
        startMatchmaking()
    }

    fun startMatchmaking() {
        val userId = auth.currentUser?.uid ?: run {
            matchmakingError.value = "Vui lòng đăng nhập để chơi trực tuyến."
            return
        }
        if (isMatchmaking.get()) return

        isMatchmaking.set(true)
        val queueData = hashMapOf(
            "userId" to userId,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "waiting",
            "matchId" to null
        )

        viewModelScope.launch {
            try {
                db.collection("matchmaking_queue")
                    .document(userId)
                    .set(queueData)
                    .await()
                listenForMatchmaking(userId)
            } catch (e: Exception) {
                matchmakingError.value = "Không thể tham gia hàng đợi: ${e.message}"
                isMatchmaking.set(false)
                db.collection("matchmaking_queue").document(userId).delete()
            }
        }
    }

    private fun listenForMatchmaking(userId: String) {
        matchmakingListener?.remove()
        matchmakingListener = db.collection("matchmaking_queue")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    matchmakingError.value = "Lỗi nghe hàng đợi: ${error?.message}"
                    isMatchmaking.set(false)
                    return@addSnapshotListener
                }

                val status = snapshot.getString("status")
                val matchIdFromQueue = snapshot.getString("matchId")

                if (status == "matched" && matchIdFromQueue != null) {
                    matchId.value = matchIdFromQueue
                    viewModelScope.launch {
                        try {
                            val matchDoc = db.collection("matches").document(matchIdFromQueue).get().await()
                            if (!matchDoc.exists()) {
                                matchmakingError.value = "Trận đấu không tồn tại."
                                cancelMatchmaking()
                                return@launch
                            }
                            val player1Id = matchDoc.getString("player1")
                            val player2Id = matchDoc.getString("player2")
                            val currentUserId = auth.currentUser?.uid
                            if (player1Id == null || player2Id == null || currentUserId == null) {
                                matchmakingError.value = "Không thể xác định người chơi."
                                cancelMatchmaking()
                                return@launch
                            }
                            if (playerColor.value == null) {
                                playerColor.value = if (currentUserId == player1Id) PieceColor.WHITE else PieceColor.BLACK
                                println("listenForMatchmaking: playerColor set to ${playerColor.value} for user $currentUserId (player1: $player1Id, player2: $player2Id)")
                            }
                            listenToMatchUpdates()
                            startTimer()
                        } catch (e: Exception) {
                            matchmakingError.value = "Lỗi khi truy xuất trận đấu: ${e.message}"
                            cancelMatchmaking()
                        }
                    }
                    db.collection("matchmaking_queue").document(userId).delete()
                    isMatchmaking.set(false)
                    matchmakingListener?.remove()
                } else if (status == "waiting") {
                    viewModelScope.launch {
                        tryMatchmaking(userId)
                    }
                }
            }
    }

    private suspend fun tryMatchWithOpponent(userId: String): Boolean {
        return try {
            val snapshot = db.collection("matchmaking_queue")
                .whereEqualTo("status", "waiting")
                .get()
                .await()

            val waitingPlayers = snapshot.documents
                .filter { it.getString("userId") != userId }
                .sortedBy { it.getTimestamp("timestamp")?.toDate()?.time }

            if (waitingPlayers.isEmpty()) return false

            val opponentDoc = waitingPlayers.first()
            val opponentId = opponentDoc.getString("userId") ?: return false

            val matchId = db.collection("matches").document().id
            val initialBoard = createInitialBoard()
            val matchData = hashMapOf(
                "matchId" to matchId,
                "player1" to userId,
                "player2" to opponentId,
                "board" to boardToFlatMap(initialBoard),
                "currentTurn" to PieceColor.WHITE.toString(),
                "whiteTime" to 600,
                "blackTime" to 600,
                "status" to MatchStatus.Ongoing.toString(),
                "winner" to null,
                "drawRequest" to null,
                "lastMove" to null,
                "fiftyMoveCounter" to 0,
                "positionHistory" to chessGame.getPositionHistory(),
                "moveHistory" to emptyList<String>(),
                "whiteKingPosition" to mapOf("row" to 0, "col" to 4),
                "blackKingPosition" to mapOf("row" to 7, "col" to 4),
                "hasMoved" to mapOf(
                    "white_king" to false,
                    "white_kingside_rook" to false,
                    "white_queenside_rook" to false,
                    "black_king" to false,
                    "black_kingside_rook" to false,
                    "black_queenside_rook" to false
                )
            )

            val success = db.runTransaction { transaction ->
                val userRef = db.collection("matchmaking_queue").document(userId)
                val opponentRef = db.collection("matchmaking_queue").document(opponentId)

                val userDoc = transaction.get(userRef)
                val opponentDoc = transaction.get(opponentRef)

                if (userDoc.exists() && opponentDoc.exists() &&
                    userDoc.getString("status") == "waiting" &&
                    opponentDoc.getString("status") == "waiting"
                ) {
                    transaction.set(db.collection("matches").document(matchId), matchData)
                    transaction.update(userRef, mapOf("status" to "matched", "matchId" to matchId))
                    transaction.update(opponentRef, mapOf("status" to "matched", "matchId" to matchId))
                    true
                } else {
                    false
                }
            }.await()

            if (success) {
                this.matchId.value = matchId
                if (playerColor.value == null) {
                    playerColor.value = PieceColor.WHITE
                    println("tryMatchWithOpponent: playerColor set to ${playerColor.value} for user $userId")
                }
                listenToMatchUpdates()
                startTimer()
                return true
            }
            false
        } catch (e: Exception) {
            matchmakingError.value = "Lỗi khi ghép cặp: ${e.message}"
            db.collection("matchmaking_queue").document(userId).delete()
            return false
        }
    }

    private suspend fun tryMatchmaking(userId: String) {
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            val timeoutSeconds = 60L
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000 && isMatchmaking.get()) {
                val matched = tryMatchWithOpponent(userId)
                if (matched) return@launch
                delay(2000L)
            }

            if (isMatchmaking.get()) {
                db.collection("matchmaking_queue").document(userId).delete()
                matchmakingError.value = "Không tìm thấy đối thủ trong $timeoutSeconds giây."
                isMatchmaking.set(false)
            }
        }
    }

    fun listenToMatchUpdates() {
        matchId.value?.let { id ->
            matchListener?.remove()
            matchListener = db.collection("matches")
                .document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        matchmakingError.value = "Lỗi nghe trận đấu: ${error?.message}"
                        return@addSnapshotListener
                    }

                    if (!snapshot.exists()) {
                        isGameOver.value = true
                        gameResult.value = "Đối thủ đã thoát. Bạn thắng!"
                        timerJob?.cancel()
                        return@addSnapshotListener
                    }

                    val matchDataValue = snapshot.data ?: return@addSnapshotListener
                    matchData.value = matchDataValue

                    val boardData = matchDataValue["board"] as? List<Map<String, Any?>>
                    val currentTurnStr = matchDataValue["currentTurn"] as? String
                    val whiteTimeData = matchDataValue["whiteTime"] as? Long
                    val blackTimeData = matchDataValue["blackTime"] as? Long
                    val status = MatchStatus.fromString(matchDataValue["status"] as? String)
                    val winner = matchDataValue["winner"] as? String
                    val drawRequestData = matchDataValue["drawRequest"] as? String
                    val moveHistoryData = matchDataValue["moveHistory"] as? List<String>
                    val fiftyMoveCounterData = matchDataValue["fiftyMoveCounter"] as? Long
                    val positionHistoryData = matchDataValue["positionHistory"] as? List<String>
                    val whiteKingPositionData = matchDataValue["whiteKingPosition"] as? Map<String, Long>
                    val blackKingPositionData = matchDataValue["blackKingPosition"] as? Map<String, Long>
                    val hasMovedData = matchDataValue["hasMoved"] as? Map<String, Boolean>
                    val lastMoveData = matchDataValue["lastMove"] as? Map<String, Map<String, Long>>

                    if (boardData != null) {
                        val newBoard = flatMapToBoard(boardData)
                        for (row in 0 until 8) {
                            for (col in 0 until 8) {
                                chessGame.getBoard()[row][col] = newBoard[row][col]
                            }
                        }
                        board.value = chessGame.getBoard()
                    }

                    if (!lastMoveByThisDevice) {
                        val newTurn = try {
                            PieceColor.valueOf(currentTurnStr ?: "WHITE")
                        } catch (e: Exception) {
                            PieceColor.WHITE
                        }
                        currentTurn.value = newTurn
                        chessGame.setCurrentTurn(newTurn)
                    }
                    lastMoveByThisDevice = false

                    whiteTime.value = (whiteTimeData ?: 600).toInt()
                    blackTime.value = (blackTimeData ?: 600).toInt()
                    drawRequest.value = drawRequestData
                    moveHistory.clear()
                    moveHistory.addAll(moveHistoryData ?: emptyList())

                    if (fiftyMoveCounterData != null) {
                        chessGame.setFiftyMoveCounter(fiftyMoveCounterData.toInt())
                    }
                    if (positionHistoryData != null) {
                        chessGame.setPositionHistory(positionHistoryData)
                    }
                    if (whiteKingPositionData != null) {
                        val row = whiteKingPositionData["row"]?.toInt() ?: 0
                        val col = whiteKingPositionData["col"]?.toInt() ?: 4
                        chessGame.setWhiteKingPosition(Position(row, col))
                    }
                    if (blackKingPositionData != null) {
                        val row = blackKingPositionData["row"]?.toInt() ?: 7
                        val col = blackKingPositionData["col"]?.toInt() ?: 4
                        chessGame.setBlackKingPosition(Position(row, col))
                    }
                    if (hasMovedData != null) {
                        hasMovedData.forEach { (key, value) ->
                            chessGame.setHasMoved(key, value)
                        }
                    }
                    if (lastMoveData != null) {
                        val fromData = lastMoveData["from"]
                        val toData = lastMoveData["to"]
                        val from = if (fromData != null) {
                            Position(fromData["row"]?.toInt() ?: 0, fromData["col"]?.toInt() ?: 0)
                        } else null
                        val to = if (toData != null) {
                            Position(toData["row"]?.toInt() ?: 0, toData["col"]?.toInt() ?: 0)
                        } else null
                        chessGame.setLastMove(from, to)
                    }

                    // Kiểm tra trạng thái trò chơi sau khi đồng bộ dữ liệu từ Firestore
                    chessGame.updateGameState()
                    isGameOver.value = chessGame.isGameOver() || status != MatchStatus.Ongoing
                    gameResult.value = chessGame.getGameResult()

                    if (status != MatchStatus.Ongoing || chessGame.isGameOver()) {
                        isGameOver.value = true
                        gameResult.value = when {
                            status == MatchStatus.Draw -> {
                                chessGame.getGameResult() ?: "Hết cờ! Ván đấu hòa."
                            }
                            status == MatchStatus.Surrendered -> if (winner == auth.currentUser?.uid) "Bạn thắng! Vì đối thủ đầu hàng" else "Bạn thua! Vì đã đầu hàng"
                            status == MatchStatus.Checkmate || chessGame.isCheckmatePublic() -> if (winner == auth.currentUser?.uid) "Bạn thắng! Vì đã chiếu hết đối thủ" else "Bạn thua! Vì đã bị đối thủ chiếu hết"
                            else -> chessGame.getGameResult() ?: "Trò chơi kết thúc."
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
                matchId.value?.let { id ->
                    try {
                        db.runTransaction { transaction ->
                            val matchRef = db.collection("matches").document(id)
                            val snapshot = transaction.get(matchRef)

                            if (!snapshot.exists()) {
                                isGameOver.value = true
                                gameResult.value = "Trận đấu đã kết thúc do tài liệu bị xóa."
                                timerJob?.cancel()
                                return@runTransaction
                            }

                            var whiteTime = (snapshot.getLong("whiteTime") ?: 600).toInt()
                            var blackTime = (snapshot.getLong("blackTime") ?: 600).toInt()

                            if (currentTurn.value == PieceColor.WHITE) {
                                whiteTime = (whiteTime - 1).coerceAtLeast(0)
                                transaction.update(matchRef, "whiteTime", whiteTime)
                                if (whiteTime == 0) {
                                    transaction.update(
                                        matchRef,
                                        mapOf(
                                            "status" to MatchStatus.Checkmate.toString(),
                                            "winner" to snapshot.getString("player2")
                                        )
                                    )
                                }
                            } else {
                                blackTime = (blackTime - 1).coerceAtLeast(0)
                                transaction.update(matchRef, "blackTime", blackTime)
                                if (blackTime == 0) {
                                    transaction.update(
                                        matchRef,
                                        mapOf(
                                            "status" to MatchStatus.Checkmate.toString(),
                                            "winner" to snapshot.getString("player1")
                                        )
                                    )
                                }
                            }
                        }.await()
                    } catch (e: Exception) {
                        matchmakingError.value = "Lỗi cập nhật thời gian: ${e.message}"
                        timerJob?.cancel()
                    }
                } ?: run {
                    timerJob?.cancel()
                }
            }
        }
    }

    fun onSquareClicked(row: Int, col: Int) {
        if (isPromoting.value || playerColor.value != currentTurn.value || matchId.value == null) {
            println("onSquareClicked: Cannot move. isPromoting=${isPromoting.value}, playerColor=${playerColor.value}, currentTurn=${currentTurn.value}, matchId=${matchId.value}")
            return
        }

        val position = Position(row, col)
        val selectedMove = highlightedSquares.value.find { it.position == position }
        if (selectedMove != null) {
            val pieceBeforeMove = chessGame.getPieceAt(selectedMove.from.row, selectedMove.from.col)
            val moveNotation = if (pieceBeforeMove != null) {
                val pieceColorStr = if (pieceBeforeMove.color == PieceColor.WHITE) "trắng" else "đen"
                "${pieceBeforeMove.type} $pieceColorStr từ ${positionToString(selectedMove.from)} đến ${positionToString(position)}"
            } else {
                "Nước đi không xác định"
            }

            if (chessGame.movePiece(position)) {
                lastMoveByThisDevice = true
                if (chessGame.getPendingPromotion() != null) {
                    isPromoting.value = true
                    timerJob?.cancel()
                } else {
                    currentTurn.value = chessGame.getCurrentTurn()
                    startTimer()
                }

                moveHistory.add(moveNotation)

                if (chessGame.isGameOver()) {
                    val winnerId = if (chessGame.isCheckmatePublic()) {
                        if (currentTurn.value == PieceColor.WHITE) matchData.value?.get("player2") as? String
                        else matchData.value?.get("player1") as? String
                    } else null
                    endMatch(
                        status = when {
                            chessGame.isCheckmatePublic() -> MatchStatus.Checkmate
                            chessGame.isStalematePublic() -> MatchStatus.Draw
                            chessGame.isDrawByFiftyMoveRulePublic() -> MatchStatus.Draw
                            chessGame.isDrawByRepetitionPublic() -> MatchStatus.Draw
                            else -> MatchStatus.Ongoing
                        },
                        winner = winnerId
                    )
                }

                matchId.value?.let { id ->
                    val lastMove = chessGame.getLastMove()
                    db.collection("matches")
                        .document(id)
                        .update(
                            mapOf(
                                "board" to boardToFlatMap(chessGame.getBoard()),
                                "currentTurn" to currentTurn.value.toString(),
                                "moveHistory" to moveHistory.toList(),
                                "fiftyMoveCounter" to chessGame.getFiftyMoveCounter(),
                                "positionHistory" to chessGame.getPositionHistory(),
                                "whiteKingPosition" to mapOf(
                                    "row" to chessGame.getWhiteKingPosition().row,
                                    "col" to chessGame.getWhiteKingPosition().col
                                ),
                                "blackKingPosition" to mapOf(
                                    "row" to chessGame.getBlackKingPosition().row,
                                    "col" to chessGame.getBlackKingPosition().col
                                ),
                                "hasMoved" to chessGame.getHasMoved(),
                                "lastMove" to lastMove?.let {
                                    mapOf(
                                        "from" to mapOf("row" to it.first.row, "col" to it.first.col),
                                        "to" to mapOf("row" to it.second.row, "col" to it.second.col)
                                    )
                                }
                            )
                        )
                }
                board.value = chessGame.getBoard()
            }
            highlightedSquares.value = emptyList()
        } else {
            val piece = chessGame.getPieceAt(row, col)
            if (piece != null && piece.color == playerColor.value) {
                val moves = chessGame.selectPiece(row, col)
                println("onSquareClicked: Selected piece at ($row, $col), color=${piece.color}, playerColor=${playerColor.value}, chessGame.currentTurn=${chessGame.getCurrentTurn()}, moves=$moves")
                highlightedSquares.value = moves
            } else {
                println("onSquareClicked: No piece at ($row, $col) or wrong color. Piece=$piece, playerColor=${playerColor.value}, chessGame.currentTurn=${chessGame.getCurrentTurn()}")
                highlightedSquares.value = emptyList()
            }
        }
    }

    fun promotePawn(toType: PieceType) {
        chessGame.promotePawn(toType)
        lastMoveByThisDevice = true
        isPromoting.value = false
        currentTurn.value = chessGame.getCurrentTurn()

        val lastMove = chessGame.getLastMove()
        if (lastMove != null) {
            val piece = board.value[lastMove.second.row][lastMove.second.col]
            val moveNotation = "Phong cấp ${piece?.type} tại ${positionToString(lastMove.second)}"
            moveHistory.add(moveNotation)
        }

        if (chessGame.isGameOver()) {
            val winnerId = if (chessGame.isCheckmatePublic()) {
                if (currentTurn.value == PieceColor.WHITE) matchData.value?.get("player2") as? String
                else matchData.value?.get("player1") as? String
            } else null
            endMatch(
                status = when {
                    chessGame.isCheckmatePublic() -> MatchStatus.Checkmate
                    chessGame.isStalematePublic() -> MatchStatus.Draw
                    chessGame.isDrawByFiftyMoveRulePublic() -> MatchStatus.Draw
                    chessGame.isDrawByRepetitionPublic() -> MatchStatus.Draw
                    else -> MatchStatus.Ongoing
                },
                winner = winnerId
            )
        }

        matchId.value?.let { id ->
            val lastMove = chessGame.getLastMove()
            db.collection("matches")
                .document(id)
                .update(
                    mapOf(
                        "board" to boardToFlatMap(chessGame.getBoard()),
                        "currentTurn" to currentTurn.value.toString(),
                        "moveHistory" to moveHistory.toList(),
                        "fiftyMoveCounter" to chessGame.getFiftyMoveCounter(),
                        "positionHistory" to chessGame.getPositionHistory(),
                        "whiteKingPosition" to mapOf(
                            "row" to chessGame.getWhiteKingPosition().row,
                            "col" to chessGame.getWhiteKingPosition().col
                        ),
                        "blackKingPosition" to mapOf(
                            "row" to chessGame.getBlackKingPosition().row,
                            "col" to chessGame.getBlackKingPosition().col
                        ),
                        "hasMoved" to chessGame.getHasMoved(),
                        "lastMove" to lastMove?.let {
                            mapOf(
                                "from" to mapOf("row" to it.first.row, "col" to it.first.col),
                                "to" to mapOf("row" to it.second.row, "col" to it.second.col)
                            )
                        }
                    )
                )
        }
        board.value = chessGame.getBoard()
        startTimer()
    }

    fun requestDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(mapOf("drawRequest" to auth.currentUser?.uid))
        }
    }

    fun acceptDraw() {
        endMatch(status = MatchStatus.Draw, winner = null, drawReason = "Hòa do thỏa thuận giữa hai người chơi.")
    }

    fun declineDraw() {
        matchId.value?.let { id ->
            db.collection("matches")
                .document(id)
                .update(mapOf("drawRequest" to null))
        }
    }

    fun surrender() {
        val opponentId = if (playerColor.value == PieceColor.WHITE) {
            matchData.value?.get("player2") as? String
        } else {
            matchData.value?.get("player1") as? String
        }
        endMatch(status = MatchStatus.Surrendered, winner = opponentId)
    }

    private suspend fun updateScores(player1Id: String, player2Id: String, winner: String?) {
        try {
            db.runTransaction { transaction ->
                val player1Ref = db.collection("users").document(player1Id)
                val player2Ref = db.collection("users").document(player2Id)

                val player1Doc = transaction.get(player1Ref)
                val player2Doc = transaction.get(player2Ref)

                val player1Score = (player1Doc.getLong("score") ?: 0).toInt()
                val player2Score = (player2Doc.getLong("score") ?: 0).toInt()

                when {
                    winner == player1Id -> {
                        transaction.update(player1Ref, mapOf("score" to player1Score + 10))
                        transaction.update(player2Ref, mapOf("score" to (player2Score - 5).coerceAtLeast(0)))
                    }
                    winner == player2Id -> {
                        transaction.update(player2Ref, mapOf("score" to player2Score + 10))
                        transaction.update(player1Ref, mapOf("score" to (player1Score - 5).coerceAtLeast(0)))
                    }
                    else -> {
                        // Trường hợp hòa, không thay đổi điểm số
                    }
                }
            }.await()
        } catch (e: Exception) {
            matchmakingError.value = "Lỗi cập nhật điểm: ${e.message}"
        }
    }

    private fun endMatch(status: MatchStatus, winner: String?, drawReason: String? = null) {
        matchId.value?.let { id ->
            val updateData = mutableMapOf<String, Any?>(
                "status" to status.toString(),
                "winner" to winner
            )
            if (status == MatchStatus.Draw) {
                updateData["drawReason"] = drawReason ?: chessGame.getGameResult() ?: "Hết cờ! Ván đấu hòa."
            }

            db.collection("matches")
                .document(id)
                .update(updateData)

            viewModelScope.launch {
                val player1Id = matchData.value?.get("player1") as? String
                val player2Id = matchData.value?.get("player2") as? String
                if (player1Id != null && player2Id != null) {
                    when (status) {
                        MatchStatus.Draw -> {}
                        MatchStatus.Surrendered, MatchStatus.Checkmate -> {
                            updateScores(player1Id, player2Id, winner)
                        }
                        MatchStatus.Ongoing -> {}
                    }
                }
            }
        }
    }

    private fun createInitialBoard(): Array<Array<ChessPiece?>> {
        val newBoard = Array(8) { Array<ChessPiece?>(8) { null } }
        newBoard[0][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, Position(0, 0))
        newBoard[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE, Position(0, 1))
        newBoard[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE, Position(0, 2))
        newBoard[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE, Position(0, 3))
        newBoard[0][4] = ChessPiece(PieceType.KING, PieceColor.WHITE, Position(0, 4))
        newBoard[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE, Position(0, 5))
        newBoard[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE, Position(0, 6))
        newBoard[0][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, Position(0, 7))
        for (col in 0..7) {
            newBoard[1][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE, Position(1, col))
        }
        newBoard[7][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK, Position(7, 0))
        newBoard[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK, Position(7, 1))
        newBoard[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK, Position(7, 2))
        newBoard[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK, Position(7, 3))
        newBoard[7][4] = ChessPiece(PieceType.KING, PieceColor.BLACK, Position(7, 4))
        newBoard[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK, Position(7, 5))
        newBoard[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK, Position(7, 6))
        newBoard[7][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK, Position(7, 7))
        for (col in 0..7) {
            newBoard[6][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK, Position(6, col))
        }
        return newBoard
    }

    private fun boardToFlatMap(board: Array<Array<ChessPiece?>>): List<Map<String, Any?>> {
        val flatList = mutableListOf<Map<String, Any?>>()
        for (row in board.indices) {
            for (col in board[row].indices) {
                val piece = board[row][col]
                if (piece != null) {
                    flatList.add(
                        mapOf(
                            "type" to piece.type.toString(),
                            "color" to piece.color.toString(),
                            "position" to mapOf(
                                "row" to row,
                                "col" to col
                            )
                        )
                    )
                }
            }
        }
        return flatList
    }

    private fun flatMapToBoard(boardData: List<Map<String, Any?>>): Array<Array<ChessPiece?>> {
        val newBoard = Array(8) { Array<ChessPiece?>(8) { null } }
        for (pieceData in boardData) {
            val type = pieceData["type"]?.toString()
            val color = pieceData["color"]?.toString()
            val positionData = pieceData["position"] as? Map<String, Long>
            if (type != null && color != null && positionData != null) {
                val row = positionData["row"]?.toInt() ?: continue
                val col = positionData["col"]?.toInt() ?: continue
                if (row in 0..7 && col in 0..7) {
                    newBoard[row][col] = ChessPiece(
                        type = PieceType.valueOf(type),
                        color = PieceColor.valueOf(color),
                        position = Position(row, col)
                    )
                }
            }
        }
        return newBoard
    }

    private fun positionToString(position: Position): String {
        val col = ('a' + position.col).toString()
        val row = (8 - position.row).toString()
        return "$col$row"
    }

    fun cancelMatchmaking() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("matchmaking_queue").document(userId).delete()
        }
        matchmakingJob?.cancel()
        matchmakingListener?.remove()
        isMatchmaking.set(false)
        matchmakingError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchListener?.remove()
        matchmakingListener?.remove()
        cancelMatchmaking()
    }
}